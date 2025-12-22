package com.example.productexpirationtrackerapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ProductDetailActivity extends AppCompatActivity {

    private TextView productNameText, expiryDateText, daysLeftText, statusText;
    private Button backButton, editButton, deleteButton;

    private String productName;
    private String expiryDate;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Get data passed from ProductListActivity
        Intent intent = getIntent();
        productName = intent.getStringExtra("product_name");
        expiryDate = intent.getStringExtra("expiry_date");
        position = intent.getIntExtra("position", -1);

        // Initialize views
        initializeViews();

        // Setup click listeners
        setupClickListeners();

        // Display product data
        displayProductDetails();
    }

    private void initializeViews() {
        productNameText = findViewById(R.id.productName);
        expiryDateText = findViewById(R.id.expiryDate);
        daysLeftText = findViewById(R.id.daysLeft);
        statusText = findViewById(R.id.statusText);

        backButton = findViewById(R.id.backButton);
        editButton = findViewById(R.id.editButton);
        deleteButton = findViewById(R.id.deleteButton);
    }

    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        // Edit button
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editProduct();
            }
        });

        // Delete button
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete();
            }
        });
    }

    private void displayProductDetails() {
        // Set product name
        productNameText.setText(productName);

        // Parse and format expiry date
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date expiry = inputFormat.parse(expiryDate);

            // Display formatted date
            String formattedDate = displayFormat.format(expiry);
            expiryDateText.setText(formattedDate);

            // Calculate days left
            Date now = new Date();
            long timeDiff = expiry.getTime() - now.getTime();
            long days = TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);

            // Update days left and status
            if (days < 0) {
                daysLeftText.setText("EXPIRED");
                daysLeftText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                statusText.setText("Expired");
                statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else if (days <= 3) {
                daysLeftText.setText(days + " days left");
                daysLeftText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                statusText.setText("Expiring Soon!");
                statusText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            } else if (days <= 7) {
                daysLeftText.setText(days + " days left");
                daysLeftText.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
                statusText.setText("Warning");
                statusText.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
            } else {
                daysLeftText.setText(days + " days left");
                daysLeftText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                statusText.setText("Fresh");
                statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }

        } catch (Exception e) {
            expiryDateText.setText("Unknown date");
            daysLeftText.setText("Unknown");
            statusText.setText("Unknown");
        }
    }

    private void editProduct() {
        Toast.makeText(this, "Edit feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void confirmDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Product");
        builder.setMessage("Delete " + productName + "?");

        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteProduct();
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteProduct() {
        // Send result back to ProductListActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("deleted_position", position);
        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "Deleted: " + productName, Toast.LENGTH_SHORT).show();
        finish();
    }
}