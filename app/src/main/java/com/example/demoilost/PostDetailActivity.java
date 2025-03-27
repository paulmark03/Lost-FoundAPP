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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

        // Chat button (for now, simply shows a Toast)
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chatIntent = new Intent(PostDetailActivity.this, ChatActivity.class);
                startActivity(chatIntent);            }
        });
    }
}
