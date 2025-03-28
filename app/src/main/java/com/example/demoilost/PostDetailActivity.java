package com.example.demoilost;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.HashMap;
import java.util.Map;

public class PostDetailActivity extends AppCompatActivity {

    private ImageView detailImageView;
    private TextView detailNameTextView, detailAddressTextView, detailDescriptionTextView;
    private Button chatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        detailImageView = findViewById(R.id.detailImageView);
        detailNameTextView = findViewById(R.id.detailNameTextView);
        detailAddressTextView = findViewById(R.id.detailAddressTextView);
        detailDescriptionTextView = findViewById(R.id.detailDescriptionTextView);
        chatButton = findViewById(R.id.chatButton);

        // Retrieve post details passed via the intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String location = intent.getStringExtra("location");
        String description = intent.getStringExtra("description");
        byte[] imageBytes = intent.getByteArrayExtra("imageBytes");

        detailNameTextView.setText(name);
        detailAddressTextView.setText(location);
        detailDescriptionTextView.setText(description);

        if (imageBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            detailImageView.setImageBitmap(bitmap);
        } else {
            detailImageView.setImageResource(android.R.color.darker_gray);
        }

        // Chat button
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve the necessary IDs.
                String postId = getIntent().getStringExtra("postId");
                // Assume you passed the founder's ID when opening PostDetailActivity.
                String founderId = getIntent().getStringExtra("founderId");
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                // Generate a chat ID. One strategy is to concatenate postId, founderId, and currentUserId.
                // You might want to sort these or use a delimiter for consistency.
                String chatId = postId + "_" + founderId + "_" + currentUserId;

                // Create/update the chat document in Firestore with founderId included.
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
                            // After successfully creating/updating the chat, launch ChatActivity.
                            Intent chatIntent = new Intent(PostDetailActivity.this, ChatActivity.class);
                            chatIntent.putExtra("chatId", chatId);
                            chatIntent.putExtra("founderId", founderId); // pass it along if needed
                            startActivity(chatIntent);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(PostDetailActivity.this, "Error initiating chat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }
}
