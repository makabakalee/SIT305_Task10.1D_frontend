package com.example.llmexample;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.ArrayList;
import java.util.List;

public class ResultsActivity extends AppCompatActivity {

    private Button buttonContinue;
    private TextView textViewQ1Result;
    private TextView textViewQ2Result;
    private TextView textViewQ3Result;
    private CardView cardViewQ2;
    private CardView cardViewQ3;
    private String username;
    private ArrayList<String> userAnswers;
    private ArrayList<String> questions;
    private ArrayList<String> correctAnswers;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Initialize UI components
        buttonContinue = findViewById(R.id.buttonContinue);
        textViewQ1Result = findViewById(R.id.textViewQ1Result);
        textViewQ2Result = findViewById(R.id.textViewQ2Result);
        textViewQ3Result = findViewById(R.id.textViewQ3Result);
        cardViewQ2 = findViewById(R.id.cardViewQ2Result);
        cardViewQ3 = findViewById(R.id.cardViewQ3Result);

        // Get data from intent
        if (getIntent().hasExtra("username")) {
            username = getIntent().getStringExtra("username");
        } else {
            username = "Student";
        }

        if (getIntent().hasExtra("answers")) {
            userAnswers = getIntent().getStringArrayListExtra("answers");
        }
        
        // Get quiz data if available
        if (getIntent().hasExtra("questions")) {
            questions = getIntent().getStringArrayListExtra("questions");
        }
        
        if (getIntent().hasExtra("correctAnswers")) {
            correctAnswers = getIntent().getStringArrayListExtra("correctAnswers");
        }
        
        // Show only the question cards for which we have answers
        if (userAnswers != null) {
            // Only show Q2 if we have at least 2 answers
            if (userAnswers.size() >= 2 && cardViewQ2 != null) {
                cardViewQ2.setVisibility(View.VISIBLE);
            } else if (cardViewQ2 != null) {
                cardViewQ2.setVisibility(View.GONE);
            }
            
            // Only show Q3 if we have at least 3 answers
            if (userAnswers.size() >= 3 && cardViewQ3 != null) {
                cardViewQ3.setVisibility(View.VISIBLE);
            } else if (cardViewQ3 != null) {
                cardViewQ3.setVisibility(View.GONE);
            }
        }
        
        // Process the answers
        processAnswers();
        
        // Save quiz history to database
        saveQuizHistory();

        // Set up click listener for continue button
        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add animation effect
                buttonContinue.animate().alpha(0.5f).setDuration(200).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        buttonContinue.animate().alpha(1.0f).setDuration(200);
                        
                        // Navigate back to Home Activity
                        Intent intent = new Intent(ResultsActivity.this, HomeActivity.class);
                        intent.putExtra("username", username);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        
                        // Add transition animation
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                });
            }
        });

        // Add animations for result cards when the activity starts
        addCardAnimations();
    }
    
    private void saveQuizHistory() {
        // Check if we have questions to save
        if (questions != null && !questions.isEmpty()) {
            try {
                // Initialize database helper
                dbHelper = new DatabaseHelper(this);
                
                // Save each question with user's answer
                for (int i = 0; i < questions.size(); i++) {
                    if (i < userAnswers.size()) {
                        String title = "Question " + (i + 1);
                        String description = questions.get(i);
                        
                        // Get options for this question if available
                        String optionsString = "";
                        if (getIntent().hasExtra("options")) {
                            ArrayList<ArrayList<String>> allOptions = 
                                    (ArrayList<ArrayList<String>>) getIntent().getSerializableExtra("options");
                            
                            if (allOptions != null && i < allOptions.size()) {
                                ArrayList<String> questionOptions = allOptions.get(i);
                                if (questionOptions != null && !questionOptions.isEmpty()) {
                                    // Join options with a pipe delimiter for storage
                                    optionsString = TextUtils.join("|", questionOptions);
                                }
                            }
                        }
                        
                        // Get user's answer
                        String userAnswer = userAnswers.get(i);
                        
                        // Get correct answer if available
                        String correctAns = "";
                        if (correctAnswers != null && i < correctAnswers.size()) {
                            correctAns = correctAnswers.get(i);
                        }
                        
                        // Convert letter answer to option text if possible
                        String userAnswerText = userAnswer;
                        String correctAnswerText = correctAns;
                        
                        if (getIntent().hasExtra("options")) {
                            ArrayList<ArrayList<String>> allOptions = 
                                    (ArrayList<ArrayList<String>>) getIntent().getSerializableExtra("options");
                            
                            if (allOptions != null && i < allOptions.size()) {
                                ArrayList<String> questionOptions = allOptions.get(i);
                                
                                // Convert user answer letter to option text
                                int userIndex = letterToIndex(userAnswer);
                                if (userIndex >= 0 && userIndex < questionOptions.size()) {
                                    userAnswerText = userAnswer + ": " + questionOptions.get(userIndex);
                                }
                                
                                // Convert correct answer letter to option text
                                int correctIndex = letterToIndex(correctAns);
                                if (correctIndex >= 0 && correctIndex < questionOptions.size()) {
                                    correctAnswerText = correctAns + ": " + questionOptions.get(correctIndex);
                                }
                            }
                        }
                        
                        // Check if user answered correctly
                        boolean answered = false;
                        if (correctAnswers != null && i < correctAnswers.size()) {
                            answered = userAnswers.get(i).equals(correctAnswers.get(i));
                        }
                        
                        // Add to database with extended information
                        dbHelper.addHistoryItem(title, description, answered, username, 
                                optionsString, userAnswerText, correctAnswerText);
                    }
                }
                
                Toast.makeText(this, "Quiz history saved", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Error saving quiz history: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    // Helper method to convert answer letter to index
    private int letterToIndex(String letter) {
        if (letter == null || letter.isEmpty()) {
            return -1;
        }
        
        char firstChar = letter.charAt(0);
        if (firstChar >= 'A' && firstChar <= 'Z') {
            return firstChar - 'A';
        } else if (firstChar >= 'a' && firstChar <= 'z') {
            return firstChar - 'a';
        }
        
        return -1;
    }

    private void processAnswers() {
        // In a real app, this would contain logic to evaluate the answers
        // and provide feedback based on AI processing
        
        // For this demo, we'll display feedback based on whether answers match the API data
        if (userAnswers != null && !userAnswers.isEmpty()) {
            // For question 1 (may not have correct_answer field)
            if (questions != null && questions.size() > 0) {
                String question = questions.get(0);
                String userAnswer = userAnswers.get(0);
                
                // Set default correct answer for first question to "B" (second option)
                String correctAnswer = correctAnswers != null && correctAnswers.size() > 0 ? 
                        correctAnswers.get(0) : "B";
                
                boolean isCorrect = userAnswer.equals(correctAnswer);
                
                textViewQ1Result.setText("For question \"" + question + "\", you answered \"" + 
                        getLabelForOption(userAnswer) + "\". " + (isCorrect ? "That's correct! " : "The correct answer is \"" + 
                        getLabelForOption(correctAnswer) + "\". ") + "The AI suggests you should explore more about this topic. " +
                        "Your understanding of core concepts seems " + (isCorrect ? "very solid." : "developing."));
            } else {
                // Fallback to default text
                textViewQ1Result.setText("Based on your answer \"" + getLabelForOption(userAnswers.get(0)) + 
                        "\", the AI suggests you should explore more about film genres. " +
                        "Your understanding of core concepts seems good.");
            }
            
            // For question 2, use question from API if available and if the user answered it
            if (userAnswers.size() >= 2 && questions != null && questions.size() > 1) {
                String question = questions.get(1);
                String userAnswer = userAnswers.get(1);
                String correctAnswer = correctAnswers != null && correctAnswers.size() > 1 ? 
                        correctAnswers.get(1) : "B";
                        
                boolean isCorrect = userAnswer.equals(correctAnswer);
                
                textViewQ2Result.setText("For question \"" + question + "\", you answered \"" + 
                        getLabelForOption(userAnswer) + "\". " + (isCorrect ? "That's correct! " : "The correct answer is \"" + 
                        getLabelForOption(correctAnswer) + "\". ") + "The AI has analyzed your learning style and recommends " +
                        "visual learning materials for this topic. You seem to understand the " +
                        "theoretical concepts " + (isCorrect ? "very well." : "fairly well."));
            } else if (userAnswers.size() >= 2) {
                // Use default text if we have the answer but no question
                textViewQ2Result.setText("The AI has analyzed your learning style and recommends " +
                        "visual learning materials for this topic. You seem to understand the " +
                        "theoretical concepts well.");
            }
            
            // For question 3, use question from API if available and if the user answered it
            if (userAnswers.size() >= 3 && questions != null && questions.size() > 2) {
                String question = questions.get(2);
                String userAnswer = userAnswers.get(2);
                String correctAnswer = correctAnswers != null && correctAnswers.size() > 2 ? 
                        correctAnswers.get(2) : "B";
                        
                boolean isCorrect = userAnswer.equals(correctAnswer);
                
                textViewQ3Result.setText("For question \"" + question + "\", you answered \"" + 
                        getLabelForOption(userAnswer) + "\". " + (isCorrect ? "That's correct! " : "The correct answer is \"" + 
                        getLabelForOption(correctAnswer) + "\". ") + "Based on your past quiz performances and this response, " +
                        "you've shown " + (isCorrect ? "significant" : "some") + " improvement in understanding the core concepts. " +
                        "The AI recommends moving to " + (isCorrect ? "advanced" : "intermediate") + " topics.");
            } else if (userAnswers.size() >= 3) {
                // Use default text if we have the answer but no question
                textViewQ3Result.setText("Based on your past quiz performances and this response, " +
                        "you've shown significant improvement in understanding the core concepts. " +
                        "The AI recommends moving to advanced topics.");
            }
        } else {
            // Fallback if no answers were received
            textViewQ1Result.setText("No answer data available for evaluation.");
            textViewQ2Result.setText("No answer data available for evaluation.");
            textViewQ3Result.setText("No answer data available for evaluation.");
        }
    }
    
    // Helper method to convert option letter to human-readable text
    private String getLabelForOption(String optionLetter) {
        switch (optionLetter) {
            case "A": return "Option A";
            case "B": return "Option B";
            case "C": return "Option C";
            case "D": return "Option D";
            default: return optionLetter;
        }
    }

    private void addCardAnimations() {
        // Find all CardViews
        List<CardView> cards = new ArrayList<>();
        
        // Get the root view
        View rootView = findViewById(android.R.id.content);
        
        // Find all direct child views in the layout
        if (rootView instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) rootView;
            findAllCardViews(viewGroup, cards);
        }
        
        // Apply animation to each card
        int delay = 200;
        for (CardView card : cards) {
            if (card.getVisibility() == View.VISIBLE) {
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
    
    private void findAllCardViews(ViewGroup viewGroup, List<CardView> cards) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof CardView) {
                cards.add((CardView) child);
            } else if (child instanceof ViewGroup) {
                findAllCardViews((ViewGroup) child, cards);
            }
        }
    }
} 