package com.example.productexpirationtrackerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * ENHANCED SPLASH SCREEN ACTIVITY
 * Premium animated splash screen with orbital rings and glassmorphism
 *
 * NEW FEATURES:
 * - Orbital ring rotations (clockwise & counter-clockwise)
 * - Logo entrance animation with bounce
 * - Checkmark badge pop-in when complete
 * - Smooth cascading text animations
 * - Enhanced progress loading
 * - Pulse glow effects
 *
 * @version 2.0 Premium Edition
 */
public class MainActivity extends AppCompatActivity {

    // ========================================
    // VIEW REFERENCES
    // ========================================

    // Logo Components
    private FrameLayout logoContainer;
    private ImageView orbitalRingOuter;
    private ImageView orbitalRingInner;
    private View logoBackground;
    private FrameLayout checkmarkBadge;

    // Text Components
    private TextView loadingText, appTitle, appSubtitle, versionText, tapToContinueText;

    // Progress
    private ProgressBar loadingProgress;

    // Animations
    private Animation orbitalRotation;
    private Animation orbitalRotationReverse;
    private Animation logoEntranceAnim;
    private Animation pulseGlowAnim;
    private Animation fadeInScaleAnim;
    private Animation checkmarkPopInAnim;
    private Animation tapToContinuePulseAnim;

    // Handler
    private Handler handler;

    // State flags
    private boolean isAnimationRunning = false;
    private boolean isLoadingComplete = false;

    // SharedPreferences constants
    private static final String PREFS_NAME = "AppPrefs";
    private static final String PREF_FIRST_TIME = "is_first_time";
    private static final String PREF_SETUP_COMPLETED = "setup_completed";

    // ========================================
    // LIFECYCLE METHODS
    // ========================================

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

        // Initialize everything
        initializeViews();
        loadAnimations();

        handler = new Handler(Looper.getMainLooper());

        // Set up click listener for entire screen
        findViewById(R.id.main).setOnClickListener(v -> {
            if (isLoadingComplete) {
                proceedToMainApp();
            }
        });

        // Check first time user
        checkFirstTimeUser();

        // Start the show!
        handler.postDelayed(this::startEntranceSequence, 200);

        // Initialize database
        testDatabaseSetup();
    }

    // ========================================
    // INITIALIZATION METHODS
    // ========================================

    /**
     * Initialize all view references
     */
    private void initializeViews() {
        try {
            // Logo components
            logoContainer = findViewById(R.id.logoContainer);
            orbitalRingOuter = findViewById(R.id.orbitalRingOuter);
            orbitalRingInner = findViewById(R.id.orbitalRingInner);
            logoBackground = findViewById(R.id.logoBackground);
            checkmarkBadge = findViewById(R.id.checkmarkBadge);

            // Text components
            loadingText = findViewById(R.id.loadingText);
            appTitle = findViewById(R.id.appTitle);
            appSubtitle = findViewById(R.id.appSubtitle);
            versionText = findViewById(R.id.versionText);
            tapToContinueText = findViewById(R.id.tapToContinueText);

            // Progress
            loadingProgress = findViewById(R.id.loadingProgress);

            // Set initial visibility
            logoContainer.setAlpha(0f);
            appTitle.setAlpha(0f);
            appSubtitle.setAlpha(0f);
            checkmarkBadge.setVisibility(View.GONE);

        } catch (Exception e) {
            Toast.makeText(this, "Error initializing views: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Load all animation resources
     */
    private void loadAnimations() {
        try {
            orbitalRotation = AnimationUtils.loadAnimation(this, R.anim.orbital_rotation);
            orbitalRotationReverse = AnimationUtils.loadAnimation(this, R.anim.orbital_rotation_reverse);
            logoEntranceAnim = AnimationUtils.loadAnimation(this, R.anim.logo_entrance);
            pulseGlowAnim = AnimationUtils.loadAnimation(this, R.anim.pulse_glow);
            fadeInScaleAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale);
            checkmarkPopInAnim = AnimationUtils.loadAnimation(this, R.anim.checkmark_pop_in);
            tapToContinuePulseAnim = AnimationUtils.loadAnimation(this, R.anim.tap_to_continue_pulse);
        } catch (Exception e) {
            Toast.makeText(this, "Error loading animations: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    // ========================================
    // ANIMATION SEQUENCE
    // ========================================

    /**
     * Master entrance animation sequence
     * Orchestrates all animations in proper order
     */
    private void startEntranceSequence() {
        isAnimationRunning = true;

        // Step 1: Logo entrance (0ms)
        animateLogoEntrance();

        // Step 2: Start orbital rotations (500ms delay)
        handler.postDelayed(this::startOrbitalAnimations, 500);

        // Step 3: Title fade in (800ms delay)
        handler.postDelayed(this::animateTitle, 800);

        // Step 4: Subtitle fade in (1200ms delay)
        handler.postDelayed(this::animateSubtitle, 1200);

        // Step 5: Start loading process (1500ms delay)
        handler.postDelayed(this::startLoadingProcess, 1500);
    }

    /**
     * Animate logo container entrance with bounce
     */
    private void animateLogoEntrance() {
        if (logoContainer != null && logoEntranceAnim != null) {
            logoContainer.startAnimation(logoEntranceAnim);

            // Start pulse glow after entrance completes
            logoEntranceAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    logoContainer.setAlpha(1f);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (logoBackground != null && pulseGlowAnim != null) {
                        logoBackground.startAnimation(pulseGlowAnim);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        }
    }

    /**
     * Start orbital ring rotations
     */
    private void startOrbitalAnimations() {
        if (orbitalRingOuter != null && orbitalRotation != null) {
            orbitalRingOuter.startAnimation(orbitalRotation);
        }

        if (orbitalRingInner != null && orbitalRotationReverse != null) {
            orbitalRingInner.startAnimation(orbitalRotationReverse);
        }
    }

    /**
     * Animate app title
     */
    private void animateTitle() {
        if (appTitle != null && fadeInScaleAnim != null) {
            appTitle.setAlpha(1f);
            appTitle.startAnimation(fadeInScaleAnim);
        }
    }

    /**
     * Animate subtitle
     */
    private void animateSubtitle() {
        if (appSubtitle != null && fadeInScaleAnim != null) {
            appSubtitle.setAlpha(1f);
            Animation subtitleAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale);
            appSubtitle.startAnimation(subtitleAnim);
        }
    }

    // ========================================
    // LOADING PROCESS
    // ========================================

    /**
     * Orchestrate the loading progress sequence
     */
    private void startLoadingProcess() {
        // Stage 1: Initializing (0ms)
        updateProgress(10, getString(R.string.initializing_app));

        // Stage 2: Loading database (1000ms)
        handler.postDelayed(() ->
                updateProgress(30, getString(R.string.loading_product_database)), 1000);

        // Stage 3: Checking dates (2000ms)
        handler.postDelayed(() ->
                updateProgress(50, getString(R.string.checking_expiry_dates)), 2000);

        // Stage 4: Almost ready (3000ms)
        handler.postDelayed(() ->
                updateProgress(70, getString(R.string.almost_ready)), 3000);

        // Stage 5: Database complete (handled by testDatabaseSetup at 80%)

        // Stage 6: Ready! (4500ms)
        handler.postDelayed(() -> {
            updateProgress(100, getString(R.string.ready));
            showCompletionState();
        }, 4500);
    }

    /**
     * Update progress bar and loading text
     */
    private void updateProgress(int progress, String message) {
        if (loadingProgress != null) {
            loadingProgress.setProgress(progress);
        }
        if (loadingText != null && message != null) {
            loadingText.setText(message);
            // Subtle fade animation on text change
            loadingText.setAlpha(0.5f);
            loadingText.animate().alpha(0.9f).setDuration(300).start();
        }
    }

    /**
     * Update only progress (for database callback)
     */
    private void updateProgress(int progress) {
        if (loadingProgress != null) {
            loadingProgress.setProgress(progress);
        }
    }

    // ========================================
    // COMPLETION STATE
    // ========================================

    /**
     * Show completion state with checkmark and tap to continue
     */
    private void showCompletionState() {
        isLoadingComplete = true;
        isAnimationRunning = false;

        // Show checkmark badge with pop animation
        if (checkmarkBadge != null && checkmarkPopInAnim != null) {
            checkmarkBadge.setVisibility(View.VISIBLE);
            checkmarkBadge.startAnimation(checkmarkPopInAnim);
        }

        // Show tap to continue text with pulse
        handler.postDelayed(() -> {
            if (tapToContinueText != null && tapToContinuePulseAnim != null) {
                tapToContinueText.setVisibility(View.VISIBLE);
                tapToContinueText.startAnimation(tapToContinuePulseAnim);
            }
        }, 400);
    }

    // ========================================
    // DATABASE SETUP
    // ========================================

    /**
     * Initialize and test database
     */
    private void testDatabaseSetup() {
        AppDatabase database = AppDatabase.getDatabase(this);

        new Thread(() -> {
            try {
                List<Product> existingProducts = database.productDao().getAllProducts();
                int initialCount = existingProducts.size();

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
                }

                // Update progress to 80%
                runOnUiThread(() -> updateProgress(80));

            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (loadingText != null) {
                        loadingText.setText(getString(R.string.database_error_check_logs));
                    }
                    Toast.makeText(MainActivity.this,
                            "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    // ========================================
    // NAVIGATION
    // ========================================

    /**
     * Check if user is first time
     */
    private void checkFirstTimeUser() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstTime = preferences.getBoolean(PREF_FIRST_TIME, true);

        if (!isFirstTime) {
            // Not first time - speed up to completion
            handler.postDelayed(() -> {
                updateProgress(100);
                // Don't show tap to continue immediately for returning users
            }, 1000);
        }
    }

    /**
     * Proceed to appropriate next screen
     */
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

    /**
     * Navigate to onboarding
     */
    private void goToOnboarding() {
        try {
            Intent intent = new Intent(MainActivity.this, OnboardingActivity_enhanced.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } catch (Exception e) {
            if (loadingText != null) {
                loadingText.setText(getString(R.string.create_onboarding_first));
            }
            Toast.makeText(this, getString(R.string.create_onboarding_first),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Navigate to home/product list
     */
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
            Toast.makeText(this, getString(R.string.create_home_first),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Navigate to setup
     */
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
            Toast.makeText(this, getString(R.string.create_setup_first),
                    Toast.LENGTH_LONG).show();
        }
    }

    // ========================================
    // LIFECYCLE CLEANUP
    // ========================================

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isAnimationRunning = false;
        isLoadingComplete = false;

        // Clear all animations
        if (orbitalRingOuter != null) orbitalRingOuter.clearAnimation();
        if (orbitalRingInner != null) orbitalRingInner.clearAnimation();
        if (logoBackground != null) logoBackground.clearAnimation();
        if (logoContainer != null) logoContainer.clearAnimation();
        if (appTitle != null) appTitle.clearAnimation();
        if (appSubtitle != null) appSubtitle.clearAnimation();
        if (tapToContinueText != null) tapToContinueText.clearAnimation();

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
        // Animations are one-time on splash, no restart needed
    }
}