package com.example.demoilost;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoilost.adapter.ChatPreviewAdapter;
import com.example.demoilost.model.ChatPreviewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InboxActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private ChatPreviewAdapter chatAdapter;
    private List<ChatPreviewModel> chatList;
    private FirebaseFirestore firestore;
    private String currentUserId;
    private static final String CHAT_COLLECTION = "chats";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        initializeFirestore();
        initializeViews();
        setupBottomNavigation();
        setupSwipeToDelete();
        loadChats();
        fixChatDocumentsIfNeeded();
    }

    private void initializeFirestore() {
        firestore = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void initializeViews() {
        chatRecyclerView = findViewById(R.id.messageRecyclerView);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        chatList = new ArrayList<>();
        chatAdapter = new ChatPreviewAdapter(this, chatList);
        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void setupBottomNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottom_navigation_view);
        nav.setSelectedItemId(R.id.bottom_chat);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.bottom_map) {
                startActivity(new Intent(this, MapActivity.class));
            } else if (id == R.id.bottom_search) {
                startActivity(new Intent(this, SearchActivity.class));
            } else if (id == R.id.bottom_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else {
                return true;
            }
            overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
            finish();
            return true;
        });
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                ChatPreviewModel chat = chatList.get(position);
                String chatId = chat.getChatId();
                deleteChat(chatId, position);
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;
                Paint paint = new Paint();
                paint.setColor(Color.RED);

                RectF background = new RectF(itemView.getRight() + dX, itemView.getTop(),
                        itemView.getRight(), itemView.getBottom());
                c.drawRect(background, paint);

                Drawable icon = ContextCompat.getDrawable(InboxActivity.this, R.drawable.ic_trash);
                if (icon != null) {
                    int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                    int iconTop = itemView.getTop() + iconMargin;
                    int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                    int iconRight = itemView.getRight() - iconMargin;
                    int iconBottom = iconTop + icon.getIntrinsicHeight();
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    icon.draw(c);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        }).attachToRecyclerView(chatRecyclerView);
    }

    private void deleteChat(String chatId, int position) {
        firestore.collection(CHAT_COLLECTION).document(chatId)
                .delete()
                .addOnSuccessListener(unused -> {
                    firestore.collection(CHAT_COLLECTION).document(chatId).collection("messages")
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                    doc.getReference().delete();
                                }
                            });

                    chatList.remove(position);
                    chatAdapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Chat deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete chat: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadChats() {
        chatList.clear();

        firestore.collection(CHAT_COLLECTION)
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(userChats -> {
                    for (DocumentSnapshot doc : userChats) {
                        ChatPreviewModel chat = doc.toObject(ChatPreviewModel.class);
                        if (chat != null) chatList.add(chat);
                    }

                    firestore.collection(CHAT_COLLECTION)
                            .whereEqualTo("founderId", currentUserId)
                            .get()
                            .addOnSuccessListener(founderChats -> {
                                for (DocumentSnapshot doc : founderChats) {
                                    ChatPreviewModel chat = doc.toObject(ChatPreviewModel.class);
                                    if (chat != null && !containsChat(chat.getChatId())) {
                                        chatList.add(chat);
                                    }
                                }
                                chatAdapter.notifyDataSetChanged();
                            });
                });
    }

    private boolean containsChat(String chatId) {
        for (ChatPreviewModel chat : chatList) {
            if (chatId != null && chatId.equals(chat.getChatId())) return true;
        }
        return false;
    }

    private void fixChatDocumentsIfNeeded() {
        firestore.collection(CHAT_COLLECTION)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot) {
                        String chatId = doc.getId();
                        String senderId = doc.getString("senderId");
                        String postId = doc.getString("postId");

                        if (senderId != null && postId != null) {
                            firestore.collection("posts").document(postId)
                                    .get()
                                    .addOnSuccessListener(postDoc -> {
                                        String founderId = postDoc.getString("posterId");
                                        if (founderId != null) {
                                            Map<String, Object> updates = new HashMap<>();
                                            updates.put("userId", senderId);
                                            updates.put("founderId", founderId);
                                            updates.put("chatId", chatId);
                                            updates.put("participants", Arrays.asList(senderId, founderId));

                                            firestore.collection(CHAT_COLLECTION)
                                                    .document(chatId)
                                                    .set(updates, SetOptions.merge());
                                        }
                                    });
                        }
                    }
                });
    }
}
