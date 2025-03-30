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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PostDetailActivity extends AppCompatActivity {

    private ImageView detailImageView;
    private TextView detailTitleTextView;
    private TextView detailLocationTextView;
    private TextView detailDescriptionTextView;
    private Button chatButton;
    private FirebaseFirestore db;
    private String currentUserId;
    private String founderId;
    private String postId;
    private String title;
    private String location;
    private String description;
    private String imageUrl;
    private String address;
    private String postText = "postId";
    private String founderText = "founderId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        initViews();
        extractPostData();
        populateUI();

        // Back button
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Disable chat if viewing own post
        if (founderId != null && founderId.equals(currentUserId)) {
            chatButton.setVisibility(View.GONE);
        }

        // Chat button
        chatButton.setOnClickListener(view -> initiateChat());
    }

    private void initViews() {
        detailImageView = findViewById(R.id.detailImageView);
        detailTitleTextView = findViewById(R.id.detailNameTextView);
        detailLocationTextView = findViewById(R.id.detailAddressTextView);
        detailDescriptionTextView = findViewById(R.id.detailDescriptionTextView);
        chatButton = findViewById(R.id.chatButton);
    }

    private void extractPostData() {
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        description = intent.getStringExtra("description");
        imageUrl = intent.getStringExtra("imageUrl");
        postId = intent.getStringExtra(postText);
        founderId = intent.getStringExtra(founderText);
        address = intent.getStringExtra("address");

        // Fallback to lat/lng if address is null
        if (address == null || address.isEmpty()) {
            double lat = intent.getDoubleExtra("latitude", 0.0);
            double lng = intent.getDoubleExtra("longitude", 0.0);
            address = (lat != 0.0 || lng != 0.0) ? lat + ", " + lng : "Unknown Location";
        }
    }

    private void populateUI() {
        detailTitleTextView.setText(title != null ? title : "Untitled");
        detailLocationTextView.setText(address != null ? address : "Unknown Location");
        detailDescriptionTextView.setText(description != null ? description : "No description");

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
        chatData.put(postText, postId);
        chatData.put(founderText, founderId);
        chatData.put("userId", currentUserId);
        chatData.put("participants", Arrays.asList(founderId, currentUserId));
        chatData.put("createdAt", FieldValue.serverTimestamp());

        db.collection("chats")
                .document(chatId)
                .set(chatData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Intent chatIntent = new Intent(PostDetailActivity.this, ChatActivity.class);
                    chatIntent.putExtra("chatId", chatId);
                    chatIntent.putExtra(founderText, founderId);
                    chatIntent.putExtra(postText, postId);
                    startActivity(chatIntent);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(PostDetailActivity.this, "Error starting chat: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
