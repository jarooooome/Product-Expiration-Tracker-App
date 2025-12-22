package com.example.productexpirationtrackerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button skipButton, getStartedButton;
    private LinearLayout dotsLayout;
    private OnboardingAdapter adapter;

    // Page change callback for animations
    private ViewPager2.OnPageChangeCallback pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            updateDots(position);
            updateButtons(position);

            // Animate current page content
            animateCurrentPage(position);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            // Parallax effect for dots
            if (position < dotsLayout.getChildCount() - 1) {
                View currentDot = dotsLayout.getChildAt(position);
                View nextDot = dotsLayout.getChildAt(position + 1);

                currentDot.setScaleX(1 - positionOffset * 0.3f);
                currentDot.setScaleY(1 - positionOffset * 0.3f);
                nextDot.setScaleX(0.7f + positionOffset * 0.3f);
                nextDot.setScaleY(0.7f + positionOffset * 0.3f);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        skipButton = findViewById(R.id.skipButton);
        getStartedButton = findViewById(R.id.getStartedButton);
        dotsLayout = findViewById(R.id.dotsLayout);

        // Setup onboarding pages
        List<OnboardingPage> pages = new ArrayList<>();
        pages.add(new OnboardingPage(
                "Welcome to ExpiryTrack",
                "Never let your products expire again! Track all your items in one place.",
                "📦"
        ));
        pages.add(new OnboardingPage(
                "Track Your Products",
                "Add products with expiry dates and get organized. Add manually the dates.",
                "📅"
        ));
        pages.add(new OnboardingPage(
                "Get Smart Notifications",
                "We'll remind you before products expire. Never waste food or money again!",
                "🔔"
        ));
        pages.add(new OnboardingPage(
                "Customize Your Experience",
                "Choose your theme and notification preferences. Make it yours!",
                "🎨"
        ));

        // Setup adapter
        adapter = new OnboardingAdapter(pages);
        viewPager.setAdapter(adapter);

        // Add page transformer for animations
        viewPager.setPageTransformer(new DepthPageTransformer());

        // Register page change callback
        viewPager.registerOnPageChangeCallback(pageChangeCallback);

        // Setup dots indicator
        setupDotsIndicator(pages.size());

        // Skip button click
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSetupScreen();
            }
        });

        // Get Started button click
        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSetupScreen();
            }
        });

        // Add swipe gesture instructions
        addSwipeHint();
    }

    private void addSwipeHint() {
        // Optional: Add a subtle animation to indicate swiping
        Animation swipeHint = AnimationUtils.loadAnimation(this, R.anim.swipe_hint);
        dotsLayout.startAnimation(swipeHint);
    }

    private void animateCurrentPage(int position) {
        // Get current view from ViewPager
        View currentView = viewPager.getChildAt(0);
        if (currentView != null) {
            TextView title = currentView.findViewById(R.id.titleText);
            TextView description = currentView.findViewById(R.id.descriptionText);
            TextView icon = currentView.findViewById(R.id.iconView);

            if (title != null) {
                title.setAlpha(0f);
                title.setTranslationY(50f);
                title.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(500)
                        .setInterpolator(new OvershootInterpolator())
                        .start();
            }

            if (description != null) {
                description.setAlpha(0f);
                description.setTranslationY(30f);
                description.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(500)
                        .setStartDelay(100)
                        .setInterpolator(new OvershootInterpolator())
                        .start();
            }

            if (icon != null) {
                icon.setScaleX(0.5f);
                icon.setScaleY(0.5f);
                icon.setAlpha(0f);
                icon.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(600)
                        .setStartDelay(200)
                        .setInterpolator(new OvershootInterpolator(1.0f))
                        .start();
            }
        }
    }

    private void setupDotsIndicator(int count) {
        dotsLayout.removeAllViews();

        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            int size = 12; // 12dp
            int margin = 8; // 8dp

            float scale = getResources().getDisplayMetrics().density;
            int sizePx = (int) (size * scale + 0.5f);
            int marginPx = (int) (margin * scale + 0.5f);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizePx, sizePx);
            params.setMargins(marginPx, 0, marginPx, 0);
            dot.setLayoutParams(params);

            // Make dots circular
            dot.setBackgroundResource(R.drawable.dot_indicator);

            if (i == 0) {
                dot.setSelected(true);
            } else {
                dot.setSelected(false);
            }

            dotsLayout.addView(dot);
        }
    }

    private void updateDots(int position) {
        for (int i = 0; i < dotsLayout.getChildCount(); i++) {
            View dot = dotsLayout.getChildAt(i);
            dot.setSelected(i == position);

            // Scale animation for active dot
            if (i == position) {
                dot.animate()
                        .scaleX(1.5f)
                        .scaleY(1.5f)
                        .setDuration(300)
                        .start();
            } else {
                dot.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .start();
            }
        }
    }

    private void updateButtons(int position) {
        if (position == adapter.getItemCount() - 1) {
            // Last page
            skipButton.setVisibility(View.GONE);
            getStartedButton.setVisibility(View.VISIBLE);

            // Animate get started button
            getStartedButton.setAlpha(0f);
            getStartedButton.setTranslationY(50f);
            getStartedButton.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
        } else {
            skipButton.setVisibility(View.VISIBLE);
            getStartedButton.setVisibility(View.GONE);
        }
    }

    private void goToSetupScreen() {
        Intent intent = new Intent(OnboardingActivity.this, SetupActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
    }
}