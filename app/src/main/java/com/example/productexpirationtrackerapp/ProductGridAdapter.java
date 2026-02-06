package com.example.productexpirationtrackerapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ProductGridAdapter extends RecyclerView.Adapter<ProductGridAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private OnProductClickListener listener;

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

        // Calculate days until expiry
        long daysUntilExpiry = calculateDaysUntilExpiry(product.getExpiryDate());

        // Set expiry info and warning indicators based on days remaining
        if (daysUntilExpiry < 0) {
            // Expired
            holder.expiryInfo.setText("Expired");
            holder.expiryInfo.setTextColor(Color.parseColor("#F44336")); // Red
            holder.redDot.setVisibility(View.VISIBLE);
            holder.yellowDot1.setVisibility(View.GONE);
            holder.yellowDot2.setVisibility(View.GONE);
        } else if (daysUntilExpiry == 0) {
            // Expires today
            holder.expiryInfo.setText("Expires: Today");
            holder.expiryInfo.setTextColor(Color.parseColor("#F44336")); // Red
            holder.redDot.setVisibility(View.VISIBLE);
            holder.yellowDot1.setVisibility(View.GONE);
            holder.yellowDot2.setVisibility(View.GONE);
        } else if (daysUntilExpiry == 1) {
            // Expires tomorrow
            holder.expiryInfo.setText("Expires: Tomorrow");
            holder.expiryInfo.setTextColor(Color.parseColor("#FF9800")); // Orange
            holder.redDot.setVisibility(View.VISIBLE);
            holder.yellowDot1.setVisibility(View.GONE);
            holder.yellowDot2.setVisibility(View.GONE);
        } else if (daysUntilExpiry <= 3) {
            // Expires in 2-3 days
            holder.expiryInfo.setText("Expires: " + daysUntilExpiry + " days");
            holder.expiryInfo.setTextColor(Color.parseColor("#FF9800")); // Orange
            holder.redDot.setVisibility(View.GONE);
            holder.yellowDot1.setVisibility(View.VISIBLE);
            holder.yellowDot2.setVisibility(View.VISIBLE);
        } else if (daysUntilExpiry <= 7) {
            // Expires in 4-7 days
            holder.expiryInfo.setText("Expires: " + daysUntilExpiry + " days");
            holder.expiryInfo.setTextColor(Color.parseColor("#FFC107")); // Yellow
            holder.redDot.setVisibility(View.GONE);
            holder.yellowDot1.setVisibility(View.VISIBLE);
            holder.yellowDot2.setVisibility(View.GONE);
        } else {
            // More than 7 days
            holder.expiryInfo.setText("Expires: " + product.getExpiryDate());
            holder.expiryInfo.setTextColor(Color.parseColor("#4CAF50")); // Green
            holder.redDot.setVisibility(View.GONE);
            holder.yellowDot1.setVisibility(View.GONE);
            holder.yellowDot2.setVisibility(View.GONE);
        }

        // Show category if available
        if (product.getCategory() != null && !product.getCategory().isEmpty()) {
            holder.categoryLabel.setText("Category: " + product.getCategory());
            holder.categoryLabel.setVisibility(View.VISIBLE);
        } else {
            holder.categoryLabel.setVisibility(View.GONE);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    /**
     * Calculate days until expiry using Calendar API (non-deprecated)
     * @param expiryDateStr Date string in format "yyyy-MM-dd"
     * @return Number of days until expiry (negative if expired)
     */
    private long calculateDaysUntilExpiry(String expiryDateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date expiryDate = sdf.parse(expiryDateStr);

            if (expiryDate == null) {
                return 999; // Return large number if parsing fails
            }

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

        } catch (ParseException e) {
            e.printStackTrace();
            return 999; // Return large number if parsing fails
        }
    }

    /**
     * ViewHolder class for product grid items
     */
    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView categoryLabel;
        TextView expiryInfo;
        View redDot;
        View yellowDot1;
        View yellowDot2;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            categoryLabel = itemView.findViewById(R.id.categoryLabel);
            expiryInfo = itemView.findViewById(R.id.expiryInfo);
            redDot = itemView.findViewById(R.id.redDot);
            yellowDot1 = itemView.findViewById(R.id.yellowDot1);
            yellowDot2 = itemView.findViewById(R.id.yellowDot2);
        }
    }
}