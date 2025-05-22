package com.example.llmexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class HomeActivity extends AppCompatActivity {

    private TextView textViewUserName;
    private ImageView buttonStartTask;
    private Button buttonHistory;
    private Button buttonShare;
    private Button buttonUpgrade;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize UI components
        textViewUserName = findViewById(R.id.textViewUserName);
        buttonStartTask = findViewById(R.id.buttonStartTask);
        buttonHistory = findViewById(R.id.buttonHistory);
        buttonShare = findViewById(R.id.buttonShare);
        buttonUpgrade = findViewById(R.id.buttonUpgrade);

        // Get username from intent
        if (getIntent().hasExtra("username")) {
            username = getIntent().getStringExtra("username");
            textViewUserName.setText(username);
        } else {
            username = "Student";
            textViewUserName.setText(username);
        }

        // Set up click listener for task start button
        buttonStartTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add animation effect
                buttonStartTask.animate().rotation(360f).setDuration(500).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        // Reset rotation
                        buttonStartTask.setRotation(0f);
                        
                        // Start MainActivity to load quiz data from backend
                        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                        intent.putExtra("username", username);
                        intent.putExtra("taskTitle", "Generated Task 1");
                        intent.putExtra("taskDescription", "Small Description for the generated Task");
                        // We'll use this flag to indicate we're coming from HomeActivity
                        intent.putExtra("fromHomeActivity", true);
                        startActivity(intent);
                        
                        // Add transition animation
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                });
            }
        });

        // Set up click listener for History button
        buttonHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add animation effect
                buttonHistory.animate().alpha(0.5f).setDuration(200).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        buttonHistory.animate().alpha(1.0f).setDuration(200);
                        
                        // Navigate to History Activity
                        Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
                        intent.putExtra("username", username);
                        startActivity(intent);
                        
                        // Add transition animation
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                });
            }
        });

        // Set up click listener for Share button
        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ShareProfileActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        // Set up click listener for Upgrade button
        buttonUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, UpgradeActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        // Add loading animation for the card when the activity starts
        CardView taskCard = findViewById(R.id.cardViewTask);
        if (taskCard != null) {
            taskCard.setAlpha(0f);
            taskCard.setTranslationY(50f);
            taskCard.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setStartDelay(300);
        }
    }

    @Override
    public void onBackPressed() {
        // Show a custom dialog or directly go to login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
} 