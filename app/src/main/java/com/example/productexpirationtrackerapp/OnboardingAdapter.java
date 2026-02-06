package com.example.productexpirationtrackerapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.ViewHolder> {

    private List<OnboardingPage> pages;

    public OnboardingAdapter(List<OnboardingPage> pages) {
        this.pages = pages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onboarding_page, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OnboardingPage page = pages.get(position);
        holder.iconText.setText(page.getIcon());
        holder.titleText.setText(page.getTitle());
        holder.descText.setText(page.getDescription());

        // Tag for finding this view later for animation
        holder.itemView.setTag("page_" + position);
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView iconText;
        TextView titleText;
        TextView descText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconText = itemView.findViewById(R.id.iconText);
            titleText = itemView.findViewById(R.id.titleText);
            descText = itemView.findViewById(R.id.descText);
        }
    }
}