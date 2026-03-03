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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConsumedHistoryAdapter extends RecyclerView.Adapter<ConsumedHistoryAdapter.ViewHolder> {

    private List<ConsumedProduct> consumedProducts = new ArrayList<>();
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ConsumedProduct product);
    }

    public ConsumedHistoryAdapter(Context context) {
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setConsumedProducts(List<ConsumedProduct> products) {
        this.consumedProducts = products != null ? products : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_consumed_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConsumedProduct product = consumedProducts.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return consumedProducts.size();
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout cardInner;
        ImageView productImage;
        TextView productName, actionType, category, quantity, actionDate, expiryDate;
        View cardDivider;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardInner    = itemView.findViewById(R.id.cardInner);
            productImage = itemView.findViewById(R.id.productImage);
            productName  = itemView.findViewById(R.id.productName);
            actionType   = itemView.findViewById(R.id.actionType);
            category     = itemView.findViewById(R.id.category);
            quantity     = itemView.findViewById(R.id.quantity);
            actionDate   = itemView.findViewById(R.id.actionDate);
            expiryDate   = itemView.findViewById(R.id.expiryDate);
            cardDivider  = itemView.findViewById(R.id.cardDivider);
        }

        void bind(ConsumedProduct product) {
            float dp = context.getResources().getDisplayMetrics().density;
            SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            boolean isDark = "black".equals(prefs.getString("color_theme", "white"));

            // ── Palette ───────────────────────────────────────────────────────
            int cardBg      = isDark ? Color.parseColor("#1E1E1E") : Color.WHITE;
            int cardBorder  = isDark ? Color.parseColor("#2E2E2E") : Color.parseColor("#EEEEEE");
            int textPrimary = isDark ? Color.WHITE                 : Color.parseColor("#1A1A1A");
            int textSecond  = isDark ? Color.parseColor("#888888") : Color.parseColor("#888888");
            int textMeta    = isDark ? Color.parseColor("#555555") : Color.parseColor("#BBBBBB");
            int imageBg     = isDark ? Color.parseColor("#2A2A2A") : Color.parseColor("#F0F0F0");
            int dividerColor = isDark ? Color.parseColor("#262626") : Color.parseColor("#F3F3F3");

            // ── Card background (rounded) ─────────────────────────────────────
            GradientDrawable cardBgDrawable = new GradientDrawable();
            cardBgDrawable.setColor(cardBg);
            cardBgDrawable.setCornerRadius(12 * dp);
            cardBgDrawable.setStroke(1, cardBorder);
            if (cardInner != null) cardInner.setBackground(cardBgDrawable);

            // ── Bottom separator ──────────────────────────────────────────────
            if (cardDivider != null) cardDivider.setBackgroundColor(dividerColor);

            // ── Product image ─────────────────────────────────────────────────
            if (productImage != null) {
                GradientDrawable imgBg = new GradientDrawable();
                imgBg.setColor(imageBg);
                imgBg.setCornerRadius(8 * dp);
                productImage.setBackground(imgBg);

                if (product.getPhoto() != null && product.getPhoto().length > 0) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(
                                product.getPhoto(), 0, product.getPhoto().length);
                        if (bitmap != null) {
                            productImage.setImageBitmap(bitmap);
                            productImage.setBackground(null);
                        } else {
                            productImage.setImageResource(R.drawable.ic_default_product);
                        }
                    } catch (Exception e) {
                        productImage.setImageResource(R.drawable.ic_default_product);
                    }
                } else {
                    productImage.setImageResource(R.drawable.ic_default_product);
                }
            }

            // ── Product name ──────────────────────────────────────────────────
            if (productName != null) {
                productName.setText(product.getProductName() != null
                        ? product.getProductName() : "Unknown");
                productName.setTextColor(textPrimary);
            }

            // ── Action badge (CONSUMED = green, DISCARDED = red) ──────────────
            if (actionType != null) {
                String action = product.getActionType();
                boolean isConsumed = "CONSUMED".equalsIgnoreCase(action);

                String label     = isConsumed ? "CONSUMED" : "DISCARDED";
                int badgeBgColor = isConsumed
                        ? Color.parseColor("#388E3C")   // green
                        : Color.parseColor("#C62828");  // red

                actionType.setText(label);
                actionType.setTextColor(Color.WHITE);

                GradientDrawable badgeBg = new GradientDrawable();
                badgeBg.setColor(badgeBgColor);
                badgeBg.setCornerRadius(4 * dp);
                actionType.setBackground(badgeBg);
            }

            // ── Category ──────────────────────────────────────────────────────
            if (category != null) {
                String cat = product.getCategory();
                category.setText(cat != null && !cat.isEmpty() ? cat : "No category");
                category.setTextColor(textSecond);
            }

            // ── Quantity (show if present) ────────────────────────────────────
            if (quantity != null) {
                String qty = product.getQuantity();
                if (qty != null && !qty.isEmpty() && !qty.equals("0")) {
                    quantity.setText("Qty: " + qty);
                    quantity.setTextColor(textSecond);
                    quantity.setVisibility(View.VISIBLE);
                } else {
                    quantity.setVisibility(View.GONE);
                }
            }

            // ── Dates ─────────────────────────────────────────────────────────
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

            if (actionDate != null) {
                String prefix = "CONSUMED".equalsIgnoreCase(product.getActionType())
                        ? "Action: " : "Action: ";
                try {
                    Date date = product.getActionDate();
                    actionDate.setText(date != null ? prefix + sdf.format(date) : prefix + "—");
                } catch (Exception e) {
                    actionDate.setText(prefix + "—");
                }
                actionDate.setTextColor(textMeta);
            }

            if (expiryDate != null) {
                try {
                    Date date = product.getExpiryDate();
                    expiryDate.setText(date != null ? "Expired: " + sdf.format(date) : "");
                } catch (Exception e) {
                    expiryDate.setText("");
                }
                expiryDate.setTextColor(textMeta);
            }

            // ── Item click ────────────────────────────────────────────────────
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(product);
            });
        }
    }
}