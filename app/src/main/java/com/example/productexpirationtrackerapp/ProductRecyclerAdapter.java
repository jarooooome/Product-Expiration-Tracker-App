package com.example.productexpirationtrackerapp;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * MINIMAL CHANGE ADAPTER
 * This adapter wraps your existing ProductListAdapter to work with RecyclerView
 * Add this new file and you won't need to modify your existing adapter!
 */
public class ProductRecyclerAdapter extends RecyclerView.Adapter<ProductRecyclerAdapter.ViewHolder> {

    private ProductListAdapter listAdapter;
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(int position);
    }

    public ProductRecyclerAdapter(ProductListAdapter listAdapter) {
        this.listAdapter = listAdapter;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = listAdapter.getView(0, null, parent);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        View view = listAdapter.getView(position, holder.itemView, (ViewGroup) holder.itemView.getParent());

        // Setup click listeners
        final int pos = position;
        view.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(pos);
            }
        });

        view.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                return longClickListener.onItemLongClick(pos);
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return listAdapter.getCount();
    }

    public void updateData(List<Product> products) {
        listAdapter.notifyDataSetChanged();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View itemView) {
            super(itemView);
        }
    }
}