package com.example.productexpirationtrackerapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConsumedHistoryAdapter extends RecyclerView.Adapter<ConsumedHistoryAdapter.ViewHolder> {

    private List<ConsumedProduct> consumedProducts = new ArrayList<>();
    private Context context;
    private OnItemClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private boolean isDarkTheme = false;

    public interface OnItemClickListener {
        void onItemClick(ConsumedProduct product);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ConsumedHistoryAdapter(Context context) {
        this.context = context;
        // Load theme preference
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String theme = prefs.getString("color_theme", "light");
        isDarkTheme = theme.equals("dark") || theme.equals("black");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_consumed_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConsumedProduct product = consumedProducts.get(position);

        // Set text colors based on theme
        int primaryTextColor;
        int secondaryTextColor;

        if (isDarkTheme) {
            primaryTextColor = Color.WHITE;
            secondaryTextColor = Color.LTGRAY;
        } else {
            primaryTextColor = Color.parseColor("#333333");
            secondaryTextColor = Color.parseColor("#666666");
        }

        // Set product name with theme color
        holder.productName.setText(product.getProductName());
        holder.productName.setTextColor(primaryTextColor);

        // Set category with theme color
        String category = product.getCategory();
        holder.category.setText(category != null && !category.isEmpty() ? category : "No category");
        holder.category.setTextColor(secondaryTextColor);

        // Set quantity
        String quantity = product.getQuantity();
        if (quantity != null && !quantity.isEmpty()) {
            holder.quantity.setVisibility(View.VISIBLE);
            holder.quantity.setText("Qty: " + quantity);
            holder.quantity.setTextColor(secondaryTextColor);
        } else {
            holder.quantity.setVisibility(View.GONE);
        }

        // Set action type with color (stays green/red regardless of theme)
        String actionType = product.getActionType();
        holder.actionType.setText(actionType);

        if ("CONSUMED".equals(actionType)) {
            holder.actionType.setBackgroundColor(Color.parseColor("#4CAF50")); // Green stays green
        } else {
            holder.actionType.setBackgroundColor(Color.parseColor("#F44336")); // Red stays red
        }
        holder.actionType.setTextColor(Color.WHITE);

        // Set dates with theme colors
        holder.actionDate.setText("Action: " + dateFormat.format(product.getActionDate()));
        holder.actionDate.setTextColor(secondaryTextColor);

        holder.expiryDate.setText("Expired: " + dateFormat.format(product.getExpiryDate()));
        holder.expiryDate.setTextColor(secondaryTextColor);

        // Set product image
        if (product.hasPhoto()) {
            byte[] photoBytes = product.getPhoto();
            Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
            holder.productImage.setImageBitmap(bitmap);
        } else {
            // Set placeholder based on category
            int placeholderRes = getPlaceholderForCategory(product.getCategory());
            holder.productImage.setImageResource(placeholderRes);
        }

        // Set card background based on theme (light or dark)
        if (isDarkTheme) {
            holder.itemView.setBackgroundColor(Color.parseColor("#2D2D2D")); // Dark gray for dark theme
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE); // White for light theme
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(product);
            }
        });
    }

    private int getPlaceholderForCategory(String category) {
        if (category == null) return R.drawable.ic_default_product;

        switch (category) {
            case "Dairy":
            case "Vegetables":
            case "Fruits":
            case "Meats":
                return R.drawable.ic_food_placeholder;
            case "Beverages":
                return R.drawable.ic_drinks_placeholder;
            case "Medicine":
                return R.drawable.ic_medicine_placeholder;
            case "Other":
                return R.drawable.ic_other_placeholder;
            default:
                return R.drawable.ic_default_product;
        }
    }

    @Override
    public int getItemCount() {
        return consumedProducts.size();
    }

    public void setConsumedProducts(List<ConsumedProduct> products) {
        this.consumedProducts = products;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView category;
        TextView quantity;
        TextView actionType;
        TextView actionDate;
        TextView expiryDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            category = itemView.findViewById(R.id.category);
            quantity = itemView.findViewById(R.id.quantity);
            actionType = itemView.findViewById(R.id.actionType);
            actionDate = itemView.findViewById(R.id.actionDate);
            expiryDate = itemView.findViewById(R.id.expiryDate);
        }
    }
}