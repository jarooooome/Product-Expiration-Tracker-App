package com.example.productexpirationtrackerapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private List<Product> productList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ProductAdapter(List<Product> productList) {
        this.productList = productList != null ? productList : new ArrayList<>();
    }

    public void updateList(List<Product> newList) {
        this.productList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
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
        // Fix: Use getAdapterPosition() instead of captured position
        int adapterPosition = holder.getAdapterPosition();
        if (adapterPosition == RecyclerView.NO_POSITION) {
            return;
        }

        Product product = productList.get(adapterPosition);

        // Set product data
        holder.productName.setText(product.getName());

        // Fix: Use string resource instead of concatenation
        String expiryText = holder.itemView.getContext().getString(
                R.string.expires_format,
                product.getFormattedExpiryDate()
        );
        holder.productExpiry.setText(expiryText);

        // Get first character from product name for icon
        if (product.getName() != null && !product.getName().isEmpty()) {
            String firstChar = product.getName().substring(0, 1);
            holder.productIcon.setText(firstChar);
        } else {
            holder.productIcon.setText("📦"); // Default icon
        }

        // Calculate days left
        Date now = new Date();
        long timeDiff = product.getExpiryDate().getTime() - now.getTime();
        long daysLeft = TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);

        // Set days left text and color
        if (daysLeft < 0) {
            holder.productDaysLeft.setText(R.string.expired);
            holder.productDaysLeft.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark));
            holder.productDaysLeft.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_light));
            holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark));
        } else if (daysLeft <= 3) {
            String daysLeftText = holder.itemView.getContext().getString(R.string.days_left, daysLeft);
            holder.productDaysLeft.setText(daysLeftText);
            holder.productDaysLeft.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_dark));
            holder.productDaysLeft.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_light));
            holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_dark));
        } else if (daysLeft <= 7) {
            String daysLeftText = holder.itemView.getContext().getString(R.string.days_left, daysLeft);
            holder.productDaysLeft.setText(daysLeftText);
            holder.productDaysLeft.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_light));
            holder.productDaysLeft.setBackgroundColor(0xFFFBE9E7);
            holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_light));
        } else {
            String daysLeftText = holder.itemView.getContext().getString(R.string.days_left, daysLeft);
            holder.productDaysLeft.setText(daysLeftText);
            holder.productDaysLeft.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_dark));
            holder.productDaysLeft.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_light));
            holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_dark));
        }

        // Fix: Use adapterPosition variable in click listeners
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onItemClick(pos);
                }
            }
        });

        holder.itemLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int pos = holder.getAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onItemLongClick(pos);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
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