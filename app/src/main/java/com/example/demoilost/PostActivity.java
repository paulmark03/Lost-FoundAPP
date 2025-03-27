package com.example.demoilost;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity {
    private static final int SELECT_PHOTO_REQUEST = 100;
    private ImageView photoImageView;
    private Button selectPhotoButton, postButton;
    private EditText descriptionEditText, nameEditText, locationEditText;
    private Uri selectedPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        photoImageView = findViewById(R.id.photoImageView);
        selectPhotoButton = findViewById(R.id.selectPhotoButton);
        postButton = findViewById(R.id.postButton);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        nameEditText = findViewById(R.id.nameEditText);
        locationEditText = findViewById(R.id.locationEditText);

        selectPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch intent to pick an image from the gallery
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, SELECT_PHOTO_REQUEST);
            }
        });

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImageAndPost();
            }
        });
    }

    // Handle image selection result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PHOTO_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedPhotoUri = data.getData();
            photoImageView.setImageURI(selectedPhotoUri);
        }
    }

    // Convert selected image to a byte array and create the Firestore post
    private void uploadImageAndPost() {
        if (selectedPhotoUri != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedPhotoUri);
                byte[] imageBytes = getBytes(inputStream);
                // Convert byte[] to Firestore Blob
                com.google.firebase.firestore.Blob imageBlob = com.google.firebase.firestore.Blob.fromBytes(imageBytes);
                createPost(imageBlob);
            } catch (IOException e) {
                Toast.makeText(this, "Error reading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            createPost(null);
        }
    }

    // Helper to convert InputStream to byte[]
    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    // Create a new Firestore document with the post data
    private void createPost(com.google.firebase.firestore.Blob imageBlob) {
        String description = descriptionEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();

        Map<String, Object> post = new HashMap<>();
        post.put("name", name);
        post.put("location", location);
        post.put("description", description);
        post.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
        if (imageBlob != null) {
            post.put("image", imageBlob);
        }

        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("posts")
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(PostActivity.this, "Post added", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(PostActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
