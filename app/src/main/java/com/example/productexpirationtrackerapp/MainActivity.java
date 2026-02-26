package com.example.productexpirationtrackerapp;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // ADDED: Tag for logging

    private TextView loadingText, appTitle, appSubtitle, versionText;
    private View progressFill;
    private Handler handler;
    private Animation fadeInAnimation;
    private ValueAnimator progressAnimator;

    private ImageView icon1, icon2, icon3, icon4;
    private Handler iconHandler = new Handler();

    // Wave views for circular animation
    private View wave1, wave2, wave3, wave4;
    private View logoCenter;

    private int currentProgress = 0;
    private boolean isLoadingComplete = false;

    private static final String PREFS_NAME = "AppPrefs";
    private static final String PREF_FIRST_TIME = "is_first_time";
    private static final String PREF_SETUP_COMPLETED = "setup_completed";

    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ADDED: Log when activity starts
        Log.d(TAG, "onCreate started");

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        fadeInAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeInAnimation.setDuration(1000);
        handler = new Handler();

        handler.postDelayed(() -> {
            startWelcomeAnimations();
            startLogoWaveAnimation(); // Start wave animation
            startRealLoadingProcess();
        }, 100);
    }

    private void startLogoWaveAnimation() {
        // Remove clipChildren setting code since it's already in XML
        // Just start the animations

        // Animate wave1 - REDUCED RADIUS (smaller max scale)
        animateWaveWithValueAnimator(wave1, 0.2f, 1.0f, 0.6f, 0f, 0);
        // Animate wave2 - REDUCED RADIUS (smaller max scale)
        animateWaveWithValueAnimator(wave2, 0.2f, 1.1f, 0.5f, 0f, 500);
        // Animate wave3 - REDUCED RADIUS (smaller max scale)
        animateWaveWithValueAnimator(wave3, 0.2f, 1.2f, 0.4f, 0f, 1000);
        // Animate wave4 - REDUCED RADIUS (smaller max scale)
        animateWaveWithValueAnimator(wave4, 0.2f, 1.3f, 0.3f, 0f, 1500);

        // Add a subtle pulse animation to the ET logo using ValueAnimator
        startLogoPulseAnimation();
    }

    private void animateWaveWithValueAnimator(View wave, float startScale, float endScale,
                                              float startAlpha, float endAlpha, long startDelay) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(3000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setStartDelay(startDelay);
        animator.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();

            // Calculate current scale
            float currentScale = startScale + (endScale - startScale) * fraction;
            wave.setScaleX(currentScale);
            wave.setScaleY(currentScale);

            // Calculate current alpha
            float currentAlpha = startAlpha + (endAlpha - startAlpha) * fraction;
            wave.setAlpha(currentAlpha);
        });

        animator.start();
    }

    private void startLogoPulseAnimation() {
        ValueAnimator pulseAnimator = ValueAnimator.ofFloat(1f, 1.05f, 1f);
        pulseAnimator.setDuration(1500);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.RESTART);
        pulseAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            logoCenter.setScaleX(value);
            logoCenter.setScaleY(value);
        });
        pulseAnimator.start();
    }

    private void startRealLoadingProcess() {
        updateProgress(0, "Welcome to ExpiryTrack...");

        handler.postDelayed(() -> updateProgress(5, "Loading application framework..."), 300);
        handler.postDelayed(() -> updateProgress(10, "Initializing core modules..."), 800);
        handler.postDelayed(() -> updateProgress(15, "Setting up user interface..."), 1300);

        handler.postDelayed(() -> {
            updateProgress(20, "Preparing database system...");
            checkDatabaseAndUpdateProgress();
        }, 1800);
    }

    private void checkDatabaseAndUpdateProgress() {
        updateProgress(25, "Establishing database connection...");
        database = AppDatabase.getDatabase(this);

        handler.postDelayed(() -> {
            updateProgress(30, "Verifying database integrity...");

            new Thread(() -> {
                try {
                    Thread.sleep(800);
                    runOnUiThread(() -> updateProgress(35, "Connected to local database..."));

                    Thread.sleep(600);
                    runOnUiThread(() -> updateProgress(40, "Scanning existing products..."));

                    List<Product> existingProducts = database.productDao().getAllProducts();
                    int initialCount = existingProducts.size();

                    runOnUiThread(() ->
                            updateProgress(45, "Found " + initialCount + " product(s)..."));

                    Thread.sleep(500);

                    // REMOVED: Default product insertion code
                    if (initialCount == 0) {
                        runOnUiThread(() ->
                                updateProgress(55, "Database ready - no products found"));
                        Thread.sleep(500);

                        runOnUiThread(() ->
                                updateProgress(70, "Ready to add your first product!"));
                    } else {
                        runOnUiThread(() ->
                                updateProgress(70, "Database loaded successfully"));
                    }

                    runOnUiThread(this::checkUserPreferencesAndUpdateProgress);

                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        updateProgress(70, "Database warning - using fallback mode");
                        checkUserPreferencesAndUpdateProgress();
                    });
                }
            }).start();

        }, 500);
    }

    private void checkUserPreferencesAndUpdateProgress() {
        updateProgress(75, "Loading user configuration...");

        handler.postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean isFirstTime = prefs.getBoolean(PREF_FIRST_TIME, true);
            boolean setupCompleted = prefs.getBoolean(PREF_SETUP_COMPLETED, false);

            // ADDED: Log the preference values
            Log.d(TAG, "checkUserPreferences - isFirstTime: " + isFirstTime + ", setupCompleted: " + setupCompleted);

            String status = isFirstTime
                    ? "First-time user detected"
                    : setupCompleted
                    ? "Welcome back! Loading your data..."
                    : "Setup incomplete - configuration needed";

            updateProgress(85, status);
            handler.postDelayed(this::loadThemeAndFinalize, 800);
        }, 500);
    }

    private void loadThemeAndFinalize() {
        updateProgress(87, "Applying visual theme...");

        handler.postDelayed(() -> {
            updateProgress(90, "Theme selected");
            handler.postDelayed(() -> {
                updateProgress(94, "Preparing systems...");
                handler.postDelayed(() -> {
                    updateProgress(100, "Ready! Launching application...");
                    isLoadingComplete = true;

                    // ADDED: Log before navigation
                    Log.d(TAG, "loadThemeAndFinalize - About to navigate, currentProgress: " + currentProgress);

                    handler.postDelayed(this::navigateBasedOnUserStatus, 800);
                }, 800);
            }, 800);
        }, 500);
    }

    private void navigateBasedOnUserStatus() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean(PREF_FIRST_TIME, true);
        boolean setupCompleted = prefs.getBoolean(PREF_SETUP_COMPLETED, false);

        // ADDED: Log the navigation decision
        Log.d(TAG, "NAVIGATION DECISION - isFirstTime: " + isFirstTime + ", setupCompleted: " + setupCompleted);
        Log.d(TAG, "Navigating to: " + (isFirstTime ? "Onboarding" : setupCompleted ? "ProductList" : "Setup"));

        if (isFirstTime) goToOnboarding();
        else if (setupCompleted) goToProductList();
        else goToSetup();
    }

    private void updateProgress(int progress, String status) {
        currentProgress = progress;
        runOnUiThread(() -> {
            loadingText.setText(status);

            // ADDED: Log progress updates
            Log.d(TAG, "Progress: " + progress + "% - " + status);

            // Wait for layout to be measured
            progressFill.post(() -> {
                View track = findViewById(R.id.progressTrack);
                if (track != null && track.getWidth() > 0) {
                    int maxWidth = track.getWidth();
                    int newWidth = (int) (maxWidth * (progress / 100f));
                    int currentWidth = progressFill.getLayoutParams().width;

                    if (progressAnimator != null && progressAnimator.isRunning()) {
                        progressAnimator.cancel();
                    }

                    progressAnimator = ValueAnimator.ofInt(currentWidth, newWidth);
                    progressAnimator.setDuration(300);
                    progressAnimator.addUpdateListener(a -> {
                        progressFill.getLayoutParams().width = (int) a.getAnimatedValue();
                        progressFill.requestLayout();
                    });
                    progressAnimator.start();
                } else {
                    // If track not measured yet, try again
                    if (progress < 100) {
                        handler.postDelayed(() -> updateProgress(progress, status), 50);
                    }
                }
            });
        });
    }

    private void initializeViews() {
        loadingText = findViewById(R.id.loadingText);
        appTitle = findViewById(R.id.appTitle);
        appSubtitle = findViewById(R.id.appSubtitle);
        versionText = findViewById(R.id.versionText);
        progressFill = findViewById(R.id.progressFill);

        // Wave views
        wave1 = findViewById(R.id.wave1);
        wave2 = findViewById(R.id.wave2);
        wave3 = findViewById(R.id.wave3);
        wave4 = findViewById(R.id.wave4);
        logoCenter = findViewById(R.id.logoCenter);

        icon1 = findViewById(R.id.icon1);
        icon2 = findViewById(R.id.icon2);
        icon3 = findViewById(R.id.icon3);
        icon4 = findViewById(R.id.icon4);

        // Set version text
        if (versionText != null) {
            versionText.setText("Version 1.0");
        }

        // Initialize progress bar
        progressFill.getLayoutParams().width = 0;
        progressFill.requestLayout();
    }

    private void startWelcomeAnimations() {
        // Use fade animation for title
        appTitle.startAnimation(fadeInAnimation);
        appTitle.setVisibility(View.VISIBLE);

        handler.postDelayed(() -> {
            appSubtitle.startAnimation(fadeInAnimation);
            appSubtitle.setVisibility(View.VISIBLE);
        }, 500);

        handler.postDelayed(this::startIconWaveAnimation, 1500);
    }

    private void startIconWaveAnimation() {
        icon1.setVisibility(View.VISIBLE);
        icon2.setVisibility(View.VISIBLE);
        icon3.setVisibility(View.VISIBLE);
        icon4.setVisibility(View.VISIBLE);

        animateIconWave(icon1, 0);
        animateIconWave(icon2, 200);
        animateIconWave(icon3, 400);
        animateIconWave(icon4, 600);
    }

    private void animateIconWave(ImageView icon, long delay) {
        iconHandler.postDelayed(() -> {
            ValueAnimator anim = ValueAnimator.ofFloat(0f, -12f, 0f, 12f, 0f);
            anim.setDuration(5000);
            anim.setRepeatCount(ValueAnimator.INFINITE);
            anim.setRepeatMode(ValueAnimator.RESTART);
            anim.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                icon.setTranslationY(value);
            });
            anim.start();
        }, delay);
    }

    private void goToOnboarding() {
        try {
            // ADDED: Log when navigating to Onboarding
            Log.d(TAG, "Attempting to navigate to OnboardingActivity");
            Intent intent = new Intent(this, OnboardingActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to Onboarding: " + e.getMessage()); // ADDED: Error log
            Toast.makeText(this, "Create OnboardingActivity first!", Toast.LENGTH_LONG).show();
        }
    }

    private void goToProductList() {
        try {
            // ADDED: Log when navigating to ProductList
            Log.d(TAG, "Attempting to navigate to ProductListActivity");
            Intent intent = new Intent(this, ProductListActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to ProductList: " + e.getMessage()); // ADDED: Error log
            Toast.makeText(this, "Create ProductListActivity first!", Toast.LENGTH_LONG).show();
        }
    }

    private void goToSetup() {
        try {
            // ADDED: Log when navigating to Setup
            Log.d(TAG, "Attempting to navigate to SetupActivity");
            Intent intent = new Intent(this, SetupActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to Setup: " + e.getMessage()); // ADDED: Error log
            Toast.makeText(this, "Create SetupActivity first!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ADDED: Log when activity is destroyed
        Log.d(TAG, "onDestroy called");

        if (progressAnimator != null && progressAnimator.isRunning()) {
            progressAnimator.cancel();
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if (iconHandler != null) {
            iconHandler.removeCallbacksAndMessages(null);
        }
    }
}