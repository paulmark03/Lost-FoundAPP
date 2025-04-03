package com.example.demoilost;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

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

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView profileName;
    private ImageView profileIcon;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        initViews();
        setupBottomNav();
        loadUserProfile();
    }

    private void initViews() {
        profileName = findViewById(R.id.profileName);
        profileIcon = findViewById(R.id.profileIcon);

        ImageView editIcon = findViewById(R.id.editIcon);
        editIcon.setOnClickListener(v -> pickImage());

        // Dynamically set labels
        ((TextView) findViewById(R.id.rowMyPosts).findViewById(R.id.settingLabel)).setText("My Posts");
        ((TextView) findViewById(R.id.rowManageAccount).findViewById(R.id.settingLabel)).setText("Manage Account");
        ((TextView) findViewById(R.id.rowPrivacy).findViewById(R.id.settingLabel)).setText("Privacy & Security");
        ((TextView) findViewById(R.id.rowLogout).findViewById(R.id.settingLabel)).setText("Log Out");

        // Click actions
        findViewById(R.id.rowMyPosts).setOnClickListener(v ->
                startActivity(new Intent(this, MyPostsActivity.class)));

        ActivityResultLauncher<Intent> manageAccountLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadUserProfile();
                    }
                });

        findViewById(R.id.rowManageAccount).setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageAccountActivity.class);
            manageAccountLauncher.launch(intent);
        });

        findViewById(R.id.rowPrivacy).setOnClickListener(v ->
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Privacy & Security")
                        .setMessage("We respect your privacy. Your information is only used for authentication and communication between users.")
                        .setPositiveButton("OK", null)
                        .show());

        findViewById(R.id.rowLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_navigation_view);
        nav.setSelectedItemId(R.id.bottom_settings);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.bottom_map) {
                startNewIntent(MapActivity.class);
            } else if (id == R.id.bottom_search) {
                startNewIntent(SearchActivity.class);
            } else if (id == R.id.bottom_chat) {
                startNewIntent(InboxActivity.class);
            }
            return id == R.id.bottom_settings;
        });
    }

    private void startNewIntent(Class<?> cls) {
        startActivity(new Intent(this, cls));
        overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
        finish();
    }

    private void loadUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        String photoUrl = doc.getString("photoUrl");

                        profileName.setText(name != null ? name : "No Name");

                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            Glide.with(this).load(photoUrl).placeholder(R.drawable.ic_profile).into(profileIcon);
                        } else {
                            profileIcon.setImageResource(R.drawable.ic_profile);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show());
    }

    private void uploadToImgur(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            byte[] imageBytes = IOUtils.toByteArray(inputStream);
            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder().add("image", base64Image).build();

            Request request = new Request.Builder()
                    .url("https://api.imgur.com/3/image")
                    .header("Authorization", "Client-ID ad8d936a2f446c7")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(SettingsActivity.this, "Upload failed", Toast.LENGTH_SHORT).show());
                }

                @Override public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject json = new JSONObject(response.body().string());
                            String imageUrl = json.getJSONObject("data").getString("link");
                            runOnUiThread(() -> updateProfilePicture(imageUrl));
                        } catch (JSONException e) {
                            runOnUiThread(() ->
                                    Toast.makeText(SettingsActivity.this, "Failed to parse image URL", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(SettingsActivity.this, "Imgur upload failed", Toast.LENGTH_SHORT).show());
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error reading image", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProfilePicture(String imageUrl) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(imageUrl)).build();

        user.updateProfile(profileUpdate);
        FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid())
                .update("photoUrl", imageUrl)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(profileIcon);
            uploadToImgur(imageUri);
        }

        if (requestCode == 101 && resultCode == RESULT_OK) {
            loadUserProfile(); // just in case
        }
    }
}
