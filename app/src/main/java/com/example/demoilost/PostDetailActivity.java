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
    private ImageView backButton;
    private TextView detailTitleTextView;
    private TextView detailLocationTextView;
    private TextView detailDescriptionTextView;
    private Button chatButton;

    private FirebaseFirestore db;
    private String currentUserId;
    private String founderId;
    private String postId;
    private String title;
    private String description;
    private String imageUrl;
    private String address;

    private static final String POST_ID_KEY = "postId";
    private static final String FOUNDER_ID_KEY = "founderId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        initializeFirebase();
        initializeViews();
        extractPostData();
        populateUI();
        setupListeners();
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void initializeViews() {
        detailImageView = findViewById(R.id.detailImageView);
        detailTitleTextView = findViewById(R.id.detailNameTextView);
        detailLocationTextView = findViewById(R.id.detailAddressTextView);
        detailDescriptionTextView = findViewById(R.id.detailDescriptionTextView);
        chatButton = findViewById(R.id.chatButton);
        backButton = findViewById(R.id.backButton);
    }

    private void extractPostData() {
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        description = intent.getStringExtra("description");
        imageUrl = intent.getStringExtra("imageUrl");
        postId = intent.getStringExtra(POST_ID_KEY);
        founderId = intent.getStringExtra(FOUNDER_ID_KEY);
        address = intent.getStringExtra("address");

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

        if (founderId != null && founderId.equals(currentUserId)) {
            chatButton.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        chatButton.setOnClickListener(v -> {
            if (founderId == null || postId == null) {
                showToast("Invalid post details");
            } else {
                initiateChat();
            }
        });
    }

    private void initiateChat() {
        String chatId = postId + "_" + founderId + "_" + currentUserId;

        Map<String, Object> chatData = new HashMap<>();
        chatData.put("chatId", chatId);
        chatData.put(POST_ID_KEY, postId);
        chatData.put(FOUNDER_ID_KEY, founderId);
        chatData.put("userId", currentUserId);
        chatData.put("participants", Arrays.asList(founderId, currentUserId));
        chatData.put("createdAt", FieldValue.serverTimestamp());

        db.collection("chats")
                .document(chatId)
                .set(chatData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Intent chatIntent = new Intent(this, ChatActivity.class);
                    chatIntent.putExtra("chatId", chatId);
                    chatIntent.putExtra(FOUNDER_ID_KEY, founderId);
                    chatIntent.putExtra(POST_ID_KEY, postId);
                    startActivity(chatIntent);
                })
                .addOnFailureListener(e ->
                        showToast("Error starting chat: " + e.getMessage()));
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
