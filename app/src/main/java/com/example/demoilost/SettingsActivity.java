package com.example.demoilost;

import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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

import java.io.ByteArrayOutputStream;
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

    private TextView profileName;
    private ImageView profileIcon;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> manageAccountLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        initLaunchers();
        initViews();
        setupBottomNav();
        loadUserProfile();
    }

    private void initLaunchers() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            Glide.with(this).load(imageUri).into(profileIcon);
                            uploadToImgur(imageUri);
                        }
                    }
                });

        manageAccountLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadUserProfile();
                    }
                });
    }

    private void initViews() {
        profileName = findViewById(R.id.profileName);
        profileIcon = findViewById(R.id.profileIcon);

        findViewById(R.id.editIcon).setOnClickListener(v -> pickImage());

        ((TextView) findViewById(R.id.rowMyPosts).findViewById(R.id.settingLabel)).setText("My Posts");
        ((TextView) findViewById(R.id.rowManageAccount).findViewById(R.id.settingLabel)).setText("Manage Account");
        ((TextView) findViewById(R.id.rowPrivacy).findViewById(R.id.settingLabel)).setText("Privacy & Security");
        ((TextView) findViewById(R.id.rowLogout).findViewById(R.id.settingLabel)).setText("Log Out");

        findViewById(R.id.rowMyPosts).setOnClickListener(v ->
                startActivity(new Intent(this, MyPostsActivity.class)));

        findViewById(R.id.rowManageAccount).setOnClickListener(v ->
                manageAccountLauncher.launch(new Intent(this, ManageAccountActivity.class)));

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
        imagePickerLauncher.launch(intent);
    }

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_navigation_view);
        nav.setSelectedItemId(R.id.bottom_settings);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.bottom_map) {
                navigateTo(MapActivity.class);
                return true;
            }

            if (id == R.id.bottom_search) {
                navigateTo(SearchActivity.class);
                return true;
            }

            if (id == R.id.bottom_settings) {
                return true;
            }

            if (id == R.id.bottom_chat) {
                navigateTo(InboxActivity.class);
                return true;
            }

            return false;
        });
    }



    private void navigateTo(Class<?> cls) {
        NavigationUtils.navigateTo(this, cls);
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
                            Glide.with(this).load(photoUrl)
                                    .placeholder(R.drawable.ic_profile)
                                    .into(profileIcon);
                        } else {
                            profileIcon.setImageResource(R.drawable.ic_profile);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show());
    }

    private void uploadToImgur(Uri uri) {
        ImgurUploader.upload(this, uri, new ImgurUploader.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                updateProfilePicture(imageUrl);
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(SettingsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
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
}
