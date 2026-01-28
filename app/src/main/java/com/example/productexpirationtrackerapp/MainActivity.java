package com.example.productexpirationtrackerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Date; // Add this import
import java.util.List; // Add this import
import android.util.Log; // Add this import

public class MainActivity extends AppCompatActivity {

    private View dot1, dot2, dot3;
    private TextView loadingText, appTitle, appSubtitle, versionText;
    private Button skipButton;
    private Handler handler;
    private boolean isAnimationRunning = false;
    private Animation fadeInAnimation;

    // Add these constants for shared preferences
    private static final String PREFS_NAME = "AppPrefs";
    private static final String PREF_FIRST_TIME = "is_first_time"; // Changed from setup_completed
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

        // Check immediately if user should see onboarding
        checkFirstTimeUser();

        // Start animations with a small delay
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startOneByOneDotAnimation();
                startWelcomeAnimations();
                startLoadingProcess();
                setupSkipButton();
            }
        }, 100);

        // ============ ADD ONLY THIS DATABASE TEST SECTION ============
        // Initialize and test database (run this once)
        testDatabaseSetup();
        // ============ END OF DATABASE TEST SECTION ============
    }

    // ============ ADD THIS NEW METHOD ============
    private void testDatabaseSetup() {
        // Initialize database
        database = AppDatabase.getDatabase(this);

        // Test database in background thread
        new Thread(() -> {
            try {
                // Get current product count
                List<Product> existingProducts = database.productDao().getAllProducts();
                int initialCount = existingProducts.size();

                // Only add test data if database is empty
                if (initialCount == 0) {
                    // Add test products
                    Product milk = new Product("Milk", "2024-12-31");
                    Product eggs = new Product("Eggs", "2024-12-15");
                    Product bread = new Product("Bread", "2024-12-20");

                    database.productDao().insert(milk);
                    database.productDao().insert(eggs);
                    database.productDao().insert(bread);

                    // Get updated product list
                    List<Product> allProducts = database.productDao().getAllProducts();

                    // Log results
                    Log.d("DatabaseTest", "Added 3 test products");
                    Log.d("DatabaseTest", "Total products: " + allProducts.size());

                    for (Product p : allProducts) {
                        Log.d("DatabaseTest", "Product: " + p.getDisplayText());
                    }

                    // Update loading text on main thread
                    runOnUiThread(() -> {
                        if (loadingText != null) {
                            loadingText.setText("Database initialized with " + allProducts.size() + " products");
                        }
                    });
                } else {
                    Log.d("DatabaseTest", "Database already has " + initialCount + " products");

                    runOnUiThread(() -> {
                        if (loadingText != null) {
                            loadingText.setText("Found " + initialCount + " products in database");
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("DatabaseTest", "Error: " + e.getMessage(), e);

                runOnUiThread(() -> {
                    if (loadingText != null) {
                        loadingText.setText("Database error - check logs");
                    }
                    Toast.makeText(MainActivity.this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    // ============ END OF NEW METHOD ============

    // ALL YOUR EXISTING METHODS BELOW - NO CHANGES NEEDED
    private void initializeViews() {
        try {
            dot1 = findViewById(R.id.dot1);
            dot2 = findViewById(R.id.dot2);
            dot3 = findViewById(R.id.dot3);
            loadingText = findViewById(R.id.loadingText);
            appTitle = findViewById(R.id.appTitle);
            appSubtitle = findViewById(R.id.appSubtitle);
            versionText = findViewById(R.id.versionText);
            skipButton = findViewById(R.id.skipButton);

            // Set version text
            if (versionText != null) {
                versionText.setText("Version 1.0");
            }

            // Reset dots to initial position
            resetDots();
        } catch (Exception e) {
            Toast.makeText(this, "Error finding views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ============ ONLY UPDATE THIS METHOD ============
    private void checkFirstTimeUser() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstTime = preferences.getBoolean(PREF_FIRST_TIME, true); // Default to true (first time)

        // If it's not the first time AND setup is completed, go directly to ProductList
        if (!isFirstTime) {
            boolean setupCompleted = preferences.getBoolean(PREF_SETUP_COMPLETED, false);
            if (setupCompleted) {
                // User has completed setup before, go directly to ProductList
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        goToProductList();
                    }
                }, 1000); // Short delay to show logo
            } else {
                // USER HAS SEEN ONBOARDING BUT NOT COMPLETED SETUP
                // Go to SetupActivity after splash (with 1 second delay like ProductList)
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        goToSetup();
                    }
                }, 1000); // Same 1 second delay as above
            }
        }
        // If it's first time, we'll show animations and then decide in proceedToMainApp()
    }
    // ============ END OF UPDATED METHOD ============

    private void resetDots() {
        if (dot1 != null) {
            dot1.setTranslationY(0);
            dot1.setScaleX(1.0f);
            dot1.setScaleY(1.0f);
        }
        if (dot2 != null) {
            dot2.setTranslationY(0);
            dot2.setScaleX(1.0f);
            dot2.setScaleY(1.0f);
        }
        if (dot3 != null) {
            dot3.setTranslationY(0);
            dot3.setScaleX(1.0f);
            dot3.setScaleY(1.0f);
        }
    }

    private void startOneByOneDotAnimation() {
        isAnimationRunning = true;
        resetDots();

        // Start the animation sequence
        animateDotSequence();
    }

    private void animateDotSequence() {
        if (!isAnimationRunning) return;

        // Reset all dots first
        resetDots();

        // VERY FAST WAVE but SLOW BALL MOVEMENT:
        // All dots start with very short delays between them
        jumpDot(dot1, 0, new Runnable() {
            @Override
            public void run() {
                // Very short delay before repeating the wave
                if (isAnimationRunning) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isAnimationRunning) {
                                animateDotSequence();
                            }
                        }
                    }, 300);
                }
            }
        });

        // Dot2 starts with VERY SHORT delay after Dot1
        jumpDot(dot2, 150, null);

        // Dot3 starts with VERY SHORT delay after Dot2
        jumpDot(dot3, 300, null);
    }

    private void jumpDot(final View dot, long startDelay, final Runnable onComplete) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // VERY SLOW ball movement
                dot.animate()
                        .translationY(-30f)
                        .scaleY(1.4f)
                        .scaleX(1.1f)
                        .setDuration(600)  // VERY SLOW: 600ms for up movement
                        .setInterpolator(new OvershootInterpolator(0.4f)) // Very smooth
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                // Short pause at the top
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        // VERY SLOW ball movement down
                                        dot.animate()
                                                .translationY(0f)
                                                .scaleY(1.0f)
                                                .scaleX(1.0f)
                                                .setDuration(600)  // VERY SLOW: 600ms for down movement
                                                .setInterpolator(new OvershootInterpolator(0.4f))
                                                .withEndAction(onComplete);
                                    }
                                }, 100); // Short pause at top
                            }
                        });
            }
        }, startDelay);
    }

    private void startWelcomeAnimations() {
        if (appTitle != null) {
            appTitle.startAnimation(fadeInAnimation);
        }

        if (appSubtitle != null) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    appSubtitle.startAnimation(fadeInAnimation);
                }
            }, 500);
        }

        if (skipButton != null) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    skipButton.setVisibility(View.VISIBLE);
                    skipButton.startAnimation(fadeInAnimation);
                }
            }, 2000);
        }
    }

    private void startLoadingProcess() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (loadingText != null) {
                    loadingText.setText("Initializing app...");
                    loadingText.startAnimation(fadeInAnimation);
                }
            }
        }, 1000);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (loadingText != null) {
                    loadingText.setText("Loading product database...");
                    loadingText.startAnimation(fadeInAnimation);
                }
            }
        }, 2000);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (loadingText != null) {
                    loadingText.setText("Checking expiry dates...");
                    loadingText.startAnimation(fadeInAnimation);
                }
            }
        }, 3000);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (loadingText != null) {
                    loadingText.setText("Almost ready...");
                    loadingText.startAnimation(fadeInAnimation);
                }
            }
        }, 4000);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                proceedToMainApp();
            }
        }, 5000);
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

    private void proceedToMainApp() {
        isAnimationRunning = false;

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstTime = preferences.getBoolean(PREF_FIRST_TIME, true);

        if (isFirstTime) {
            // First time user: go to Onboarding
            goToOnboarding();
        } else {
            // Returning user: check if setup was completed
            boolean setupCompleted = preferences.getBoolean(PREF_SETUP_COMPLETED, false);
            if (setupCompleted) {
                goToProductList();
            } else {
                // User skipped setup before, go to SetupActivity
                goToSetup();
            }
        }
    }

    private void skipToMainApp() {
        isAnimationRunning = false;

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        if (loadingText != null) {
            loadingText.setText("Skipping...");
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                proceedToMainApp();
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
        isAnimationRunning = false;

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
        if (dot1 != null && dot2 != null && dot3 != null) {
            if (!isAnimationRunning) {
                isAnimationRunning = true;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startOneByOneDotAnimation();
                    }
                }, 100);
            }
        }
    }
}