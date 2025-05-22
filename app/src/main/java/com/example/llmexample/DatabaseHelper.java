package com.example.llmexample;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database info
    private static final String DATABASE_NAME = "llm_app.db";
    private static final int DATABASE_VERSION = 2; // Increased version for schema change

    // Tables
    private static final String TABLE_HISTORY = "history";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_ANSWERED = "answered";
    private static final String KEY_DATE = "date";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_OPTIONS = "options"; // New field for options (comma separated)
    private static final String KEY_USER_ANSWER = "user_answer"; // New field for user's answer
    private static final String KEY_CORRECT_ANSWER = "correct_answer"; // New field for correct answer

    // Table create statements
    private static final String CREATE_TABLE_HISTORY = "CREATE TABLE " + TABLE_HISTORY +
            "(" +
            KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            KEY_TITLE + " TEXT, " +
            KEY_DESCRIPTION + " TEXT, " +
            KEY_ANSWERED + " INTEGER, " + // 0 for false, 1 for true
            KEY_DATE + " TEXT, " +
            KEY_USERNAME + " TEXT, " +
            KEY_OPTIONS + " TEXT, " +
            KEY_USER_ANSWER + " TEXT, " +
            KEY_CORRECT_ANSWER + " TEXT" +
            ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_HISTORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add new columns for existing table
            db.execSQL("ALTER TABLE " + TABLE_HISTORY + " ADD COLUMN " + KEY_OPTIONS + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_HISTORY + " ADD COLUMN " + KEY_USER_ANSWER + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_HISTORY + " ADD COLUMN " + KEY_CORRECT_ANSWER + " TEXT");
        } else {
            // Drop older tables if existed
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
            // Create tables again
            onCreate(db);
        }
    }

    // Add a new history item with extended information
    public long addHistoryItem(String title, String description, boolean answered, 
                              String username, String options, String userAnswer, 
                              String correctAnswer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        // Get current date in the format yyyy/MM/dd
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        
        values.put(KEY_TITLE, title);
        values.put(KEY_DESCRIPTION, description);
        values.put(KEY_ANSWERED, answered ? 1 : 0);
        values.put(KEY_DATE, currentDate);
        values.put(KEY_USERNAME, username);
        values.put(KEY_OPTIONS, options);
        values.put(KEY_USER_ANSWER, userAnswer);
        values.put(KEY_CORRECT_ANSWER, correctAnswer);
        
        // Insert row
        long id = db.insert(TABLE_HISTORY, null, values);
        db.close();
        return id;
    }

    // Keep the old method for backward compatibility
    public long addHistoryItem(String title, String description, boolean answered, String username) {
        return addHistoryItem(title, description, answered, username, "", "", "");
    }

    // Get all history items for a specific user
    public List<HistoryActivity.HistoryItem> getAllHistoryItems(String username) {
        List<HistoryActivity.HistoryItem> historyItems = new ArrayList<>();
        
        // Select all query
        String selectQuery = "SELECT * FROM " + TABLE_HISTORY + 
                " WHERE " + KEY_USERNAME + " = ?" +
                " ORDER BY " + KEY_DATE + " DESC";
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{username});
        
        // Loop through all rows and add to list
        if (cursor.moveToFirst()) {
            do {
                // Using safer way to get column indices
                int titleIndex = cursor.getColumnIndexOrThrow(KEY_TITLE);
                int descriptionIndex = cursor.getColumnIndexOrThrow(KEY_DESCRIPTION);
                int answeredIndex = cursor.getColumnIndexOrThrow(KEY_ANSWERED);
                int dateIndex = cursor.getColumnIndexOrThrow(KEY_DATE);
                
                // Get indices for new fields - handle case where columns might not exist in old DB
                int optionsIndex = -1;
                int userAnswerIndex = -1;
                int correctAnswerIndex = -1;
                
                try {
                    optionsIndex = cursor.getColumnIndexOrThrow(KEY_OPTIONS);
                    userAnswerIndex = cursor.getColumnIndexOrThrow(KEY_USER_ANSWER);
                    correctAnswerIndex = cursor.getColumnIndexOrThrow(KEY_CORRECT_ANSWER);
                } catch (IllegalArgumentException e) {
                    // Column doesn't exist in this version of the database
                }
                
                String title = cursor.getString(titleIndex);
                String description = cursor.getString(descriptionIndex);
                boolean answered = cursor.getInt(answeredIndex) == 1;
                String date = cursor.getString(dateIndex);
                
                // Get new field values if available
                String options = optionsIndex >= 0 ? cursor.getString(optionsIndex) : "";
                String userAnswer = userAnswerIndex >= 0 ? cursor.getString(userAnswerIndex) : "";
                String correctAnswer = correctAnswerIndex >= 0 ? cursor.getString(correctAnswerIndex) : "";
                
                HistoryActivity.HistoryItem item = new HistoryActivity.HistoryItem(
                        title, description, answered, date, options, userAnswer, correctAnswer);
                historyItems.add(item);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return historyItems;
    }

    // Delete history item by ID
    public void deleteHistoryItem(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HISTORY, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
    
    // Delete history item by title and date for a specific user
    public void deleteHistoryItem(String title, String date, String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HISTORY, 
                KEY_TITLE + " = ? AND " + KEY_DATE + " = ? AND " + KEY_USERNAME + " = ?", 
                new String[]{title, date, username});
        db.close();
    }

    // Clear all history for a user
    public void clearHistory(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HISTORY, KEY_USERNAME + " = ?", new String[]{username});
        db.close();
    }
} 