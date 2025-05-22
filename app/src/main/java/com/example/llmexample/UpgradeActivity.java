package com.example.llmexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class UpgradeActivity extends AppCompatActivity {

    private static final String TAG = "UpgradeActivity";
    
    // PayPal configuration
    private static final String PAYPAL_CLIENT_ID = "YOUR_PAYPAL_CLIENT_ID";
    private static final int PAYPAL_REQUEST_CODE = 7777;
    
    // PayPal sandbox for testing, use PayPalConfiguration.ENVIRONMENT_PRODUCTION for production
    private static final String PAYPAL_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;
    
    // PayPal configuration object
    private static PayPalConfiguration paypalConfig = new PayPalConfiguration()
            .environment(PAYPAL_ENVIRONMENT)
            .clientId(PAYPAL_CLIENT_ID)
            .merchantName("Learning App Example")
            .merchantPrivacyPolicyUri(android.net.Uri.parse("https://example.com/privacy"))
            .merchantUserAgreementUri(android.net.Uri.parse("https://example.com/terms"));
    
    private TextView textViewTitle;
    private Button buttonBack;
    private Button buttonPurchaseStarter;
    private Button buttonPurchaseIntermediate;
    private Button buttonPurchaseAdvanced;
    private TextView textViewPaymentStatus;
    private String username;
    private String selectedPlanName;
    private double selectedPlanPrice;
    private boolean isPayPalAvailable = true; // Assume PayPal is available by default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade);

        // Initialize UI components
        textViewTitle = findViewById(R.id.textViewTitle);
        buttonBack = findViewById(R.id.buttonBack);
        buttonPurchaseStarter = findViewById(R.id.buttonPurchaseStarter);
        buttonPurchaseIntermediate = findViewById(R.id.buttonPurchaseIntermediate);
        buttonPurchaseAdvanced = findViewById(R.id.buttonPurchaseAdvanced);
        textViewPaymentStatus = findViewById(R.id.textViewGooglePayStatus);

        // Get username from intent
        if (getIntent().hasExtra("username")) {
            username = getIntent().getStringExtra("username");
        } else {
            username = "Student";
        }

        // Start PayPal service
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfig);
        startService(intent);

        // Set up click listener for back button
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Set up click listeners for purchase buttons
        buttonPurchaseStarter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPlanName = "Starter";
                selectedPlanPrice = 9.99;
                if (isPayPalAvailable) {
                    processPayPalPayment(selectedPlanName, selectedPlanPrice);
                } else {
                    simulatePurchase(selectedPlanName, selectedPlanPrice);
                }
            }
        });

        buttonPurchaseIntermediate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPlanName = "Intermediate";
                selectedPlanPrice = 19.99;
                if (isPayPalAvailable) {
                    processPayPalPayment(selectedPlanName, selectedPlanPrice);
                } else {
                    simulatePurchase(selectedPlanName, selectedPlanPrice);
                }
            }
        });

        buttonPurchaseAdvanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPlanName = "Advanced";
                selectedPlanPrice = 29.99;
                if (isPayPalAvailable) {
                    processPayPalPayment(selectedPlanName, selectedPlanPrice);
                } else {
                    simulatePurchase(selectedPlanName, selectedPlanPrice);
                }
            }
        });

        // Add animations
        addAnimations();
    }
    
    /**
     * Process payment using PayPal
     */
    private void processPayPalPayment(String planName, double price) {
        // Creating a PayPal payment
        PayPalPayment payment = new PayPalPayment(
                new BigDecimal(String.valueOf(price)), "USD", 
                "Purchase " + planName + " Plan", 
                PayPalPayment.PAYMENT_INTENT_SALE);
        
        // Creating PayPal payment intent
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfig);
        
        // Starting the intent activity for result
        // the request code will be used on the method onActivityResult
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
        
        Toast.makeText(this, 
                "Processing " + planName + " ($" + price + ") purchase via PayPal...", 
                Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Simulate purchase for when real payment is not available
     */
    private void simulatePurchase(String planName, double price) {
        // Simulate purchase process for devices without PayPal
        Toast.makeText(this, 
                "Processing " + planName + " ($" + price + ") purchase request...", 
                Toast.LENGTH_SHORT).show();
                
        // Simulate payment processing
        final CardView loadingCard = findViewById(R.id.cardViewLoading);
        if (loadingCard != null) {
            loadingCard.setVisibility(View.VISIBLE);
            loadingCard.setAlpha(0f);
            loadingCard.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
        }
        
        // Simulate delay and show success message
        loadingCard.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (loadingCard != null) {
                    loadingCard.animate()
                            .alpha(0f)
                            .setDuration(300)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    loadingCard.setVisibility(View.GONE);
                                    Toast.makeText(UpgradeActivity.this, 
                                            planName + " purchase successful!", 
                                            Toast.LENGTH_LONG).show();
                                    
                                    // Return to home page
                                    Intent intent = new Intent(UpgradeActivity.this, HomeActivity.class);
                                    intent.putExtra("username", username);
                                    intent.putExtra("planPurchased", planName);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                }
                            })
                            .start();
                }
            }
        }, 2000);
    }

    /**
     * Handle payment result
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Check which request we're responding to
        if (requestCode == PAYPAL_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null) {
                    try {
                        // Getting the payment details
                        String paymentDetails = confirmation.toJSONObject().toString(4);
                        Log.i("paymentDetails", paymentDetails);
                        
                        // Starting a new activity for the payment details and status
                        handlePaymentSuccess();
                        
                    } catch (JSONException e) {
                        Log.e("paymentExample", "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("paymentExample", "The user canceled.");
                Toast.makeText(this, "Payment canceled", Toast.LENGTH_SHORT).show();
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i("paymentExample", "An invalid Payment or PayPalConfiguration was submitted.");
                Toast.makeText(this, "Invalid payment configuration", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * Handle payment success
     */
    private void handlePaymentSuccess() {
        Toast.makeText(this, selectedPlanName + " purchase successful!", Toast.LENGTH_LONG).show();
        
        // Return to home page with purchased plan info
        Intent intent = new Intent(UpgradeActivity.this, HomeActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("planPurchased", selectedPlanName);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Add animations to the plan cards
     */
    private void addAnimations() {
        // Animate plan cards with sequential timing
        CardView starterCard = findViewById(R.id.cardViewStarter);
        CardView intermediateCard = findViewById(R.id.cardViewIntermediate);
        CardView advancedCard = findViewById(R.id.cardViewAdvanced);
        
        if (starterCard != null) {
            starterCard.setAlpha(0f);
            starterCard.setTranslationY(50f);
            starterCard.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setStartDelay(200);
        }
        
        if (intermediateCard != null) {
            intermediateCard.setAlpha(0f);
            intermediateCard.setTranslationY(50f);
            intermediateCard.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setStartDelay(400);
        }
        
        if (advancedCard != null) {
            advancedCard.setAlpha(0f);
            advancedCard.setTranslationY(50f);
            advancedCard.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setStartDelay(600);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    
    @Override
    public void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }
} 