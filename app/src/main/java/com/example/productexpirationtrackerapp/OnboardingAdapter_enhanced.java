package com.example.productexpirationtrackerapp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OnboardingAdapter_enhanced extends RecyclerView.Adapter<OnboardingAdapter_enhanced.ViewHolder> {

    private final List<OnboardingPage_enhanced> pages;

    public OnboardingAdapter_enhanced(List<OnboardingPage_enhanced> pages) {
        this.pages = pages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onboarding_page_enhanced, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OnboardingPage_enhanced page = pages.get(position);

        // Set title and description
        holder.titleText.setText(page.getTitle());
        holder.descText.setText(page.getDescription());

        // Set icon - prefer vector drawable over emoji
        if (page.hasVectorIcon()) {
            holder.iconImage.setVisibility(View.VISIBLE);
            holder.iconText.setVisibility(View.GONE);
            holder.iconImage.setImageResource(page.getIconResId());
        } else {
            holder.iconImage.setVisibility(View.GONE);
            holder.iconText.setVisibility(View.VISIBLE);
            holder.iconText.setText(page.getIcon());
        }

        // Set custom background if available
        if (page.hasCustomBackground()) {
            holder.itemView.setBackgroundResource(page.getBackgroundResId());
        }

        // Tag for finding this view later for animation
        holder.itemView.setTag("page_" + position);
        holder.iconContainer.setTag("icon_" + position);

        // Initial entrance animation
        animateEntrance(holder);
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    private void animateEntrance(ViewHolder holder) {
        // Fade in animation for all elements
        holder.iconContainer.setAlpha(0f);
        holder.titleText.setAlpha(0f);
        holder.descText.setAlpha(0f);

        // Scale animation for icon
        holder.iconContainer.setScaleX(0.3f);
        holder.iconContainer.setScaleY(0.3f);

        // Translate animation for text
        holder.titleText.setTranslationY(50f);
        holder.descText.setTranslationY(50f);

        // Animate icon
        holder.iconContainer.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setInterpolator(new OvershootInterpolator())
                .setStartDelay(100)
                .start();

        // Animate title
        holder.titleText.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setStartDelay(300)
                .start();

        // Animate description
        holder.descText.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setStartDelay(400)
                .start();
    }

    public void animateIconBounce(View iconContainer) {
        if (iconContainer == null) return;

        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(iconContainer, "scaleX", 1f, 1.15f, 0.95f, 1.05f, 1f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(iconContainer, "scaleY", 1f, 1.15f, 0.95f, 1.05f, 1f);
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(iconContainer, "rotation", 0f, -5f, 5f, -3f, 3f, 0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleXAnimator, scaleYAnimator, rotationAnimator);
        animatorSet.setDuration(600);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImage;
        TextView iconText;
        TextView titleText;
        TextView descText;
        View iconContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImage = itemView.findViewById(R.id.iconImage);
            iconText = itemView.findViewById(R.id.iconText);
            titleText = itemView.findViewById(R.id.titleText);
            descText = itemView.findViewById(R.id.descText);
            iconContainer = itemView.findViewById(R.id.iconContainer);
        }
    }
}