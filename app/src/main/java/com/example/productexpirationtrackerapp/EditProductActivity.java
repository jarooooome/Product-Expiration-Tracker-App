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
import java.util.List;
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
        String[] categories = {"Dairy", "Vegetables", "Fruits", "Meats", "Beverages", "Medicine", "Other"};

        // Custom adapter that forces correct text color on the selected item view
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                if (view instanceof android.widget.TextView) {
                    boolean dark = "black".equals(preferences.getString("color_theme", "white"));
                    ((android.widget.TextView) view).setTextColor(
                            dark ? android.graphics.Color.WHITE : android.graphics.Color.parseColor("#1A1A1A")
                    );
                }
                return view;
            }

            @Override
            public android.view.View getDropDownView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.view.View view = super.getDropDownView(position, convertView, parent);
                if (view instanceof android.widget.TextView) {
                    boolean dark = "black".equals(preferences.getString("color_theme", "white"));
                    ((android.widget.TextView) view).setTextColor(
                            dark ? android.graphics.Color.WHITE : android.graphics.Color.parseColor("#1A1A1A")
                    );
                    view.setBackgroundColor(
                            dark ? android.graphics.Color.parseColor("#222222") : android.graphics.Color.WHITE
                    );
                }
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Force text color whenever selection changes
        categorySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (view instanceof android.widget.TextView) {
                    boolean dark = "black".equals(preferences.getString("color_theme", "white"));
                    ((android.widget.TextView) view).setTextColor(
                            dark ? android.graphics.Color.WHITE : android.graphics.Color.parseColor("#1A1A1A")
                    );
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

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

        final int finalQuantity = quantity;
        final String finalNotes = notes;
        final String finalName = name;
        final String finalCategory = category;
        final String finalExpiry = expiry;

        // Persist to DB: scan getAllProducts() for this product ID, update, then save
        if (productId != -1) {
            androidx.lifecycle.LiveData<List<Product>> allLive = productViewModel.getAllProducts();

            allLive.observe(this, new androidx.lifecycle.Observer<List<Product>>() {
                @Override
                public void onChanged(List<Product> products) {
                    allLive.removeObserver(this); // one-shot — never accumulates
                    if (products != null) {
                        for (Product p : products) {
                            if (p.getId() == productId) {
                                p.setName(finalName);
                                p.setCategory(finalCategory);
                                // setQuantity takes String in the Product model
                                p.setQuantity(finalQuantity + (finalQuantity == 1 ? " item" : " items"));
                                p.setNotes(finalNotes.isEmpty() ? "" : finalNotes);
                                try {
                                    java.text.SimpleDateFormat sdf =
                                            new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                                    p.setExpiryDate(sdf.parse(finalExpiry));
                                } catch (Exception e) {
                                    android.util.Log.e("EditProduct", "Date parse error: " + e.getMessage());
                                }
                                productViewModel.update(p);
                                break;
                            }
                        }
                    }
                    // Return updated values to caller for immediate UI refresh
                    deliverResult(finalName, finalExpiry, finalCategory, finalQuantity, finalNotes);
                }
            });
        } else {
            deliverResult(name, expiry, category, finalQuantity, finalNotes);
        }
    }

    /** Sends result back to the caller and finishes this activity. */
    private void deliverResult(String name, String expiry, String category,
                               int quantity, String notes) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("product_name", name);
        resultIntent.putExtra("expiry_date", expiry);
        resultIntent.putExtra("category", category);
        resultIntent.putExtra("quantity", quantity + (quantity == 1 ? " item" : " items"));
        resultIntent.putExtra("notes", notes.isEmpty() ? "No notes" : notes);
        resultIntent.putExtra("product_id", productId);
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

        // ── Palette ──────────────────────────────────────────────────────
        int mainBg       = isDark ? Color.parseColor("#121212") : Color.parseColor("#F7F7F7");
        int labelColor   = isDark ? Color.WHITE                 : Color.parseColor("#888888");
        int fieldBg      = isDark ? Color.parseColor("#222222") : Color.WHITE;
        int fieldBorder  = isDark ? Color.parseColor("#3A3A3A") : Color.parseColor("#DEDEDE");
        int fieldText    = isDark ? Color.WHITE                 : Color.parseColor("#1A1A1A");
        int hintColor    = isDark ? Color.parseColor("#555555") : Color.parseColor("#AAAAAA");
        int titleColor   = isDark ? Color.WHITE                 : Color.parseColor("#1A1A1A");
        int dividerColor = isDark ? Color.parseColor("#2A2A2A") : Color.parseColor("#E8E8E8");
        int accentGreen  = isDark ? Color.parseColor("#4CAF50") : Color.parseColor("#388E3C");
        int iconTint     = isDark ? Color.parseColor("#AAAAAA") : Color.parseColor("#888888");

        // ── Root + scroll background ──────────────────────────────────────
        LinearLayout rootLayout = findViewById(R.id.rootLayout);
        if (rootLayout != null) rootLayout.setBackgroundColor(mainBg);
        View scrollView = (View) rootLayout.getParent();
        if (scrollView != null) scrollView.setBackgroundColor(mainBg);

        // ── Back arrow ────────────────────────────────────────────────────
        if (cancelButton != null)
            cancelButton.setImageTintList(
                    android.content.res.ColorStateList.valueOf(titleColor));

        // ── Header title ──────────────────────────────────────────────────
        TextView headerTitle = findViewById(R.id.headerTitle);
        if (headerTitle != null) headerTitle.setTextColor(titleColor);

        // ── Divider ───────────────────────────────────────────────────────
        android.view.View div1 = findViewById(R.id.divider1);
        if (div1 != null) div1.setBackgroundColor(dividerColor);

        // ── Section labels (ALL-CAPS small) ───────────────────────────────
        int[] labelIds = { R.id.productNameLabel, R.id.expiryDateLabel,
                R.id.categoryLabel, R.id.quantityLabel, R.id.notesLabel };
        for (int id : labelIds) {
            TextView lbl = findViewById(id);
            if (lbl != null) lbl.setTextColor(labelColor);
        }

        // ── EditText fields ───────────────────────────────────────────────
        EditText[] fields = { productNameEdit, expiryDateEdit, quantityEdit, notesEdit };
        for (EditText et : fields) {
            if (et != null) {
                et.setTextColor(fieldText);
                et.setHintTextColor(hintColor);
                android.graphics.drawable.GradientDrawable bg =
                        new android.graphics.drawable.GradientDrawable();
                bg.setColor(fieldBg);
                bg.setCornerRadius(8 * dp);
                bg.setStroke(1, fieldBorder);
                et.setBackground(bg);
            }
        }

        // ── Spinner background ────────────────────────────────────────────
        if (categorySpinner != null) {
            android.graphics.drawable.GradientDrawable spinnerBg =
                    new android.graphics.drawable.GradientDrawable();
            spinnerBg.setColor(fieldBg);
            spinnerBg.setCornerRadius(8 * dp);
            spinnerBg.setStroke(1, fieldBorder);
            categorySpinner.setBackground(spinnerBg);
            // Force selected item text colour
            categorySpinner.post(() -> {
                android.view.View sv = categorySpinner.getSelectedView();
                if (sv instanceof android.widget.TextView)
                    ((android.widget.TextView) sv).setTextColor(fieldText);
            });
        }

        // ── Calendar icon tint ────────────────────────────────────────────
        if (expiryDateEdit != null) {
            android.graphics.drawable.Drawable[] drawables =
                    expiryDateEdit.getCompoundDrawablesRelative();
            if (drawables[2] != null) {
                drawables[2] = drawables[2].mutate();
                drawables[2].setTint(iconTint);
                expiryDateEdit.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        drawables[0], drawables[1], drawables[2], drawables[3]);
            }
        }

        // ── Dropdown icon tint ────────────────────────────────────────────
        ImageView dropdownIcon = findViewById(R.id.dropdownIcon);
        if (dropdownIcon != null)
            dropdownIcon.setImageTintList(
                    android.content.res.ColorStateList.valueOf(iconTint));

        // ── Save button (green, rounded) ──────────────────────────────────
        if (saveButton != null) {
            android.graphics.drawable.GradientDrawable saveBg =
                    new android.graphics.drawable.GradientDrawable();
            saveBg.setColor(Color.parseColor("#BB86FC")); // light violet — matches app accent
            saveBg.setCornerRadius(8 * dp);
            saveButton.setBackground(saveBg);
            saveButton.setTextColor(Color.WHITE);
        }
    }
}