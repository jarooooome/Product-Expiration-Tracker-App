package com.example.productexpirationtrackerapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetail";

    // UI Components
    private Button backButton;
    private TextView titleTextView;
    private ImageView productPhoto;
    private Button consumeButton;
    private TextView productNameText;
    private TextView expiryDateText;
    private TextView daysLeftText;
    private TextView categoryText;
    private TextView quantityText;
    private TextView notesText;
    private Button editButton;
    private Button deleteButton;

    // Data
    private int productId;
    private String productName;
    private String expiryDate;
    private ProductViewModel productViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Initialize ViewModel
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // Initialize views
        initializeViews();

        // Get intent data
        getIntentData();

        // Setup click listeners
        setupClickListeners();

        // Display product details
        displayProductDetails();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        titleTextView = findViewById(R.id.titleTextView);
        productPhoto = findViewById(R.id.productPhoto);
        consumeButton = findViewById(R.id.consumeButton);
        productNameText = findViewById(R.id.productNameText);
        expiryDateText = findViewById(R.id.expiryDateText);
        daysLeftText = findViewById(R.id.daysLeftText);
        categoryText = findViewById(R.id.categoryText);
        quantityText = findViewById(R.id.quantityText);
        notesText = findViewById(R.id.notesText);
        editButton = findViewById(R.id.editButton);
        deleteButton = findViewById(R.id.deleteButton);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        productId = intent.getIntExtra("product_id", -1);
        productName = intent.getStringExtra("product_name");
        expiryDate = intent.getStringExtra("expiry_date");

        Log.d(TAG, "Product ID: " + productId);
        Log.d(TAG, "Product Name: " + productName);
        Log.d(TAG, "Expiry Date: " + expiryDate);
    }

    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());

        // Edit button - NEW FUNCTIONALITY
        editButton.setOnClickListener(v -> {
            if (productId != -1) {
                Intent intent = new Intent(ProductDetailActivity.this, EditProductActivity.class);
                intent.putExtra("product_id", productId);
                intent.putExtra("product_name", productName);
                intent.putExtra("expiry_date", expiryDate);
                intent.putExtra("category", categoryText.getText().toString());
                intent.putExtra("quantity", quantityText.getText().toString());
                intent.putExtra("notes", notesText.getText().toString());
                startActivityForResult(intent, 101);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else {
                Toast.makeText(this, "Error: Product ID not found", Toast.LENGTH_SHORT).show();
            }
        });

        // Delete button
        deleteButton.setOnClickListener(v -> {
            // Show confirmation dialog
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete Product")
                    .setMessage("Are you sure you want to delete this product?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        if (productId != -1) {
                            productViewModel.deleteById(productId);
                            Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show();

                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("deleted_product_id", productId);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // Consume button (optional - you can add functionality later)
        consumeButton.setOnClickListener(v -> {
            Toast.makeText(this, "Consume feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void displayProductDetails() {
        // Set product name
        if (productName != null) {
            productNameText.setText(productName);
            titleTextView.setText(productName);
        }

        // Set expiry date
        if (expiryDate != null) {
            expiryDateText.setText(expiryDate);
            calculateAndDisplayDaysLeft(expiryDate);
        }

        // For now, we're using dummy data since we only passed basic info
        // In a real app, you would fetch full product details from ViewModel
        categoryText.setText("Category"); // This would come from database
        quantityText.setText("1"); // This would come from database
        notesText.setText("No notes"); // This would come from database
    }

    private void calculateAndDisplayDaysLeft(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date expiryDate = sdf.parse(dateString);
            Date today = new Date();

            if (expiryDate != null) {
                long diff = expiryDate.getTime() - today.getTime();
                long daysLeft = diff / (24 * 60 * 60 * 1000);

                if (daysLeft < 0) {
                    daysLeftText.setText("⚠️ Expired");
                    daysLeftText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                } else if (daysLeft <= 7) {
                    daysLeftText.setText("⚠️ " + daysLeft + " days left (Soon)");
                    daysLeftText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                } else {
                    daysLeftText.setText("✅ " + daysLeft + " days left (Safe)");
                    daysLeftText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                }
            }
        } catch (Exception e) {
            daysLeftText.setText("Unknown");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK) {
            // Refresh data after edit
            if (data != null) {
                String updatedName = data.getStringExtra("product_name");
                String updatedExpiry = data.getStringExtra("expiry_date");
                String updatedCategory = data.getStringExtra("category");
                String updatedQuantity = data.getStringExtra("quantity");
                String updatedNotes = data.getStringExtra("notes");

                // Update UI with new data
                if (updatedName != null) {
                    productNameText.setText(updatedName);
                    titleTextView.setText(updatedName);
                    productName = updatedName;
                }
                if (updatedExpiry != null) {
                    expiryDateText.setText(updatedExpiry);
                    expiryDate = updatedExpiry;
                    calculateAndDisplayDaysLeft(updatedExpiry);
                }
                if (updatedCategory != null) categoryText.setText(updatedCategory);
                if (updatedQuantity != null) quantityText.setText(updatedQuantity);
                if (updatedNotes != null) notesText.setText(updatedNotes);

                Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show();
            }
        }
    }
}