package com.example.demoilost;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
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

    private RecyclerView messageRecyclerView;
    private ChatPreviewAdapter adapter;
    private List<ChatPreviewModel> chatList;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        //migrateChatDocuments();

        fixChatsFromSenderId();

        messageRecyclerView = findViewById(R.id.messageRecyclerView);
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        chatList = new ArrayList<>();
        adapter = new ChatPreviewAdapter(this, chatList);
        messageRecyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadChats();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.bottom_chat);

        bottomNavigationView.setOnItemSelectedListener(item -> {
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

    private void loadChats() {
        chatList.clear();

        // 1. Load chats where I am the sender
        db.collection("chats")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(userChats -> {
                    for (DocumentSnapshot doc : userChats) {
                        ChatPreviewModel chat = doc.toObject(ChatPreviewModel.class);
                        if (chat != null) chatList.add(chat);
                    }

                    // 2. Load chats where I am the receiver (founder)
                    db.collection("chats")
                            .whereEqualTo("founderId", currentUserId)
                            .get()
                            .addOnSuccessListener(founderChats -> {
                                for (DocumentSnapshot doc : founderChats) {
                                    ChatPreviewModel chat = doc.toObject(ChatPreviewModel.class);
                                    if (chat != null && !containsChat(chat.getChatId())) {
                                        chatList.add(chat);
                                    }
                                }

                                adapter.notifyDataSetChanged(); // done loading both
                            })
                            .addOnFailureListener(e ->
                                    Log.e("MessagesActivity", "Failed to load chats as founder", e)
                            );
                })
                .addOnFailureListener(e ->
                        Log.e("MessagesActivity", "Failed to load chats as user", e)
                );
    }

    private boolean containsChat(String chatId) {
        for (ChatPreviewModel chat : chatList) {
            if (chatId != null && chatId.equals(chat.getChatId())) return true;
        }
        return false;
    }


    //Merge firestore
//    private void migrateChatDocuments() {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        db.collection("chats")
//                .get()
//                .addOnSuccessListener(querySnapshots -> {
//                    for (DocumentSnapshot doc : querySnapshots) {
//                        String docId = doc.getId();
//                        String chatId = doc.getString("chatId");
//                        String founderId = doc.getString("founderId");
//                        String userId = doc.getString("userId");
//
//                        // Build fields that are missing
//                        Map<String, Object> updates = new HashMap<>();
//
//                        if (chatId == null) updates.put("chatId", docId);
//
//                        // Optional hardcoded fallback if you can detect who is missing
//                        if (founderId == null) updates.put("founderId", "UNKNOWN_FOUNDER");
//                        if (userId == null) updates.put("userId", "UNKNOWN_USER");
//
//                        if (!updates.isEmpty()) {
//                            db.collection("chats")
//                                    .document(docId)
//                                    .set(updates, SetOptions.merge())
//                                    .addOnSuccessListener(aVoid ->
//                                            Log.d("Migration", "Updated chat " + docId)
//                                    )
//                                    .addOnFailureListener(e ->
//                                            Log.e("Migration", "Failed to update " + docId, e)
//                                    );
//                        }
//                    }
//                })
//                .addOnFailureListener(e ->
//                        Log.e("Migration", "Failed to fetch chats", e)
//                );
//    }

    private void fixChatsFromSenderId() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("chats")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    for (DocumentSnapshot doc : querySnapshots) {
                        String docId = doc.getId();
                        String senderId = doc.getString("senderId");
                        String postId = doc.getString("postId");

                        if (senderId != null && postId != null) {
                            // Get post to find out who the founder is
                            db.collection("posts")
                                    .document(postId)
                                    .get()
                                    .addOnSuccessListener(postSnapshot -> {
                                        String founderId = postSnapshot.getString("posterId"); // or "userId" depending on schema
                                        if (founderId != null) {
                                            Map<String, Object> updates = new HashMap<>();
                                            updates.put("userId", senderId);
                                            updates.put("founderId", founderId);
                                            updates.put("chatId", docId);
                                            updates.put("participants", Arrays.asList(senderId, founderId));

                                            db.collection("chats")
                                                    .document(docId)
                                                    .set(updates, SetOptions.merge());
                                        }
                                    });
                        }
                    }
                });
    }


}
