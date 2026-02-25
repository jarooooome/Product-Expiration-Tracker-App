package com.example.productexpirationtrackerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.mlkit.vision.MlKitAnalyzer;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductScannerActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final String TAG = "ProductScanner";

    private PreviewView previewView;
    private TextView tvInstruction;
    private TextView tvBarcodeResult;
    private TextView tvNameResult;
    private TextView tvExpiryResult;
    private TextView tvBatchResult;
    private Button btnCapture;
    private Button btnCancel;
    private View scanningOverlay;

    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;
    private BarcodeScanner barcodeScanner;
    private TextRecognizer textRecognizer;

    private String detectedBarcode = null;
    private String detectedProductName = null;
    private String detectedExpiryDate = null;
    private String detectedBatchNumber = null;

    private boolean isProcessing = false;
    private boolean captureTriggered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_scanner);

        initializeViews();
        setupClickListeners();
        initializeScanners();
        checkCameraPermission();
    }

    private void initializeViews() {
        previewView = findViewById(R.id.previewView);
        tvInstruction = findViewById(R.id.tvInstruction);
        tvBarcodeResult = findViewById(R.id.tvBarcodeResult);
        tvNameResult = findViewById(R.id.tvNameResult);
        tvExpiryResult = findViewById(R.id.tvExpiryResult);
        tvBatchResult = findViewById(R.id.tvBatchResult);
        btnCapture = findViewById(R.id.btnCapture);
        btnCancel = findViewById(R.id.btnCancel);
        scanningOverlay = findViewById(R.id.scanningOverlay);

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void setupClickListeners() {
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (detectedBarcode != null || detectedProductName != null) {
                    returnResults();
                } else {
                    Toast.makeText(ProductScannerActivity.this,
                            "Keep scanning until product is detected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initializeScanners() {
        // Initialize barcode scanner with Philippine-relevant formats
        BarcodeScannerOptions barcodeOptions = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_EAN_13,      // Philippine standard
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_UPC_A,
                        Barcode.FORMAT_UPC_E,
                        Barcode.FORMAT_CODE_128,    // GS1 format (has expiry/lot)
                        Barcode.FORMAT_CODE_39,
                        Barcode.FORMAT_QR_CODE,
                        Barcode.FORMAT_DATA_MATRIX  // Common in medicines
                )
                .build();

        barcodeScanner = BarcodeScanning.getClient(barcodeOptions);
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraPreview();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera: " + e.getMessage());
                Toast.makeText(this, "Error starting camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraPreview() {
        if (cameraProvider == null) return;

        // Create preview use case
        Preview preview = new Preview.Builder()
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Create image analysis use case with ML Kit analyzer
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        // Set up combined analyzer for both barcode and text
        imageAnalysis.setAnalyzer(cameraExecutor,
                new MlKitAnalyzer(
                        Arrays.asList(barcodeScanner, textRecognizer),
                        0,
                        ContextCompat.getMainExecutor(this),
                        result -> {
                            if (captureTriggered || isProcessing) return;

                            // Get barcode results
                            List<Barcode> barcodes = result.getValue(barcodeScanner);
                            if (barcodes != null && !barcodes.isEmpty()) {
                                processBarcodeResult(barcodes.get(0));
                            }

                            // Get text results
                            Text text = result.getValue(textRecognizer);
                            if (text != null) {
                                processTextResult(text);
                            }

                            updateResultDisplay();
                        }
                ));

        // Select back camera
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(
                    (LifecycleOwner) this,
                    cameraSelector,
                    preview,
                    imageAnalysis);
        } catch (Exception e) {
            Log.e(TAG, "Error binding camera: " + e.getMessage());
        }
    }

    private void processBarcodeResult(Barcode barcode) {
        String rawValue = barcode.getRawValue();
        if (rawValue == null || rawValue.isEmpty()) return;

        Log.d(TAG, "Barcode detected: " + rawValue);
        detectedBarcode = rawValue;

        // Check if it's a GS1 barcode (contains expiry/lot)
        parseGS1Barcode(rawValue);
    }

    private void parseGS1Barcode(String rawValue) {
        // Clean the raw value
        String cleanData = rawValue.replaceAll("^\\]C1", "")
                .replaceAll("[()]", "");

        // Look for Application Identifiers
        // Pattern for (01) GTIN, (17) Expiry, (10) Lot
        Pattern pattern = Pattern.compile("\\((\\d{2})\\)([^\\(]+)");
        Matcher matcher = pattern.matcher(rawValue);

        while (matcher.find()) {
            String ai = matcher.group(1);
            String value = matcher.group(2).trim();

            switch (ai) {
                case "01": // GTIN
                    if (detectedBarcode == null) {
                        detectedBarcode = value;
                    }
                    break;
                case "17": // Expiration Date
                case "15": // Best Before Date
                    detectedExpiryDate = parseGS1Date(value);
                    break;
                case "10": // Batch/Lot Number
                    detectedBatchNumber = value;
                    break;
            }
        }

        // If no parentheses format, try direct parsing
        if (detectedExpiryDate == null && cleanData.length() >= 16) {
            // Sometimes format is: 01[GTIN]17[EXPIRY]10[LOT]
            if (cleanData.startsWith("01") && cleanData.length() >= 24) {
                // Extract GTIN (14 digits after 01)
                if (detectedBarcode == null && cleanData.length() >= 16) {
                    detectedBarcode = cleanData.substring(2, 16);
                }
                // Look for 17 (expiry) after GTIN
                int expiryIndex = cleanData.indexOf("17", 16);
                if (expiryIndex > 0 && expiryIndex + 8 <= cleanData.length()) {
                    String expiryStr = cleanData.substring(expiryIndex + 2, expiryIndex + 8);
                    detectedExpiryDate = parseGS1Date(expiryStr);
                }
                // Look for 10 (lot) after expiry
                int lotIndex = cleanData.indexOf("10", expiryIndex + 8);
                if (lotIndex > 0 && lotIndex + 2 < cleanData.length()) {
                    detectedBatchNumber = cleanData.substring(lotIndex + 2);
                }
            }
        }
    }

    private String parseGS1Date(String gs1Date) {
        if (gs1Date == null || gs1Date.length() < 6) return null;

        try {
            // GS1 dates are YYMMDD format
            String year = "20" + gs1Date.substring(0, 2);
            String month = gs1Date.substring(2, 4);
            String day = gs1Date.substring(4, 6);
            return year + "-" + month + "-" + day;
        } catch (Exception e) {
            return null;
        }
    }

    private void processTextResult(Text text) {
        String fullText = text.getText();
        if (fullText == null || fullText.isEmpty()) return;

        Log.d(TAG, "Text detected: " + fullText);

        // Try to find product name (usually largest text block)
        findProductName(text);

        // Try to find expiry date in text
        findExpiryDate(fullText);

        // Try to find batch/lot number in text
        findBatchNumber(fullText);
    }

    private void findProductName(Text text) {
        // Find the largest text block - often the product name
        Text.TextBlock largestBlock = null;
        float largestArea = 0;

        for (Text.TextBlock block : text.getTextBlocks()) {
            Rect boundingBox = block.getBoundingBox();
            if (boundingBox != null) {
                float area = boundingBox.width() * boundingBox.height();
                // Also check if it's near the top (often where product name is)
                if (area > largestArea && boundingBox.top < 500) {
                    largestArea = area;
                    largestBlock = block;
                }
            }
        }

        if (largestBlock != null) {
            String possibleName = largestBlock.getText();
            // Avoid capturing expiry-related text as name
            if (!possibleName.toUpperCase().contains("EXP") &&
                    !possibleName.toUpperCase().contains("BEST") &&
                    !possibleName.toUpperCase().contains("USE BY") &&
                    !possibleName.toUpperCase().contains("LOT") &&
                    !possibleName.toUpperCase().contains("BATCH")) {
                detectedProductName = possibleName;
            }
        }
    }

    private void findExpiryDate(String text) {
        // Common expiry date patterns
        String[] patterns = {
                "(?:EXP|EXPIRY|EXPIRES|BEST BEFORE|BEST BY|USE BY)[:\\s]*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})",
                "(?:EXP|EXPIRY|EXPIRES|BEST BEFORE|BEST BY|USE BY)[:\\s]*(\\d{2,4}[/-]\\d{1,2}[/-]\\d{1,2})",
                "(\\d{2}[/-]\\d{2}[/-]\\d{4})",  // DD-MM-YYYY or MM-DD-YYYY
                "(\\d{4}[/-]\\d{2}[/-]\\d{2})",  // YYYY-MM-DD
                "(\\d{2}[/-]\\d{2}[/-]\\d{2})"   // DD-MM-YY or MM-DD-YY
        };

        for (String patternStr : patterns) {
            Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String dateStr = matcher.group(1);
                // Try to normalize to YYYY-MM-DD
                try {
                    String normalized = normalizeDate(dateStr);
                    if (normalized != null) {
                        detectedExpiryDate = normalized;
                        return;
                    }
                } catch (Exception e) {
                    // Keep trying other patterns
                }
            }
        }
    }

    private String normalizeDate(String dateStr) {
        // Try various formats and convert to YYYY-MM-DD
        String[] formats = {
                "dd-MM-yyyy", "MM-dd-yyyy", "yyyy-MM-dd",
                "dd/MM/yyyy", "MM/dd/yyyy", "yyyy/MM/dd",
                "dd-MM-yy", "MM-dd-yy", "yy-MM-dd"
        };

        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                sdf.setLenient(false);
                Date date = sdf.parse(dateStr);
                if (date != null) {
                    SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    return output.format(date);
                }
            } catch (ParseException e) {
                // Try next format
            }
        }
        return dateStr; // Return original if can't parse
    }

    private void findBatchNumber(String text) {
        // Look for batch/lot patterns
        Pattern batchPattern = Pattern.compile(
                "(?:LOT|BATCH|LOT NO|BATCH NO)[:\\s]*([A-Z0-9]{3,15})",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = batchPattern.matcher(text);
        if (matcher.find()) {
            detectedBatchNumber = matcher.group(1);
        }
    }

    private void updateResultDisplay() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (detectedBarcode != null) {
                    tvBarcodeResult.setText("📋 Barcode: " + detectedBarcode);
                    tvBarcodeResult.setVisibility(View.VISIBLE);
                } else {
                    tvBarcodeResult.setVisibility(View.GONE);
                }

                if (detectedProductName != null) {
                    tvNameResult.setText("📦 Product: " + detectedProductName);
                    tvNameResult.setVisibility(View.VISIBLE);
                } else {
                    tvNameResult.setVisibility(View.GONE);
                }

                if (detectedExpiryDate != null) {
                    tvExpiryResult.setText("⏰ Expiry: " + detectedExpiryDate);
                    tvExpiryResult.setVisibility(View.VISIBLE);
                } else {
                    tvExpiryResult.setVisibility(View.GONE);
                }

                if (detectedBatchNumber != null) {
                    tvBatchResult.setText("🔢 Batch: " + detectedBatchNumber);
                    tvBatchResult.setVisibility(View.VISIBLE);
                } else {
                    tvBatchResult.setVisibility(View.GONE);
                }

                // Enable capture button if we have at least barcode or name
                btnCapture.setEnabled(detectedBarcode != null || detectedProductName != null);
                if (btnCapture.isEnabled()) {
                    btnCapture.setAlpha(1.0f);
                } else {
                    btnCapture.setAlpha(0.5f);
                }

                // Update instruction
                if (detectedBarcode == null && detectedProductName == null) {
                    tvInstruction.setText("📷 Point camera at product label");
                } else {
                    tvInstruction.setText("✅ Product detected! Press CAPTURE");
                }
            }
        });
    }

    private void returnResults() {
        Intent resultIntent = new Intent();
        if (detectedBarcode != null) {
            resultIntent.putExtra("barcode", detectedBarcode);
        }
        if (detectedProductName != null) {
            resultIntent.putExtra("product_name", detectedProductName);
        }
        if (detectedExpiryDate != null) {
            resultIntent.putExtra("expiry_date", detectedExpiryDate);
        }
        if (detectedBatchNumber != null) {
            resultIntent.putExtra("batch_number", detectedBatchNumber);
        }

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        if (barcodeScanner != null) {
            barcodeScanner.close();
        }
    }
}