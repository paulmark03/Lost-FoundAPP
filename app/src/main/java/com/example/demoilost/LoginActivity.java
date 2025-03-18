package com.example.demoilost;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

            private EditText emailInput, passwordInput;
            private TextView forgotPassword, registerNow;
            private Button loginButton;
            FirebaseDatabase database;
            DatabaseReference reference;

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);  // Step 1

                setContentView(R.layout.activity_main);  // Step 2

                // Step 3: Now it's safe to initialize your views
                emailInput = findViewById(R.id.email);
                passwordInput = findViewById(R.id.password);
                forgotPassword = findViewById(R.id.forgotPassword);
                registerNow = findViewById(R.id.registerNow);
                loginButton = findViewById(R.id.loginButton);

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
                registerNow.setOnClickListener(new View.OnClickListener() {
                                                   @Override
                                                   public void onClick(View v) {
                                                       Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                                                       startActivity(intent);
                                                   }
                                               });

                // Login Button Click Event
                loginButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!validateEmail() || !validatePassword()) {

                        } else {
                            checkEmail();
                        }
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

            public void checkEmail() {
                String userEmail = emailInput.getText().toString().trim();
                String userPasssword = passwordInput.getText().toString().trim();

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
                Query checkUserDatabase = reference.orderByChild("email").equalTo(userEmail);

                checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            emailInput.setError(null);

                            String passwordFromDB = snapshot.child(userEmail).child("password").getValue(String.class);

                            if(!Objects.equals(passwordFromDB, userPasssword)) {
                                emailInput.setError(null);
                                Intent intent = new Intent(LoginActivity.this, MapActivity.class);
                                startActivity(intent);
                            } else {
                                passwordInput.setError("Invalid credentials");
                                passwordInput.requestFocus();
                            }
                        } else {
                            emailInput.setError("User does not exists");
                            emailInput.requestFocus();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

        }