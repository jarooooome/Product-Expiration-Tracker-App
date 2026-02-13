package com.example.productexpirationtrackerapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

        // Set product data
        holder.productName.setText(product.getName());
        holder.productExpiry.setText("Expires: " + product.getFormattedExpiryDate());

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
                    case "Food":
                        placeholderRes = R.drawable.ic_food_placeholder;
                        break;
                    case "Medicine":
                        placeholderRes = R.drawable.ic_medicine_placeholder;
                        break;
                    case "Drinks":
                        placeholderRes = R.drawable.ic_drinks_placeholder;
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
        ImageView productImage;  // CHANGED: from TextView productIcon to ImageView productImage
        TextView productName;
        TextView productExpiry;
        TextView productDaysLeft;
        View statusIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemLayout = itemView.findViewById(R.id.productItemLayout);
            productImage = itemView.findViewById(R.id.productImage);  // CHANGED: from productIcon to productImage
            productName = itemView.findViewById(R.id.productName);
            productExpiry = itemView.findViewById(R.id.productExpiry);
            productDaysLeft = itemView.findViewById(R.id.productDaysLeft);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
        }
    }
}