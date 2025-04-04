package com.example.demoilost;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;

    private TextView forgotPassword;
    private TextView registerNow;
    private Button loginButton;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        initializeUI();
        initializeFirebase();
        setupListeners();
    }

    private void initializeUI() {
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        forgotPassword = findViewById(R.id.forgotPassword);
        registerNow = findViewById(R.id.registerNow);
        loginButton = findViewById(R.id.loginButton);
    }

    private void initializeFirebase() {
        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
    }

    private void setupListeners() {
        forgotPassword.setOnClickListener(v -> showResetDialog());
        registerNow.setOnClickListener(v -> navigateToRegister());
        loginButton.setOnClickListener(v -> attemptLogin());
    }

    private void showResetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        final EditText inputEmail = new EditText(this);
        inputEmail.setHint("Enter your email");
        builder.setView(inputEmail);

        builder.setPositiveButton("Send Reset Link", (dialog, which) -> {
            String email = inputEmail.getText().toString().trim();
            if (!TextUtils.isEmpty(email)) {
                sendPasswordResetEmail(email);
            } else {
                showToast("Please enter an email address");
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void navigateToRegister() {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    private void attemptLogin() {
        if (!validateEmail() || !validatePassword()) return;
        signInUser();
    }

    private boolean validateEmail() {
        String email = emailInput.getText().toString();
        if (email.isEmpty()) {
            emailInput.setError("Email cannot be empty");
            return false;
        }
        emailInput.setError(null);
        return true;
    }

    private boolean validatePassword() {
        String password = passwordInput.getText().toString();
        if (password.isEmpty()) {
            passwordInput.setError("Password cannot be empty");
            return false;
        }
        passwordInput.setError(null);
        return true;
    }

    private void sendPasswordResetEmail(String email) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast("Reset link sent to " + email);
                    } else {
                        String error = (task.getException() != null) ? task.getException().getMessage() : "Unknown error";
                        showToast("Error: " + error);
                    }
                });
    }

    private void signInUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        handleSuccessfulLogin();
                    } else {
                        Log.e("LoginError", "Login failed", task.getException());
                        showToast("Invalid credentials: " + task.getException().getMessage());
                    }
                });
    }

    private void handleSuccessfulLogin() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        String displayName = user.getDisplayName() != null ? user.getDisplayName() : "Unnamed";
        String email = user.getEmail();
        String photoUrl = (user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : "";

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("name", displayName);
        userData.put("email", email);
        userData.put("photoUrl", photoUrl);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    startActivity(new Intent(LoginActivity.this, MapActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("Failed to save user: " + e.getMessage()));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
