package com.example.llmexample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class ShareProfileActivity extends AppCompatActivity {

    private TextView textViewUsername;
    private TextView textViewTotalQuestions;
    private TextView textViewCorrectAnswers;
    private TextView textViewIncorrectAnswers;
    private ImageView imageViewQRCode;
    private Button buttonBack;
    private Button buttonShare;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_profile);

        // Initialize UI components
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewTotalQuestions = findViewById(R.id.textViewTotalQuestions);
        textViewCorrectAnswers = findViewById(R.id.textViewCorrectAnswers);
        textViewIncorrectAnswers = findViewById(R.id.textViewIncorrectAnswers);
        imageViewQRCode = findViewById(R.id.imageViewQRCode);
        buttonBack = findViewById(R.id.buttonBack);
        buttonShare = findViewById(R.id.buttonShare);

        // Get username from intent
        if (getIntent().hasExtra("username")) {
            username = getIntent().getStringExtra("username");
            textViewUsername.setText(username);
        } else {
            username = "Student";
            textViewUsername.setText(username);
        }

        // Set mock data for profile stats
        textViewTotalQuestions.setText("10");
        textViewCorrectAnswers.setText("10");
        textViewIncorrectAnswers.setText("10");

        // Generate QR Code for profile sharing
        generateQRCode();

        // Set up click listener for back button
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Set up click listener for share button
        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareProfile();
            }
        });

        // Add animation effects
        addAnimations();
    }

    private void generateQRCode() {
        // Create string with profile data to encode in QR code
        String profileData = "Username: " + username + "\n" +
                             "Total Questions: 10\n" +
                             "Correct Answers: 10\n" +
                             "Incorrect Answers: 10\n" +
                             "App Link: https://example.com/llmapp";

        // Generate QR code
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(profileData, BarcodeFormat.QR_CODE, 300, 300);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            imageViewQRCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "QR Code generation error", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareProfile() {
        // Create intent to share text
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Learning Profile");
        
        String shareText = "Check out my learning progress!\n\n" +
                          "Username: " + username + "\n" +
                          "Total Questions: 10\n" +
                          "Correct Answers: 10\n" +
                          "Incorrect Answers: 10\n\n" +
                          "Download the app and learn with me: https://example.com/llmapp";
        
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share Your Profile"));
    }

    private void addAnimations() {
        // Animate profile card
        CardView profileCard = findViewById(R.id.cardViewProfile);
        if (profileCard != null) {
            profileCard.setAlpha(0f);
            profileCard.setTranslationY(-50f);
            profileCard.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setStartDelay(200);
        }

        // Animate QR code card
        CardView qrCodeCard = findViewById(R.id.cardViewQRCode);
        if (qrCodeCard != null) {
            qrCodeCard.setAlpha(0f);
            qrCodeCard.setTranslationY(50f);
            qrCodeCard.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setStartDelay(400);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
} 