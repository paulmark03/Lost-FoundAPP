package com.example.demoilost;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_messages);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.bottom_chat);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.bottom_map) {
                startActivity(new Intent(MessagesActivity.this, MapActivity.class));
                overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
                finish();                return true;
            } else if (id == R.id.bottom_search) {
                startActivity(new Intent(MessagesActivity.this, SearchActivity.class));
                overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
                finish();
                return true;
            } else if (id == R.id.bottom_settings) {
                startActivity(new Intent(MessagesActivity.this, SettingsActivity.class));
                overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
                finish();
                return true;
            } else if (id == R.id.bottom_chat) {
                return true;
            }
            return false;
        });


        // ----------------------------CHAT---------------------------

        String currentUserId = auth.getCurrentUser().getUid();
        String otherUserId = getIntent().getStringExtra("otherUserId");
        String chatId = currentUserId.compareTo(otherUserId) < 0 ?
                currentUserId + "_" + otherUserId : otherUserId + "_" + currentUserId;

        private List<ChatMessage> messageList;
        private RecyclerView recyclerView;
        private MessageAdapter adapter;

        recyclerView = findViewById(R.id.messageRecyclerView);
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList, currentUserId); // you'll create this adapter
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    for (DocumentChange change : value.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            ChatMessage message = change.getDocument().toObject(ChatMessage.class);
                            messageList.add(message);
                            adapter.notifyItemInserted(messageList.size() - 1);
                            recyclerView.scrollToPosition(messageList.size() - 1);
                        }
                    }
                });



        // ----------------------------CHAT---------------------------



    }
}