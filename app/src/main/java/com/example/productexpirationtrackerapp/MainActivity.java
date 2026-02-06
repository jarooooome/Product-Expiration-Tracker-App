package com.example.productexpirationtrackerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // REMOVED: private View dot1, dot2, dot3;
    private TextView loadingText, appTitle, appSubtitle, versionText, tapToContinueText;
    private ProgressBar loadingProgress;
    private Handler handler;
    private boolean isAnimationRunning = false;
    private boolean isLoadingComplete = false;
    private Animation fadeInAnimation;

    // Add these constants for shared preferences
    private static final String PREFS_NAME = "AppPrefs";
    private static final String PREF_FIRST_TIME = "is_first_time";
    private static final String PREF_SETUP_COMPLETED = "setup_completed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        handler = new Handler(Looper.getMainLooper());

        // Set up click listener for the entire screen
        findViewById(R.id.main).setOnClickListener(v -> {
            if (isLoadingComplete) {
                proceedToMainApp();
            }
        });

        // Check immediately if user should see onboarding
        checkFirstTimeUser();

        // Start animations with a small delay
        handler.postDelayed(() -> {
            // REMOVED: startOneByOneDotAnimation();
            startWelcomeAnimations();
            startLoadingProcess();
        }, 100);

        // Initialize and test database
        testDatabaseSetup();
    }

    private void initializeViews() {
        try {
            // REMOVED: dot1 = findViewById(R.id.dot1);
            // REMOVED: dot2 = findViewById(R.id.dot2);
            // REMOVED: dot3 = findViewById(R.id.dot3);
            loadingText = findViewById(R.id.loadingText);
            appTitle = findViewById(R.id.appTitle);
            appSubtitle = findViewById(R.id.appSubtitle);
            versionText = findViewById(R.id.versionText);
            tapToContinueText = findViewById(R.id.tapToContinueText);
            loadingProgress = findViewById(R.id.loadingProgress);

            // Set texts using string resources
            if (versionText != null) {
                versionText.setText(getString(R.string.version_1_0));
            }

            // REMOVED: resetDots();
        } catch (Exception e) {
            Toast.makeText(this, "Error finding views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void testDatabaseSetup() {
        // Initialize database
        AppDatabase database = AppDatabase.getDatabase(this);

        // Test database in background thread
        new Thread(() -> {
            try {
                // Get current product count
                List<Product> existingProducts = database.productDao().getAllProducts();
                int initialCount = existingProducts.size();

                // Only add test data if database is empty
                if (initialCount == 0) {
                    // Add test products
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                    Product milk = new Product();
                    milk.setName("Milk");
                    milk.setExpiryDate(sdf.parse("2024-12-31"));

                    Product eggs = new Product();
                    eggs.setName("Eggs");
                    eggs.setExpiryDate(sdf.parse("2024-12-15"));

                    Product bread = new Product();
                    bread.setName("Bread");
                    bread.setExpiryDate(sdf.parse("2024-12-20"));

                    database.productDao().insert(milk);
                    database.productDao().insert(eggs);
                    database.productDao().insert(bread);

                    // Update progress to 80%
                    runOnUiThread(() -> updateProgress(80));

                } else {
                    // Update progress to 80%
                    runOnUiThread(() -> updateProgress(80));
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (loadingText != null) {
                        loadingText.setText(getString(R.string.database_error_check_logs));
                    }
                    Toast.makeText(MainActivity.this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void updateProgress(int progress) {
        if (loadingProgress != null) {
            loadingProgress.setProgress(progress);
        }
    }

    private void checkFirstTimeUser() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstTime = preferences.getBoolean(PREF_FIRST_TIME, true);

        if (!isFirstTime) {
            boolean setupCompleted = preferences.getBoolean(PREF_SETUP_COMPLETED, false);
            handler.postDelayed(() -> {
                updateProgress(100);
                showTapToContinue();
            }, 1000);
        }
    }

    // REMOVED: All dot animation methods (resetDots, startOneByOneDotAnimation, animateDotSequence, jumpDot)

    private void startWelcomeAnimations() {
        if (appTitle != null) {
            appTitle.startAnimation(fadeInAnimation);
        }

        if (appSubtitle != null) {
            handler.postDelayed(() -> appSubtitle.startAnimation(fadeInAnimation), 500);
        }
    }

    private void startLoadingProcess() {
        // Initial progress
        updateProgress(10);
        loadingText.setText(getString(R.string.initializing_app));

        handler.postDelayed(() -> {
            updateProgress(30);
            loadingText.setText(getString(R.string.loading_product_database));
            loadingText.startAnimation(fadeInAnimation);
        }, 1000);

        handler.postDelayed(() -> {
            updateProgress(50);
            loadingText.setText(getString(R.string.checking_expiry_dates));
            loadingText.startAnimation(fadeInAnimation);
        }, 2000);

        handler.postDelayed(() -> {
            updateProgress(70);
            loadingText.setText(getString(R.string.almost_ready));
            loadingText.startAnimation(fadeInAnimation);
        }, 3000);

        handler.postDelayed(() -> {
            updateProgress(90);
            loadingText.setText(getString(R.string.ready));
            showTapToContinue();
        }, 4000);
    }

    private void showTapToContinue() {
        isLoadingComplete = true;
        updateProgress(100);

        // Stop any animations
        isAnimationRunning = false;

        // Show tap to continue text with animation
        if (tapToContinueText != null) {
            tapToContinueText.setVisibility(View.VISIBLE);
            tapToContinueText.startAnimation(fadeInAnimation);

            // Add pulsing animation
            Animation pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_animation);
            tapToContinueText.startAnimation(pulseAnimation);
        }

        // Change loading text to indicate readiness
        if (loadingText != null) {
            loadingText.setText(getString(R.string.ready));
        }
    }

    private void proceedToMainApp() {
        if (!isLoadingComplete) {
            return;
        }

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstTime = preferences.getBoolean(PREF_FIRST_TIME, true);

        if (isFirstTime) {
            goToOnboarding();
        } else {
            boolean setupCompleted = preferences.getBoolean(PREF_SETUP_COMPLETED, false);
            if (setupCompleted) {
                goToProductList();
            } else {
                goToSetup();
            }
        }
    }

    private void goToOnboarding() {
        try {
            Intent intent = new Intent(MainActivity.this, OnboardingActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } catch (Exception e) {
            if (loadingText != null) {
                loadingText.setText(getString(R.string.create_onboarding_first));
            }
            Toast.makeText(this, getString(R.string.create_onboarding_first), Toast.LENGTH_LONG).show();
        }
    }

    private void goToProductList() {
        try {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } catch (Exception e) {
            if (loadingText != null) {
                loadingText.setText(getString(R.string.create_home_first));
            }
            Toast.makeText(this, getString(R.string.create_home_first), Toast.LENGTH_LONG).show();
        }
    }

    private void goToSetup() {
        try {
            Intent intent = new Intent(MainActivity.this, SetupActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } catch (Exception e) {
            if (loadingText != null) {
                loadingText.setText(getString(R.string.create_setup_first));
            }
            Toast.makeText(this, getString(R.string.create_setup_first), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isAnimationRunning = false;
        isLoadingComplete = false;

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isAnimationRunning = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // REMOVED: dot checking and animation restart
        // Your splash screen animations will restart automatically
    }
}