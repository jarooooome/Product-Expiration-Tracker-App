package com.example.productexpirationtrackerapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        // Use your custom layout instead of android.R.layout.simple_list_item_1
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

        // Get first emoji/icon from product name
        if (product.getName().length() > 0) {
            String firstChar = product.getName().substring(0, 2); // Get first 2 chars (emoji)
            holder.productIcon.setText(firstChar);
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
            holder.productDaysLeft.setText(daysLeft + " days left");
            holder.productDaysLeft.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_dark));
            holder.productDaysLeft.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_light));
            holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_dark));
        } else if (daysLeft <= 7) {
            holder.productDaysLeft.setText(daysLeft + " days left");
            holder.productDaysLeft.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_light));
            holder.productDaysLeft.setBackgroundColor(0xFFFBE9E7);
            holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_light));
        } else {
            holder.productDaysLeft.setText(daysLeft + " days left");
            holder.productDaysLeft.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_dark));
            holder.productDaysLeft.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_light));
            holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_dark));
        }

        // Set click listener on the entire item
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(position);
                }
            }
        });

        // Set long click listener
        holder.itemLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.onItemLongClick(position);
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout itemLayout;
        TextView productIcon;
        TextView productName;
        TextView productExpiry;
        TextView productDaysLeft;
        View statusIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemLayout = itemView.findViewById(R.id.productItemLayout);
            productIcon = itemView.findViewById(R.id.productIcon);
            productName = itemView.findViewById(R.id.productName);
            productExpiry = itemView.findViewById(R.id.productExpiry);
            productDaysLeft = itemView.findViewById(R.id.productDaysLeft);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
        }
    }
}