package com.example.productexpirationtrackerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetail";

    // UI Components
    private ImageView backButton;       // Changed: ImageView (arrow) like CategoryDetailActivity
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
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Initialize ViewModel
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        // Initialize views
        initializeViews();

        // Get intent data
        getIntentData();

        // Setup click listeners
        setupClickListeners();

        // Display product details (basic from intent, then full load from DB)
        displayProductDetails();

        // Load full product data from database (photo, category, quantity, notes)
        loadProductFromDatabase();

        // Apply theme colours
        applyTheme();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);    // ImageView
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
        // Set basic info from intent immediately — DB load fills the rest
        if (productName != null) {
            productNameText.setText(productName);
            titleTextView.setText(productName);
        }
        if (expiryDate != null) {
            expiryDateText.setText(expiryDate);
            calculateAndDisplayDaysLeft(expiryDate);
        }
    }

    // ── Loads full product (photo, category, quantity, notes) from Room ──────
    private void loadProductFromDatabase() {
        productViewModel.getAllProducts().observe(this, products -> {
            if (products == null) return;
            for (Product p : products) {
                if (p.getId() == productId) {
                    bindFullProduct(p);
                    return;
                }
            }
        });
    }

    private void bindFullProduct(Product product) {
        // Refresh name & title in case they differ
        if (product.getName() != null) {
            productNameText.setText(product.getName());
            titleTextView.setText(product.getName());
            productName = product.getName();
        }

        // Expiry
        if (product.getExpiryDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String formatted = sdf.format(product.getExpiryDate());
            expiryDateText.setText(formatted);
            expiryDate = formatted;
            calculateAndDisplayDaysLeft(formatted);
        }

        // Category
        categoryText.setText(product.getCategory() != null ? product.getCategory() : "Not specified");

        // Quantity
        quantityText.setText(product.getQuantity() != null ? product.getQuantity() : "Not specified");

        // Notes — guard in case getNotes() doesn't exist yet
        try {
            String notes = product.getNotes();
            notesText.setText(notes != null && !notes.isEmpty() ? notes : "No notes");
        } catch (Exception ignored) {
            notesText.setText("No notes");
        }

        // Photo
        if (product.hasPhoto()) {
            byte[] bytes = product.getPhoto();
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bmp != null) {
                productPhoto.setImageBitmap(bmp);
                productPhoto.setBackground(null); // clear grey placeholder
            }
        } else {
            // Category-based placeholder
            String cat = product.getCategory();
            int placeholder = R.drawable.ic_default_product;
            if (cat != null) {
                switch (cat) {
                    case "Dairy": case "Vegetables": case "Fruits": case "Meats":
                        placeholder = R.drawable.ic_food_placeholder; break;
                    case "Beverages":
                        placeholder = R.drawable.ic_drinks_placeholder; break;
                    case "Medicine":
                        placeholder = R.drawable.ic_medicine_placeholder; break;
                    case "Other":
                        placeholder = R.drawable.ic_other_placeholder; break;
                }
            }
            productPhoto.setImageResource(placeholder);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyTheme();
    }

    // ── Theme: background, labels white, field text dark/light, buttons blue ─
    private void applyTheme() {
        String theme = preferences.getString("color_theme", "white");
        boolean isDark = "black".equals(theme);
        float dp = getResources().getDisplayMetrics().density;

        int mainBg      = isDark ? Color.parseColor("#121212") : Color.parseColor("#F5F7FA");
        int labelColor  = isDark ? Color.WHITE                 : Color.parseColor("#1A1E2C");
        int fieldBg     = isDark ? Color.parseColor("#2A2A2A") : Color.parseColor("#F0F0F0");
        int fieldText   = isDark ? Color.WHITE : Color.parseColor("#1A1E2C"); // white in dark mode
        int arrowTint   = isDark ? Color.WHITE                 : Color.parseColor("#1A1E2C");

        // Blue: brighter in light mode, slightly muted in dark
        int blueBtnColor = isDark
                ? Color.parseColor("#3D5AFE")   // indigo-blue (dark)
                : Color.parseColor("#2979FF");  // bright blue (light)

        // Root background
        LinearLayout mainLayout = findViewById(R.id.mainLayout);
        if (mainLayout != null) mainLayout.setBackgroundColor(mainBg);

        // Back arrow tint
        if (backButton != null)
            backButton.setImageTintList(
                    android.content.res.ColorStateList.valueOf(arrowTint));

        // Title
        if (titleTextView != null) titleTextView.setTextColor(labelColor);

        // Labels — white in dark, dark in light
        int[] labelIds = {
                R.id.photoLabel, R.id.productNameLabel, R.id.expiryDateLabel,
                R.id.daysLeftLabel, R.id.categoryLabel, R.id.quantityLabel, R.id.notesLabel
        };
        for (int id : labelIds) {
            TextView lbl = findViewById(id);
            if (lbl != null) lbl.setTextColor(labelColor);
        }

        // Value fields — includes daysLeftText so its bg matches other fields
        int[] fieldIds = {
                R.id.productNameText, R.id.expiryDateText, R.id.daysLeftText,
                R.id.categoryText, R.id.quantityText, R.id.notesText
        };
        for (int id : fieldIds) {
            TextView fld = findViewById(id);
            if (fld != null) {
                // daysLeftText color is handled by calculateAndDisplayDaysLeft — skip it here
                if (id != R.id.daysLeftText) fld.setTextColor(fieldText);
                android.graphics.drawable.GradientDrawable bg =
                        new android.graphics.drawable.GradientDrawable();
                bg.setColor(fieldBg);
                bg.setCornerRadius(10 * dp);
                fld.setBackground(bg);
            }
        }

        // Consume button — blue, no icon
        if (consumeButton != null) {
            consumeButton.setBackgroundColor(blueBtnColor);
            consumeButton.setTextColor(Color.WHITE);
        }

        // Edit button — blue
        if (editButton != null) {
            editButton.setBackgroundColor(blueBtnColor);
            editButton.setTextColor(Color.WHITE);
        }

        // Delete button — always red
        if (deleteButton != null) {
            deleteButton.setBackgroundColor(Color.parseColor("#D50000"));
            deleteButton.setTextColor(Color.WHITE);
        }
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
                    daysLeftText.setTextColor(Color.parseColor("#FF1744")); // bright red
                } else if (daysLeft <= 7) {
                    daysLeftText.setText("⚠️ " + daysLeft + " days left (Soon)");
                    daysLeftText.setTextColor(Color.parseColor("#FF9100")); // bright orange
                } else {
                    daysLeftText.setText("✅ " + daysLeft + " days left (Safe)");
                    daysLeftText.setTextColor(Color.parseColor("#00E676")); // bright green
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