package com.example.llmexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    private TextView textViewTaskTitle;
    private TextView textViewTaskDescription;
    private RadioGroup radioGroupQ1;
    private RadioGroup radioGroupQ2;
    private RadioGroup radioGroupQ3;
    private Button buttonSubmit;
    private ImageButton buttonExpandQ2;
    private ImageButton buttonExpandQ3;
    private LinearLayout layoutQ2Options;
    private LinearLayout layoutQ3Options;
    private CardView cardViewQ3;
    private String username;
    private List<String> selectedAnswers = new ArrayList<>();
    
    // Quiz data from API
    private ArrayList<String> questions;
    private ArrayList<ArrayList<String>> options;
    private ArrayList<String> correctAnswers;
    private boolean fromAPI = false;
    private boolean q2Expanded = false;
    private boolean q3Expanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Initialize UI components
        textViewTaskTitle = findViewById(R.id.textViewTaskTitle);
        textViewTaskDescription = findViewById(R.id.textViewTaskDescription);
        radioGroupQ1 = findViewById(R.id.radioGroupQ1);
        radioGroupQ2 = findViewById(R.id.radioGroupQ2);
        radioGroupQ3 = findViewById(R.id.radioGroupQ3);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        buttonExpandQ2 = findViewById(R.id.buttonExpandQ2);
        buttonExpandQ3 = findViewById(R.id.buttonExpandQ3);
        layoutQ2Options = findViewById(R.id.layoutQ2Options);
        layoutQ3Options = findViewById(R.id.layoutQ3Options);
        cardViewQ3 = findViewById(R.id.cardViewQ3);

        // Get data from intent
        if (getIntent().hasExtra("username")) {
            username = getIntent().getStringExtra("username");
        } else {
            username = "Student";
        }

        if (getIntent().hasExtra("taskTitle")) {
            textViewTaskTitle.setText(getIntent().getStringExtra("taskTitle"));
        }

        if (getIntent().hasExtra("taskDescription")) {
            textViewTaskDescription.setText(getIntent().getStringExtra("taskDescription"));
        }
        
        // Check if we're coming from the API
        fromAPI = getIntent().getBooleanExtra("fromAPI", false);
        if (fromAPI) {
            questions = getIntent().getStringArrayListExtra("questions");
            options = (ArrayList<ArrayList<String>>) getIntent().getSerializableExtra("options");
            correctAnswers = getIntent().getStringArrayListExtra("correctAnswers");
            
            // Update UI with API data for question 1
            updateQuestionOne();
            
            // Pre-populate questions 2 and 3 header texts (without expanding)
            if (questions != null && questions.size() > 1) {
                TextView q2TextView = findViewById(R.id.textViewQuestion2);
                TextView q2Content = findViewById(R.id.textViewQ2Content);
                
                if (q2TextView != null) {
                    q2TextView.setText("2. " + questions.get(1));
                }
                
                if (q2Content != null) {
                    q2Content.setText(questions.get(1));
                }
            }
            
            if (questions != null && questions.size() > 2) {
                TextView q3TextView = findViewById(R.id.textViewQuestion3);
                TextView q3Content = findViewById(R.id.textViewQ3Content);
                
                if (q3TextView != null) {
                    q3TextView.setText("3. " + questions.get(2));
                }
                
                if (q3Content != null) {
                    q3Content.setText(questions.get(2));
                }
            }
        }

        // Set up click listener for expand buttons
        buttonExpandQ2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!q2Expanded) {
                    // Show question 2 options and load data
                    q2Expanded = true;
                    layoutQ2Options.setVisibility(View.VISIBLE);
                    
                    // Animate the button rotation
                    buttonExpandQ2.animate().rotation(180).setDuration(300).start();
                    
                    // Update question 2 with data from API
                    updateQuestionTwo();
                    
                    // Show question 3 card after question 2 is expanded
                    cardViewQ3.setVisibility(View.VISIBLE);
                    cardViewQ3.setAlpha(0f);
                    cardViewQ3.animate().alpha(1f).setDuration(500).start();
                } else {
                    // Hide question 2 options
                    q2Expanded = false;
                    layoutQ2Options.setVisibility(View.GONE);
                    
                    // Animate button back
                    buttonExpandQ2.animate().rotation(0).setDuration(300).start();
                }
            }
        });
        
        buttonExpandQ3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!q3Expanded) {
                    // Show question 3 options and load data
                    q3Expanded = true;
                    layoutQ3Options.setVisibility(View.VISIBLE);
                    
                    // Animate the button rotation
                    buttonExpandQ3.animate().rotation(180).setDuration(300).start();
                    
                    // Update question 3 with data from API
                    updateQuestionThree();
                } else {
                    // Hide question 3 options
                    q3Expanded = false;
                    layoutQ3Options.setVisibility(View.GONE);
                    
                    // Animate button back
                    buttonExpandQ3.animate().rotation(0).setDuration(300).start();
                }
            }
        });

        // Set up click listener for submit button
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateAnswers()) {
                    // Add animation effect
                    buttonSubmit.animate().alpha(0.5f).setDuration(200).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            buttonSubmit.animate().alpha(1.0f).setDuration(200);
                            
                            // Navigate to Results Activity
                            Intent intent = new Intent(QuizActivity.this, ResultsActivity.class);
                            intent.putExtra("username", username);
                            intent.putStringArrayListExtra("answers", new ArrayList<>(selectedAnswers));
                            if (fromAPI) {
                                intent.putStringArrayListExtra("questions", questions);
                                intent.putStringArrayListExtra("correctAnswers", correctAnswers);
                                intent.putExtra("options", options);
                            }
                            startActivity(intent);
                            
                            // Add transition animation
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        }
                    });
                } else {
                    Toast.makeText(QuizActivity.this, "Please answer all questions", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Add animations for question cards when the activity starts
        addCardAnimations();
    }
    
    private void updateQuestionOne() {
        // Update question 1 if available
        if (questions != null && questions.size() > 0) {
            // Find question TextViews
            TextView q1TextView = findViewById(R.id.textViewQuestion1);
            TextView q1Content = findViewById(R.id.textViewQ1Content);
            
            if (q1TextView != null) {
                q1TextView.setText("1. " + questions.get(0));
            }
            
            if (q1Content != null) {
                q1Content.setText(questions.get(0));
            }
            
            // Update radio buttons with options
            if (options != null && options.size() > 0 && options.get(0) != null) {
                RadioButton rb1 = findViewById(R.id.radioQ1A1);
                RadioButton rb2 = findViewById(R.id.radioQ1A2);
                RadioButton rb3 = findViewById(R.id.radioQ1A3);
                RadioButton rb4 = findViewById(R.id.radioQ1A4);
                
                List<String> q1Options = options.get(0);
                if (q1Options.size() > 0 && rb1 != null) {
                    rb1.setText(q1Options.get(0));
                }
                if (q1Options.size() > 1 && rb2 != null) {
                    rb2.setText(q1Options.get(1));
                }
                if (q1Options.size() > 2 && rb3 != null) {
                    rb3.setText(q1Options.get(2));
                }
                if (q1Options.size() > 3 && rb4 != null) {
                    rb4.setText(q1Options.get(3));
                }
            }
        }
    }
    
    private void updateQuestionTwo() {
        // Update question 2 if available
        if (questions != null && questions.size() > 1) {
            // Find question TextView
            TextView q2TextView = findViewById(R.id.textViewQuestion2);
            TextView q2Content = findViewById(R.id.textViewQ2Content);
            
            if (q2TextView != null) {
                q2TextView.setText("2. " + questions.get(1));
            }
            
            if (q2Content != null) {
                q2Content.setText(questions.get(1));
            }
            
            // Update radio buttons with options
            if (options != null && options.size() > 1 && options.get(1) != null) {
                RadioButton rb1 = findViewById(R.id.radioQ2A1);
                RadioButton rb2 = findViewById(R.id.radioQ2A2);
                RadioButton rb3 = findViewById(R.id.radioQ2A3);
                RadioButton rb4 = findViewById(R.id.radioQ2A4);
                
                List<String> q2Options = options.get(1);
                if (q2Options.size() > 0 && rb1 != null) {
                    rb1.setText(q2Options.get(0));
                }
                if (q2Options.size() > 1 && rb2 != null) {
                    rb2.setText(q2Options.get(1));
                }
                if (q2Options.size() > 2 && rb3 != null) {
                    rb3.setText(q2Options.get(2));
                }
                if (q2Options.size() > 3 && rb4 != null) {
                    rb4.setText(q2Options.get(3));
                }
            }
        }
    }
    
    private void updateQuestionThree() {
        // Update question 3 if available
        if (questions != null && questions.size() > 2) {
            // Find question TextView
            TextView q3TextView = findViewById(R.id.textViewQuestion3);
            TextView q3Content = findViewById(R.id.textViewQ3Content);
            
            if (q3TextView != null) {
                q3TextView.setText("3. " + questions.get(2));
            }
            
            if (q3Content != null) {
                q3Content.setText(questions.get(2));
            }
            
            // Update radio buttons with options
            if (options != null && options.size() > 2 && options.get(2) != null) {
                RadioButton rb1 = findViewById(R.id.radioQ3A1);
                RadioButton rb2 = findViewById(R.id.radioQ3A2);
                RadioButton rb3 = findViewById(R.id.radioQ3A3);
                RadioButton rb4 = findViewById(R.id.radioQ3A4);
                
                List<String> q3Options = options.get(2);
                if (q3Options.size() > 0 && rb1 != null) {
                    rb1.setText(q3Options.get(0));
                }
                if (q3Options.size() > 1 && rb2 != null) {
                    rb2.setText(q3Options.get(1));
                }
                if (q3Options.size() > 2 && rb3 != null) {
                    rb3.setText(q3Options.get(2));
                }
                if (q3Options.size() > 3 && rb4 != null) {
                    rb4.setText(q3Options.get(3));
                }
            }
        }
    }

    private boolean validateAnswers() {
        selectedAnswers.clear();
        boolean allAnswered = true;
        
        // Check if question 1 has an answer
        int selectedRadioButtonId = radioGroupQ1.getCheckedRadioButtonId();
        if (selectedRadioButtonId == -1) {
            allAnswered = false;
        } else {
            // Get the selected answer text
            RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
            selectedAnswers.add(selectedRadioButton.getText().toString());
        }
        
        // If question 2 is expanded, check if it has an answer
        if (q2Expanded) {
            selectedRadioButtonId = radioGroupQ2.getCheckedRadioButtonId();
            if (selectedRadioButtonId == -1) {
                allAnswered = false;
            } else {
                // Get the selected answer text
                RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
                selectedAnswers.add(selectedRadioButton.getText().toString());
            }
        }
        
        // If question 3 is expanded, check if it has an answer
        if (q3Expanded) {
            selectedRadioButtonId = radioGroupQ3.getCheckedRadioButtonId();
            if (selectedRadioButtonId == -1) {
                allAnswered = false;
            } else {
                // Get the selected answer text
                RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
                selectedAnswers.add(selectedRadioButton.getText().toString());
            }
        }
        
        return allAnswered;
    }

    private void addCardAnimations() {
        // Find all CardView elements
        List<CardView> cardViews = new ArrayList<>();
        
        // Add only the cards that are visible at start
        CardView cardViewQ1 = findViewById(R.id.cardViewQ1);
        CardView cardViewQ2 = findViewById(R.id.cardViewQ2);
        if (cardViewQ1 != null) {
            cardViews.add(cardViewQ1);
        }
        if (cardViewQ2 != null) {
            cardViews.add(cardViewQ2);
        }
        
        // Apply animation to each card
        int delay = 200;
        for (CardView card : cardViews) {
            if (card != null) {
                card.setAlpha(0f);
                card.setTranslationY(50f);
                card.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(500)
                        .setStartDelay(delay);
                delay += 200; // Increase delay for each card
            }
        }
    }
} 