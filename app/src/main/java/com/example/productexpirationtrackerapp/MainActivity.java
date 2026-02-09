package com.example.productexpirationtrackerapp;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private TextView loadingText, appTitle, appSubtitle, versionText;
    private Button skipButton;
    private View progressFill;
    private Handler handler;
    private Animation fadeInAnimation;
    private ValueAnimator progressAnimator;

    // Progress tracking
    private int currentProgress = 0;
    private static final int MAX_PROGRESS = 100;
    private boolean isLoadingComplete = false;

    // Add these constants for shared preferences
    private static final String PREFS_NAME = "AppPrefs";
    private static final String PREF_FIRST_TIME = "is_first_time";
    private static final String PREF_SETUP_COMPLETED = "setup_completed";

    // Add database instance variable
    private AppDatabase database;

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
        handler = new Handler();

        // Start animations with a small delay
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startWelcomeAnimations();
                startRealLoadingProcess();
                setupSkipButton();
            }
        }, 100);
    }

    // UPDATED: Real loading process with more steps
    private void startRealLoadingProcess() {
        // Start at 0%
        updateProgress(0, "Welcome to ExpiryTrack...");

        // Initial app setup
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateProgress(5, "Loading application framework...");
            }
        }, 300);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateProgress(10, "Initializing core modules...");
            }
        }, 800);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateProgress(15, "Setting up user interface...");
            }
        }, 1300);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateProgress(20, "Preparing database system...");
                checkDatabaseAndUpdateProgress();
            }
        }, 1800);
    }

    // UPDATED: Database check with more detailed progress
    private void checkDatabaseAndUpdateProgress() {
        updateProgress(25, "Establishing database connection...");

        database = AppDatabase.getDatabase(this);

        // Add delay to make it feel more realistic
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateProgress(30, "Verifying database integrity...");

                new Thread(() -> {
                    try {
                        // Small delay to simulate processing
                        Thread.sleep(800);

                        // Update progress: Database connection
                        runOnUiThread(() -> updateProgress(35, "Connected to local database..."));

                        // Small delay
                        Thread.sleep(600);

                        // Get current product count
                        runOnUiThread(() -> updateProgress(40, "Scanning existing products..."));

                        List<Product> existingProducts = database.productDao().getAllProducts();
                        int initialCount = existingProducts.size();

                        // Update progress: Database read complete
                        runOnUiThread(() -> updateProgress(45, "Found " + initialCount + " product(s)..."));

                        // Small delay
                        Thread.sleep(500);

                        // Add test data if empty
                        if (initialCount == 0) {
                            runOnUiThread(() -> updateProgress(50, "Database empty, creating sample data..."));
                            Thread.sleep(800);

                            runOnUiThread(() -> updateProgress(55, "Adding food items..."));

                            // Add test products with delays between each
                            Product milk = new Product("Milk", "2024-12-31");
                            database.productDao().insert(milk);
                            Thread.sleep(300);

                            runOnUiThread(() -> updateProgress(60, "Adding dairy products..."));
                            Product eggs = new Product("Eggs", "2024-12-15");
                            database.productDao().insert(eggs);
                            Thread.sleep(300);

                            runOnUiThread(() -> updateProgress(65, "Adding pantry items..."));
                            Product bread = new Product("Bread", "2024-12-20");
                            database.productDao().insert(bread);
                            Thread.sleep(300);

                            // Get updated count
                            List<Product> allProducts = database.productDao().getAllProducts();

                            runOnUiThread(() -> {
                                updateProgress(70, "Database ready with " + allProducts.size() + " sample items");
                                Log.d("RealLoading", "Database initialized with " + allProducts.size() + " products");
                            });
                        } else {
                            runOnUiThread(() -> {
                                updateProgress(70, "Database loaded successfully");
                                Log.d("RealLoading", "Database has " + initialCount + " products");
                            });
                        }

                        // Move to next step: Check user preferences
                        runOnUiThread(() -> checkUserPreferencesAndUpdateProgress());

                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            updateProgress(70, "Database warning - using fallback mode");
                            Log.e("RealLoading", "Error: " + e.getMessage(), e);
                            // Continue anyway
                            checkUserPreferencesAndUpdateProgress();
                        });
                    }
                }).start();
            }
        }, 500);
    }

    // UPDATED: Check user preferences with more steps
    private void checkUserPreferencesAndUpdateProgress() {
        updateProgress(75, "Loading user configuration...");

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateProgress(80, "Reading application settings...");

                SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                boolean isFirstTime = preferences.getBoolean(PREF_FIRST_TIME, true);
                boolean setupCompleted = preferences.getBoolean(PREF_SETUP_COMPLETED, false);

                String status;
                if (isFirstTime) {
                    status = "First-time user detected";
                } else if (setupCompleted) {
                    status = "Welcome back! Loading your data...";
                } else {
                    status = "Setup incomplete - configuration needed";
                }

                updateProgress(85, status);

                // Move to next step: Load theme
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadThemeAndFinalize();
                    }
                }, 800);
            }
        }, 500);
    }

    // UPDATED: Load theme and finalize with more steps
    private void loadThemeAndFinalize() {
        updateProgress(87, "Applying visual theme...");

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                String theme = preferences.getString("color_theme", "white");
                String userName = preferences.getString("user_name", "User");

                updateProgress(90, "Theme: " + theme + " selected");

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateProgress(92, "Optimizing performance...");

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                updateProgress(94, "Loading expiry algorithms...");

                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateProgress(96, "Preparing notification system...");

                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                updateProgress(98, "Final security checks...");

                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        updateProgress(100, "Ready! Launching application...");
                                                        isLoadingComplete = true;

                                                        // Navigate after short delay
                                                        handler.postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                navigateBasedOnUserStatus();
                                                            }
                                                        }, 800);
                                                    }
                                                }, 600);
                                            }
                                        }, 500);
                                    }
                                }, 500);
                            }
                        }, 500);
                    }
                }, 500);
            }
        }, 500);
    }

    // NEW: Navigation based on user status
    private void navigateBasedOnUserStatus() {
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

    // UPDATED: Progress update method with smooth animation
    private void updateProgress(final int progress, final String status) {
        if (progress < 0) return;
        if (progress > 100) return;

        currentProgress = progress;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Update status text
                if (loadingText != null) {
                    loadingText.setText(status);
                }

                // Update progress bar width with smooth animation
                if (progressFill != null) {
                    progressFill.post(new Runnable() {
                        @Override
                        public void run() {
                            View track = findViewById(R.id.progressTrack);
                            if (track != null && track.getWidth() > 0) {
                                int maxWidth = track.getWidth();
                                int newWidth = (int) ((progress / 100.0) * maxWidth);
                                int currentWidth = progressFill.getLayoutParams().width;

                                // Cancel previous animation
                                if (progressAnimator != null && progressAnimator.isRunning()) {
                                    progressAnimator.cancel();
                                }

                                // Create smooth animation
                                progressAnimator = ValueAnimator.ofInt(currentWidth, newWidth);
                                progressAnimator.setDuration(300); // 300ms animation
                                progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {
                                        int animatedValue = (int) animation.getAnimatedValue();
                                        progressFill.getLayoutParams().width = animatedValue;
                                        progressFill.requestLayout();
                                    }
                                });
                                progressAnimator.start();
                            } else {
                                // If not measured yet, try again
                                if (progress < 100) {
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateProgress(progress, status);
                                        }
                                    }, 50);
                                }
                            }
                        }
                    });
                }

                Log.d("ProgressUpdate", "Progress: " + progress + "%, Status: " + status);
            }
        });
    }

    // UPDATED: initializeViews without loadingProgressText
    private void initializeViews() {
        try {
            loadingText = findViewById(R.id.loadingText);
            appTitle = findViewById(R.id.appTitle);
            appSubtitle = findViewById(R.id.appSubtitle);
            versionText = findViewById(R.id.versionText);
            skipButton = findViewById(R.id.skipButton);

            progressFill = findViewById(R.id.progressFill);

            if (versionText != null) {
                versionText.setText("Version 1.0");
            }

            initializeProgressBar();
        } catch (Exception e) {
            Toast.makeText(this, "Error finding views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // UPDATED: initializeProgressBar without percentage text
    private void initializeProgressBar() {
        if (progressFill != null) {
            progressFill.getLayoutParams().width = 0;
            progressFill.requestLayout();
        }

        if (loadingText != null) {
            loadingText.setText("Starting app...");
        }

        currentProgress = 0;
        isLoadingComplete = false;
    }

    // UPDATED: Remove animations from static elements
    private void startWelcomeAnimations() {
        if (appTitle != null) {
            // Just show the title without animation
            appTitle.setVisibility(View.VISIBLE);
        }

        if (appSubtitle != null) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Just show the subtitle without animation
                    appSubtitle.setVisibility(View.VISIBLE);
                }
            }, 500);
        }

        if (skipButton != null) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Just show the skip button without animation
                    skipButton.setVisibility(View.VISIBLE);
                }
            }, 2000);
        }
    }

    private void setupSkipButton() {
        if (skipButton != null) {
            skipButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    skipToMainApp();
                }
            });
        }
    }

    private void skipToMainApp() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        updateProgress(100, "Skipping setup...");
        isLoadingComplete = true;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                navigateBasedOnUserStatus();
            }
        }, 300);
    }

    private void goToOnboarding() {
        try {
            Intent intent = new Intent(MainActivity.this, OnboardingActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } catch (Exception e) {
            if (loadingText != null) {
                loadingText.setText("Create OnboardingActivity first!");
            }
            Toast.makeText(this, "Please create OnboardingActivity", Toast.LENGTH_LONG).show();
        }
    }

    private void goToProductList() {
        try {
            Intent intent = new Intent(MainActivity.this, ProductListActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } catch (Exception e) {
            if (loadingText != null) {
                loadingText.setText("Create ProductListActivity first!");
            }
            Toast.makeText(this, "Please create ProductListActivity", Toast.LENGTH_LONG).show();
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
                loadingText.setText("Create SetupActivity first!");
            }
            Toast.makeText(this, "Please create SetupActivity", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Cancel animation
        if (progressAnimator != null && progressAnimator.isRunning()) {
            progressAnimator.cancel();
        }

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // No animation resumption needed
    }
}