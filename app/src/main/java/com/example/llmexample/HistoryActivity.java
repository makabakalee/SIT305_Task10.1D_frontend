package com.example.llmexample;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private TextView textViewHistory;
    private RecyclerView recyclerViewHistory;
    private Button buttonBack;
    private Button buttonClear;
    private String username;
    private DatabaseHelper dbHelper;
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Initialize UI components
        textViewHistory = findViewById(R.id.textViewHistory);
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        buttonBack = findViewById(R.id.buttonBack);
        buttonClear = findViewById(R.id.buttonClear);

        // Get username from intent
        if (getIntent().hasExtra("username")) {
            username = getIntent().getStringExtra("username");
        } else {
            username = "Student";
        }

        // Setup RecyclerView with a LinearLayoutManager
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));

        // Load history data from database
        loadHistoryData();

        // Set up click listener for back button
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        
        // Set up click listener for clear all button
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClearConfirmDialog();
            }
        });

        // Add animations for items
        addAnimations();
    }
    
    private void showClearConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Clear All History");
        builder.setMessage("Are you sure you want to delete all history records? This action cannot be undone.");
        builder.setPositiveButton("Yes, Clear All", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearAllHistory();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void clearAllHistory() {
        try {
            // Clear all history for this user
            dbHelper.clearHistory(username);
            
            // Reload data (will show placeholder if empty)
            loadHistoryData();
            
            Toast.makeText(this, "All history cleared", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error clearing history: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    }
    
    private void deleteHistoryItem(int position) {
        try {
            // Get the item to delete
            HistoryItem item = adapter.getHistoryItems().get(position);
            
            // Remove from adapter's data
            adapter.removeItem(position);
            
            // Delete from database
            dbHelper.deleteHistoryItem(item.getTitle(), item.getDate(), username);
            
            Toast.makeText(this, "History item deleted", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error deleting item: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void loadHistoryData() {
        // Get history items from database
        List<HistoryItem> historyItems = dbHelper.getAllHistoryItems(username);
        
        // If no history items found, show a message
        if (historyItems.isEmpty()) {
            Toast.makeText(this, "No history records found", Toast.LENGTH_SHORT).show();
            // Add a placeholder item
            historyItems.add(new HistoryItem("No Questions Yet", 
                    "Complete quizzes to see your history here.", false, "", "", "", ""));
        }
        
        // Create and set adapter with history data
        adapter = new HistoryAdapter(historyItems);
        recyclerViewHistory.setAdapter(adapter);
    }

    private void addAnimations() {
        CardView headerCard = findViewById(R.id.cardViewHeader);
        if (headerCard != null) {
            headerCard.setAlpha(0f);
            headerCard.setTranslationY(-50f);
            headerCard.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setStartDelay(200);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    // History item data class
    public static class HistoryItem {
        private String title;
        private String description;
        private boolean answered;
        private String date;
        private String options;
        private String userAnswer;
        private String correctAnswer;

        public HistoryItem(String title, String description, boolean answered, String date) {
            this(title, description, answered, date, "", "", "");
        }

        public HistoryItem(String title, String description, boolean answered, String date,
                          String options, String userAnswer, String correctAnswer) {
            this.title = title;
            this.description = description;
            this.answered = answered;
            this.date = date;
            this.options = options;
            this.userAnswer = userAnswer;
            this.correctAnswer = correctAnswer;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public boolean isAnswered() {
            return answered;
        }

        public String getDate() {
            return date;
        }
        
        public String getOptions() {
            return options;
        }
        
        // Get list of options
        public List<String> getOptionsList() {
            if (TextUtils.isEmpty(options)) {
                return new ArrayList<>();
            }
            return Arrays.asList(options.split("\\|"));
        }
        
        // Get specific option by index (0-based)
        public String getOption(int index) {
            List<String> optionsList = getOptionsList();
            if (index >= 0 && index < optionsList.size()) {
                return optionsList.get(index);
            }
            return "";
        }
        
        public String getUserAnswer() {
            return userAnswer;
        }
        
        public String getCorrectAnswer() {
            return correctAnswer;
        }
        
        // Extract option letter from user answer (e.g., "A: Answer text" -> "A")
        public String getUserAnswerLetter() {
            if (!TextUtils.isEmpty(userAnswer) && userAnswer.length() > 0) {
                int colonIndex = userAnswer.indexOf(':');
                if (colonIndex > 0) {
                    return userAnswer.substring(0, colonIndex).trim();
                }
                return userAnswer.substring(0, 1).trim();
            }
            return "";
        }
    }

    // History adapter
    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

        private List<HistoryItem> historyItems;

        public HistoryAdapter(List<HistoryItem> historyItems) {
            this.historyItems = historyItems;
        }
        
        public List<HistoryItem> getHistoryItems() {
            return historyItems;
        }
        
        public void removeItem(int position) {
            if (position >= 0 && position < historyItems.size()) {
                historyItems.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, historyItems.size());
                
                // If list is now empty, add placeholder
                if (historyItems.isEmpty()) {
                    historyItems.add(new HistoryItem("No Questions Yet", 
                            "Complete quizzes to see your history here.", false, "", "", "", ""));
                    notifyItemInserted(0);
                }
            }
        }

        @Override
        public HistoryViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.history_item, parent, false);
            return new HistoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(HistoryViewHolder holder, int position) {
            HistoryItem item = historyItems.get(position);
            holder.bind(item);
            
            // Add animation for each item
            holder.itemView.setAlpha(0f);
            holder.itemView.setTranslationY(50f);
            holder.itemView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setStartDelay(100 * position);
            
            // Setup delete button click
            final int itemPosition = position;
            holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Show confirmation dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
                    builder.setTitle("Delete Record");
                    builder.setMessage("Are you sure you want to delete this history record?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteHistoryItem(itemPosition);
                        }
                    });
                    builder.setNegativeButton("Cancel", null);
                    builder.show();
                }
            });
            
            // Add click listener for detailed view
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Show full details of the question in a dialog
                    showQuestionDetailsDialog(item);
                }
            });
        }
        
        // Show a dialog with full question details
        private void showQuestionDetailsDialog(HistoryItem item) {
            AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
            builder.setTitle(item.getTitle());
            
            // Build the content of the dialog
            StringBuilder content = new StringBuilder();
            content.append(item.getDescription()).append("\n\n");
            
            // Get all options and display each one
            List<String> options = item.getOptionsList();
            if (!options.isEmpty()) {
                content.append("Options:\n");
                
                // Display each option with its letter
                for (int i = 0; i < options.size(); i++) {
                    char letter = (char) ('A' + i);
                    String option = options.get(i);
                    // Check if option already has a letter prefix
                    if (option.length() > 2 && option.charAt(1) == '.') {
                        // If it already has a prefix (like "A. Option content"), use as is
                        content.append(option).append("\n");
                    } else {
                        // Otherwise add the letter prefix
                        content.append(letter).append(". ").append(option).append("\n");
                    }
                }
                content.append("\n");
            }
            
            if (!TextUtils.isEmpty(item.getUserAnswer())) {
                content.append("Your answer: ").append(item.getUserAnswer()).append("\n");
            }
            
            if (!TextUtils.isEmpty(item.getCorrectAnswer())) {
                // Extract the actual content of the correct answer (not just the letter)
                String correctAnswer = item.getCorrectAnswer();
                
                // First check if the answer is a single letter (like "A", "B", "C", "D")
                if (correctAnswer.length() == 1 && Character.isLetter(correctAnswer.charAt(0))) {
                    // Convert the letter to an index (A->0, B->1, etc.)
                    int index = correctAnswer.charAt(0) - 'A';
                    // Get the full option text for that index
                    if (index >= 0 && index < options.size()) {
                        correctAnswer = options.get(index); // Just the content without letter prefix
                    }
                } else if (correctAnswer.contains(":")) {
                    // If format is "A: Some text", extract just the text after the colon
                    int colonIndex = correctAnswer.indexOf(':');
                    if (colonIndex >= 0 && colonIndex + 1 < correctAnswer.length()) {
                        correctAnswer = correctAnswer.substring(colonIndex + 1).trim();
                    }
                } else {
                    // Handle case where correctAnswer might be in format "A. Some text"
                    if (correctAnswer.length() > 2 && correctAnswer.charAt(1) == '.') {
                        correctAnswer = correctAnswer.substring(2).trim();
                    }
                    // Any other format, just use as is
                }
                
                content.append("Correct answer: ").append(correctAnswer);
            }
            
            builder.setMessage(content.toString());
            builder.setPositiveButton("Close", null);
            builder.show();
        }

        @Override
        public int getItemCount() {
            return historyItems.size();
        }

        class HistoryViewHolder extends RecyclerView.ViewHolder {
            private TextView textViewTitle;
            private TextView textViewDescription;
            private TextView textViewOptionA;
            private TextView textViewOptionB;
            private TextView textViewOptionC;
            private TextView textViewOptionD;
            private TextView textViewAnswer;
            private ImageView imageViewStatus;
            private TextView textViewDate;
            private ImageButton buttonDelete;

            public HistoryViewHolder(View itemView) {
                super(itemView);
                textViewTitle = itemView.findViewById(R.id.textViewTitle);
                textViewDescription = itemView.findViewById(R.id.textViewDescription);
                textViewOptionA = itemView.findViewById(R.id.textViewOptionA);
                textViewOptionB = itemView.findViewById(R.id.textViewOptionB);
                textViewOptionC = itemView.findViewById(R.id.textViewOptionC);
                textViewOptionD = itemView.findViewById(R.id.textViewOptionD);
                textViewAnswer = itemView.findViewById(R.id.textViewAnswer);
                imageViewStatus = itemView.findViewById(R.id.imageViewStatus);
                textViewDate = itemView.findViewById(R.id.textViewDate);
                buttonDelete = itemView.findViewById(R.id.buttonDelete);
            }

            public void bind(HistoryItem item) {
                textViewTitle.setText(item.getTitle());
                textViewDescription.setText(item.getDescription());
                textViewDate.setText(item.getDate());
                
                // Get all options
                List<String> options = item.getOptionsList();
                
                // Set option texts - or hide if not available
                if (options.size() > 0) {
                    textViewOptionA.setText("A. " + options.get(0));
                    textViewOptionA.setVisibility(View.VISIBLE);
                } else {
                    textViewOptionA.setVisibility(View.GONE);
                }
                
                if (options.size() > 1) {
                    textViewOptionB.setText("B. " + options.get(1));
                    textViewOptionB.setVisibility(View.VISIBLE);
                } else {
                    textViewOptionB.setVisibility(View.GONE);
                }
                
                if (options.size() > 2) {
                    textViewOptionC.setText("C. " + options.get(2));
                    textViewOptionC.setVisibility(View.VISIBLE);
                } else {
                    textViewOptionC.setVisibility(View.GONE);
                }
                
                if (options.size() > 3) {
                    textViewOptionD.setText("D. " + options.get(3));
                    textViewOptionD.setVisibility(View.VISIBLE);
                } else {
                    textViewOptionD.setVisibility(View.GONE);
                }
                
                // Get the user's answer letter
                String userAnswerLetter = item.getUserAnswerLetter();
                
                // Highlight the selected option
                if (!TextUtils.isEmpty(userAnswerLetter)) {
                    highlightUserAnswer(userAnswerLetter);
                }
                
                // Set the user's answer text
                if (!TextUtils.isEmpty(item.getUserAnswer())) {
                    textViewAnswer.setText("Your answer: " + item.getUserAnswer());
                    textViewAnswer.setVisibility(View.VISIBLE);
                } else {
                    textViewAnswer.setVisibility(View.GONE);
                }
                
                // Set icon based on whether question was answered correctly
                if (item.isAnswered()) {
                    imageViewStatus.setImageResource(android.R.drawable.ic_menu_send);
                    imageViewStatus.setColorFilter(getResources().getColor(android.R.color.holo_green_dark));
                } else {
                    imageViewStatus.setImageResource(android.R.drawable.ic_menu_help);
                    imageViewStatus.setColorFilter(getResources().getColor(android.R.color.holo_orange_dark));
                }
                
                // Hide delete button for placeholder item
                if (item.getTitle().equals("No Questions Yet")) {
                    buttonDelete.setVisibility(View.INVISIBLE);
                } else {
                    buttonDelete.setVisibility(View.VISIBLE);
                }
            }
            
            // Highlight the option that matches the user's answer
            private void highlightUserAnswer(String userAnswerLetter) {
                // Reset all option backgrounds first
                textViewOptionA.setBackgroundColor(0x00000000); // Transparent
                textViewOptionB.setBackgroundColor(0x00000000);
                textViewOptionC.setBackgroundColor(0x00000000);
                textViewOptionD.setBackgroundColor(0x00000000);
                
                // Highlight the selected option
                switch (userAnswerLetter) {
                    case "A":
                        textViewOptionA.setBackgroundColor(0x552196F3); // Semi-transparent blue
                        textViewOptionA.setTextColor(0xFF2196F3); // Blue
                        break;
                    case "B":
                        textViewOptionB.setBackgroundColor(0x552196F3);
                        textViewOptionB.setTextColor(0xFF2196F3);
                        break;
                    case "C":
                        textViewOptionC.setBackgroundColor(0x552196F3);
                        textViewOptionC.setTextColor(0xFF2196F3);
                        break;
                    case "D":
                        textViewOptionD.setBackgroundColor(0x552196F3);
                        textViewOptionD.setTextColor(0xFF2196F3);
                        break;
                }
            }
        }
    }
} 