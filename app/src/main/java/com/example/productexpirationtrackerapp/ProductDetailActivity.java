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
    private ImageView backButton;        // now ImageView, not Button
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

    // Label TextViews (for theme coloring)
    private TextView photoLabel;
    private TextView productNameLabel;
    private TextView expiryDateLabel;
    private TextView daysLeftLabel;
    private TextView categoryLabel;
    private TextView quantityLabel;
    private TextView notesLabel;

    // Data
    private int productId;
    private String productName;
    private String expiryDate;
    private Product currentProduct;
    private ProductViewModel productViewModel;
    private ConsumedProductViewModel consumedProductViewModel;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        consumedProductViewModel = new ViewModelProvider(this).get(ConsumedProductViewModel.class);

        initializeViews();
        getIntentData();
        setupClickListeners();

        // Load full product from DB — populates photo, category, quantity, notes
        loadProductFromDatabase();

        // Apply theme colours
        applyTheme();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyTheme();
    }

    // ── Init ─────────────────────────────────────────────────────────────────

    private void initializeViews() {
        backButton      = findViewById(R.id.backButton);
        titleTextView   = findViewById(R.id.titleTextView);
        productPhoto    = findViewById(R.id.productPhoto);
        consumeButton   = findViewById(R.id.consumeButton);
        productNameText = findViewById(R.id.productNameText);
        expiryDateText  = findViewById(R.id.expiryDateText);
        daysLeftText    = findViewById(R.id.daysLeftText);
        categoryText    = findViewById(R.id.categoryText);
        quantityText    = findViewById(R.id.quantityText);
        notesText       = findViewById(R.id.notesText);
        editButton      = findViewById(R.id.editButton);
        deleteButton    = findViewById(R.id.deleteButton);

        // Labels
        photoLabel      = findViewById(R.id.photoLabel);
        productNameLabel= findViewById(R.id.productNameLabel);
        expiryDateLabel = findViewById(R.id.expiryDateLabel);
        daysLeftLabel   = findViewById(R.id.daysLeftLabel);
        categoryLabel   = findViewById(R.id.categoryLabel);
        quantityLabel   = findViewById(R.id.quantityLabel);
        notesLabel      = findViewById(R.id.notesLabel);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        productId   = intent.getIntExtra("product_id", -1);
        productName = intent.getStringExtra("product_name");
        expiryDate  = intent.getStringExtra("expiry_date");

        Log.d(TAG, "Product ID: " + productId + ", Name: " + productName);

        // Show basic info immediately (full data fills in once DB loads)
        if (productName != null) {
            productNameText.setText(productName);
            titleTextView.setText(productName);
        }
        if (expiryDate != null) {
            expiryDateText.setText(expiryDate);
            calculateAndDisplayDaysLeft(expiryDate);
        }
    }

    // ── Load full product from Room DB ────────────────────────────────────────

    private void loadProductFromDatabase() {
        productViewModel.getAllProducts().observe(this, products -> {
            if (products == null) return;
            for (Product p : products) {
                if (p.getId() == productId) {
                    populateFromProduct(p);
                    return;
                }
            }
        });
    }

    private void populateFromProduct(Product product) {
        this.currentProduct = product;

        // Name & title
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
        categoryText.setText(
                product.getCategory() != null ? product.getCategory() : "Not specified");

        // Quantity
        quantityText.setText(
                product.getQuantity() != null ? product.getQuantity() : "Not specified");

        // Notes
        String notes = null;
        try { notes = product.getNotes(); } catch (Exception ignored) {}
        notesText.setText(notes != null && !notes.isEmpty() ? notes : "No notes");

        // ── Photo ─────────────────────────────────────────────────────────
        if (product.hasPhoto()) {
            byte[] photoBytes = product.getPhoto();
            Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
            if (bitmap != null) {
                productPhoto.setImageBitmap(bitmap);
                productPhoto.setBackground(null); // remove placeholder bg once image loads
            }
        } else {
            // Show category-appropriate placeholder
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

        updateConsumeButton();
    }

    // ── Check if product is expired ──────────────────────────────────────────

    private boolean isProductExpired() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date expiry = sdf.parse(expiryDate);
            Date today = new Date();
            return expiry != null && expiry.before(today);
        } catch (Exception e) {
            Log.e(TAG, "Error checking expiry: " + e.getMessage());
            return false;
        }
    }

    // ── Update consume button based on expiry ────────────────────────────────

    private void updateConsumeButton() {
        if (consumeButton == null) return;

        boolean isExpired = isProductExpired();
        float dp = getResources().getDisplayMetrics().density;

        if (isExpired) {
            consumeButton.setText("DISCARD PRODUCT");

            // Red color for discard
            android.graphics.drawable.GradientDrawable buttonBg =
                    new android.graphics.drawable.GradientDrawable();
            buttonBg.setColor(Color.parseColor("#D32F2F")); // Red
            buttonBg.setCornerRadius(8 * dp);
            consumeButton.setBackground(buttonBg);
        } else {
            consumeButton.setText("CONSUME PRODUCT");

            // Green color for consume
            android.graphics.drawable.GradientDrawable buttonBg =
                    new android.graphics.drawable.GradientDrawable();
            buttonBg.setColor(Color.parseColor("#4CAF50")); // Green
            buttonBg.setCornerRadius(8 * dp);
            consumeButton.setBackground(buttonBg);
        }
    }

    // ── Click listeners ───────────────────────────────────────────────────────

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

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

        deleteButton.setOnClickListener(v -> {
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

        consumeButton.setOnClickListener(v -> {
            if (currentProduct == null) {
                Toast.makeText(this, "Product data not loaded", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isExpired = isProductExpired();
            String actionType = isExpired ? "DISCARDED" : "CONSUMED";
            String buttonText = isExpired ? "discard" : "consume";
            String dialogTitle = isExpired ? "Discard Product" : "Consume Product";
            String dialogMessage = "Are you sure you want to " + buttonText + " \"" +
                    currentProduct.getName() + "\"?";

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle(dialogTitle)
                    .setMessage(dialogMessage)
                    .setPositiveButton("Yes, " + (isExpired ? "Discard" : "Consume"),
                            (dialog, which) -> {
                                // Create ConsumedProduct record with appropriate action type
                                ConsumedProduct consumedProduct = new ConsumedProduct(
                                        currentProduct.getId(),
                                        currentProduct.getName(),
                                        currentProduct.getCategory(),
                                        currentProduct.getQuantity(),
                                        currentProduct.getExpiryDate(),
                                        actionType,  // "CONSUMED" or "DISCARDED"
                                        currentProduct.getPhoto()
                                );

                                // Insert into consumed_products
                                consumedProductViewModel.insert(consumedProduct);

                                // Delete from products
                                productViewModel.delete(currentProduct);

                                Toast.makeText(this,
                                        "Product marked as " + actionType + "!",
                                        Toast.LENGTH_SHORT).show();

                                // Return to product list
                                finish();
                            })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    // ── Days left ─────────────────────────────────────────────────────────────

    private void calculateAndDisplayDaysLeft(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date expiry = sdf.parse(dateString);
            Date today = new Date();

            if (expiry != null) {
                long diff = expiry.getTime() - today.getTime();
                long daysLeft = diff / (24 * 60 * 60 * 1000);

                if (daysLeft < 0) {
                    daysLeftText.setText("⚠️ Expired");
                    daysLeftText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                } else if (daysLeft <= 7) {
                    daysLeftText.setText("⚠️ " + daysLeft + " days left (Soon)");
                    daysLeftText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
                } else {
                    daysLeftText.setText("✅ " + daysLeft + " days left (Safe)");
                    daysLeftText.setTextColor(Color.parseColor("#00C853")); // green
                }
            }

            updateConsumeButton();
        } catch (Exception e) {
            daysLeftText.setText("Unknown");
        }
    }

    // ── Theme ─────────────────────────────────────────────────────────────────

    private void applyTheme() {
        String theme = preferences.getString("color_theme", "white");
        boolean isDark = "black".equals(theme);
        float dp = getResources().getDisplayMetrics().density;

        // ── Palette ──────────────────────────────────────────────────────
        int mainBg        = isDark ? Color.parseColor("#121212") : Color.parseColor("#F7F7F7");
        int labelColor    = isDark ? Color.WHITE                 : Color.parseColor("#888888");
        int fieldBg       = isDark ? Color.parseColor("#222222") : Color.WHITE;
        int fieldBorder   = isDark ? Color.parseColor("#3A3A3A") : Color.parseColor("#DEDEDE");
        int fieldText     = isDark ? Color.WHITE                 : Color.parseColor("#1A1A1A");
        int titleColor    = isDark ? Color.WHITE                 : Color.parseColor("#1A1A1A");
        int dividerColor  = isDark ? Color.parseColor("#2A2A2A") : Color.parseColor("#E8E8E8");
        int accentGreen   = isDark ? Color.parseColor("#4CAF50") : Color.parseColor("#388E3C");

        // ── Root background ───────────────────────────────────────────────
        LinearLayout mainLayout = findViewById(R.id.mainLayout);
        if (mainLayout != null) mainLayout.setBackgroundColor(mainBg);
        if (getWindow() != null) getWindow().getDecorView().setBackgroundColor(mainBg);

        // ── Back arrow ────────────────────────────────────────────────────
        if (backButton != null)
            backButton.setImageTintList(
                    android.content.res.ColorStateList.valueOf(titleColor));

        // ── Title ─────────────────────────────────────────────────────────
        if (titleTextView != null) titleTextView.setTextColor(titleColor);

        // ── Dividers ──────────────────────────────────────────────────────
        android.view.View div1 = findViewById(R.id.divider1);
        android.view.View div2 = findViewById(R.id.divider2);
        if (div1 != null) div1.setBackgroundColor(dividerColor);
        if (div2 != null) div2.setBackgroundColor(dividerColor);

        // ── Section labels (ALL-CAPS small) ───────────────────────────────
        TextView[] labels = { photoLabel, productNameLabel, expiryDateLabel,
                daysLeftLabel, categoryLabel, quantityLabel, notesLabel };
        for (TextView lbl : labels) {
            if (lbl != null) lbl.setTextColor(labelColor);
        }

        // ── Value fields ──────────────────────────────────────────────────
        TextView[] valueFields = { productNameText, expiryDateText, categoryText,
                quantityText, notesText };
        for (TextView fld : valueFields) {
            if (fld != null) {
                fld.setTextColor(fieldText);
                android.graphics.drawable.GradientDrawable bg =
                        new android.graphics.drawable.GradientDrawable();
                bg.setColor(fieldBg);
                bg.setCornerRadius(10 * dp);
                bg.setStroke(1, fieldBorder);
                fld.setBackground(bg);
            }
        }

        // ── Consume button will be updated by updateConsumeButton() ───────
        // The theme will be applied there with the correct colors

        // ── Edit button (green) ───────────────────────────────────────────
        if (editButton != null) {
            android.graphics.drawable.GradientDrawable editBg =
                    new android.graphics.drawable.GradientDrawable();
            editBg.setColor(accentGreen);
            editBg.setCornerRadius(8 * dp);
            editButton.setBackground(editBg);
            editButton.setTextColor(Color.WHITE);
        }

        // ── Delete button (always red) ────────────────────────────────────
        if (deleteButton != null) {
            android.graphics.drawable.GradientDrawable deleteBg =
                    new android.graphics.drawable.GradientDrawable();
            deleteBg.setColor(Color.parseColor("#D32F2F"));
            deleteBg.setCornerRadius(8 * dp);
            deleteButton.setBackground(deleteBg);
            deleteButton.setTextColor(Color.WHITE);
        }
    }

    // ── Edit result ───────────────────────────────────────────────────────────

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            String updatedName     = data.getStringExtra("product_name");
            String updatedExpiry   = data.getStringExtra("expiry_date");
            String updatedCategory = data.getStringExtra("category");
            String updatedQuantity = data.getStringExtra("quantity");
            String updatedNotes    = data.getStringExtra("notes");

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
            if (updatedNotes    != null) notesText.setText(updatedNotes);

            updateConsumeButton();
            Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show();
        }
    }
}