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
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private ArrayList<Product> productList;
    private OnItemClickListener listener;
    private List<Integer> selectedPositions = new ArrayList<>();

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setSelectedPositions(List<Integer> positions) {
        if (positions == null) {
            this.selectedPositions.clear();
        } else {
            this.selectedPositions = positions;
        }
        notifyDataSetChanged();
    }

    public ProductAdapter(ArrayList<Product> productList) {
        this.productList = productList;
    }

    // Helper method to darken a color
    private int darkenColor(int color, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = hsv[2] * factor;
        return Color.HSVToColor(hsv);
    }

    // Helper method to get theme background color
    private int getThemeBackgroundColor(Context context, String theme) {
        switch (theme) {
            case "green":
                return ContextCompat.getColor(context, R.color.color_background_green);
            case "blue":
                return ContextCompat.getColor(context, R.color.color_background_blue);
            case "pink":
                return ContextCompat.getColor(context, R.color.color_background_pink);
            case "purple":
                return ContextCompat.getColor(context, R.color.color_background_purple);
            case "black":
                return ContextCompat.getColor(context, R.color.color_background_black);
            case "white":
            default:
                return ContextCompat.getColor(context, R.color.color_background_white);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        Context context = holder.itemView.getContext();

        // LOAD THEME HERE - EVERY TIME!
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String theme = prefs.getString("color_theme", "white");

        // Get theme background color
        int themeBackgroundColor = getThemeBackgroundColor(context, theme);

        // Make product background DARKER (70% of original)
        int productBackgroundColor = darkenColor(themeBackgroundColor, 0.7f);

        // Set product background
        holder.itemLayout.setBackgroundColor(productBackgroundColor);

        // Check if this item is selected
        boolean isSelected = selectedPositions.contains(position);

        // Set selection overlay
        if (isSelected) {
            // Selected state - highlight with semi-transparent overlay
            holder.itemLayout.setBackgroundColor(Color.parseColor("#805A9DFF")); // Light blue with transparency
        } else {
            // Normal state - use theme background
            holder.itemLayout.setBackgroundColor(productBackgroundColor);
        }

        // Text colors based on theme
        boolean isBlackTheme = theme.equals("black");
        int textColor;
        int secondaryTextColor;

        if (isBlackTheme) {
            textColor = Color.WHITE;
            secondaryTextColor = Color.LTGRAY;
        } else {
            textColor = Color.parseColor("#333333");
            secondaryTextColor = Color.parseColor("#666666");
        }

        // Set product data
        holder.productName.setText(product.getName());
        holder.productName.setTextColor(textColor);

        holder.productExpiry.setText("Expires: " + product.getFormattedExpiryDate());
        holder.productExpiry.setTextColor(secondaryTextColor);

        // Load product image
        if (product.hasPhoto()) {
            byte[] photoBytes = product.getPhoto();
            Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
            holder.productImage.setImageBitmap(bitmap);
            holder.productImage.setVisibility(View.VISIBLE);
        } else {
            String category = product.getCategory();
            int placeholderRes = R.drawable.ic_default_product;

            if (category != null) {
                switch (category) {
                    case "Dairy":
                    case "Vegetables":
                    case "Fruits":
                    case "Meats":
                        placeholderRes = R.drawable.ic_food_placeholder;
                        break;
                    case "Beverages":
                        placeholderRes = R.drawable.ic_drinks_placeholder;
                        break;
                    case "Medicine":
                        placeholderRes = R.drawable.ic_medicine_placeholder;
                        break;
                    case "Other":
                        placeholderRes = R.drawable.ic_other_placeholder;
                        break;
                }
            }
            holder.productImage.setImageResource(placeholderRes);
            holder.productImage.setVisibility(View.VISIBLE);
        }

        // Calculate days left
        Date now = new Date();
        long timeDiff = product.getExpiryDate().getTime() - now.getTime();
        long daysLeft = TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);

        // Set days left text and color
        if (daysLeft < 0) {
            holder.productDaysLeft.setText("EXPIRED");
            holder.productDaysLeft.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
            holder.productDaysLeft.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_red_light));
            holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
        } else if (daysLeft <= 3) {
            holder.productDaysLeft.setText(daysLeft + " days left ⚠️");
            holder.productDaysLeft.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark));
            holder.productDaysLeft.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_orange_light));
            holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark));
        } else {
            holder.productDaysLeft.setText(daysLeft + " days left");
            holder.productDaysLeft.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
            holder.productDaysLeft.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_light));
            holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
        }

        // Click listeners
        holder.itemLayout.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                listener.onItemClick(adapterPosition);
            }
        });

        holder.itemLayout.setOnLongClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                listener.onItemLongClick(adapterPosition);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateData(ArrayList<Product> newList) {
        this.productList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout itemLayout;
        ImageView productImage;
        TextView productName;
        TextView productExpiry;
        TextView productDaysLeft;
        View statusIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemLayout = itemView.findViewById(R.id.productItemLayout);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productExpiry = itemView.findViewById(R.id.productExpiry);
            productDaysLeft = itemView.findViewById(R.id.productDaysLeft);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
        }
    }
}