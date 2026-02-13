package com.example.productexpirationtrackerapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ProductRecyclerAdapter extends RecyclerView.Adapter<ProductRecyclerAdapter.ProductViewHolder> {

    private List<Product> products = new ArrayList<>();
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product currentProduct = products.get(position);

        // Set product name
        holder.productName.setText(currentProduct.getName());

        // Set expiry date
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String expiryDate = sdf.format(currentProduct.getExpiryDate());
        holder.productExpiry.setText("Expires: " + expiryDate);

        // Load product image from byte array
        if (currentProduct.hasPhoto()) {
            // Convert byte array to Bitmap
            byte[] photoBytes = currentProduct.getPhoto();
            Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
            holder.productImage.setImageBitmap(bitmap);
        } else {
            // If no image, show category-based placeholder
            String category = currentProduct.getCategory();
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
        }

        // Calculate days until expiry and set days left text
        long daysUntilExpiry = calculateDaysUntilExpiry(currentProduct.getExpiryDate());
        if (daysUntilExpiry < 0) {
            holder.productDaysLeft.setText("Expired!");
            holder.productDaysLeft.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_red_dark));
            holder.productDaysLeft.setBackgroundColor(holder.itemView.getContext().getColor(android.R.color.holo_red_light));
            holder.statusIndicator.setBackgroundColor(holder.itemView.getContext().getColor(android.R.color.holo_red_dark));
        } else if (daysUntilExpiry <= 3) {
            holder.productDaysLeft.setText(daysUntilExpiry + " days left ⚠️");
            holder.productDaysLeft.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));
            holder.productDaysLeft.setBackgroundColor(holder.itemView.getContext().getColor(android.R.color.holo_orange_light));
            holder.statusIndicator.setBackgroundColor(holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));
        } else {
            holder.productDaysLeft.setText(daysUntilExpiry + " days left");
            holder.productDaysLeft.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
            holder.productDaysLeft.setBackgroundColor(holder.itemView.getContext().getColor(android.R.color.holo_green_light));
            holder.statusIndicator.setBackgroundColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    private long calculateDaysUntilExpiry(Date expiryDate) {
        if (expiryDate == null) return 0;

        Date today = new Date();
        long diffInMillies = expiryDate.getTime() - today.getTime();
        return TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView productImage;  // Make sure this matches your layout ID
        private TextView productName;
        private TextView productExpiry;
        private TextView productDaysLeft;
        private View statusIndicator;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views with correct IDs from your layout
            productImage = itemView.findViewById(R.id.productImage);  // Changed from 'producticon' to 'productImage'
            productName = itemView.findViewById(R.id.productName);
            productExpiry = itemView.findViewById(R.id.productExpiry);
            productDaysLeft = itemView.findViewById(R.id.productDaysLeft);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition(); // FIXED: Use getAdapterPosition() instead of storing position
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(products.get(position));
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition(); // FIXED: Use getAdapterPosition() instead of storing position
                    if (longClickListener != null && position != RecyclerView.NO_POSITION) {
                        longClickListener.onItemLongClick(products.get(position));
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Product product);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }
}