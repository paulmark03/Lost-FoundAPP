package com.example.demoilost;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.demoilost.adapter.ChatAdapter;
import com.example.demoilost.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText inputMessage;
    private Button sendButton;
    private ChatAdapter chatAdapter;
    private List<Message> messagesList;
    private FirebaseFirestore firestore;
    private String chatId;
    private String founderId;
    private String postId; // Optional if needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize views
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        inputMessage = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        // Optional: a header TextView to display chat title
        TextView chatHeader = findViewById(R.id.chatHeader);

        messagesList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();

        // Retrieve extras from the Intent
        chatId = getIntent().getStringExtra("chatId");
        founderId = getIntent().getStringExtra("founderId");
        postId = getIntent().getStringExtra("postId");

        Log.d("ChatActivity", "chatId: " + chatId);
        Log.d("ChatActivity", "founderId: " + founderId);
        Log.d("ChatActivity", "postId: " + postId);

        // Optionally update the header with the founder's information
        if (chatHeader != null) {
            chatHeader.setText("Chat with Founder: " + (founderId != null ? founderId : "Unknown"));
        }

        // Set up RecyclerView with ChatAdapter
        chatAdapter = new ChatAdapter(messagesList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Listen for messages in this chat in real time
        firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e("ChatActivity", "Error listening for messages", error);
                        return;
                    }
                    messagesList.clear();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Message message = doc.toObject(Message.class);
                            messagesList.add(message);
                        }
                    }
                    chatAdapter.notifyDataSetChanged();
                });

        // Handle sending a new message when the send button is clicked
        sendButton.setOnClickListener(v -> {
            String messageText = inputMessage.getText().toString().trim();
            if (!messageText.isEmpty()) {
                Message message = new Message(
                        FirebaseAuth.getInstance().getCurrentUser().getUid(),
                        messageText,
                        new Date()
                );
                firestore.collection("chats")
                        .document(chatId)
                        .collection("messages")
                        .add(message)
                        .addOnSuccessListener(documentReference -> {
                            inputMessage.setText(""); // Clear input field upon success
                        })
                        .addOnFailureListener(e -> {
                            Log.e("ChatActivity", "Error sending message", e);
                        });
            }
        });
    }
}
