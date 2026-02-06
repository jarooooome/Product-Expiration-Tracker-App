package com.example.productexpirationtrackerapp;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPrefs";
    private static final String PREF_FIRST_TIME = "is_first_time";

    private ViewPager2 viewPager;
    private LinearLayout indicatorLayout;
    private TextView btnNext, btnSkip;
    private List<OnboardingPage> pages;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        indicatorLayout = findViewById(R.id.indicatorLayout);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);

        createPages();
        setupViewPager();
        setupIndicators();
        setupListeners();
    }

    private void createPages() {
        pages = new ArrayList<>();
        pages.add(new OnboardingPage(
                getString(R.string.onboarding_title_2),
                getString(R.string.onboarding_desc_2),
                "📦"
        ));
        pages.add(new OnboardingPage(
                getString(R.string.onboarding_title_3),
                getString(R.string.onboarding_desc_3),
                "🔔"
        ));
        pages.add(new OnboardingPage(
                getString(R.string.onboarding_title_4),
                getString(R.string.onboarding_desc_4),
                "🎨"
        ));
    }

    private void setupViewPager() {
        OnboardingAdapter adapter = new OnboardingAdapter(pages);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicators(position);

                if (position == pages.size() - 1) {
                    btnNext.setText(getString(R.string.get_started));
                    btnSkip.setVisibility(View.GONE);
                } else {
                    btnNext.setText(getString(R.string.next));
                    btnSkip.setVisibility(View.VISIBLE);
                }

                // Animate icon when page changes
                animateIcon(position);
            }
        });
    }

    private void setupIndicators() {
        indicatorLayout.removeAllViews();
        for (int i = 0; i < pages.size(); i++) {
            View dot = new View(this);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    getResources().getDimensionPixelSize(R.dimen.dot_size),
                    getResources().getDimensionPixelSize(R.dimen.dot_size)
            );
            params.setMargins(
                    getResources().getDimensionPixelSize(R.dimen.dot_margin),
                    0,
                    getResources().getDimensionPixelSize(R.dimen.dot_margin),
                    0
            );
            dot.setLayoutParams(params);

            dot.setBackgroundResource(i == 0 ?
                    R.drawable.dot_active :
                    R.drawable.dot_inactive
            );

            indicatorLayout.addView(dot);
        }
    }

    private void updateIndicators(int position) {
        for (int i = 0; i < indicatorLayout.getChildCount(); i++) {
            View dot = indicatorLayout.getChildAt(i);
            if (i == position) {
                dot.setBackgroundResource(R.drawable.dot_active);
                dot.animate().scaleX(1.5f).scaleY(1.5f).setDuration(200).start();
            } else {
                dot.setBackgroundResource(R.drawable.dot_inactive);
                dot.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
            }
        }
    }

    private void setupListeners() {
        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < pages.size() - 1) {
                viewPager.setCurrentItem(current + 1, true);
            } else {
                completeOnboarding();
            }
        });

        btnSkip.setOnClickListener(v -> completeOnboarding());
    }

    private void completeOnboarding() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREF_FIRST_TIME, false);
        editor.apply();

        Intent intent = new Intent(OnboardingActivity.this, SetupActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void animateIcon(int position) {
        // Find the current page view
        View pageView = viewPager.findViewWithTag("page_" + position);
        if (pageView != null) {
            View icon = pageView.findViewById(R.id.iconText);
            if (icon != null) {
                // Bounce animation
                ObjectAnimator bounceY = ObjectAnimator.ofFloat(icon, "scaleY", 0.8f, 1.2f, 1.0f);
                bounceY.setDuration(600);
                bounceY.setInterpolator(new BounceInterpolator());
                bounceY.start();

                ObjectAnimator bounceX = ObjectAnimator.ofFloat(icon, "scaleX", 0.9f, 1.1f, 1.0f);
                bounceX.setDuration(600);
                bounceX.setInterpolator(new BounceInterpolator());
                bounceX.start();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Animate icon after a short delay
        handler.postDelayed(() -> animateIcon(viewPager.getCurrentItem()), 300);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}