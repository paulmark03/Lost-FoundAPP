package com.example.demoilost;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostActivity extends AppCompatActivity {

    private static final int SELECT_PHOTO_REQUEST = 100;
    private ImageView photoImageView;
    private Button selectPhotoButton, postButton;
    private EditText descriptionEditText, nameEditText, locationEditText;
    private Uri selectedPhotoUri;
    private String uploadedImageUrl = null;

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

        selectPhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, SELECT_PHOTO_REQUEST);
        });

        postButton.setOnClickListener(v -> {
            if (selectedPhotoUri != null) {
                uploadImageToImgur(selectedPhotoUri);
            } else {
                createPost(null); // No image selected
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PHOTO_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedPhotoUri = data.getData();
            photoImageView.setImageURI(selectedPhotoUri);
        }
    }

    private void uploadImageToImgur(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            byte[] imageBytes = IOUtils.toByteArray(inputStream);
            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            OkHttpClient client = new OkHttpClient();

            RequestBody body = new FormBody.Builder()
                    .add("image", base64Image)
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.imgur.com/3/image")
                    .header("Authorization", "Client-ID ad8d936a2f446c7") // Replace with real Client ID
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(PostActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String json = response.body().string();
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            uploadedImageUrl = jsonObject.getJSONObject("data").getString("link");
                            runOnUiThread(() -> createPost(uploadedImageUrl));
                        } catch (JSONException e) {
                            runOnUiThread(() -> Toast.makeText(PostActivity.this, "Error parsing image URL", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        runOnUiThread(() -> Toast.makeText(PostActivity.this, "Imgur upload failed", Toast.LENGTH_SHORT).show());
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to read image", Toast.LENGTH_SHORT).show();
        }
    }

    private void createPost(String imageUrl) {
        String description = descriptionEditText.getText().toString().trim();
        String title = nameEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> post = new HashMap<>();
        post.put("title", title);
        post.put("location", location);
        post.put("description", description);
        post.put("posterId", uid);
        post.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
        if (imageUrl != null) {
            post.put("imageUrl", imageUrl);
        }

        FirebaseFirestore.getInstance().collection("posts")
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(PostActivity.this, "Post created!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(PostActivity.this, "Post failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
