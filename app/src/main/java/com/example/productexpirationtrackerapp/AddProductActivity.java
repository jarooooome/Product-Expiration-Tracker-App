package com.example.productexpirationtrackerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
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
    private static final String TAG = "AddProductTheme";

    private Bitmap productPhotoBitmap;
    private String productPhotoPath;
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reapply theme when activity resumes
        applyThemeFromDatabase();
    }

    private void applyThemeFromDatabase() {
        // Get user from database to get theme
        User user = userRepository.getUserSync();

        if (user != null) {
            String theme = user.getColorTheme();
            Log.d(TAG, "Applying theme: " + theme);
            applyCustomThemeColors(theme);
        } else {
            // Fallback to SharedPreferences
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            String theme = prefs.getString("color_theme", "light");
            Log.d(TAG, "No user found, applying theme: " + theme);
            applyCustomThemeColors(theme);
        }
    }

    private void applyCustomThemeColors(String theme) {
        boolean isDarkTheme = theme.equals("dark") || theme.equals("black");

        int primaryColor;
        int textColor;
        int backgroundColor;
        int hintColor;

        if (isDarkTheme) {
            // Dark theme colors
            primaryColor = ContextCompat.getColor(this, R.color.color_primary_dark);
            textColor = ContextCompat.getColor(this, R.color.color_text_dark);
            backgroundColor = ContextCompat.getColor(this, R.color.color_background_dark);
            hintColor = Color.parseColor("#80FFFFFF"); // Semi-transparent white
        } else {
            // Light theme colors
            primaryColor = ContextCompat.getColor(this, R.color.color_primary_light);
            textColor = ContextCompat.getColor(this, R.color.color_text_light);
            backgroundColor = ContextCompat.getColor(this, R.color.color_background_light);
            hintColor = Color.parseColor("#80000000"); // Semi-transparent black
        }

        Log.d(TAG, "Theme: " + (isDarkTheme ? "DARK" : "LIGHT"));

        // Apply background color to main layout
        mainLayout = findViewById(R.id.mainLayout);
        if (mainLayout != null) {
            mainLayout.setBackgroundColor(backgroundColor);
        }

        // Set text colors for labels
        if (titleTextView != null) {
            titleTextView.setTextColor(textColor);
        }
        if (photoLabel != null) {
            photoLabel.setTextColor(textColor);
        }
        if (productNameLabel != null) {
            productNameLabel.setTextColor(textColor);
        }
        if (expiryDateLabel != null) {
            expiryDateLabel.setTextColor(textColor);
        }
        if (categoryLabel != null) {
            categoryLabel.setTextColor(textColor);
        }
        if (quantityLabel != null) {
            quantityLabel.setTextColor(textColor);
        }
        if (notesLabel != null) {
            notesLabel.setTextColor(textColor);
        }

        // Set text colors for EditText fields
        if (productNameEditText != null) {
            productNameEditText.setTextColor(textColor);
            productNameEditText.setHintTextColor(hintColor);
        }
        if (expiryDateEditText != null) {
            expiryDateEditText.setTextColor(textColor);
            expiryDateEditText.setHintTextColor(hintColor);
        }
        if (quantityEditText != null) {
            quantityEditText.setTextColor(textColor);
            quantityEditText.setHintTextColor(hintColor);
        }
        if (notesEditText != null) {
            notesEditText.setTextColor(textColor);
            notesEditText.setHintTextColor(hintColor);
        }

        // Apply button background colors
        if (saveButton != null) {
            saveButton.setBackgroundColor(primaryColor);
            saveButton.setTextColor(Color.WHITE);
        }

        if (cancelButton != null) {
            cancelButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            cancelButton.setTextColor(Color.WHITE);
        }

        // Apply colors to photo buttons
        if (takePhotoButton != null) {
            takePhotoButton.setBackgroundColor(primaryColor);
            takePhotoButton.setTextColor(Color.WHITE);
        }

        if (choosePhotoButton != null) {
            choosePhotoButton.setBackgroundColor(primaryColor);
            choosePhotoButton.setTextColor(Color.WHITE);
        }

        if (removePhotoButton != null) {
            removePhotoButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            removePhotoButton.setTextColor(Color.WHITE);
        }

        // Set date picker button color
        if (datePickerButton != null) {
            datePickerButton.setBackgroundColor(primaryColor);
            datePickerButton.setTextColor(Color.WHITE);
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
                    boolean isDarkTheme = user != null ?
                            (user.getColorTheme().equals("dark") || user.getColorTheme().equals("black")) : false;

                    if (isDarkTheme) {
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
    }

    private void openCamera() {
        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        } else {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            } else {
                Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
            }
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
            if (requestCode == CAMERA_REQUEST_CODE && data != null) {
                // Handle camera photo
                try {
                    productPhotoBitmap = (Bitmap) data.getExtras().get("data");
                    if (productPhotoBitmap != null) {
                        productPhotoPreview.setImageBitmap(productPhotoBitmap);
                        removePhotoButton.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Error loading camera photo", Toast.LENGTH_SHORT).show();
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
        }
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