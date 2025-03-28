package com.example.demoilost;

import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.widget.AutocompleteActivity;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_GALLERY = 102;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;

    private ImageView photoImageView;
    private Button selectPhotoButton, postButton;
    private EditText descriptionEditText, nameEditText, locationEditText;
    private Uri selectedPhotoUri, cameraPhotoUri;
    private String uploadedImageUrl = null;
    private GeoPoint geoPointFromSearch = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());


        photoImageView = findViewById(R.id.photoImageView);
        selectPhotoButton = findViewById(R.id.selectPhotoButton);
        postButton = findViewById(R.id.postButton);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        nameEditText = findViewById(R.id.nameEditText);
        locationEditText = findViewById(R.id.locationEditText);

        selectPhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_GALLERY);
        });

        postButton.setOnClickListener(v -> {
            if (selectedPhotoUri != null) {
                uploadImageToImgur(selectedPhotoUri);
            } else {
                createPost(null); // No image selected
            }
        });

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyAxay_dovw88a_M3TvSTbL2q_YPbOCYlJA");
        }

        locationEditText.setOnClickListener(v -> openAutocomplete());

    }

    private void openAutocomplete() {
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
        );

        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(this);
        startActivityForResult(intent, 1001);
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);

        } else {
            openCameraIntent(); //  your existing method
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkCameraPermissionAndOpen();
            } else {
                Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void openCameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                photoFile = File.createTempFile("IMG_", ".jpg", storageDir);
            } catch (IOException ex) {
                Toast.makeText(this, "Failed to create file", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                cameraPhotoUri = FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".provider",
                        photoFile
                );
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPhotoUri);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                locationEditText.setText(place.getAddress());
                geoPointFromSearch = new GeoPoint(
                        place.getLatLng().latitude,
                        place.getLatLng().longitude
                );
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (cameraPhotoUri != null) {
                selectedPhotoUri = cameraPhotoUri;
                photoImageView.setImageURI(cameraPhotoUri);
            }
        }

        if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK && data != null) {
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

        GeoPoint geoPoint = geoPointFromSearch;
        if (geoPoint == null) {
            Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show();
            return;
        }


        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> post = new HashMap<>();
        post.put("title", title);
        post.put("location", geoPoint);
        post.put("address", locationEditText.getText().toString());
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
