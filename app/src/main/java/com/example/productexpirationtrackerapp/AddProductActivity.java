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
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
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
    private ImageView productPhotoPreview;

    private ProductViewModel productViewModel;
    private UserRepository userRepository;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 101;
    private static final int CAMERA_PERMISSION_CODE = 102;

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

            // Apply theme using ThemeUtils
            ThemeUtils.applyTheme(this, theme);

            // Apply additional custom theme colors
            applyCustomThemeColors(theme);
        } else {
            // Fallback to default theme
            ThemeUtils.applyTheme(this, "white");
        }
    }

    private void applyCustomThemeColors(String theme) {
        int primaryColor;
        int textColor;
        int backgroundColor;

        // Get colors based on theme
        switch (theme) {
            case "green":
                primaryColor = getResources().getColor(R.color.color_primary_green);
                textColor = getResources().getColor(R.color.color_text_green);
                backgroundColor = getResources().getColor(R.color.color_background_green);
                break;
            case "blue":
                primaryColor = getResources().getColor(R.color.color_primary_blue);
                textColor = getResources().getColor(R.color.color_text_blue);
                backgroundColor = getResources().getColor(R.color.color_background_blue);
                break;
            case "pink":
                primaryColor = getResources().getColor(R.color.color_primary_pink);
                textColor = getResources().getColor(R.color.color_text_pink);
                backgroundColor = getResources().getColor(R.color.color_background_pink);
                break;
            case "purple":
                primaryColor = getResources().getColor(R.color.color_primary_purple);
                textColor = getResources().getColor(R.color.color_text_purple);
                backgroundColor = getResources().getColor(R.color.color_background_purple);
                break;
            case "black":
                primaryColor = getResources().getColor(R.color.color_primary_black);
                textColor = getResources().getColor(R.color.color_text_black);
                backgroundColor = getResources().getColor(R.color.color_background_black);
                break;
            case "white":
            default:
                primaryColor = getResources().getColor(R.color.color_primary_white);
                textColor = getResources().getColor(R.color.color_text_white);
                backgroundColor = getResources().getColor(R.color.color_background_white);
                break;
        }

        // Apply colors to views if they exist
        if (titleTextView != null) {
            titleTextView.setTextColor(textColor);
        }

        // Apply button background colors
        if (saveButton != null) {
            saveButton.setBackgroundColor(primaryColor);
        }

        if (cancelButton != null) {
            cancelButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        }

        // Apply colors to photo buttons
        if (takePhotoButton != null) {
            takePhotoButton.setBackgroundColor(primaryColor);
        }

        if (choosePhotoButton != null) {
            choosePhotoButton.setBackgroundColor(primaryColor);
        }

        if (removePhotoButton != null) {
            removePhotoButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    private void initializeViews() {
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

        // Set default date (7 days from now) - Fixed initialization
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        expiryDateEditText.setText(dateFormat.format(calendar.getTime()));
    }

    private void setupCategorySpinner() {
        // Create an ArrayAdapter using the string array and default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.product_categories, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        categorySpinner.setAdapter(adapter);

        // Set selection listener
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get selected category
                String category = parent.getItemAtPosition(position).toString();

                // Only set selectedCategory if it's not the default "Select Category" option
                if (position > 0) {
                    selectedCategory = category;
                } else {
                    selectedCategory = "";
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
                // Handle gallery photo with better error handling
                Uri selectedImage = data.getData();
                try {
                    // Load image with options to prevent memory issues
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4; // Reduce image size by 4x

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
                // Convert bitmap to byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Compress the bitmap (70% quality to save space)
                productPhotoBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                photoBytes = stream.toByteArray();
                stream.close();
            } catch (Exception e) {
                Toast.makeText(this, "Error processing photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        // Create product object using empty constructor and set all fields
        Product product = new Product();
        product.setName(productName);
        product.setExpiryDate(expiryDate);
        product.setCategory(category);
        product.setQuantity(quantity);
        product.setNotes(notes);
        product.setPhoto(photoBytes);

        // Save to database
        productViewModel.insert(product);

        // Show success message
        Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show();

        // Return to ProductListActivity
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}