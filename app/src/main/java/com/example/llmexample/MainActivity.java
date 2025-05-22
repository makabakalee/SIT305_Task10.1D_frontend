package com.example.llmexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int MAX_QUESTIONS = 3; // Maximum number of questions to show
    private ListView listView;
    private LinearLayout loadingContainer;
    private ArrayList<String> quizItems;
    private ArrayAdapter<String> adapter;
    private boolean fromHomeActivity = false;
    private String username;
    private String taskTitle;
    private String taskDescription;
    private ArrayList<String> questions = new ArrayList<>();
    private ArrayList<ArrayList<String>> options = new ArrayList<>();
    private ArrayList<String> correctAnswers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if we're coming from HomeActivity
        if (getIntent() != null && getIntent().getBooleanExtra("fromHomeActivity", false)) {
            fromHomeActivity = true;
            username = getIntent().getStringExtra("username");
            taskTitle = getIntent().getStringExtra("taskTitle");
            taskDescription = getIntent().getStringExtra("taskDescription");
        }

        // Initialize UI components
        listView = findViewById(R.id.listView);
        loadingContainer = findViewById(R.id.loadingContainer);
        quizItems = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, quizItems);
        listView.setAdapter(adapter);

        // Show loading state
        loadingContainer.setVisibility(View.VISIBLE);

        // URL for the Flask server
        String url = "http://10.0.2.2:5000/getQuiz?topic=Movies";
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create the JSON Object Request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Hide loading container when response is received
                        loadingContainer.setVisibility(View.GONE);

                        try {
                            Log.i(TAG, "Response received: " + response.toString());
                            JSONArray quizArray = response.getJSONArray("quiz");
                            processQuizData(quizArray);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing JSON: " + e.getMessage(), e);
                            Toast.makeText(MainActivity.this, "Error parsing JSON: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            
                            // Fallback to hardcoded data if API fails
                            useHardcodedData();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Hide loading container on error
                        loadingContainer.setVisibility(View.GONE);

                        String errorMsg = "Unknown error";
                        if (error.networkResponse != null) {
                            errorMsg = "HTTP " + error.networkResponse.statusCode + ": " + new String(error.networkResponse.data);
                        } else if (error.getMessage() != null) {
                            errorMsg = error.getMessage();
                        }
                        Log.e(TAG, "Error fetching quiz: " + errorMsg, error);
                        Toast.makeText(MainActivity.this, "Error fetching quiz: " + errorMsg, Toast.LENGTH_LONG).show();
                        
                        // Fallback to hardcoded data if API fails
                        useHardcodedData();
                    }
                });

        // Set a custom retry policy with a longer timeout
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                60000, // 60-second timeout
                2, // 2 retries
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT // Default backoff multiplier
        ));

        // Add the request to the queue
        Log.i(TAG, "Sending request to: " + url);
        queue.add(jsonObjectRequest);
    }
    
    private void useHardcodedData() {
        try {
            // Parse predefined JSON data as fallback
            String jsonData = "[\n" +
                    "{\n" +
                    "  \"options\": [\n" +
                    "    \"The film's budget is the most important factor.\",\n" +
                    "    \"Genre shapes the audience's expectations and emotional response.\",\n" +
                    "    \"The director's style is the most influential element.\",\n" +
                    "    \"Genre simply provides a variety of films to choose from.\"\n" +
                    "  ],\n" +
                    "  \"question\": \"What is the primary way in which genre influences a film's overall reception?\"\n" +
                    "},\n" +
                    "{\n" +
                    "  \"correct_answer\": \"B\",\n" +
                    "  \"options\": [\n" +
                    "    \"It usually increases audience engagement by creating a sense of excitement.\",\n" +
                    "    \"It often leads to a more cautious and analytical viewing experience.\",\n" +
                    "    \"It frequently decreases audience engagement due to its intensity.\",\n" +
                    "    \"It's a universally accepted and appreciated genre for all audiences.\"\n" +
                    "  ],\n" +
                    "  \"question\": \"How does the inclusion of a horror genre typically affect a film's audience engagement?\"\n" +
                    "},\n" +
                    "{\n" +
                    "  \"correct_answer\": \"B\",\n" +
                    "  \"options\": [\n" +
                    "    \"It's a straightforward and predictable genre with little to no impact on the audience.\",\n" +
                    "    \"It can create a complex and nuanced emotional experience, challenging viewers' perspectives.\",\n" +
                    "    \"It's primarily a commercial genre, focused on maximizing box office revenue.\",\n" +
                    "    \"It's a generic genre with no discernible effect on the viewer.\"\n" +
                    "  ],\n" +
                    "  \"question\": \"Consider a film that blends elements of comedy and drama.  Which of the following best describes the genre's potential impact on the viewing experience?\"\n" +
                    "}\n]";
            
            JSONArray quizArray = new JSONArray(jsonData);
            processQuizData(quizArray);
            
        } catch (Exception e) {
            Log.e(TAG, "Error parsing hardcoded JSON: " + e.getMessage(), e);
            Toast.makeText(MainActivity.this, "Error parsing hardcoded JSON: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void processQuizData(JSONArray quizArray) throws Exception {
        // Hide loading state
        loadingContainer.setVisibility(View.GONE);
        
        // Clear previous quiz items
        quizItems.clear();
        questions.clear();
        options.clear();
        correctAnswers.clear();
        
        // Process each quiz question, limiting to MAX_QUESTIONS
        int numberOfQuestions = Math.min(quizArray.length(), MAX_QUESTIONS);
        for (int i = 0; i < numberOfQuestions; i++) {
            JSONObject quizQuestion = quizArray.getJSONObject(i);
            
            String question = quizQuestion.getString("question");
            JSONArray optionsArray = quizQuestion.getJSONArray("options");
            
            // First question may not have correct_answer field, default to "B"
            String correctAnswer = quizQuestion.has("correct_answer") ? 
                    quizQuestion.getString("correct_answer") : "B";
            
            // Store question data for QuizActivity
            questions.add(question);
            ArrayList<String> questionOptions = new ArrayList<>();
            for (int j = 0; j < optionsArray.length(); j++) {
                questionOptions.add(optionsArray.getString(j));
            }
            options.add(questionOptions);
            correctAnswers.add(correctAnswer);
            
            // Build the display string for this question
            StringBuilder questionText = new StringBuilder();
            questionText.append("Q").append(i + 1).append(": ").append(question).append("\n");
            for (int j = 0; j < optionsArray.length(); j++) {
                char optionLetter = (char) ('A' + j);
                questionText.append(optionLetter).append(": ")
                        .append(optionsArray.getString(j)).append("\n");
            }
            questionText.append("Correct Answer: ").append(correctAnswer);
            
            // Add to the list
            quizItems.add(questionText.toString());
        }
        
        // Update the ListView
        adapter.notifyDataSetChanged();
        
        if (fromHomeActivity) {
            // If we came from HomeActivity, forward to QuizActivity
            launchQuizActivity();
        } else {
            Toast.makeText(MainActivity.this, "Quiz loaded successfully!", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void launchQuizActivity() {
        // Navigate to Quiz Activity with the loaded quiz data
        Intent intent = new Intent(MainActivity.this, QuizActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("taskTitle", taskTitle);
        intent.putExtra("taskDescription", taskDescription);
        intent.putStringArrayListExtra("questions", questions);
        intent.putExtra("options", options);
        intent.putStringArrayListExtra("correctAnswers", correctAnswers);
        intent.putExtra("fromAPI", true);
        startActivity(intent);
        
        // Add transition animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        
        // Finish this activity to remove it from the back stack
        finish();
    }
}