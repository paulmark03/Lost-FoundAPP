package com.example.demoilost;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ManageAccountActivity extends AppCompatActivity {

    private EditText nameEditText;
    private Button saveButton;
    private Button deleteButton;
    private ImageView backButton;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_account);

        nameEditText = findViewById(R.id.nameEditText);
        saveButton = findViewById(R.id.saveNameButton);
        deleteButton = findViewById(R.id.deleteAccountButton);
        backButton = findViewById(R.id.backButton);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            nameEditText.setText(currentUser.getDisplayName());
        }

        saveButton.setOnClickListener(v -> updateDisplayName());
        deleteButton.setOnClickListener(v -> confirmDelete());
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ManageAccountActivity.this, SettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });



    }

    private void updateDisplayName() {
        String newName = nameEditText.getText().toString().trim();
        if (TextUtils.isEmpty(newName)) {
            nameEditText.setError("Name cannot be empty");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);

        db.collection("users").document(currentUser.getUid())
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Name updated", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish(); // Go back to Settings
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update name", Toast.LENGTH_SHORT).show());
    }


    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to permanently delete your account?")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {
        String userId = currentUser.getUid();

        // Step 1: Delete all user's posts
        db.collection("posts")
                .whereEqualTo("posterId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }

                    // Step 2: Delete user document from Firestore
                    db.collection("users").document(userId).delete();

                    // Step 3: Delete user from Firebase Auth
                    currentUser.delete()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Account and posts deleted", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ManageAccountActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show());

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete posts: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

}
