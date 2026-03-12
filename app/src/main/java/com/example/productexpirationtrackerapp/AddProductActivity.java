package com.example.productexpirationtrackerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup;

import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddProductActivity extends AppCompatActivity {

    private EditText productNameEditText;
    private EditText expiryDateEditText;
    private Spinner categorySpinner;
    private EditText quantityEditText;
    private EditText notesEditText;
    private Button datePickerButton;
    private Button saveButton;
    private Button cancelButton;
    private Button takePhotoButton;
    private Button choosePhotoButton;
    private Button removePhotoButton;
    // NEW: Add scan button variable
    private Button scanProductButton;
    private TextView titleTextView;
    private TextView photoLabel;
    private TextView productNameLabel;
    private TextView expiryDateLabel;
    private TextView categoryLabel;
    private TextView quantityLabel;
    private TextView notesLabel;
    private ImageView productPhotoPreview;
    private View mainLayout;

    private ProductViewModel productViewModel;
    private UserRepository userRepository;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 101;
    private static final int CAMERA_PERMISSION_CODE = 102;
    // NEW: Constant for scanner request
    private static final int SCAN_PRODUCT_REQUEST_CODE = 103;
    private static final String TAG = "AddProductTheme";

    private Bitmap productPhotoBitmap;
    private String productPhotoPath;
    private Uri photoUri;          // URI of the file written by the camera
    private String selectedCategory = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Initialize ViewModel
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // Initialize UserRepository to get theme
        userRepository = new UserRepository(getApplication());

        // Initialize date formatter
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        calendar = Calendar.getInstance();

        // Initialize views
        initializeViews();

        // Setup category spinner
        setupCategorySpinner();

        // Apply theme from database
        applyThemeFromDatabase();

        // Setup click listeners
        setupClickListeners();

        // Pre-fill form if launched from scanner shortcut on product list screen
        prefillFromIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reapply theme when activity resumes
        applyThemeFromDatabase();
    }

    private void applyThemeFromDatabase() {
        User user = userRepository.getUserSync();
        String theme;

        if (user != null && user.getColorTheme() != null) {
            theme = user.getColorTheme();
        } else {
            // Fallback to SharedPreferences so dark mode persists correctly
            android.content.SharedPreferences prefs =
                    getSharedPreferences("AppPrefs", MODE_PRIVATE);
            theme = prefs.getString("color_theme", "white");
        }

        Log.d(TAG, "AddProduct applying theme: " + theme);
        ThemeUtils.applyTheme(this, theme);
        applyCustomThemeColors(theme);
    }

    private void applyCustomThemeColors(String theme) {
        boolean isDark = "black".equals(theme);
        float dp = getResources().getDisplayMetrics().density;

        // ── Palette ──────────────────────────────────────────────────────
        // Dark mode: pure black/white only, except violet on 3 specific buttons
        // Light mode: white bg, dark text, violet accent on same 3 buttons
        int backgroundColor  = isDark ? Color.parseColor("#121212") : Color.parseColor("#F7F7F7");
        int cardColor        = isDark ? Color.parseColor("#1C1C1C") : Color.WHITE;
        int fieldBgColor     = isDark ? Color.parseColor("#222222") : Color.WHITE;
        int fieldBorderColor = isDark ? Color.parseColor("#3A3A3A") : Color.parseColor("#DEDEDE");
        int textColor        = isDark ? Color.WHITE                 : Color.parseColor("#1A1A1A");
        int labelColor       = isDark ? Color.WHITE                 : Color.parseColor("#888888");  // white in dark
        int hintColor        = isDark ? Color.parseColor("#555555") : Color.parseColor("#AAAAAA");
        int accentColor      = isDark ? Color.parseColor("#4CAF50") : Color.parseColor("#388E3C");  // green
        int dividerColor     = isDark ? Color.parseColor("#2A2A2A") : Color.parseColor("#E8E8E8");

        // ── Page background ───────────────────────────────────────────────
        mainLayout = findViewById(R.id.mainLayout);
        if (mainLayout != null) mainLayout.setBackgroundColor(backgroundColor);

        // ── Header accent bar (stays violet always) ───────────────────────
        android.view.View accentBar = findViewById(R.id.headerAccentBar);
        if (accentBar != null) accentBar.setBackgroundColor(accentColor);

        // ── Dividers ──────────────────────────────────────────────────────
        android.view.View div1 = findViewById(R.id.divider1);
        android.view.View div2 = findViewById(R.id.divider2);
        if (div1 != null) div1.setBackgroundColor(dividerColor);
        if (div2 != null) div2.setBackgroundColor(dividerColor);

        // ── Photo card ────────────────────────────────────────────────────
        android.view.View photoCard = findViewById(R.id.photoCard);
        if (photoCard != null) {
            android.graphics.drawable.GradientDrawable cardBg = new android.graphics.drawable.GradientDrawable();
            cardBg.setColor(cardColor);
            cardBg.setCornerRadius(12 * dp);
            cardBg.setStroke(1, fieldBorderColor);
            photoCard.setBackground(cardBg);
        }

        // ── Photo preview placeholder ─────────────────────────────────────
        if (productPhotoPreview != null) {
            android.graphics.drawable.GradientDrawable previewBg = new android.graphics.drawable.GradientDrawable();
            previewBg.setColor(isDark ? Color.parseColor("#2A2A2A") : Color.parseColor("#F0F0F0"));
            previewBg.setCornerRadius(10 * dp);
            previewBg.setStroke(1, fieldBorderColor);
            productPhotoPreview.setBackground(previewBg);
        }

        // ── Titles and labels ─────────────────────────────────────────────
        if (titleTextView    != null) titleTextView.setTextColor(textColor);
        if (photoLabel       != null) photoLabel.setTextColor(labelColor);
        if (productNameLabel != null) productNameLabel.setTextColor(labelColor);
        if (expiryDateLabel  != null) expiryDateLabel.setTextColor(labelColor);
        if (categoryLabel    != null) categoryLabel.setTextColor(labelColor);
        if (quantityLabel    != null) quantityLabel.setTextColor(labelColor);
        if (notesLabel       != null) notesLabel.setTextColor(labelColor);

        // ── EditText fields ───────────────────────────────────────────────
        android.view.View[] fieldViews = {
                productNameEditText, expiryDateEditText, quantityEditText, notesEditText
        };
        for (android.view.View fv : fieldViews) {
            if (fv != null) {
                android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
                bg.setColor(fieldBgColor);
                bg.setCornerRadius(8 * dp);
                bg.setStroke(1, fieldBorderColor);
                fv.setBackground(bg);
            }
        }
        if (categorySpinner != null) {
            android.graphics.drawable.GradientDrawable spinBg = new android.graphics.drawable.GradientDrawable();
            spinBg.setColor(fieldBgColor);
            spinBg.setCornerRadius(8 * dp);
            spinBg.setStroke(1, fieldBorderColor);
            categorySpinner.setBackground(spinBg);
        }
        // ── Spinner: force selected text color via post (getSelectedView is null at layout time)
        final int spinnerTextColor = textColor;
        if (categorySpinner != null) {
            categorySpinner.post(() -> {
                android.view.View sv = categorySpinner.getSelectedView();
                if (sv instanceof android.widget.TextView) {
                    ((android.widget.TextView) sv).setTextColor(spinnerTextColor);
                }
            });
        }
        // ── Dropdown arrow tint ───────────────────────────────────────────
        android.widget.ImageView dropdownIcon = findViewById(R.id.dropdownIcon);
        if (dropdownIcon != null)
            dropdownIcon.setImageTintList(
                    android.content.res.ColorStateList.valueOf(textColor));

        if (productNameEditText != null) { productNameEditText.setTextColor(textColor); productNameEditText.setHintTextColor(hintColor); }
        if (expiryDateEditText  != null) { expiryDateEditText.setTextColor(textColor);  expiryDateEditText.setHintTextColor(hintColor); }
        if (quantityEditText    != null) { quantityEditText.setTextColor(textColor);    quantityEditText.setHintTextColor(hintColor); }
        if (notesEditText       != null) { notesEditText.setTextColor(textColor);       notesEditText.setHintTextColor(hintColor); }

        // ── Scan button container — VIOLET outlined card ────────────────
        android.view.View scanContainer = findViewById(R.id.scanButtonContainer);
        if (scanContainer != null) {
            android.graphics.drawable.GradientDrawable scanBg = new android.graphics.drawable.GradientDrawable();
            scanBg.setColor(isDark ? Color.argb(25, 98, 0, 238) : Color.argb(12, 98, 0, 238));
            scanBg.setCornerRadius(10 * dp);
            scanBg.setStroke(2, accentColor);
            scanContainer.setBackground(scanBg);
        }
        if (scanProductButton != null) {
            scanProductButton.setTextColor(accentColor);
            scanProductButton.setBackgroundColor(Color.TRANSPARENT);
        }

        // ── Date picker button (plain, no bg) ────────────────────────────
        if (datePickerButton != null) datePickerButton.setBackgroundColor(Color.TRANSPARENT);

        // ── Take Photo + Gallery — VIOLET text only ───────────────────────
        if (takePhotoButton   != null) { takePhotoButton.setTextColor(accentColor);   takePhotoButton.setBackgroundColor(Color.TRANSPARENT); }
        if (choosePhotoButton != null) { choosePhotoButton.setTextColor(accentColor); choosePhotoButton.setBackgroundColor(Color.TRANSPARENT); }

        // ── Remove photo — red text ───────────────────────────────────────
        if (removePhotoButton != null) {
            removePhotoButton.setTextColor(Color.parseColor("#E53935"));
            removePhotoButton.setBackgroundColor(Color.TRANSPARENT);
        }

        // ── Cancel — plain text, no accent ──────────────────────────────
        if (cancelButton != null) {
            cancelButton.setTextColor(textColor);
            cancelButton.setBackgroundColor(Color.TRANSPARENT);
        }

        // ── Save — green background, always white text ──────────────────
        if (saveButton != null) {
            android.graphics.drawable.GradientDrawable saveBg = new android.graphics.drawable.GradientDrawable();
            saveBg.setColor(accentColor);
            saveBg.setCornerRadius(8 * dp);
            saveButton.setBackground(saveBg);
            saveButton.setTextColor(Color.WHITE);
        }
    }

    private void initializeViews() {
        mainLayout = findViewById(R.id.mainLayout);
        productNameEditText = findViewById(R.id.productNameEditText);
        expiryDateEditText = findViewById(R.id.expiryDateEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        quantityEditText = findViewById(R.id.quantityEditText);
        notesEditText = findViewById(R.id.notesEditText);
        datePickerButton = findViewById(R.id.datePickerButton);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        takePhotoButton = findViewById(R.id.takePhotoButton);
        choosePhotoButton = findViewById(R.id.choosePhotoButton);
        removePhotoButton = findViewById(R.id.removePhotoButton);
        // NEW: Initialize scan button
        scanProductButton = findViewById(R.id.btnScanProduct);
        titleTextView = findViewById(R.id.titleTextView);
        productPhotoPreview = findViewById(R.id.productPhotoPreview);

        // Initialize label TextViews
        photoLabel = findViewById(R.id.photoLabel);
        productNameLabel = findViewById(R.id.productNameLabel);
        expiryDateLabel = findViewById(R.id.expiryDateLabel);
        categoryLabel = findViewById(R.id.categoryLabel);
        quantityLabel = findViewById(R.id.quantityLabel);
        notesLabel = findViewById(R.id.notesLabel);

        // Set default date (7 days from now)
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        expiryDateEditText.setText(dateFormat.format(calendar.getTime()));
    }

    private void setupCategorySpinner() {
        // Food-specific categories
        String[] categories = {
                "Select a category",
                "Dairy",
                "Vegetables",
                "Fruits",
                "Meats",
                "Beverages",
                "Medicine",
                "Other"
        };

        // Create custom adapter for spinner with theme support
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        ) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;

                // Get current theme
                User user = userRepository.getUserSync();
                String theme = user != null ? user.getColorTheme() : "white";

                // Set text color based on theme
                if (theme.equals("black")) {
                    textView.setTextColor(Color.WHITE);
                    textView.setBackgroundColor(Color.parseColor("#333333"));
                } else {
                    textView.setTextColor(Color.BLACK);
                    textView.setBackgroundColor(Color.WHITE);
                }
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Set selection listener
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String category = parent.getItemAtPosition(position).toString();

                if (position > 0) {
                    selectedCategory = category;
                } else {
                    selectedCategory = "";
                }

                // Change spinner selected item text color based on theme
                if (view != null && view instanceof TextView) {
                    User user = userRepository.getUserSync();
                    String theme = (user != null && user.getColorTheme() != null)
                            ? user.getColorTheme()
                            : getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("color_theme", "white");

                    if (theme.equals("black")) {
                        ((TextView) view).setTextColor(Color.WHITE);
                    } else {
                        ((TextView) view).setTextColor(Color.BLACK);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategory = "";
            }
        });
    }

    private void setupClickListeners() {
        // Date picker button
        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProduct();
            }
        });

        // Cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Take photo button
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });

        // Choose photo from gallery button
        choosePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        // Remove photo button
        removePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removePhoto();
            }
        });

        // NEW: Scan product button click listener
        scanProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProductScanner();
            }
        });
    }

    // NEW: Method to open scanner
    private void openProductScanner() {
        Intent intent = new Intent(AddProductActivity.this, ProductScannerActivity.class);
        startActivityForResult(intent, SCAN_PRODUCT_REQUEST_CODE);
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
            return;
        }

        // Create a temp file for the full-resolution photo
        File photoFile = createImageFile();
        if (photoFile == null) {
            Toast.makeText(this, "Could not create image file", Toast.LENGTH_SHORT).show();
            return;
        }

        photoUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                photoFile);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(new Date());
            String fileName = "PRODUCT_" + timeStamp;
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(fileName, ".jpg", storageDir);
            productPhotoPath = image.getAbsolutePath();
            return image;
        } catch (IOException e) {
            Log.e(TAG, "Error creating image file: " + e.getMessage());
            return null;
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    private void removePhoto() {
        productPhotoBitmap = null;
        productPhotoPath = null;
        productPhotoPreview.setImageResource(0);
        productPhotoPreview.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        removePhotoButton.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                // Load the full-resolution photo from the file we told the camera to write to
                if (photoUri != null) {
                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 2; // scale down slightly to save memory
                        InputStream inputStream = getContentResolver().openInputStream(photoUri);
                        productPhotoBitmap = BitmapFactory.decodeStream(inputStream, null, options);
                        if (inputStream != null) inputStream.close();

                        if (productPhotoBitmap != null) {
                            productPhotoPreview.setImageBitmap(productPhotoBitmap);
                            removePhotoButton.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(this, "Could not load photo", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error loading camera photo: " + e.getMessage());
                        Toast.makeText(this, "Error loading photo", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (requestCode == GALLERY_REQUEST_CODE && data != null) {
                // Handle gallery photo
                Uri selectedImage = data.getData();
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4;

                    InputStream inputStream = getContentResolver().openInputStream(selectedImage);
                    productPhotoBitmap = BitmapFactory.decodeStream(inputStream, null, options);
                    inputStream.close();

                    if (productPhotoBitmap != null) {
                        productPhotoPreview.setImageBitmap(productPhotoBitmap);
                        removePhotoButton.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(this, "Could not load image", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                }
            }
            // NEW: Handle scanner results
            else if (requestCode == SCAN_PRODUCT_REQUEST_CODE && data != null) {
                handleScanResult(data);
            }
        }
    }

    // Handle scanned data returned from ProductScannerActivity
    private void handleScanResult(Intent data) {
        String barcode     = data.getStringExtra("barcode");
        String productName = data.getStringExtra("product_name");
        String expiryDate  = data.getStringExtra("expiry_date");
        String batchNumber = data.getStringExtra("batch_number");
        String category    = data.getStringExtra("category"); // from Open Food Facts API

        // ── Product name ───────────────────────────────────────────────
        if (productName != null && !productName.isEmpty()) {
            productNameEditText.setText(productName);
        }

        // ── Expiry date ────────────────────────────────────────────────
        if (expiryDate != null && !expiryDate.isEmpty()) {
            try {
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = fmt.parse(expiryDate);
                if (date != null) expiryDateEditText.setText(fmt.format(date));
            } catch (ParseException e) {
                expiryDateEditText.setText(expiryDate);
            }
        }

        // ── Category — auto-select spinner ─────────────────────────────
        if (category != null && !category.isEmpty()) {
            String[] categories = {
                    "Select a category","Dairy","Vegetables","Fruits",
                    "Meats","Beverages","Medicine","Other"
            };
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equalsIgnoreCase(category)) {
                    categorySpinner.setSelection(i);
                    selectedCategory = categories[i];
                    break;
                }
            }
        }

        // ── Barcode → notes ────────────────────────────────────────────
        if (barcode != null && !barcode.isEmpty()) {
            String notes = notesEditText.getText().toString();
            String barcodeNote = "Barcode: " + barcode;
            if (!notes.contains(barcodeNote)) {
                notesEditText.setText(notes.isEmpty() ? barcodeNote : notes + "\n" + barcodeNote);
            }
        }

        // ── Batch → notes ──────────────────────────────────────────────
        if (batchNumber != null && !batchNumber.isEmpty()) {
            String notes = notesEditText.getText().toString();
            String batchNote = "Batch: " + batchNumber;
            if (!notes.contains(batchNote)) {
                notesEditText.setText(notes.isEmpty() ? batchNote : notes + "\n" + batchNote);
            }
        }

        // ── Toast feedback ─────────────────────────────────────────────
        if (productName != null && !productName.isEmpty()) {
            Toast.makeText(this, "✓ Product info auto-filled!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Barcode scanned — please enter product name manually",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Pre-fills the form when AddProductActivity is launched directly from the
     * product list barcode shortcut (extras forwarded from ProductScannerActivity).
     */
    private void prefillFromIntent(Intent intent) {
        if (intent == null) return;
        boolean hasData = intent.hasExtra("barcode")
                || intent.hasExtra("product_name")
                || intent.hasExtra("category")
                || intent.hasExtra("expiry_date")
                || intent.hasExtra("batch_number");
        if (hasData) handleScanResult(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(year, month, dayOfMonth);
                        expiryDateEditText.setText(dateFormat.format(calendar.getTime()));
                    }
                },
                year, month, day
        );

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void saveProduct() {
        // Get values from form
        String productName = productNameEditText.getText().toString().trim();
        String expiryDateStr = expiryDateEditText.getText().toString().trim();
        String category = selectedCategory;
        String quantity = quantityEditText.getText().toString().trim();
        String notes = notesEditText.getText().toString().trim();

        // Validate required fields
        if (TextUtils.isEmpty(productName)) {
            productNameEditText.setError("Product name is required");
            productNameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(expiryDateStr)) {
            expiryDateEditText.setError("Expiry date is required");
            expiryDateEditText.requestFocus();
            return;
        }

        // Validate date format
        Date expiryDate;
        try {
            expiryDate = dateFormat.parse(expiryDateStr);
            if (expiryDate == null) {
                throw new ParseException("Invalid date", 0);
            }
        } catch (ParseException e) {
            expiryDateEditText.setError("Please use YYYY-MM-DD format");
            expiryDateEditText.requestFocus();
            return;
        }

        // Prepare photo byte array
        byte[] photoBytes = null;
        if (productPhotoBitmap != null) {
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                productPhotoBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                photoBytes = stream.toByteArray();
                stream.close();
            } catch (Exception e) {
                Toast.makeText(this, "Error processing photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        // Create product object
        Product product = new Product();
        product.setName(productName);
        product.setExpiryDate(expiryDate);
        product.setCategory(category);
        product.setQuantity(quantity);
        product.setNotes(notes);
        product.setPhoto(photoBytes);

        // Save to database
        productViewModel.insert(product);

        Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show();

        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}