package com.example.demoilost;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import java.util.Arrays;
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
    private FirebaseFirestore db;
    private String currentUserId, founderId, postId, title, location, description, imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        initViews();
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        extractPostData();
        populateUI();

        chatButton.setOnClickListener(view -> initiateChat());
    }

    private void initViews() {
        detailImageView = findViewById(R.id.detailImageView);
        detailTitleTextView = findViewById(R.id.detailNameTextView);
        detailLocationTextView = findViewById(R.id.detailAddressTextView);
        detailDescriptionTextView = findViewById(R.id.detailDescriptionTextView);
        chatButton = findViewById(R.id.chatButton);
    }

    private void extractPostData(){
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        location = intent.getStringExtra("location");
        description = intent.getStringExtra("description");
        imageUrl = intent.getStringExtra("imageUrl");
        postId = intent.getStringExtra("postId");
        founderId = intent.getStringExtra("founderId");
        double latitude = intent.getDoubleExtra("latitude", 0.0);
        double longitude = intent.getDoubleExtra("longitude", 0.0);
        String locationText = latitude != 0.0 || longitude != 0.0
                ? latitude + ", " + longitude
                : "Location not available";
    }



    private void populateUI() {
        detailTitleTextView.setText(title != null ? title : "Untitled");
        detailLocationTextView.setText(location != null ? location : "Unknown Location");
        detailDescriptionTextView.setText(description != null ? description : "No description");

        // Load image using Glide
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_profile)
                    .into(detailImageView);
        } else {
            detailImageView.setImageResource(android.R.color.darker_gray);
        }
    }

    private void initiateChat() {
        if (founderId == null || postId == null) {
            Toast.makeText(this, "Invalid post details", Toast.LENGTH_SHORT).show();
            return;
        }

        String chatId = postId + "_" + founderId + "_" + currentUserId;

        Map<String, Object> chatData = new HashMap<>();
        chatData.put("chatId", chatId);
        chatData.put("postId", postId);
        chatData.put("founderId", founderId);
        chatData.put("userId", currentUserId);
        chatData.put("participants", Arrays.asList(founderId, currentUserId));
        chatData.put("createdAt", FieldValue.serverTimestamp());

        db.collection("chats")
                .document(chatId)
                .set(chatData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Intent chatIntent = new Intent(PostDetailActivity.this, ChatActivity.class);
                    chatIntent.putExtra("chatId", chatId);
                    chatIntent.putExtra("founderId", founderId);
                    chatIntent.putExtra("postId", postId);
                    startActivity(chatIntent);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(PostDetailActivity.this, "Error starting chat: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
