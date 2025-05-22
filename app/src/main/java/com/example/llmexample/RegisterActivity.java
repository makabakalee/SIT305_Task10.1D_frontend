package com.example.llmexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextRegUsername;
    private EditText editTextEmail;
    private EditText editTextConfirmEmail;
    private EditText editTextRegPassword;
    private EditText editTextConfirmPassword;
    private EditText editTextPhone;
    private Button buttonCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize UI components
        editTextRegUsername = findViewById(R.id.editTextRegUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextConfirmEmail = findViewById(R.id.editTextConfirmEmail);
        editTextRegPassword = findViewById(R.id.editTextRegPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextPhone = findViewById(R.id.editTextPhone);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);

        // Set up click listener for create account button
        buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm()) {
                    // Add animation effect
                    buttonCreateAccount.animate().alpha(0.5f).setDuration(200).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            buttonCreateAccount.animate().alpha(1.0f).setDuration(200);
                            
                            // Navigate to Interests Activity
                            Intent intent = new Intent(RegisterActivity.this, InterestsActivity.class);
                            intent.putExtra("username", editTextRegUsername.getText().toString().trim());
                            startActivity(intent);
                            
                            // Add transition animation
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        }
                    });
                }
            }
        });
    }

    private boolean validateForm() {
        String username = editTextRegUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String confirmEmail = editTextConfirmEmail.getText().toString().trim();
        String password = editTextRegPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        // Validate username
        if (username.isEmpty()) {
            showToast("Please enter a username");
            return false;
        }

        // Validate email
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please enter a valid email address");
            return false;
        }

        // Validate email confirmation
        if (!email.equals(confirmEmail)) {
            showToast("Email addresses do not match");
            return false;
        }

        // Validate password
        if (password.isEmpty() || password.length() < 6) {
            showToast("Password must be at least 6 characters");
            return false;
        }

        // Validate password confirmation
        if (!password.equals(confirmPassword)) {
            showToast("Passwords do not match");
            return false;
        }

        // Validate phone
        if (phone.isEmpty() || phone.length() < 10) {
            showToast("Please enter a valid phone number");
            return false;
        }

        return true;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
} 