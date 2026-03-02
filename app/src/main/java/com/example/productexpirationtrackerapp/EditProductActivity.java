package com.example.productexpirationtrackerapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
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
    private ImageView cancelButton;   // Changed: ImageView arrow like CategoryDetailActivity

    private int productId;
    private String originalName;
    private String originalExpiry;
    private String originalCategory;
    private String originalQuantity;
    private String originalNotes;

    private ProductViewModel productViewModel;
    private Calendar calendar;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        // Initialize ViewModel
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

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

        // Apply dark/light theme
        applyTheme();
    }

    private void initializeViews() {
        productNameEdit = findViewById(R.id.productNameEdit);
        expiryDateEdit = findViewById(R.id.expiryDateEdit);
        categorySpinner = findViewById(R.id.categorySpinner);
        quantityEdit = findViewById(R.id.quantityEdit);
        notesEdit = findViewById(R.id.notesEdit);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);   // ImageView

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

    @Override
    protected void onResume() {
        super.onResume();
        applyTheme();
    }

    private void applyTheme() {
        String theme = preferences.getString("color_theme", "white");
        boolean isDark = "black".equals(theme);
        float dp = getResources().getDisplayMetrics().density;

        int mainBg      = isDark ? Color.parseColor("#121212") : Color.parseColor("#F5F7FA");
        int labelColor  = isDark ? Color.WHITE                 : Color.parseColor("#1A1E2C");
        int fieldBg     = isDark ? Color.parseColor("#2A2A2A") : Color.WHITE;
        int fieldText   = isDark ? Color.WHITE                 : Color.parseColor("#1A1E2C");
        int hintColor   = isDark ? Color.parseColor("#888888") : Color.parseColor("#AAAAAA");
        int arrowTint   = isDark ? Color.WHITE                 : Color.parseColor("#1A1E2C");

        // Root background
        LinearLayout rootLayout = findViewById(R.id.rootLayout);
        if (rootLayout != null) rootLayout.setBackgroundColor(mainBg);

        // ScrollView background
        View scrollView = (View) rootLayout.getParent();
        if (scrollView != null) scrollView.setBackgroundColor(mainBg);

        // Back arrow tint
        if (cancelButton != null)
            cancelButton.setImageTintList(
                    android.content.res.ColorStateList.valueOf(arrowTint));

        // Header title
        TextView headerTitle = findViewById(R.id.headerTitle);
        if (headerTitle != null) headerTitle.setTextColor(labelColor);

        // All label TextViews — identify by iterating labelIds
        int[] labelIds = {
                R.id.productNameLabel, R.id.expiryDateLabel,
                R.id.categoryLabel, R.id.quantityLabel, R.id.notesLabel
        };
        for (int id : labelIds) {
            TextView lbl = findViewById(id);
            if (lbl != null) lbl.setTextColor(labelColor);
        }

        // EditText fields
        EditText[] fields = { productNameEdit, expiryDateEdit, quantityEdit, notesEdit };
        for (EditText et : fields) {
            if (et != null) {
                et.setTextColor(fieldText);
                et.setHintTextColor(hintColor);
                android.graphics.drawable.GradientDrawable bg =
                        new android.graphics.drawable.GradientDrawable();
                bg.setColor(fieldBg);
                bg.setCornerRadius(10 * dp);
                et.setBackground(bg);
            }
        }

        // Spinner background
        if (categorySpinner != null) {
            android.graphics.drawable.GradientDrawable spinnerBg =
                    new android.graphics.drawable.GradientDrawable();
            spinnerBg.setColor(fieldBg);
            spinnerBg.setCornerRadius(10 * dp);
            categorySpinner.setBackground(spinnerBg);
        }

        // Save button — keep existing blue
        if (saveButton != null) {
            saveButton.setBackgroundColor(Color.parseColor("#4361EE"));
            saveButton.setTextColor(Color.WHITE);
        }

        // Calendar icon — tint the drawableEnd on expiryDateEdit
        int iconTint = isDark ? Color.parseColor("#AAAAAA") : Color.parseColor("#8A8F9E");
        if (expiryDateEdit != null) {
            android.graphics.drawable.Drawable[] drawables = expiryDateEdit.getCompoundDrawablesRelative();
            if (drawables[2] != null) { // drawableEnd
                drawables[2] = drawables[2].mutate();
                drawables[2].setTint(iconTint);
                expiryDateEdit.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        drawables[0], drawables[1], drawables[2], drawables[3]);
            }
        }
        // Dropdown icon — still an ImageView overlay on the spinner
        ImageView dropdownIcon = findViewById(R.id.dropdownIcon);
        if (dropdownIcon != null)
            dropdownIcon.setImageTintList(
                    android.content.res.ColorStateList.valueOf(iconTint));
    }
}