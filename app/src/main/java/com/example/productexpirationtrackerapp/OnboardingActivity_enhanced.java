package com.example.productexpirationtrackerapp;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity_enhanced extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPrefs";
    private static final String PREF_FIRST_TIME = "is_first_time";

    private ViewPager2 viewPager;
    private LinearLayout indicatorLayout;
    private TextView btnNext, btnSkip, progressText;
    private View progressFill, mainContainer;
    private List<OnboardingPage_enhanced> pages;
    private OnboardingAdapter_enhanced adapter;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_enhanced);

        viewPager = findViewById(R.id.viewPager);
        indicatorLayout = findViewById(R.id.indicatorLayout);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);
        progressFill = findViewById(R.id.progressFill);
        progressText = findViewById(R.id.progressText);
        mainContainer = findViewById(R.id.mainContainer);

        createPages();
        setupViewPager();
        setupIndicators();
        setupListeners();
        updateProgress(0);
    }

    private void createPages() {
        pages = new ArrayList<>();

        // Page 1: Track Your Products (Green gradient)
        pages.add(new OnboardingPage_enhanced(
                getString(R.string.onboarding_title_2),
                getString(R.string.onboarding_desc_2),
                R.drawable.ic_calendar,
                R.drawable.onboarding_gradient_page1
        ));

        // Page 2: Get Smart Notifications (Orange gradient)
        pages.add(new OnboardingPage_enhanced(
                getString(R.string.onboarding_title_3),
                getString(R.string.onboarding_desc_3),
                R.drawable.ic_notification,
                R.drawable.onboarding_gradient_page2
        ));

        // Page 3: Customize Your Experience (Pink gradient)
        pages.add(new OnboardingPage_enhanced(
                getString(R.string.onboarding_title_4),
                getString(R.string.onboarding_desc_4),
                R.drawable.ic_palette,
                R.drawable.onboarding_gradient_page3
        ));
    }

    private void setupViewPager() {
        adapter = new OnboardingAdapter_enhanced(pages);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicators(position);
                updateProgress(position);
                updateBackgroundGradient(position);

                if (position == pages.size() - 1) {
                    btnNext.setText(getString(R.string.get_started));
                    btnSkip.setVisibility(View.GONE);
                    animateButton(btnNext, true);
                } else {
                    btnNext.setText(getString(R.string.next));
                    btnSkip.setVisibility(View.VISIBLE);
                }

                // Animate icon when page changes
                animateIcon(position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                // Smooth progress bar update during scroll
                float progress = (position + positionOffset) / (float) (pages.size() - 1);
                updateProgressBar(progress);
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
                    R.drawable.dot_active_enhanced :
                    R.drawable.dot_inactive_enhanced
            );

            indicatorLayout.addView(dot);
        }
    }

    private void updateIndicators(int position) {
        for (int i = 0; i < indicatorLayout.getChildCount(); i++) {
            View dot = indicatorLayout.getChildAt(i);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dot.getLayoutParams();

            if (i == position) {
                // Active dot - elongated pill shape
                dot.setBackgroundResource(R.drawable.dot_active_enhanced);

                ValueAnimator widthAnimator = ValueAnimator.ofInt(
                        params.width,
                        (int) (getResources().getDimensionPixelSize(R.dimen.dot_size) * 3)
                );
                widthAnimator.addUpdateListener(animation -> {
                    params.width = (int) animation.getAnimatedValue();
                    dot.setLayoutParams(params);
                });
                widthAnimator.setDuration(300);
                widthAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                widthAnimator.start();

                dot.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .start();
            } else {
                // Inactive dot - small circle
                dot.setBackgroundResource(R.drawable.dot_inactive_enhanced);

                ValueAnimator widthAnimator = ValueAnimator.ofInt(
                        params.width,
                        getResources().getDimensionPixelSize(R.dimen.dot_size)
                );
                widthAnimator.addUpdateListener(animation -> {
                    params.width = (int) animation.getAnimatedValue();
                    dot.setLayoutParams(params);
                });
                widthAnimator.setDuration(300);
                widthAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                widthAnimator.start();

                dot.animate()
                        .alpha(0.6f)
                        .setDuration(300)
                        .start();
            }
        }
    }

    private void updateProgress(int position) {
        String progress = String.format("%d/%d", position + 1, pages.size());
        progressText.setText(progress);

        // Animate progress text
        progressText.setScaleX(0.8f);
        progressText.setScaleY(0.8f);
        progressText.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .start();
    }

    private void updateProgressBar(float progress) {
        ViewGroup.LayoutParams params = progressFill.getLayoutParams();
        int maxWidth = ((View) progressFill.getParent()).getWidth();
        int targetWidth = (int) (maxWidth * progress);

        ObjectAnimator widthAnimator = (ObjectAnimator) ObjectAnimator.ofInt(params.width, targetWidth);
        widthAnimator.addUpdateListener(animation -> {
            params.width = (int) animation.getAnimatedValue();
            progressFill.setLayoutParams(params);
        });
        widthAnimator.setDuration(300);
        widthAnimator.setInterpolator(new DecelerateInterpolator());
        widthAnimator.start();
    }

    private void updateBackgroundGradient(int position) {
        if (position >= 0 && position < pages.size()) {
            OnboardingPage_enhanced page = pages.get(position);
            if (page.hasCustomBackground()) {
                // Smooth transition to new background
                mainContainer.animate()
                        .alpha(0.95f)
                        .setDuration(150)
                        .withEndAction(() -> {
                            mainContainer.setBackgroundResource(page.getBackgroundResId());
                            mainContainer.animate()
                                    .alpha(1f)
                                    .setDuration(150)
                                    .start();
                        })
                        .start();
            }
        }
    }

    private void setupListeners() {
        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < pages.size() - 1) {
                viewPager.setCurrentItem(current + 1, true);
                animateButton(btnNext, false);
            } else {
                completeOnboarding();
            }
        });

        btnSkip.setOnClickListener(v -> {
            animateButton(btnSkip, false);
            handler.postDelayed(this::completeOnboarding, 150);
        });
    }

    private void animateButton(View button, boolean emphasize) {
        if (emphasize) {
            // Pulse animation for "Get Started" button
            button.animate()
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        button.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(200)
                                .start();
                    })
                    .start();
        } else {
            // Simple press animation
            button.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        button.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();
                    })
                    .start();
        }
    }

    private void completeOnboarding() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREF_FIRST_TIME, false);
        editor.apply();

        Intent intent = new Intent(OnboardingActivity_enhanced.this, SetupActivity_Enhanced.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void animateIcon(int position) {
        // Find the current page's icon container
        handler.postDelayed(() -> {
            View pageView = findViewPagerChildByTag("page_" + position);
            if (pageView != null) {
                View iconContainer = pageView.findViewWithTag("icon_" + position);
                if (iconContainer != null && adapter != null) {
                    adapter.animateIconBounce(iconContainer);
                }
            }
        }, 200);
    }

    private View findViewPagerChildByTag(String tag) {
        for (int i = 0; i < viewPager.getChildCount(); i++) {
            View child = viewPager.getChildAt(i);
            if (child instanceof androidx.recyclerview.widget.RecyclerView) {
                androidx.recyclerview.widget.RecyclerView recyclerView =
                        (androidx.recyclerview.widget.RecyclerView) child;
                for (int j = 0; j < recyclerView.getChildCount(); j++) {
                    View item = recyclerView.getChildAt(j);
                    if (tag.equals(item.getTag())) {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Animate current icon after a short delay
        handler.postDelayed(() -> animateIcon(viewPager.getCurrentItem()), 300);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}