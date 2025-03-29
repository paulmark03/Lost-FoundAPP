package com.example.demoilost;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import android.util.Base64;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;



public class SettingsActivity extends AppCompatActivity {

    TextView profileName, username;
    ImageView profileIcon;

    //Image URI
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        ImageView editIcon = findViewById(R.id.editIcon);

        editIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Link views to XML
        profileName = findViewById(R.id.profileName);
        username = findViewById(R.id.username);
        profileIcon = findViewById(R.id.profileIcon);

        loadUserProfile(); //  Load user info from Firebase

        // Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.bottom_settings);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.bottom_map) {
                startActivity(new Intent(SettingsActivity.this, MapActivity.class));
                overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
                finish();
                return true;
            } else if (id == R.id.bottom_search) {
                startActivity(new Intent(SettingsActivity.this, SearchActivity.class));
                overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
                finish();
                return true;
            } else if (id == R.id.bottom_chat) {
                startActivity(new Intent(SettingsActivity.this, InboxActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return id == R.id.bottom_settings;
        });
    }

    // Update profil picture
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            // Show preview immediately
            Glide.with(this).load(imageUri).into(profileIcon);

            // Upload to Imgur and update Firebase Auth
            uploadToImgur(imageUri);
        }
    }



    private void loadUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            profileName.setText(name != null ? name : "No Name");
            username.setText(email != null ? "@" + email.split("@")[0] : "unknown");

            if (photoUrl != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_profile)
                        .into(profileIcon);
            } else {
                profileIcon.setImageResource(R.drawable.ic_profile); // fallback
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateAuthProfilePicture(String imageUrl) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(imageUrl))
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    private void uploadToImgur(Uri imageUri) {
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
                    .header("Authorization", "Client-ID ad8d936a2f446c7")  // Replace this!
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(SettingsActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String json = response.body().string();
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            String imageUrl = jsonObject.getJSONObject("data").getString("link");

                            runOnUiThread(() -> updateAuthProfilePicture(imageUrl)); //Upload to Firebase Auth
                        } catch (JSONException e) {
                            runOnUiThread(() ->
                                    Toast.makeText(SettingsActivity.this, "Failed to parse Imgur response", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(SettingsActivity.this, "Imgur upload failed", Toast.LENGTH_SHORT).show());
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error reading image", Toast.LENGTH_SHORT).show();
        }
    }



}

