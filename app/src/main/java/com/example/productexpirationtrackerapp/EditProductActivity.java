package com.example.productexpirationtrackerapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditProductActivity extends AppCompatActivity {

    private EditText productNameEdit;
    private EditText expiryDateEdit;
    private Spinner categorySpinner;
    private EditText quantityEdit;
    private EditText notesEdit;
    private Button saveButton;
    private Button cancelButton;

    private int productId;
    private String originalName;
    private String originalExpiry;
    private String originalCategory;
    private String originalQuantity;
    private String originalNotes;

    private ProductViewModel productViewModel;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        // Initialize ViewModel
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // Initialize views
        initializeViews();

        // Get intent data
        getIntentData();

        // Setup date picker
        setupDatePicker();

        // Setup spinner
        setupSpinner();

        // Setup click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        productNameEdit = findViewById(R.id.productNameEdit);
        expiryDateEdit = findViewById(R.id.expiryDateEdit);
        categorySpinner = findViewById(R.id.categorySpinner);
        quantityEdit = findViewById(R.id.quantityEdit);
        notesEdit = findViewById(R.id.notesEdit);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        calendar = Calendar.getInstance();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        productId = intent.getIntExtra("product_id", -1);
        originalName = intent.getStringExtra("product_name");
        originalExpiry = intent.getStringExtra("expiry_date");
        originalCategory = intent.getStringExtra("category");
        originalQuantity = intent.getStringExtra("quantity");
        originalNotes = intent.getStringExtra("notes");

        // Set existing values
        if (originalName != null) productNameEdit.setText(originalName);
        if (originalExpiry != null) expiryDateEdit.setText(originalExpiry);
        if (originalQuantity != null) quantityEdit.setText(originalQuantity.replace(" items", "").replace(" item", ""));
        if (originalNotes != null && !originalNotes.equals("No notes")) notesEdit.setText(originalNotes);
    }

    private void setupSpinner() {
        // Create array of categories
        String[] categories = {"Dairy", "Vegetables", "Fruits", "Meats", "Beverages", "Medicine", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Set selected category
        if (originalCategory != null && !originalCategory.equals("Not specified")) {
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(originalCategory)) {
                    categorySpinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private void setupDatePicker() {
        expiryDateEdit.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        expiryDateEdit.setText(sdf.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    private void setupClickListeners() {
        saveButton.setOnClickListener(v -> saveProduct());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void saveProduct() {
        // Validate inputs
        String name = productNameEdit.getText().toString().trim();
        String expiry = expiryDateEdit.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();
        String quantityStr = quantityEdit.getText().toString().trim();
        String notes = notesEdit.getText().toString().trim();

        if (name.isEmpty()) {
            productNameEdit.setError("Product name is required");
            return;
        }

        if (expiry.isEmpty()) {
            expiryDateEdit.setError("Expiry date is required");
            return;
        }

        int quantity = 1;
        if (!quantityStr.isEmpty()) {
            try {
                quantity = Integer.parseInt(quantityStr);
            } catch (NumberFormatException e) {
                quantityEdit.setError("Invalid quantity");
                return;
            }
        }

        // Create intent to return data
        Intent resultIntent = new Intent();
        resultIntent.putExtra("product_name", name);
        resultIntent.putExtra("expiry_date", expiry);
        resultIntent.putExtra("category", category);
        resultIntent.putExtra("quantity", quantity + (quantity == 1 ? " item" : " items"));
        resultIntent.putExtra("notes", notes.isEmpty() ? "No notes" : notes);

        setResult(RESULT_OK, resultIntent);
        finish();
    }
}