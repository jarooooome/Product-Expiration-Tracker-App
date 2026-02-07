package com.example.productexpirationtrackerapp;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ProductGridAdapter extends RecyclerView.Adapter<ProductGridAdapter.ProductViewHolder> {

    private final Context context;
    private final List<Product> productList;
    private final OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductGridAdapter(Context context, List<Product> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_grid, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        // Set product name
        holder.productName.setText(product.getName());

        // Set quantity
        if (product.getQuantity() > 1) {
            holder.quantityText.setText("×" + product.getQuantity());
            holder.quantityBadge.setVisibility(View.VISIBLE);
        } else {
            holder.quantityBadge.setVisibility(View.GONE);
        }

        // Set category (optional)
        if (product.getCategory() != null && !product.getCategory().isEmpty()) {
            holder.categoryText.setText(product.getCategory());
            holder.categoryText.setVisibility(View.VISIBLE);
        } else {
            holder.categoryText.setVisibility(View.GONE);
        }

        // Calculate days until expiry
        long daysUntilExpiry = calculateDaysUntilExpiry(product.getExpiryDate());

        // Set product icon (emoji based on category or default)
        String icon = getIconForCategory(product.getCategory());
        holder.productIcon.setText(icon);

        // Update card styling based on expiry status
        updateCardStatus(holder, daysUntilExpiry, product);

        // Entrance animation
        animateCard(holder.itemView, position);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Add click animation
                animateClick(holder.itemView);
                listener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    private void updateCardStatus(ProductViewHolder holder, long daysUntilExpiry, Product product) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String formattedDate = sdf.format(product.getExpiryDate());

        if (daysUntilExpiry < 0) {
            // EXPIRED
            holder.cardBackground.setBackgroundResource(R.drawable.card_gradient_expired);
            holder.statusBadge.setBackgroundResource(R.drawable.badge_expired);
            holder.statusText.setText("EXPIRED");
            holder.expiryDaysText.setText(Math.abs(daysUntilExpiry) + " days ago");
            holder.expiryDateText.setText("Expired: " + formattedDate);

        } else if (daysUntilExpiry == 0) {
            // EXPIRES TODAY
            holder.cardBackground.setBackgroundResource(R.drawable.card_gradient_expired);
            holder.statusBadge.setBackgroundResource(R.drawable.badge_expired);
            holder.statusText.setText("TODAY");
            holder.expiryDaysText.setText("Expires today!");
            holder.expiryDateText.setText(formattedDate);

        } else if (daysUntilExpiry == 1) {
            // EXPIRES TOMORROW
            holder.cardBackground.setBackgroundResource(R.drawable.card_gradient_warning);
            holder.statusBadge.setBackgroundResource(R.drawable.badge_warning);
            holder.statusText.setText("TOMORROW");
            holder.expiryDaysText.setText("1 day left");
            holder.expiryDateText.setText("Expires: " + formattedDate);

        } else if (daysUntilExpiry <= 7) {
            // EXPIRING SOON (2-7 days)
            holder.cardBackground.setBackgroundResource(R.drawable.card_gradient_warning);
            holder.statusBadge.setBackgroundResource(R.drawable.badge_warning);
            holder.statusText.setText("EXPIRING SOON");
            holder.expiryDaysText.setText(daysUntilExpiry + " days left");
            holder.expiryDateText.setText("Expires: " + formattedDate);

        } else {
            // FRESH (>7 days)
            holder.cardBackground.setBackgroundResource(R.drawable.card_gradient_fresh);
            holder.statusBadge.setBackgroundResource(R.drawable.badge_fresh);
            holder.statusText.setText("FRESH");
            holder.expiryDaysText.setText(daysUntilExpiry + " days left");
            holder.expiryDateText.setText("Expires: " + formattedDate);
        }
    }

    private long calculateDaysUntilExpiry(Date expiryDate) {
        if (expiryDate == null) {
            return 999;
        }

        try {
            // Create Calendar for expiry date and reset time to midnight
            Calendar expiryCal = Calendar.getInstance();
            expiryCal.setTime(expiryDate);
            expiryCal.set(Calendar.HOUR_OF_DAY, 0);
            expiryCal.set(Calendar.MINUTE, 0);
            expiryCal.set(Calendar.SECOND, 0);
            expiryCal.set(Calendar.MILLISECOND, 0);

            // Create Calendar for today and reset time to midnight
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            // Calculate difference in milliseconds and convert to days
            long diffInMillis = expiryCal.getTimeInMillis() - today.getTimeInMillis();
            return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            android.util.Log.e("ProductGridAdapter", "Error calculating days", e);
            return 999;
        }
    }

    private String getIconForCategory(String category) {
        if (category == null || category.isEmpty()) {
            return "📦";
        }

        String lowerCategory = category.toLowerCase();

        // Food categories
        if (lowerCategory.contains("dairy")) return "🥛";
        if (lowerCategory.contains("meat") || lowerCategory.contains("fish")) return "🍖";
        if (lowerCategory.contains("fruit")) return "🍎";
        if (lowerCategory.contains("vegetable")) return "🥬";
        if (lowerCategory.contains("bread") || lowerCategory.contains("bakery")) return "🍞";
        if (lowerCategory.contains("snack")) return "🍪";
        if (lowerCategory.contains("frozen")) return "🧊";
        if (lowerCategory.contains("canned")) return "🥫";

        // Drinks
        if (lowerCategory.contains("drink") || lowerCategory.contains("beverage")) return "🥤";
        if (lowerCategory.contains("juice")) return "🧃";
        if (lowerCategory.contains("water")) return "💧";
        if (lowerCategory.contains("coffee") || lowerCategory.contains("tea")) return "☕";

        // Medicine & Health
        if (lowerCategory.contains("medicine") || lowerCategory.contains("medication")) return "💊";
        if (lowerCategory.contains("vitamin") || lowerCategory.contains("supplement")) return "💊";

        // Personal Care
        if (lowerCategory.contains("cosmetic") || lowerCategory.contains("beauty")) return "💄";
        if (lowerCategory.contains("skincare")) return "🧴";

        // Default
        return "📦";
    }

    private void animateCard(View view, int position) {
        // Fade in + Scale up animation
        view.setAlpha(0f);
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);

        view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setStartDelay(position * 50L) // Stagger animation
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    private void animateClick(View view) {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f);
        scaleDownX.setDuration(100);
        scaleDownY.setDuration(100);

        scaleDownX.start();
        scaleDownY.start();

        view.postDelayed(() -> {
            ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1f);
            ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1f);
            scaleUpX.setDuration(100);
            scaleUpY.setDuration(100);
            scaleUpX.start();
            scaleUpY.start();
        }, 100);
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        FrameLayout cardBackground;
        LinearLayout statusBadge;
        LinearLayout quantityBadge;
        TextView statusText;
        TextView quantityText;
        TextView productIcon;
        ImageView productImage;
        TextView productName;
        TextView categoryText;
        TextView expiryDaysText;
        TextView expiryDateText;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            cardBackground = itemView.findViewById(R.id.cardBackground);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            quantityBadge = itemView.findViewById(R.id.quantityBadge);
            statusText = itemView.findViewById(R.id.statusText);
            quantityText = itemView.findViewById(R.id.quantityText);
            productIcon = itemView.findViewById(R.id.productIcon);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            categoryText = itemView.findViewById(R.id.categoryText);
            expiryDaysText = itemView.findViewById(R.id.expiryDaysText);
            expiryDateText = itemView.findViewById(R.id.expiryDateText);
        }
    }
}