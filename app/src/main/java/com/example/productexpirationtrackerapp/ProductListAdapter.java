package com.example.productexpirationtrackerapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductListAdapter extends ArrayAdapter<Product> {

    private Context context;
    private List<Product> productList;
    private SimpleDateFormat dateFormat;

    public ProductListAdapter(Context context, List<Product> productList) {
        super(context, R.layout.list_item_product_with_photo, productList);
        this.context = context;
        this.productList = productList;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(R.layout.list_item_product_with_photo, parent, false);
        }

        Product currentProduct = productList.get(position);

        // Get views
        ImageView productPhoto = listItem.findViewById(R.id.productPhoto);
        TextView productName = listItem.findViewById(R.id.productName);
        TextView productExpiry = listItem.findViewById(R.id.productExpiry);

        // Set product name
        productName.setText(currentProduct.getName());

        // Set expiry date
        String expiryText = "Expires: " + dateFormat.format(currentProduct.getExpiryDate());
        productExpiry.setText(expiryText);

        // Set product photo
        if (currentProduct.hasPhoto()) {
            // Convert byte array to bitmap
            byte[] photoBytes = currentProduct.getPhoto();
            Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
            productPhoto.setImageBitmap(bitmap);
        } else {
            // Set default placeholder (you can create a drawable for this)
            productPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        return listItem;
    }

    // Update data method
    public void updateData(List<Product> newProductList) {
        productList.clear();
        productList.addAll(newProductList);
        notifyDataSetChanged();
    }
}