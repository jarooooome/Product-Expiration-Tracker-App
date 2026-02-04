package com.example.productexpirationtrackerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ProductDetailActivity extends AppCompatActivity {

    private TextView titleTextView;
    private Button backButton, editButton, deleteButton;
    private ImageView productPhoto;
    private TextView productNameText, expiryDateText, daysLeftText;
    private TextView categoryText, quantityText, notesText;

    private ProductViewModel productViewModel;
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

        // Initialize ViewModel
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

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

    private void initializeViews() {
        titleTextView = findViewById(R.id.titleTextView);
        backButton = findViewById(R.id.backButton);
        editButton = findViewById(R.id.editButton);
        deleteButton = findViewById(R.id.deleteButton);

        productPhoto = findViewById(R.id.productPhoto);
        productNameText = findViewById(R.id.productNameText);
        expiryDateText = findViewById(R.id.expiryDateText);
        daysLeftText = findViewById(R.id.daysLeftText);
        categoryText = findViewById(R.id.categoryText);
        quantityText = findViewById(R.id.quantityText);
        notesText = findViewById(R.id.notesText);
    }

    private void applyThemeFromDatabase() {
        User user = userRepository.getUserSync();
        if (user != null) {
            String theme = user.getColorTheme();
            ThemeUtils.applyTheme(this, theme);
            applyCustomThemeColors(theme);
        }
    }

    private void applyCustomThemeColors(String theme) {
        int primaryColor;

        switch (theme) {
            case "green":
                primaryColor = getResources().getColor(R.color.color_primary_green);
                break;
            case "blue":
                primaryColor = getResources().getColor(R.color.color_primary_blue);
                break;
            case "pink":
                primaryColor = getResources().getColor(R.color.color_primary_pink);
                break;
            case "purple":
                primaryColor = getResources().getColor(R.color.color_primary_purple);
                break;
            case "black":
                primaryColor = getResources().getColor(R.color.color_primary_black);
                break;
            case "white":
            default:
                primaryColor = getResources().getColor(R.color.color_primary_white);
                break;
        }

        // Apply button colors
        if (backButton != null) backButton.setBackgroundColor(primaryColor);
        if (editButton != null) editButton.setBackgroundColor(primaryColor);
        if (deleteButton != null) deleteButton.setBackgroundColor(0xFFF44336); // Red for delete
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

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
        // Observe all products to find the one with matching ID
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
        // Set product name
        productNameText.setText(product.getName());
        titleTextView.setText(product.getName() + " Details");

        // Set expiry date
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        expiryDateText.setText(displayFormat.format(product.getExpiryDate()));

        // Calculate and set days left
        calculateAndSetDaysLeft(product.getExpiryDate());

        // Set product photo
        if (product.hasPhoto()) {
            byte[] photoBytes = product.getPhoto();
            Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
            productPhoto.setImageBitmap(bitmap);
        } else {
            productPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Set category, quantity, and notes using the helper methods from Product.java
        categoryText.setText(product.hasCategory() ? product.getCategory() : "Not specified");
        quantityText.setText(product.hasQuantity() ? product.getQuantity() : "Not specified");
        notesText.setText(product.hasNotes() ? product.getNotes() : "No notes");
    }

    private void calculateAndSetDaysLeft(Date expiryDate) {
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
    }

    private void editProduct() {
        Toast.makeText(this, "Edit feature coming soon!", Toast.LENGTH_SHORT).show();
        // You can implement edit functionality here
        // Intent editIntent = new Intent(this, EditProductActivity.class);
        // editIntent.putExtra("product_id", productId);
        // startActivityForResult(editIntent, 100);
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

    private void deleteProduct() {
        if (currentProduct != null) {
            productViewModel.delete(currentProduct);

            // Send result back to ProductListActivity
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