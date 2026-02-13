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
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private ArrayList<Product> productList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ProductAdapter(ArrayList<Product> productList) {
        this.productList = productList;
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

        // ✅ CHECK CURRENT THEME EVERY TIME!
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String theme = prefs.getString("color_theme", "white");
        boolean isBlackTheme = theme.equals("black");

        // Set text colors based on current theme
        int textColor;
        int secondaryTextColor;

        if (isBlackTheme) {
            textColor = Color.WHITE;           // Primary text: WHITE
            secondaryTextColor = Color.LTGRAY;  // Secondary text: LIGHT GRAY
        } else {
            textColor = Color.parseColor("#333333");  // Dark gray for light themes
            secondaryTextColor = Color.parseColor("#666666"); // Medium gray
        }

        // Set product data with theme-aware colors
        holder.productName.setText(product.getName());
        holder.productName.setTextColor(textColor);

        holder.productExpiry.setText("Expires: " + product.getFormattedExpiryDate());
        holder.productExpiry.setTextColor(secondaryTextColor);

        // Load product image from byte array
        if (product.hasPhoto()) {
            // Convert byte array to Bitmap
            byte[] photoBytes = product.getPhoto();
            Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
            holder.productImage.setImageBitmap(bitmap);
            holder.productImage.setVisibility(View.VISIBLE);
        } else {
            // If no image, show category-based placeholder
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

        // Set days left text and color (these stay the same for all themes)
        if (daysLeft < 0) {
            holder.productDaysLeft.setText("EXPIRED");
            holder.productDaysLeft.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark));
            holder.productDaysLeft.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_light));
            holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark));
        } else if (daysLeft <= 3) {
            holder.productDaysLeft.setText(daysLeft + " days left ⚠️");
            holder.productDaysLeft.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_dark));
            holder.productDaysLeft.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_light));
            holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_dark));
        } else {
            holder.productDaysLeft.setText(daysLeft + " days left");
            holder.productDaysLeft.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_dark));
            holder.productDaysLeft.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_light));
            holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_dark));
        }

        // FIXED: Use getAdapterPosition() instead of storing position
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onItemClick(adapterPosition);
                }
            }
        });

        holder.itemLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onItemLongClick(adapterPosition);
                    return true;
                }
                return false;
            }
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