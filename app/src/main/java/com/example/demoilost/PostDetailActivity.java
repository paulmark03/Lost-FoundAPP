package com.example.demoilost;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class PostDetailActivity extends AppCompatActivity {

    private ImageView detailImageView;
    private TextView detailTitleTextView, detailLocationTextView, detailDescriptionTextView;
    private Button chatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        detailImageView = findViewById(R.id.detailImageView);
        detailTitleTextView = findViewById(R.id.detailNameTextView);
        detailLocationTextView = findViewById(R.id.detailAddressTextView);
        detailDescriptionTextView = findViewById(R.id.detailDescriptionTextView);
        chatButton = findViewById(R.id.chatButton);

        // Get post data from intent
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String location = intent.getStringExtra("location");
        String description = intent.getStringExtra("description");
        String imageUrl = intent.getStringExtra("imageUrl");
        String postId = intent.getStringExtra("postId");
        String founderId = intent.getStringExtra("founderId"); // aka posterId

        detailTitleTextView.setText(title);
        detailLocationTextView.setText(location);
        detailDescriptionTextView.setText(description);

        // Load image using Glide
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_profile)
                    .into(detailImageView);
        } else {
            detailImageView.setImageResource(android.R.color.darker_gray);
        }

        // 💬 Chat Button
        chatButton.setOnClickListener(view -> {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            String chatId = postId + "_" + founderId + "_" + currentUserId;

            Map<String, Object> chatData = new HashMap<>();
            chatData.put("postId", postId);
            chatData.put("founderId", founderId);
            chatData.put("userId", currentUserId);
            chatData.put("createdAt", FieldValue.serverTimestamp());

            FirebaseFirestore.getInstance()
                    .collection("chats")
                    .document(chatId)
                    .set(chatData, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Intent chatIntent = new Intent(PostDetailActivity.this, ChatActivity.class);
                        chatIntent.putExtra("chatId", chatId);
                        chatIntent.putExtra("founderId", founderId);
                        startActivity(chatIntent);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(PostDetailActivity.this, "Error initiating chat: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}
