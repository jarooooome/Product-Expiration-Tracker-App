package com.example.productexpirationtrackerapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private static final int VIEW_LIST = 0;
    private static final int VIEW_GRID = 1;

    private ArrayList<Product> productList;
    private boolean isGridView = false;
    private OnItemClickListener listener;
    private OnMenuClickListener menuListener;
    private List<Integer> selectedPositions = new ArrayList<>();

    // ── Interfaces ──────────────────────────────────────────────────────────

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }

    public interface OnMenuClickListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    // ── Constructor ─────────────────────────────────────────────────────────

    public ProductAdapter(ArrayList<Product> productList) {
        this.productList = productList;
    }

    // ── Setters ─────────────────────────────────────────────────────────────

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnMenuClickListener(OnMenuClickListener listener) {
        this.menuListener = listener;
    }

    public void setGridView(boolean grid) {
        this.isGridView = grid;
        notifyDataSetChanged();
    }

    public void setSelectedPositions(List<Integer> positions) {
        this.selectedPositions = positions != null ? positions : new ArrayList<>();
        notifyDataSetChanged();
    }

    // ── View type ───────────────────────────────────────────────────────────

    @Override
    public int getItemViewType(int position) {
        return isGridView ? VIEW_GRID : VIEW_LIST;
    }

    // ── Inflate ─────────────────────────────────────────────────────────────

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = (viewType == VIEW_GRID)
                ? R.layout.item_product_grid
                : R.layout.item_product;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    // ── Bind ────────────────────────────────────────────────────────────────

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        Context ctx = holder.itemView.getContext();

        // Read theme
        SharedPreferences prefs = ctx.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String theme = prefs.getString("color_theme", "white");
        boolean isDark = "black".equals(theme);

        int cardBg        = isDark ? Color.parseColor("#1E1E1E") : Color.WHITE;
        int textPrimary   = isDark ? Color.WHITE                 : Color.parseColor("#1A1E2C");
        int textSecondary = isDark ? Color.LTGRAY                : Color.parseColor("#8A8F9E");
        float dp          = ctx.getResources().getDisplayMetrics().density;

        // Card background (CardView)
        if (holder.productCard != null) {
            holder.productCard.setCardBackgroundColor(cardBg);
        }

        // Selection highlight on itemLayout
        if (holder.itemLayout != null) {
            if (selectedPositions.contains(position)) {
                holder.itemLayout.setBackgroundColor(Color.parseColor("#805A9DFF"));
            } else {
                holder.itemLayout.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        // Name
        if (holder.productName != null) {
            holder.productName.setText(product.getName());
            holder.productName.setTextColor(textPrimary);
        }

        // Expiry
        if (holder.productExpiry != null) {
            holder.productExpiry.setText("Expires: " + product.getFormattedExpiryDate());
            holder.productExpiry.setTextColor(textSecondary);
        }

        // ── Status badge ─────────────────────────────────────────────────
        if (holder.productDaysLeft != null && product.getExpiryDate() != null) {
            Date today = new Date();
            long timeDiff = product.getExpiryDate().getTime() - today.getTime();
            long daysLeft = TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);

            String badgeText;
            int badgeColor;

            if (product.getExpiryDate().before(today)) {
                badgeText  = "EXPIRED";
                badgeColor = Color.parseColor("#D50000");          // red
            } else if (daysLeft <= 7) {
                badgeText  = daysLeft + " day" + (daysLeft == 1 ? "" : "s") + " left";
                badgeColor = Color.parseColor("#FF6D00");          // orange
            } else {
                badgeText  = daysLeft + " days left";
                badgeColor = Color.parseColor("#00C853");          // GREEN = safe
            }

            holder.productDaysLeft.setText(badgeText);
            holder.productDaysLeft.setTextColor(Color.WHITE);

            GradientDrawable badge = new GradientDrawable();
            badge.setShape(GradientDrawable.RECTANGLE);
            badge.setCornerRadius(50 * dp);
            badge.setColor(badgeColor);
            holder.productDaysLeft.setBackground(badge);
        }

        // statusIndicator is gone in new layouts — just zero it out safely
        if (holder.statusIndicator != null) {
            holder.statusIndicator.setBackgroundColor(Color.TRANSPARENT);
        }

        // ── Product image ─────────────────────────────────────────────────
        if (holder.productImage != null) {
            if (product.hasPhoto()) {
                byte[] photoBytes = product.getPhoto();
                Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
                holder.productImage.setImageBitmap(bitmap);
            } else {
                String category = product.getCategory();
                int placeholder = R.drawable.ic_default_product;
                if (category != null) {
                    switch (category) {
                        case "Dairy":
                        case "Vegetables":
                        case "Fruits":
                        case "Meats":
                            placeholder = R.drawable.ic_food_placeholder;
                            break;
                        case "Beverages":
                            placeholder = R.drawable.ic_drinks_placeholder;
                            break;
                        case "Medicine":
                            placeholder = R.drawable.ic_medicine_placeholder;
                            break;
                        case "Other":
                            placeholder = R.drawable.ic_other_placeholder;
                            break;
                    }
                }
                holder.productImage.setImageResource(placeholder);
            }
        }

        // ── ⋮ Three-dot menu ──────────────────────────────────────────────
        if (holder.menuButton != null) {
            int menuTint = isGridView
                    ? Color.WHITE
                    : (isDark ? Color.parseColor("#AAAAAA") : Color.parseColor("#8A8F9E"));
            holder.menuButton.setColorFilter(menuTint);

            holder.menuButton.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                PopupMenu popup = new PopupMenu(ctx, v);
                popup.getMenu().add(0, 0, 0, "Edit");
                popup.getMenu().add(0, 1, 1, "Delete");
                popup.setOnMenuItemClickListener(item -> {
                    if (menuListener != null) {
                        if (item.getItemId() == 0) menuListener.onEditClick(pos);
                        else                        menuListener.onDeleteClick(pos);
                    }
                    return true;
                });
                popup.show();
            });
        }

        // ── Card click (view details) ─────────────────────────────────────
        View clickTarget = holder.itemLayout != null ? holder.itemLayout : holder.itemView;
        clickTarget.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listener != null) listener.onItemClick(pos);
        });
        clickTarget.setOnLongClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listener != null) {
                listener.onItemLongClick(pos);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return productList == null ? 0 : productList.size();
    }

    // ── updateData overloads ───────────────────────────────────────────────

    public void updateData(ArrayList<Product> newList) {
        this.productList = newList;
        notifyDataSetChanged();
    }

    /** Overload for List<Product> used by CategoryDetailActivity */
    public void updateData(List<Product> newList) {
        this.productList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    // ── ViewHolder ────────────────────────────────────────────────────────

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView     productCard;
        LinearLayout itemLayout;
        ImageView    productImage;
        TextView     productName;
        TextView     productExpiry;
        TextView     productDaysLeft;
        View         statusIndicator;
        ImageView    menuButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productCard     = itemView.findViewById(R.id.productCard);
            itemLayout      = itemView.findViewById(R.id.productItemLayout);
            productImage    = itemView.findViewById(R.id.productImage);
            productName     = itemView.findViewById(R.id.productName);
            productExpiry   = itemView.findViewById(R.id.productExpiry);
            productDaysLeft = itemView.findViewById(R.id.productDaysLeft);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
            menuButton      = itemView.findViewById(R.id.menuButton);
        }
    }
}