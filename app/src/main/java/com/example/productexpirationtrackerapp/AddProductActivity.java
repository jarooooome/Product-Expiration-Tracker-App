package com.example.productexpirationtrackerapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddProductActivity extends AppCompatActivity {

    private ImageButton backButton;
    private CardView barcodeScannerCard;
    private TextInputEditText productNameInput;
    private TextInputEditText brandInput;
    private TextInputEditText categoryInput;
    private TextInputEditText quantityInput;
    private TextInputEditText storageLocationInput;
    private TextInputEditText expirationDateInput;
    private Button saveButton;

    private AppDatabase database;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Initialize views first
        initializeViews();

        // Setup database
        setupDatabase();

        // Setup date picker
        setupDatePicker();

        // Setup button click listeners
        setupButtons();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        barcodeScannerCard = findViewById(R.id.barcodeScannerCard);
        productNameInput = findViewById(R.id.productNameInput);
        brandInput = findViewById(R.id.brandInput);
        categoryInput = findViewById(R.id.categoryInput);
        quantityInput = findViewById(R.id.quantityInput);
        storageLocationInput = findViewById(R.id.storageLocationInput);
        expirationDateInput = findViewById(R.id.expirationDateInput);
        saveButton = findViewById(R.id.saveButton);

        calendar = Calendar.getInstance();
    }

    private void setupDatabase() {
        database = AppDatabase.getDatabase(this);
    }

    private void setupDatePicker() {
        expirationDateInput.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    expirationDateInput.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void setupButtons() {
        backButton.setOnClickListener(v -> finish());

        barcodeScannerCard.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.barcode_scanner_coming_soon), Toast.LENGTH_SHORT).show();
            // TODO: Implement barcode scanner
        });

        saveButton.setOnClickListener(v -> saveProduct());
    }

    private void saveProduct() {
        String name = productNameInput.getText() != null ? productNameInput.getText().toString().trim() : "";
        String brand = brandInput.getText() != null ? brandInput.getText().toString().trim() : "";
        String category = categoryInput.getText() != null ? categoryInput.getText().toString().trim() : "";
        String quantityStr = quantityInput.getText() != null ? quantityInput.getText().toString().trim() : "";
        String storageLocation = storageLocationInput.getText() != null ? storageLocationInput.getText().toString().trim() : "";
        String expiryDateStr = expirationDateInput.getText() != null ? expirationDateInput.getText().toString().trim() : "";

        // Validation
        if (name.isEmpty()) {
            productNameInput.setError(getString(R.string.product_name_required));
            productNameInput.requestFocus();
            return;
        }

        if (expiryDateStr.isEmpty()) {
            expirationDateInput.setError(getString(R.string.expiration_date_required));
            expirationDateInput.requestFocus();
            return;
        }

        // Create product object with Date object
        try {
            // Parse date string to Date object
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            java.util.Date expiryDate = sdf.parse(expiryDateStr);

            Product product = new Product();
            product.setName(name);
            product.setExpiryDate(expiryDate);

            if (!brand.isEmpty()) {
                product.setBrand(brand);
            }

            if (!category.isEmpty()) {
                product.setCategory(category);
            }

            if (!quantityStr.isEmpty()) {
                try {
                    int quantity = Integer.parseInt(quantityStr);
                    product.setQuantity(quantity);
                } catch (NumberFormatException e) {
                    quantityInput.setError(getString(R.string.invalid_quantity));
                    return;
                }
            }

            if (!storageLocation.isEmpty()) {
                product.setStorageLocation(storageLocation);
            }

            // Save to database
            new Thread(() -> {
                try {
                    database.productDao().insert(product);

                    runOnUiThread(() -> {
                        Toast.makeText(AddProductActivity.this,
                                getString(R.string.product_added_successfully), Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(AddProductActivity.this,
                                getString(R.string.error_saving_product, e.getMessage()), Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();

        } catch (java.text.ParseException e) {
            expirationDateInput.setError(getString(R.string.invalid_date_format));
            expirationDateInput.requestFocus();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_generic, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }
}