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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private TextView forgotPassword, registerNow;
    private Button loginButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI Elements
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        forgotPassword = findViewById(R.id.forgotPassword);
        registerNow = findViewById(R.id.registerNow);
        loginButton = findViewById(R.id.loginButton);

        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();

        // Enable edge-to-edge UI
        EdgeToEdge.enable(this);


        forgotPassword.setOnClickListener(v -> {
            // Show a dialog to collect the user's email
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setTitle("Reset Password");

            final EditText inputEmail = new EditText(LoginActivity.this);
            inputEmail.setHint("Enter your email");
            builder.setView(inputEmail);

            builder.setPositiveButton("Send Reset Link", (dialog, which) -> {
                String email = inputEmail.getText().toString().trim();
                if (!TextUtils.isEmpty(email)) {
                    sendPasswordResetEmail(email);
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Please enter an email address", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", (dialogInterface, i) ->
                    dialogInterface.dismiss()
            );

            builder.show();
        });


        // Register Now Click Event
        registerNow.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Login Button Click Event
        loginButton.setOnClickListener(v -> {
            if (!validateEmail() || !validatePassword()) {
                return;
            }
            signInUser();
        });
    }

    private void sendPasswordResetEmail(String email) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this,
                                "Reset link sent to " + email,
                                Toast.LENGTH_LONG).show();
                    } else {
                        String error = (task.getException() != null)
                                ? task.getException().getMessage() : "unknown error";
                        Toast.makeText(LoginActivity.this,
                                "Error: " + error,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    public Boolean validateEmail() {
        String val = emailInput.getText().toString();
        if (val.isEmpty()) {
            emailInput.setError("Email cannot be empty");
            return false;
        } else {
            emailInput.setError(null);
            return true;
        }
    }

    public Boolean validatePassword() {
        String val = passwordInput.getText().toString();
        if (val.isEmpty()) {
            passwordInput.setError("Password cannot be empty");
            return false;
        } else {
            passwordInput.setError(null);
            return true;
        }
    }

    public void signInUser() {
        String userEmail = emailInput.getText().toString().trim();
        String userPassword = passwordInput.getText().toString().trim();

        auth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Login successful
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            String displayName = user.getDisplayName();
                            String email = user.getEmail();
                            String photoUrl = (user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : "";

                            Map<String, Object> userData = new HashMap<>();
                            userData.put("uid", uid);
                            userData.put("name", displayName != null ? displayName : "Unnamed");
                            userData.put("email", email);
                            userData.put("photoUrl", photoUrl);

                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(uid)
                                    .set(userData, SetOptions.merge())
                                    .addOnSuccessListener(aVoid -> {
                                        // Go to MapActivity after saving user
                                        Intent intent = new Intent(LoginActivity.this, MapActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(LoginActivity.this, "Failed to save user: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                    );
                        }
                    } else {
                        // Login failed
                        Log.e("LoginError", "Login failed", task.getException());
                        Toast.makeText(LoginActivity.this, "Invalid credentials: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



}