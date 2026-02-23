package com.example.productexpirationtrackerapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
    private TextView titleTextView;
    private LinearLayout mainLayout;

    // Labels
    private TextView productNameLabel, expiryDateLabel, categoryLabel, quantityLabel, notesLabel;

    private int productId;
    private String originalName;
    private String originalExpiry;
    private String originalCategory;
    private String originalQuantity;
    private String originalNotes;

    private ProductViewModel productViewModel;
    private UserRepository userRepository;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        // Initialize UserRepository for theme
        userRepository = new UserRepository(getApplication());

        // Initialize ViewModel
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // Initialize views
        initializeViews();

        // Apply theme
        applyThemeFromDatabase();

        // Get intent data
        getIntentData();

        // Setup date picker
        setupDatePicker();

        // Setup spinner
        setupSpinner();

        // Setup click listeners
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reapply theme when returning
        applyThemeFromDatabase();
    }

    private void initializeViews() {
        mainLayout = findViewById(R.id.mainLayout);
        titleTextView = findViewById(R.id.titleTextView);

        productNameEdit = findViewById(R.id.productNameEdit);
        expiryDateEdit = findViewById(R.id.expiryDateEdit);
        categorySpinner = findViewById(R.id.categorySpinner);
        quantityEdit = findViewById(R.id.quantityEdit);
        notesEdit = findViewById(R.id.notesEdit);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        // Initialize labels
        productNameLabel = findViewById(R.id.productNameLabel);
        expiryDateLabel = findViewById(R.id.expiryDateLabel);
        categoryLabel = findViewById(R.id.categoryLabel);
        quantityLabel = findViewById(R.id.quantityLabel);
        notesLabel = findViewById(R.id.notesLabel);

        calendar = Calendar.getInstance();
    }

    // ========== THEME METHODS ==========

    private void applyThemeFromDatabase() {
        User user = userRepository.getUserSync();

        if (user != null) {
            String theme = user.getColorTheme();
            applyThemeColors(theme);
        } else {
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            String theme = prefs.getString("color_theme", "light");
            applyThemeColors(theme);
        }
    }

    private void applyThemeColors(String theme) {
        boolean isDarkTheme = theme.equals("dark") || theme.equals("black");

        int primaryColor;
        int backgroundColor;
        int textColor;
        int hintColor;

        if (isDarkTheme) {
            // Dark theme colors
            primaryColor = ContextCompat.getColor(this, R.color.color_primary_dark);
            backgroundColor = ContextCompat.getColor(this, R.color.color_background_dark);
            textColor = ContextCompat.getColor(this, R.color.color_text_dark);
            hintColor = Color.parseColor("#80FFFFFF"); // Semi-transparent white
        } else {
            // Light theme colors
            primaryColor = ContextCompat.getColor(this, R.color.color_primary_light);
            backgroundColor = ContextCompat.getColor(this, R.color.color_background_light);
            textColor = ContextCompat.getColor(this, R.color.color_text_light);
            hintColor = Color.parseColor("#80000000"); // Semi-transparent black
        }

        // Apply background color to main layout
        if (mainLayout != null) {
            mainLayout.setBackgroundColor(backgroundColor);
        }

        // Set title color
        if (titleTextView != null) {
            titleTextView.setTextColor(textColor);
        }

        // Set label colors
        if (productNameLabel != null) productNameLabel.setTextColor(textColor);
        if (expiryDateLabel != null) expiryDateLabel.setTextColor(textColor);
        if (categoryLabel != null) categoryLabel.setTextColor(textColor);
        if (quantityLabel != null) quantityLabel.setTextColor(textColor);
        if (notesLabel != null) notesLabel.setTextColor(textColor);

        // Set EditText colors
        if (productNameEdit != null) {
            productNameEdit.setTextColor(textColor);
            productNameEdit.setHintTextColor(hintColor);
        }
        if (expiryDateEdit != null) {
            expiryDateEdit.setTextColor(textColor);
            expiryDateEdit.setHintTextColor(hintColor);
        }
        if (quantityEdit != null) {
            quantityEdit.setTextColor(textColor);
            quantityEdit.setHintTextColor(hintColor);
        }
        if (notesEdit != null) {
            notesEdit.setTextColor(textColor);
            notesEdit.setHintTextColor(hintColor);
        }

        // Set button colors
        if (saveButton != null) {
            saveButton.setBackgroundColor(primaryColor);
            saveButton.setTextColor(Color.WHITE);
        }

        if (cancelButton != null) {
            cancelButton.setBackgroundColor(Color.parseColor("#757575"));
            cancelButton.setTextColor(Color.WHITE);
        }
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

        // Create custom adapter for spinner with theme support
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        ) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if (view instanceof TextView) {
                    TextView textView = (TextView) view;

                    // Get current theme
                    User user = userRepository.getUserSync();
                    boolean isDarkTheme = user != null ?
                            (user.getColorTheme().equals("dark") || user.getColorTheme().equals("black")) : false;

                    // Set text color based on theme
                    if (isDarkTheme) {
                        textView.setTextColor(Color.WHITE);
                        textView.setBackgroundColor(Color.parseColor("#333333"));
                    } else {
                        textView.setTextColor(Color.BLACK);
                        textView.setBackgroundColor(Color.WHITE);
                    }
                }
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Set selected category
        if (originalCategory != null && !originalCategory.equals("Not specified") && !originalCategory.equals("Category")) {
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