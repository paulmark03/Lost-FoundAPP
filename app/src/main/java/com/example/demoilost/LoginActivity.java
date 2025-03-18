package com.example.demoilost;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private TextView forgotPassword, registerNow;
    private Button loginButton;
    private FirebaseAuth auth; // Firebase Authentication instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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
                        // Login successful; navigate to MapActivity
                        Intent intent = new Intent(LoginActivity.this, MapActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Login failed; show error message
                        Toast.makeText(LoginActivity.this, "Invalid credentials: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}