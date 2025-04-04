package com.example.demoilost;

import android.Manifest;
import android.content.Intent;
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
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.demoilost.model.PostModel;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

import okhttp3.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;


public class PostActivity extends AppCompatActivity {


    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> autocompleteLauncher;


    // Views
    private ImageView photoImageView;
    private Button selectPhotoButton;
    private Button postButton;
    private EditText descriptionEditText;
    private EditText nameEditText;
    private EditText locationEditText;

    // State
    private Uri selectedPhotoUri;
    private GeoPoint geoPointFromSearch;
    private boolean isTestMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        initActivityLaunchers();
        initViews();
        initListeners();
        initPlacesApi();
    }

    private void initViews() {
        photoImageView = findViewById(R.id.photoImageView);
        selectPhotoButton = findViewById(R.id.selectPhotoButton);
        postButton = findViewById(R.id.postButton);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        nameEditText = findViewById(R.id.nameEditText);
        locationEditText = findViewById(R.id.locationEditText);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void initListeners() {
        if (!isTestMode) {
            locationEditText.setOnClickListener(v -> openAutocomplete());
        }

        selectPhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });

        postButton.setOnClickListener(v -> attemptPost());
    }

    private void initPlacesApi() {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.PLACES_API_KEY);
        }
    }

    private void attemptPost() {
        String title = nameEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        if (title.isEmpty()) {
            nameEditText.setError("Title is required");
            return;
        }

        if (location.isEmpty()) {
            locationEditText.setError("Location is required");
            return;
        }

        if (description.isEmpty()) {
            descriptionEditText.setError("Description is required");
            return;
        }

        if (selectedPhotoUri == null && !isTestMode) {
            showToast("Please upload an image before posting");
            return;
        }

        if (isTestMode) {
            createPost("https://i.imgur.com/test.jpg");
        } else {
            uploadImageToImgur(selectedPhotoUri);
        }
    }

    private void openAutocomplete() {
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.ADDRESS_COMPONENTS,
                Place.Field.LOCATION
        );

        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(this);
        autocompleteLauncher.launch(intent);
    }



    private void uploadImageToImgur(Uri imageUri) {
        ImgurUploader.upload(this, imageUri, new ImgurUploader.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                createPost(imageUrl);
            }

            @Override
            public void onFailure(String errorMessage) {
                showToast(errorMessage);
            }
        });
    }


    private void createPost(String imageUrl) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        GeoPoint geoPoint = geoPointFromSearch;

        if (geoPoint == null) {
            showToast("Please select a location");
            return;
        }

        Map<String, Object> post = new HashMap<>();
        post.put("title", nameEditText.getText().toString().trim());
        post.put("description", descriptionEditText.getText().toString().trim());
        post.put("address", locationEditText.getText().toString().trim());
        post.put("posterId", uid);
        post.put("location", geoPoint);
        post.put("timestamp", FieldValue.serverTimestamp());
        if (imageUrl != null) post.put("imageUrl", imageUrl);

        FirebaseFirestore.getInstance().collection("posts")
                .add(post)
                .addOnSuccessListener(ref -> {
                    showToast("Post created!");
                    Intent intent = new Intent(this, MapActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> showToast("Post failed: " + e.getMessage()));
    }

    // Test helpers
    public void setTestImage(Uri uri) {
        selectedPhotoUri = uri;
        if (photoImageView != null) photoImageView.setImageURI(uri);
    }

    public void setTestLocation(String address, double lat, double lng) {
        locationEditText.setText(address);
        geoPointFromSearch = new GeoPoint(lat, lng);
    }

    public void setTestMode(boolean testMode) {
        this.isTestMode = testMode;
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }



    private void initActivityLaunchers() {
        galleryLauncher = registerForActivityResult(
                new StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedPhotoUri = result.getData().getData();
                        photoImageView.setImageURI(selectedPhotoUri);
                    }
                });

        autocompleteLauncher = registerForActivityResult(
                new StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Place place = Autocomplete.getPlaceFromIntent(result.getData());

                        // ✅ Use helper to get readable address
                        locationEditText.setText(buildAddressFromComponents(place));

                        LatLng location = place.getLocation();
                        if (location != null) {
                            geoPointFromSearch = new GeoPoint(location.latitude, location.longitude);
                        } else {
                            showToast("No coordinates found for this location");
                        }
                    }
                });

    }
    private String buildAddressFromComponents(Place place) {
        if (place.getAddressComponents() == null) return "";

        StringBuilder addressBuilder = new StringBuilder();
        for (AddressComponent component : place.getAddressComponents().asList()) {
            // You can be more selective based on types like "locality", "route", etc.
            addressBuilder.append(component.getName()).append(" ");
        }

        return addressBuilder.toString().trim();
    }


}
