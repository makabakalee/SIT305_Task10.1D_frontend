package com.example.llmexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class InterestsActivity extends AppCompatActivity {

    private Button buttonNext;
    private List<Button> topicButtons = new ArrayList<>();
    private List<String> selectedTopics = new ArrayList<>();
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);

        // Get username from intent
        if (getIntent().hasExtra("username")) {
            username = getIntent().getStringExtra("username");
        } else {
            username = "Student";
        }

        // Initialize buttons
        buttonNext = findViewById(R.id.buttonNext);
        
        // Add all topic buttons to the list
        topicButtons.add(findViewById(R.id.buttonAlgorithms1));
        topicButtons.add(findViewById(R.id.buttonDataStructures1));
        topicButtons.add(findViewById(R.id.buttonWebDev1));
        topicButtons.add(findViewById(R.id.buttonTesting1));
        topicButtons.add(findViewById(R.id.buttonAlgorithms2));
        topicButtons.add(findViewById(R.id.buttonDataStructures2));
        topicButtons.add(findViewById(R.id.buttonWebDev2));
        topicButtons.add(findViewById(R.id.buttonTesting2));
        topicButtons.add(findViewById(R.id.buttonAlgorithms3));
        topicButtons.add(findViewById(R.id.buttonDataStructures3));

        // Set up click listeners for topic buttons
        for (Button button : topicButtons) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleTopicSelection((Button) v);
                }
            });
        }

        // Set up next button click listener
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedTopics.isEmpty()) {
                    Toast.makeText(InterestsActivity.this, "Please select at least one topic", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Add animation effect
                buttonNext.animate().alpha(0.5f).setDuration(200).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        buttonNext.animate().alpha(1.0f).setDuration(200);
                        
                        // Navigate to Home Activity
                        Intent intent = new Intent(InterestsActivity.this, HomeActivity.class);
                        intent.putExtra("username", username);
                        intent.putStringArrayListExtra("selectedTopics", new ArrayList<>(selectedTopics));
                        startActivity(intent);
                        
                        // Add transition animation
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        
                        // Finish the activity to prevent going back to interests screen
                        finish();
                    }
                });
            }
        });
    }

    private void toggleTopicSelection(Button button) {
        String topic = button.getText().toString();
        
        if (selectedTopics.contains(topic)) {
            // Deselect the topic
            selectedTopics.remove(topic);
            button.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        } else {
            // Check if maximum number of topics reached
            if (selectedTopics.size() >= 10) {
                Toast.makeText(this, "You can select a maximum of 10 topics", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Select the topic
            selectedTopics.add(topic);
            button.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        }
        
        // Add animation effect for feedback
        button.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(new Runnable() {
            @Override
            public void run() {
                button.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100);
            }
        });
    }
} 