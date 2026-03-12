package com.example.productexpirationtrackerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.mlkit.vision.MlKitAnalyzer;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductScannerActivity extends AppCompatActivity {

    private static final int    CAMERA_PERMISSION_CODE  = 100;
    private static final String TAG                     = "ProductScanner";
    private static final long   ANALYSIS_THROTTLE_MS    = 400;
    private static final long   AUTO_CAPTURE_DELAY_MS   = 1500;
    private static final long   LOOKUP_GRACE_MS         = 2000; // extra wait if lookup running

    // ── Views ─────────────────────────────────────────────────────────────
    private PreviewView  previewView;
    private View         laserLine;
    private TextView     tvInstruction;
    private TextView     tvBarcodeResult;
    private TextView     tvNameResult;
    private TextView     tvExpiryResult;
    private TextView     tvBatchResult;
    private LinearLayout detectedCard;
    private ImageView    btnCapture;
    private ImageView    btnBack;
    private ImageView    btnFlash;
    private ImageView    btnFlipCamera;
    private ScanFrameView scanFrameView;
    private ProgressBar  lookupProgress;
    private TextView     tvLookupStatus;

    // ── Camera ────────────────────────────────────────────────────────────
    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;
    private androidx.camera.core.Camera camera;
    private boolean isFlashOn = false;
    private int     lensFacing = CameraSelector.LENS_FACING_BACK;

    // ── ML Kit ────────────────────────────────────────────────────────────
    private BarcodeScanner barcodeScanner;

    // ── Scan results ──────────────────────────────────────────────────────
    private String detectedBarcode     = null;
    private String detectedExpiryDate  = null; // from GS1 barcode data
    private String detectedBatchNumber = null; // from GS1 barcode data

    // ── API fetch results ─────────────────────────────────────────────────
    private String fetchedProductName = null;
    private String fetchedCategory    = null;
    private String fetchedBrand       = null;

    // ── State flags ───────────────────────────────────────────────────────
    private long    lastAnalysisTime       = 0;
    private long    firstBarcodeDetectedAt = -1;
    private boolean autoCaptured           = false;
    private boolean barcodeConfirmed       = false;
    private boolean isLookingUp            = false;
    private boolean lookupComplete         = false;

    // ── Threading ─────────────────────────────────────────────────────────
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler     = new Handler(Looper.getMainLooper());
    private ObjectAnimator        laserAnimator;

    // ══════════════════════════════════════════════════════════════════════
    // LIFECYCLE
    // ══════════════════════════════════════════════════════════════════════

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setContentView(R.layout.activity_product_scanner);

        initializeViews();
        setupClickListeners();
        setupTapToFocus();
        initializeScanners();
        startLaserAnimation();
        checkCameraPermission();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFlashOn && camera != null) camera.getCameraControl().enableTorch(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFlashOn && camera != null) camera.getCameraControl().enableTorch(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (laserAnimator  != null) laserAnimator.cancel();
        if (cameraExecutor != null) cameraExecutor.shutdown();
        if (networkExecutor!= null) networkExecutor.shutdown();
        if (barcodeScanner != null) barcodeScanner.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                startCamera();
            else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // INIT
    // ══════════════════════════════════════════════════════════════════════

    private void initializeViews() {
        previewView     = findViewById(R.id.previewView);
        laserLine       = findViewById(R.id.laserLine);
        tvInstruction   = findViewById(R.id.tvInstruction);
        tvBarcodeResult = findViewById(R.id.tvBarcodeResult);
        tvNameResult    = findViewById(R.id.tvNameResult);
        tvExpiryResult  = findViewById(R.id.tvExpiryResult);
        tvBatchResult   = findViewById(R.id.tvBatchResult);
        detectedCard    = findViewById(R.id.detectedCard);
        btnCapture      = findViewById(R.id.btnCapture);
        btnBack         = findViewById(R.id.btnBack);
        btnFlash        = findViewById(R.id.btnFlash);
        btnFlipCamera   = findViewById(R.id.btnFlipCamera);
        scanFrameView   = findViewById(R.id.scanFrameView);
        lookupProgress  = findViewById(R.id.lookupProgress);
        tvLookupStatus  = findViewById(R.id.tvLookupStatus);
        cameraExecutor  = Executors.newSingleThreadExecutor();
    }

    private void initializeScanners() {
        barcodeScanner = BarcodeScanning.getClient(
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                                Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8,
                                Barcode.FORMAT_UPC_A,  Barcode.FORMAT_UPC_E,
                                Barcode.FORMAT_CODE_128, Barcode.FORMAT_CODE_39,
                                Barcode.FORMAT_QR_CODE,  Barcode.FORMAT_DATA_MATRIX)
                        .build());
    }

    // ══════════════════════════════════════════════════════════════════════
    // CLICK + TOUCH
    // ══════════════════════════════════════════════════════════════════════

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnFlash.setOnClickListener(v -> {
            isFlashOn = !isFlashOn;
            if (camera != null) camera.getCameraControl().enableTorch(isFlashOn);
            btnFlash.setImageResource(isFlashOn ? R.drawable.ic_flash_on : R.drawable.ic_flash_off);
            btnFlash.setColorFilter(isFlashOn ? Color.parseColor("#FFD600") : Color.WHITE);
        });

        btnFlipCamera.setOnClickListener(v -> {
            lensFacing = (lensFacing == CameraSelector.LENS_FACING_BACK)
                    ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK;
            if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                isFlashOn = false;
                btnFlash.setImageResource(R.drawable.ic_flash_off);
                btnFlash.setColorFilter(Color.WHITE);
            }
            resetScanState();
            bindCameraPreview();
        });

        btnCapture.setOnClickListener(v -> {
            if (detectedBarcode == null) {
                showBarcodeOnlyWarning();
                return;
            }
            if (isLookingUp) {
                Toast.makeText(this, "Fetching product info, please wait...",
                        Toast.LENGTH_SHORT).show();
            } else {
                returnResults();
            }
        });
    }

    private void setupTapToFocus() {
        // intentionally empty — focus handled in dispatchTouchEvent below
        // so it works regardless of which view consumes the touch
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // Tap-to-focus: fires before any child view gets the event
        if (event.getAction() == MotionEvent.ACTION_UP && camera != null) {
            // Only focus when tapping the camera preview area (not buttons)
            float y = event.getY();
            int screenHeight = getWindow().getDecorView().getHeight();
            boolean inBottomPanel = y > screenHeight * 0.75f;
            boolean inTopBar = y < screenHeight * 0.18f;
            if (!inBottomPanel && !inTopBar) {
                MeteringPointFactory factory = previewView.getMeteringPointFactory();
                MeteringPoint point = factory.createPoint(event.getX(), event.getY());
                FocusMeteringAction action = new FocusMeteringAction.Builder(point)
                        .setAutoCancelDuration(3, TimeUnit.SECONDS).build();
                camera.getCameraControl().startFocusAndMetering(action);

                View ring = findViewById(R.id.focusRing);
                if (ring != null) {
                    ring.setX(event.getX() - ring.getWidth() / 2f);
                    ring.setY(event.getY() - ring.getHeight() / 2f);
                    ring.setAlpha(1f);
                    ring.setVisibility(View.VISIBLE);
                    ring.animate().alpha(0f).setDuration(700)
                            .withEndAction(() -> ring.setVisibility(View.GONE)).start();
                }
            }
        }
        return super.dispatchTouchEvent(event); // always pass through to child views
    }

    // ══════════════════════════════════════════════════════════════════════
    // CAMERA
    // ══════════════════════════════════════════════════════════════════════

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)
            startCamera();
        else
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                cameraProvider = future.get();
                bindCameraPreview();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera start error: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(this, "Camera error", Toast.LENGTH_SHORT).show());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraPreview() {
        if (cameraProvider == null) return;

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor,
                new MlKitAnalyzer(
                        Collections.singletonList(barcodeScanner),
                        ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
                        ContextCompat.getMainExecutor(this),
                        result -> {
                            long now = System.currentTimeMillis();
                            if (now - lastAnalysisTime < ANALYSIS_THROTTLE_MS) return;
                            lastAnalysisTime = now;
                            if (autoCaptured) return;

                            List<Barcode> barcodes = result.getValue(barcodeScanner);
                            boolean barcodeFound = barcodes != null && !barcodes.isEmpty();

                            if (barcodeFound) {
                                processBarcodeResult(barcodes.get(0));
                                barcodeConfirmed = true;

                                // Kick off API lookup once
                                if (!isLookingUp && !lookupComplete && detectedBarcode != null)
                                    lookupProductInfo(detectedBarcode);

                                // Auto-capture countdown
                                if (firstBarcodeDetectedAt < 0) {
                                    firstBarcodeDetectedAt = now;
                                } else {
                                    long held = now - firstBarcodeDetectedAt;
                                    if (held >= AUTO_CAPTURE_DELAY_MS) {
                                        if (!isLookingUp || held >= AUTO_CAPTURE_DELAY_MS + LOOKUP_GRACE_MS) {
                                            autoCaptured = true;
                                            runOnUiThread(this::returnResults);
                                            return;
                                        }
                                    }
                                }
                            } else {
                                firstBarcodeDetectedAt = -1;
                                barcodeConfirmed = false;
                            }

                            updateResultDisplay();
                        }
                ));

        try {
            cameraProvider.unbindAll();
            camera = cameraProvider.bindToLifecycle(
                    (LifecycleOwner) this,
                    new CameraSelector.Builder().requireLensFacing(lensFacing).build(),
                    preview, imageAnalysis);
        } catch (Exception e) {
            Log.e(TAG, "Camera bind error: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // BARCODE PROCESSING
    // ══════════════════════════════════════════════════════════════════════

    private void processBarcodeResult(Barcode barcode) {
        String raw = barcode.getRawValue();
        if (raw == null || raw.isEmpty() || detectedBarcode != null) return;
        Log.d(TAG, "Barcode: " + raw);
        detectedBarcode = raw;
        parseGS1Barcode(raw);
    }

    private void parseGS1Barcode(String raw) {
        String clean = raw.replaceAll("^\\]C1", "").replaceAll("[()]", "");
        Matcher m = Pattern.compile("\\((\\d{2})\\)([^(]+)").matcher(raw);
        while (m.find()) {
            String ai = m.group(1), val = m.group(2).trim();
            if (("17".equals(ai) || "15".equals(ai)) && detectedExpiryDate == null)
                detectedExpiryDate = parseGS1Date(val);
            if ("10".equals(ai) && detectedBatchNumber == null)
                detectedBatchNumber = val;
        }
        if (detectedExpiryDate == null && clean.startsWith("01") && clean.length() >= 24) {
            int ei = clean.indexOf("17", 16);
            if (ei > 0 && ei + 8 <= clean.length())
                detectedExpiryDate = parseGS1Date(clean.substring(ei + 2, ei + 8));
            if (ei > 0) {
                int li = clean.indexOf("10", ei + 8);
                if (li > 0 && li + 2 < clean.length()) detectedBatchNumber = clean.substring(li + 2);
            }
        }
    }

    private String parseGS1Date(String d) {
        if (d == null || d.length() < 6) return null;
        try {
            String day = d.substring(4, 6);
            if ("00".equals(day)) day = "01";
            return "20" + d.substring(0, 2) + "-" + d.substring(2, 4) + "-" + day;
        } catch (Exception e) { return null; }
    }

    // ══════════════════════════════════════════════════════════════════════
    // OPEN FOOD FACTS API
    // ══════════════════════════════════════════════════════════════════════

    /**
     * HYBRID lookup:
     *   - Online  → queries Open Food Facts API for name, brand, category
     *   - Offline → skips API silently; barcode + any GS1 data still captured
     */
    private void lookupProductInfo(String barcode) {
        isLookingUp = true;

        // ── Check connectivity first ──────────────────────────────────
        boolean online = isNetworkAvailable();

        runOnUiThread(() -> {
            if (tvLookupStatus != null) {
                if (online) {
                    if (lookupProgress != null) lookupProgress.setVisibility(View.VISIBLE);
                    tvLookupStatus.setText("Looking up product...");
                    tvLookupStatus.setTextColor(Color.parseColor("#AAAAAA"));
                } else {
                    // Offline — tell user name needs to be typed
                    tvLookupStatus.setText("Offline — enter product name manually");
                    tvLookupStatus.setTextColor(Color.parseColor("#FF9800")); // orange
                }
                tvLookupStatus.setVisibility(View.VISIBLE);
            }
        });

        if (!online) {
            // Skip API call entirely — mark lookup as done so auto-capture proceeds
            isLookingUp   = false;
            lookupComplete = true;
            return;
        }

        // ── Online path: query Open Food Facts ────────────────────────
        networkExecutor.execute(() -> {
            String name = null, category = null, brand = null;
            try {
                URL url = new URL("https://world.openfoodfacts.org/api/v0/product/"
                        + barcode + ".json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("User-Agent",
                        "ProductExpirationTrackerApp/1.0 (Android)");

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    br.close();

                    JSONObject json = new JSONObject(sb.toString());
                    if (json.optInt("status", 0) == 1) {
                        JSONObject p = json.getJSONObject("product");
                        name = p.optString("product_name_en", "");
                        if (name.isEmpty()) name = p.optString("product_name", "");
                        if (name.isEmpty()) name = null;

                        brand = p.optString("brands", "");
                        if (brand.isEmpty()) brand = null;

                        String cats = p.optString("categories", "");
                        if (!cats.isEmpty()) category = mapToAppCategory(cats);
                    }
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.w(TAG, "Lookup failed: " + e.getMessage());
                // Network dropped mid-request — treat as offline gracefully
            }

            final String fn = name, fc = category, fb = brand;
            mainHandler.post(() -> {
                fetchedProductName = fn;
                fetchedCategory    = fc;
                fetchedBrand       = fb;
                isLookingUp        = false;
                lookupComplete     = true;
                if (lookupProgress != null) lookupProgress.setVisibility(View.GONE);
                if (tvLookupStatus != null) {
                    if (fn != null) {
                        tvLookupStatus.setText("✓ Product found!");
                        tvLookupStatus.setTextColor(Color.parseColor("#4CAF50"));
                    } else {
                        tvLookupStatus.setText("Not in database — enter name manually");
                        tvLookupStatus.setTextColor(Color.parseColor("#AAAAAA"));
                    }
                }
                updateResultDisplay();
            });
        });
    }

    /** Returns true if device has an active network connection */
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    /** Maps Open Food Facts category tags → app spinner values */
    private String mapToAppCategory(String cats) {
        String l = cats.toLowerCase(Locale.US);
        if (l.contains("dairy") || l.contains("milk") || l.contains("cheese")
                || l.contains("yogurt") || l.contains("butter"))      return "Dairy";
        if (l.contains("vegetable") || l.contains("veggie"))           return "Vegetables";
        if (l.contains("fruit") || l.contains("juice"))                return "Fruits";
        if (l.contains("meat") || l.contains("beef") || l.contains("pork")
                || l.contains("chicken") || l.contains("fish"))        return "Meats";
        if (l.contains("beverage") || l.contains("drink") || l.contains("water")
                || l.contains("soda") || l.contains("coffee") || l.contains("tea")) return "Beverages";
        if (l.contains("medicine") || l.contains("drug") || l.contains("supplement")
                || l.contains("vitamin"))                              return "Medicine";
        return "Other";
    }

    // ══════════════════════════════════════════════════════════════════════
    // UI
    // ══════════════════════════════════════════════════════════════════════

    private void updateResultDisplay() {
        runOnUiThread(() -> {
            boolean hasBarcode = detectedBarcode != null;
            detectedCard.setVisibility(hasBarcode ? View.VISIBLE : View.GONE);

            if (hasBarcode) {
                tvBarcodeResult.setText("Barcode: " + detectedBarcode);
                tvBarcodeResult.setVisibility(View.VISIBLE);
            }
            if (fetchedProductName != null) {
                String display = fetchedBrand != null
                        ? fetchedBrand + " – " + fetchedProductName : fetchedProductName;
                tvNameResult.setText("Product: " + display);
                tvNameResult.setTextColor(Color.WHITE);
                tvNameResult.setVisibility(View.VISIBLE);
            } else if (hasBarcode && lookupComplete) {
                tvNameResult.setText("Product: not found — enter manually");
                tvNameResult.setTextColor(Color.parseColor("#AAAAAA"));
                tvNameResult.setVisibility(View.VISIBLE);
            }
            if (fetchedCategory != null) {
                tvBatchResult.setText("Category: " + fetchedCategory);
                tvBatchResult.setVisibility(View.VISIBLE);
            }

            if (hasBarcode) {
                tvInstruction.setText(isLookingUp
                        ? "Barcode found — fetching product info..."
                        : "✓ Ready — hold steady to auto-capture");
                tvInstruction.setTextColor(Color.parseColor("#4CAF50"));
            } else {
                tvInstruction.setText("Point camera at a barcode");
                tvInstruction.setTextColor(0xCCFFFFFF);
            }

            btnCapture.setEnabled(hasBarcode);
            btnCapture.setAlpha(hasBarcode ? 1.0f : 0.45f);
            if (hasBarcode) {
                btnCapture.animate().scaleX(1.08f).scaleY(1.08f).setDuration(180)
                        .withEndAction(() -> btnCapture.animate()
                                .scaleX(1f).scaleY(1f).setDuration(180).start()).start();
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    // RESULTS → AddProductActivity
    // ══════════════════════════════════════════════════════════════════════

    private void returnResults() {
        if (laserAnimator != null) laserAnimator.cancel();
        Intent result = new Intent();
        if (detectedBarcode    != null) result.putExtra("barcode",      detectedBarcode);
        if (detectedExpiryDate != null) result.putExtra("expiry_date",  detectedExpiryDate);
        if (detectedBatchNumber!= null) result.putExtra("batch_number", detectedBatchNumber);
        if (fetchedProductName != null) {
            String full = (fetchedBrand != null && !fetchedBrand.isEmpty())
                    ? fetchedBrand + " " + fetchedProductName : fetchedProductName;
            result.putExtra("product_name", full);
        }
        if (fetchedCategory != null) result.putExtra("category", fetchedCategory);
        setResult(RESULT_OK, result);
        finish();
    }

    // ══════════════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════════════

    private void showBarcodeOnlyWarning() {
        if (scanFrameView != null)
            ObjectAnimator.ofFloat(scanFrameView, "translationX",
                    0f, -18f, 18f, -12f, 12f, -6f, 6f, 0f).setDuration(400).start();
        new AlertDialog.Builder(this)
                .setTitle("Barcodes Only")
                .setMessage("This scanner only reads product barcodes.\n\n"
                        + "Point the camera directly at the barcode on the product label and hold steady.")
                .setPositiveButton("Got it", null).show();
        resetScanState();
        updateResultDisplay();
    }

    private void resetScanState() {
        detectedBarcode = null; detectedExpiryDate = null; detectedBatchNumber = null;
        fetchedProductName = null; fetchedCategory = null; fetchedBrand = null;
        barcodeConfirmed = false; firstBarcodeDetectedAt = -1;
        autoCaptured = false; isLookingUp = false; lookupComplete = false;
        if (lookupProgress != null) lookupProgress.setVisibility(View.GONE);
        if (tvLookupStatus != null) tvLookupStatus.setVisibility(View.GONE);
    }

    private void startLaserAnimation() {
        if (laserLine == null) return;
        laserLine.post(() -> {
            float dp = getResources().getDisplayMetrics().density;
            float fh = 200f * dp;
            float ft = (previewView.getHeight() - fh) / 2f - 60f * dp;
            laserAnimator = ObjectAnimator.ofFloat(laserLine, "translationY", ft, ft + fh - 2f * dp);
            laserAnimator.setDuration(1800);
            laserAnimator.setRepeatCount(ValueAnimator.INFINITE);
            laserAnimator.setRepeatMode(ValueAnimator.REVERSE);
            laserAnimator.setInterpolator(new LinearInterpolator());
            laserAnimator.start();
        });
    }
}