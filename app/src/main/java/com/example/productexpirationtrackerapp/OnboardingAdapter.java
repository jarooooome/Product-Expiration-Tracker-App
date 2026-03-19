package com.example.productexpirationtrackerapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.cardview.widget.CardView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private List<OnboardingPage> pages;

    // Per-screen exact background colors — chosen for maximum emoji contrast
    private static final int[] PAGE_COLORS = {
            0xFF1A3A2A, // Screen 1: Deep dark forest green — box pops like kraft paper on dark shelf
            0xFF0D1B4A, // Screen 2: Deep navy blue         — calendar stands out cleanly
            0xFF2C2000, // Screen 3: Near-black dark amber   — golden bell glows like a lit lantern
            0xFF1A1F3A  // Screen 4: Deep dark navy/indigo   — palette colors float vividly
    };

    public OnboardingAdapter(List<OnboardingPage> pages) {
        this.pages = pages;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onboarding_page, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        OnboardingPage page = pages.get(position);
        holder.titleText.setText(page.getTitle());
        holder.descriptionText.setText(page.getDescription());
        if (holder.iconView != null) holder.iconView.setImageResource(page.getIconResId());

        // Set exact per-screen dark background — emoji floats directly on it, no overlay
        if (holder.iconCard != null) {
            holder.iconCard.setCardBackgroundColor(PAGE_COLORS[position % PAGE_COLORS.length]);
        }
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, descriptionText;
        android.widget.ImageView iconView;
        CardView iconCard;
        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            iconView = (android.widget.ImageView) itemView.findViewById(R.id.iconView);
            iconCard = itemView.findViewById(R.id.iconCard);
        }
    }
}