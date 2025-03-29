package com.example.demoilost;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
    private FirebaseAuth auth; // Firebase Authentication instance

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
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        // Forgot Password Click Event
        forgotPassword.setOnClickListener(v ->
                Toast.makeText(LoginActivity.this, "Forgot Password Clicked", Toast.LENGTH_SHORT).show());

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
                        Toast.makeText(LoginActivity.this, "Invalid credentials: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



}