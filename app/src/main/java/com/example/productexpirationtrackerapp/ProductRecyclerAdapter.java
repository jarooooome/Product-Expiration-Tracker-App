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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ProductRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_GRID = 0;
    private static final int VIEW_TYPE_LIST = 1;

    private Context context;
    private List<Product> productList;
    private int currentViewType = VIEW_TYPE_GRID; // Default to grid view
    private OnProductClickListener clickListener;
    private OnProductLongClickListener longClickListener;

    // Interfaces for click listeners
    public interface OnProductClickListener {
        void onProductClick(Product product, int position);
    }

    public interface OnProductLongClickListener {
        void onProductLongClick(Product product, int position);
    }

    public ProductRecyclerAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList != null ? productList : new ArrayList<>();
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnProductLongClickListener(OnProductLongClickListener listener) {
        this.longClickListener = listener;
    }

    // Toggle between grid and list view
    public void setViewType(int viewType) {
        this.currentViewType = viewType;
        notifyDataSetChanged();
    }

    public int getViewType() {
        return currentViewType;
    }

    @Override
    public int getItemViewType(int position) {
        return currentViewType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_GRID) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_product_grid, parent, false);
            return new GridViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_product_list, parent, false);
            return new ListViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Product product = productList.get(position);

        if (holder instanceof GridViewHolder) {
            bindGridView((GridViewHolder) holder, product, position);
        } else if (holder instanceof ListViewHolder) {
            bindListView((ListViewHolder) holder, product, position);
        }
    }

    private void bindGridView(GridViewHolder holder, Product product, int position) {
        holder.productName.setText(product.getName());

        // Format expiry date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        holder.expiryDate.setText("Expires: " + sdf.format(product.getExpiryDate()));

        // Calculate days left
        long daysLeft = getDaysUntilExpiry(product.getExpiryDate());
        holder.daysLeft.setText(getDaysLeftText(daysLeft));

        // Set category badge
        holder.categoryBadge.setText(product.getCategory());
        holder.categoryBadge.setBackgroundColor(getCategoryColor(product.getCategory()));

        // Set status color
        int statusColor = getStatusColor(daysLeft);
        holder.statusDot.setBackgroundColor(statusColor);
        holder.daysLeft.setTextColor(statusColor);

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onProductClick(product, position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onProductLongClick(product, position);
            }
            return true;
        });
    }

    private void bindListView(ListViewHolder holder, Product product, int position) {
        holder.productName.setText(product.getName());

        // Format expiry date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        holder.expiryDate.setText("Expires: " + sdf.format(product.getExpiryDate()));

        // Calculate days left
        long daysLeft = getDaysUntilExpiry(product.getExpiryDate());
        holder.daysLeft.setText(getDaysLeftText(daysLeft));

        // Set category badge
        holder.categoryBadge.setText(product.getCategory());
        holder.categoryBadge.setBackgroundColor(getCategoryColor(product.getCategory()));

        // Set status color
        int statusColor = getStatusColor(daysLeft);
        holder.statusDot.setBackgroundColor(statusColor);
        holder.daysLeft.setTextColor(statusColor);

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onProductClick(product, position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onProductLongClick(product, position);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateData(List<Product> newProductList) {
        this.productList = newProductList != null ? newProductList : new ArrayList<>();
        notifyDataSetChanged();
    }

    // Helper methods
    private long getDaysUntilExpiry(Date expiryDate) {
        long diff = expiryDate.getTime() - System.currentTimeMillis();
        return TimeUnit.MILLISECONDS.toDays(diff);
    }

    private String getDaysLeftText(long daysLeft) {
        if (daysLeft < 0) {
            return "Expired";
        } else if (daysLeft == 0) {
            return "Expires today";
        } else if (daysLeft == 1) {
            return "1 day left";
        } else {
            return daysLeft + " days left";
        }
    }

    private int getStatusColor(long daysLeft) {
        if (daysLeft < 0) {
            return Color.parseColor("#F44336"); // Red - Expired
        } else if (daysLeft <= 3) {
            return Color.parseColor("#FF9800"); // Orange - Expiring soon
        } else if (daysLeft <= 7) {
            return Color.parseColor("#FFC107"); // Amber - Warning
        } else {
            return Color.parseColor("#4CAF50"); // Green - Fresh
        }
    }

    private int getCategoryColor(String category) {
        switch (category.toLowerCase()) {
            case "food":
                return Color.parseColor("#4CAF50"); // Green
            case "medicine":
                return Color.parseColor("#2196F3"); // Blue
            case "drinks":
                return Color.parseColor("#FF9800"); // Orange
            case "other":
                return Color.parseColor("#9C27B0"); // Purple
            default:
                return Color.parseColor("#757575"); // Gray
        }
    }

    // ViewHolder for Grid View
    static class GridViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView expiryDate;
        TextView daysLeft;
        TextView categoryBadge;
        View statusDot;

        GridViewHolder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImageGrid);
            productName = itemView.findViewById(R.id.productNameGrid);
            expiryDate = itemView.findViewById(R.id.expiryDateGrid);
            daysLeft = itemView.findViewById(R.id.daysLeftGrid);
            categoryBadge = itemView.findViewById(R.id.categoryBadgeGrid);
            statusDot = itemView.findViewById(R.id.statusDotGrid);
        }
    }

    // ViewHolder for List View
    static class ListViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView expiryDate;
        TextView daysLeft;
        TextView categoryBadge;
        View statusDot;

        ListViewHolder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImageList);
            productName = itemView.findViewById(R.id.productNameList);
            expiryDate = itemView.findViewById(R.id.expiryDateList);
            daysLeft = itemView.findViewById(R.id.daysLeftList);
            categoryBadge = itemView.findViewById(R.id.categoryBadgeList);
            statusDot = itemView.findViewById(R.id.statusDotList);
        }
    }
}