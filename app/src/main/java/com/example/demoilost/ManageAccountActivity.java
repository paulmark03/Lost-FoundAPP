package com.example.demoilost;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageAccountActivity extends AppCompatActivity {

    private EditText nameEditText;
    private Button saveButton, deleteButton;
    private ImageView backButton;

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private static final String CHAT_COLLECTION = "chats";
    private static final String USERS_COLLECTION = "users";
    private static final String POSTS_COLLECTION = "posts";

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_account);

        initViews();
        initFirebase();

        if (currentUser != null && currentUser.getDisplayName() != null) {
            nameEditText.setText(currentUser.getDisplayName());
        }

        saveButton.setOnClickListener(v -> updateDisplayName());
        deleteButton.setOnClickListener(v -> confirmDelete());
        backButton.setOnClickListener(v -> navigateBack());
    }

    private void initViews() {
        nameEditText = findViewById(R.id.nameEditText);
        saveButton = findViewById(R.id.saveNameButton);
        deleteButton = findViewById(R.id.deleteAccountButton);
        backButton = findViewById(R.id.backButton);
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();
    }

    private void navigateBack() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void updateDisplayName() {
        String newName = nameEditText.getText().toString().trim();
        if (TextUtils.isEmpty(newName)) {
            nameEditText.setError("Name cannot be empty");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);

        db.collection(USERS_COLLECTION).document(currentUser.getUid())
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> updateFirebaseUserProfile(newName))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update name", Toast.LENGTH_SHORT).show());
    }

    private void updateFirebaseUserProfile(String newName) {
        currentUser.updateProfile(
                new UserProfileChangeRequest.Builder().setDisplayName(newName).build()
        ).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Name updated", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to update auth profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to permanently delete your account?")
                .setPositiveButton("Delete", (dialog, which) -> deleteUserData())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUserData() {
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        deleteUserPosts(userId, () ->
                deleteUserChats(userId, () ->
                        deleteUserAndAuth(userId)
                )
        );
    }

    private void deleteUserPosts(String userId, Runnable onComplete) {
        db.collection(POSTS_COLLECTION)
                .whereEqualTo("posterId", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        doc.getReference().delete();
                    }
                    onComplete.run();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete posts", Toast.LENGTH_SHORT).show());
    }

    private void deleteUserChats(String userId, Runnable onComplete) {
        List<DocumentSnapshot> allChats = new ArrayList<>();

        db.collection(CHAT_COLLECTION).whereEqualTo("userId", userId).get()
                .addOnSuccessListener(userChats -> {
                    allChats.addAll(userChats.getDocuments());

                    db.collection(CHAT_COLLECTION).whereEqualTo("founderId", userId).get()
                            .addOnSuccessListener(founderChats -> {
                                allChats.addAll(founderChats.getDocuments());

                                for (DocumentSnapshot chat : allChats) {
                                    String chatId = chat.getId();

                                    // Delete messages in subcollection
                                    db.collection(CHAT_COLLECTION).document(chatId)
                                            .collection("messages").get()
                                            .addOnSuccessListener(messages -> {
                                                for (DocumentSnapshot msg : messages.getDocuments()) {
                                                    msg.getReference().delete();
                                                }
                                            });

                                    chat.getReference().delete();
                                }

                                onComplete.run();
                            });
                });
    }

    private void deleteUserAndAuth(String userId) {
        db.collection(USERS_COLLECTION).document(userId).delete();

        currentUser.delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show());
    }
}
