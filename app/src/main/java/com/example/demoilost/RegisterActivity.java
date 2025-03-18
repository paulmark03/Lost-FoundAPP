package com.example.demoilost;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, passwordInput, confirmPasswordInput;
    private CheckBox termsCheckbox;
    private Button signupButton;
    private TextView loginRedirect;
    private FirebaseAuth auth; // Firebase Authentication instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize UI Elements
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        termsCheckbox = findViewById(R.id.termsCheckbox);
        signupButton = findViewById(R.id.signupButton);
        loginRedirect = findViewById(R.id.loginRedirect);

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance();

        // Sign Up Button Click
        signupButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            // Basic Validation
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!termsCheckbox.isChecked()) {
                Toast.makeText(RegisterActivity.this, "You must accept the Terms & Privacy Policy", Toast.LENGTH_SHORT).show();
                return;
            }

            // Use Firebase Authentication to create a new user
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Update the user's display name
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();
                                user.updateProfile(profileUpdates);
                            }

                            Toast.makeText(RegisterActivity.this, "Sign Up Successful!", Toast.LENGTH_SHORT).show();

                            // Navigate to Login Page
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Display the error message from Firebase
                            Toast.makeText(RegisterActivity.this, "Sign Up Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Redirect to Login
        loginRedirect.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}