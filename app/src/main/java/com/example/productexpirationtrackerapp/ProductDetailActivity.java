package com.example.productexpirationtrackerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ProductDetailActivity extends AppCompatActivity {

    // Declare ALL variables here - ONLY ONCE!
    private TextView titleTextView;
    private Button backButton, consumeButton, editButton, deleteButton;
    private ImageView productPhoto;
    private TextView productNameText, expiryDateText, daysLeftText;
    private TextView categoryText, quantityText, notesText;
    private LinearLayout mainLayout;

    // Label TextViews
    private TextView photoLabel, productNameLabel, expiryDateLabel, daysLeftLabel;
    private TextView categoryLabel, quantityLabel, notesLabel;

    private ProductViewModel productViewModel;
    private ConsumedProductViewModel consumedProductViewModel; // ADDED
    private UserRepository userRepository;

    private int productId;
    private Product currentProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Get product ID from intent
        Intent intent = getIntent();
        productId = intent.getIntExtra("product_id", -1);

        if (productId == -1) {
            Toast.makeText(this, "Error: Product not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize ViewModels
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        consumedProductViewModel = new ViewModelProvider(this).get(ConsumedProductViewModel.class); // ADDED

        // Initialize UserRepository for theme
        userRepository = new UserRepository(getApplication());

        // Initialize views
        initializeViews();

        // Apply theme
        applyThemeFromDatabase();

        // Setup click listeners
        setupClickListeners();

        // Load product details
        loadProductDetails();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reapply theme every time the activity resumes
        applyThemeFromDatabase();
        Log.d("THEME_DEBUG", "ProductDetailActivity onResume - theme reapplied");
    }

    private void initializeViews() {
        // Initialize all views
        mainLayout = findViewById(R.id.mainLayout);
        titleTextView = findViewById(R.id.titleTextView);
        backButton = findViewById(R.id.backButton);
        consumeButton = findViewById(R.id.consumeButton);
        editButton = findViewById(R.id.editButton);
        deleteButton = findViewById(R.id.deleteButton);

        productPhoto = findViewById(R.id.productPhoto);
        productNameText = findViewById(R.id.productNameText);
        expiryDateText = findViewById(R.id.expiryDateText);
        daysLeftText = findViewById(R.id.daysLeftText);
        categoryText = findViewById(R.id.categoryText);
        quantityText = findViewById(R.id.quantityText);
        notesText = findViewById(R.id.notesText);

        // Initialize labels
        photoLabel = findViewById(R.id.photoLabel);
        productNameLabel = findViewById(R.id.productNameLabel);
        expiryDateLabel = findViewById(R.id.expiryDateLabel);
        daysLeftLabel = findViewById(R.id.daysLeftLabel);
        categoryLabel = findViewById(R.id.categoryLabel);
        quantityLabel = findViewById(R.id.quantityLabel);
        notesLabel = findViewById(R.id.notesLabel);
    }

    private void applyThemeFromDatabase() {
        // Use callback instead of sync
        userRepository.getUser(new UserRepository.UserRepositoryCallback() {
            @Override
            public void onUserLoaded(User user) {
                runOnUiThread(() -> {
                    if (user != null) {
                        String theme = user.getColorTheme();
                        Log.d("THEME_DEBUG", "ProductDetailActivity - Theme from database: " + theme);
                        ThemeUtils.applyTheme(ProductDetailActivity.this, theme);
                        applyCustomThemeColors(theme);
                    } else {
                        Log.d("THEME_DEBUG", "ProductDetailActivity - No user found, using default theme");
                        applyCustomThemeColors("white");
                    }
                });
            }
        });
    }

    private void applyCustomThemeColors(String theme) {
        int primaryColor;
        int consumeButtonColor;
        int textColor;
        int backgroundColor;
        int labelColor;

        switch (theme) {
            case "green":
                primaryColor = getResources().getColor(R.color.color_primary_green);
                consumeButtonColor = getResources().getColor(R.color.color_primary_green);
                textColor = getResources().getColor(R.color.color_text_green);
                backgroundColor = getResources().getColor(R.color.color_background_green);
                labelColor = textColor;
                break;
            case "blue":
                primaryColor = getResources().getColor(R.color.color_primary_blue);
                consumeButtonColor = getResources().getColor(R.color.color_primary_blue);
                textColor = getResources().getColor(R.color.color_text_blue);
                backgroundColor = getResources().getColor(R.color.color_background_blue);
                labelColor = textColor;
                break;
            case "pink":
                primaryColor = getResources().getColor(R.color.color_primary_pink);
                consumeButtonColor = getResources().getColor(R.color.color_primary_pink);
                textColor = getResources().getColor(R.color.color_text_pink);
                backgroundColor = getResources().getColor(R.color.color_background_pink);
                labelColor = textColor;
                break;
            case "purple":
                primaryColor = getResources().getColor(R.color.color_primary_purple);
                consumeButtonColor = getResources().getColor(R.color.color_primary_purple);
                textColor = getResources().getColor(R.color.color_text_purple);
                backgroundColor = getResources().getColor(R.color.color_background_purple);
                labelColor = textColor;
                break;
            case "black":
                primaryColor = getResources().getColor(R.color.color_primary_black);
                consumeButtonColor = 0xFF4CAF50; // Green for contrast on black
                textColor = getResources().getColor(R.color.color_text_black);
                backgroundColor = getResources().getColor(R.color.color_background_black);
                labelColor = Color.WHITE;
                break;
            case "white":
            default:
                primaryColor = getResources().getColor(R.color.color_primary_white);
                consumeButtonColor = 0xFF4CAF50; // Green
                textColor = getResources().getColor(R.color.color_text_white);
                backgroundColor = getResources().getColor(R.color.color_background_white);
                labelColor = textColor;
                break;
        }

        // Apply background color
        if (mainLayout != null) {
            mainLayout.setBackgroundColor(backgroundColor);
        }

        // Apply text colors to labels
        if (photoLabel != null) photoLabel.setTextColor(labelColor);
        if (productNameLabel != null) productNameLabel.setTextColor(labelColor);
        if (expiryDateLabel != null) expiryDateLabel.setTextColor(labelColor);
        if (daysLeftLabel != null) daysLeftLabel.setTextColor(labelColor);
        if (categoryLabel != null) categoryLabel.setTextColor(labelColor);
        if (quantityLabel != null) quantityLabel.setTextColor(labelColor);
        if (notesLabel != null) notesLabel.setTextColor(labelColor);

        // Apply title color
        if (titleTextView != null) titleTextView.setTextColor(labelColor);

        // Apply button colors (except consume button - it will be updated based on expiry)
        if (backButton != null) {
            backButton.setBackgroundColor(primaryColor);
            backButton.setTextColor(Color.WHITE);
        }

        if (editButton != null) {
            editButton.setBackgroundColor(primaryColor);
            editButton.setTextColor(Color.WHITE);
        }

        if (deleteButton != null) {
            deleteButton.setBackgroundColor(0xFFF44336); // Red for delete
            deleteButton.setTextColor(Color.WHITE);
        }

        // Apply text colors to data fields
        if (productNameText != null) productNameText.setTextColor(textColor);
        if (expiryDateText != null) expiryDateText.setTextColor(textColor);
        if (categoryText != null) categoryText.setTextColor(textColor);
        if (quantityText != null) quantityText.setTextColor(textColor);
        if (notesText != null) notesText.setTextColor(textColor);

        // Log for debugging
        Log.d("THEME_DEBUG", "Applied theme: " + theme + " to ProductDetailActivity");
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        // Note: consumeButton click listener is set dynamically in updateConsumeButton()

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editProduct();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete();
            }
        });
    }

    private void loadProductDetails() {
        productViewModel.getAllProducts().observe(this, new Observer<List<Product>>() {
            @Override
            public void onChanged(List<Product> products) {
                for (Product product : products) {
                    if (product.getId() == productId) {
                        currentProduct = product;
                        displayProductDetails(product);
                        break;
                    }
                }
            }
        });
    }

    private void displayProductDetails(Product product) {
        productNameText.setText(product.getName());
        titleTextView.setText(product.getName() + " Details");

        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        expiryDateText.setText(displayFormat.format(product.getExpiryDate()));

        long daysLeft = calculateAndSetDaysLeft(product.getExpiryDate());

        if (product.hasPhoto()) {
            byte[] photoBytes = product.getPhoto();
            Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
            productPhoto.setImageBitmap(bitmap);
        } else {
            productPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        categoryText.setText(product.hasCategory() ? product.getCategory() : "Not specified");
        quantityText.setText(product.hasQuantity() ? product.getQuantity() : "Not specified");
        notesText.setText(product.hasNotes() ? product.getNotes() : "No notes");

        // Update consume button based on expiry status
        updateConsumeButton(daysLeft);
    }

    private long calculateAndSetDaysLeft(Date expiryDate) {
        Date now = new Date();
        long timeDiff = expiryDate.getTime() - now.getTime();
        long days = TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);

        if (days < 0) {
            daysLeftText.setText("EXPIRED " + Math.abs(days) + " days ago");
            daysLeftText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else if (days == 0) {
            daysLeftText.setText("EXPIRES TODAY!");
            daysLeftText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else if (days <= 3) {
            daysLeftText.setText(days + " days left - URGENT!");
            daysLeftText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else if (days <= 7) {
            daysLeftText.setText(days + " days left - SOON");
            daysLeftText.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
        } else {
            daysLeftText.setText(days + " days left - FRESH");
            daysLeftText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }

        return days;
    }

    private void updateConsumeButton(long daysLeft) {
        if (consumeButton == null) return;

        if (daysLeft < 0) {
            // Expired product
            consumeButton.setText("🗑️ Discard");
            consumeButton.setBackgroundColor(0xFF757575); // Gray
            consumeButton.setOnClickListener(v -> {
                // For expired, just delete directly without asking
                deleteProduct();
            });
        } else {
            // Fresh or soon-to-expire product
            consumeButton.setText("✔ CONSUME");
            // Color will be set by theme in applyCustomThemeColors()
            // But we need to reapply the theme color
            User user = userRepository.getUserSync();
            if (user != null) {
                String theme = user.getColorTheme();
                int consumeButtonColor;
                switch (theme) {
                    case "green":
                        consumeButtonColor = getResources().getColor(R.color.color_primary_green);
                        break;
                    case "blue":
                        consumeButtonColor = getResources().getColor(R.color.color_primary_blue);
                        break;
                    case "pink":
                        consumeButtonColor = getResources().getColor(R.color.color_primary_pink);
                        break;
                    case "purple":
                        consumeButtonColor = getResources().getColor(R.color.color_primary_purple);
                        break;
                    case "black":
                        consumeButtonColor = 0xFF4CAF50;
                        break;
                    default:
                        consumeButtonColor = 0xFF4CAF50;
                        break;
                }
                consumeButton.setBackgroundColor(consumeButtonColor);
            }
            consumeButton.setOnClickListener(v -> {
                confirmConsume();
            });
        }
    }

    private void confirmConsume() {
        new AlertDialog.Builder(this)
                .setTitle("Consume Product")
                .setMessage("Did you consume/use \"" + currentProduct.getName() + "\"?")
                .setPositiveButton("YES, CONSUMED", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        consumeProduct();
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    // ✅ UPDATED: Save to history before consuming
    private void consumeProduct() {
        if (currentProduct != null) {
            // Save to history first
            saveToHistory("CONSUMED");

            // Then delete from products table
            productViewModel.delete(currentProduct);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("deleted_product_id", productId);
            resultIntent.putExtra("consumed", true);
            setResult(RESULT_OK, resultIntent);

            Toast.makeText(this, "✓ " + currentProduct.getName() + " consumed!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // ✅ NEW: Helper method to save to history
    private void saveToHistory(String actionType) {
        if (currentProduct != null) {
            // Create ConsumedProduct object
            ConsumedProduct consumedProduct = new ConsumedProduct(
                    currentProduct.getId(),
                    currentProduct.getName(),
                    currentProduct.getCategory(),
                    currentProduct.getQuantity(),
                    currentProduct.getExpiryDate(),
                    actionType,
                    currentProduct.getPhoto()
            );

            // Save to database
            consumedProductViewModel.insert(consumedProduct);

            Log.d("HISTORY", "✅ Saved to history: " + currentProduct.getName() + " - " + actionType);
        }
    }

    private void editProduct() {
        Toast.makeText(this, "Edit feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete \"" + currentProduct.getName() + "\"?")
                .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteProduct();
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    // ✅ UPDATED: Save to history before deleting
    private void deleteProduct() {
        if (currentProduct != null) {
            // Save to history first (as DISCARDED)
            saveToHistory("DISCARDED");

            // Then delete from products table
            productViewModel.delete(currentProduct);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("deleted_product_id", productId);
            setResult(RESULT_OK, resultIntent);

            Toast.makeText(this, "Deleted: " + currentProduct.getName(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}