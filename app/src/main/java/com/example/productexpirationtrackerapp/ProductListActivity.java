package com.example.productexpirationtrackerapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ProductListActivity extends AppCompatActivity {

    private static final String TAG = "ProductListDebug";

    // UI Components - matching your simple XML
    private TextView titleTextView;
    private Button backButton, addButton;
    private TextView productCountText;
    private ListView productListView;

    // Data
    private ArrayList<String> productList;
    private ArrayList<String> expiryDates; // Store dates separately
    private ArrayAdapter<String> adapter;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "ProductListActivity onCreate started");

        try {
            // This matches your simple activity_product_list.xml
            setContentView(R.layout.activity_product_list);
            Log.d(TAG, "Simple layout set successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting content view: " + e.getMessage());
            Toast.makeText(this, "Layout error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Get preferences
        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        Log.d(TAG, "Preferences loaded");

        // Initialize ALL UI components
        initializeViews();

        // Setup product list
        setupProductList();

        // Setup button click listeners
        setupClickListeners();

        // Update the UI
        updateProductCount();

        Log.d(TAG, "ProductListActivity setup complete");
    }

    private void initializeViews() {
        Log.d(TAG, "Starting initializeViews for simple layout");

        try {
            // Initialize views that exist in your SIMPLE XML
            titleTextView = findViewById(R.id.titleTextView);
            backButton = findViewById(R.id.backButton);
            addButton = findViewById(R.id.addButton);
            productCountText = findViewById(R.id.productCountText);
            productListView = findViewById(R.id.productListView);

            Log.d(TAG, "Simple views found successfully");

            // Check if views were found
            if (titleTextView == null) Log.e(TAG, "titleTextView is null!");
            if (backButton == null) Log.e(TAG, "backButton is null!");
            if (addButton != null) Log.d(TAG, "addButton found");
            if (productCountText != null) Log.d(TAG, "productCountText found");
            if (productListView != null) Log.d(TAG, "productListView found");

        } catch (Exception e) {
            Log.e(TAG, "Error in initializeViews: " + e.getMessage());
            Toast.makeText(this, "View error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupProductList() {
        Log.d(TAG, "Setting up simple product list");

        // Initialize lists
        productList = new ArrayList<>();
        expiryDates = new ArrayList<>();

        // Add sample products with their dates
        addProductWithDate("🥛 Milk", "2024-12-25");
        addProductWithDate("🥚 Eggs", "2024-12-20");
        addProductWithDate("🍞 Bread", "2024-12-30");
        addProductWithDate("🧀 Cheese", "2025-01-15");
        addProductWithDate("🍗 Chicken", "2024-12-28");
        addProductWithDate("🍎 Apples", "2025-01-10");
        addProductWithDate("☕ Coffee", "2025-03-01");
        addProductWithDate("🍪 Cookies", "2025-02-14");

        Log.d(TAG, "Created " + productList.size() + " sample products");

        // Create adapter for the ListView
        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                productList
        );

        // Set adapter to ListView
        if (productListView != null) {
            productListView.setAdapter(adapter);
            Log.d(TAG, "Adapter set to ListView");
        }

        // Set user's name in title
        String userName = preferences.getString("user_name", "User");
        if (titleTextView != null) {
            titleTextView.setText("📦 " + userName + "'s Products");
            Log.d(TAG, "Title set to: " + titleTextView.getText());
        }
    }

    private void addProductWithDate(String productName, String dateStr) {
        try {
            // Convert date to display format
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            String displayDate = displayFormat.format(date);

            // Add to lists
            productList.add(productName + " - Expires: " + displayDate);
            expiryDates.add(dateStr);

        } catch (Exception e) {
            // If parsing fails, use default
            productList.add(productName + " - Expires: Unknown");
            expiryDates.add("2024-12-31");
            Log.e(TAG, "Date parsing error for " + productName + ": " + e.getMessage());
        }
    }

    private void setupClickListeners() {
        Log.d(TAG, "Setting up click listeners");

        // Back button - go to MainActivity
        if (backButton != null) {
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Back button clicked");
                    Intent intent = new Intent(ProductListActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }
            });
        }

        // Add button - add new product
        if (addButton != null) {
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Add button clicked");
                    addNewProduct();
                }
            });
        }

        // List item click - OPEN PRODUCT DETAILS
        if (productListView != null) {
            productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String productText = productList.get(position);

                    // Extract product name (remove emoji if needed)
                    String[] parts = productText.split(" - Expires: ");
                    String productName = parts[0];

                    // Get the expiry date
                    String expiryDate = "";
                    if (position < expiryDates.size()) {
                        expiryDate = expiryDates.get(position);
                    } else {
                        // Try to extract from display text
                        if (parts.length > 1) {
                            expiryDate = convertDisplayDateToStorage(parts[1]);
                        }
                    }

                    Log.d(TAG, "Opening product details: " + productName + " | Date: " + expiryDate);

                    // Open ProductDetailActivity
                    Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
                    intent.putExtra("product_name", productName);
                    intent.putExtra("expiry_date", expiryDate);
                    intent.putExtra("position", position);

                    // Use startActivityForResult to handle deletion from detail screen
                    startActivityForResult(intent, 100);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }

        // List item long click - delete product
        if (productListView != null) {
            productListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    String removedProduct = productList.get(position);
                    String productName = removedProduct.split(" - ")[0];

                    // Remove from both lists
                    productList.remove(position);
                    if (position < expiryDates.size()) {
                        expiryDates.remove(position);
                    }

                    adapter.notifyDataSetChanged();
                    updateProductCount();

                    Toast.makeText(ProductListActivity.this,
                            "Removed: " + productName,
                            Toast.LENGTH_SHORT).show();

                    Log.d(TAG, "Item long clicked and removed: " + productName);
                    return true;
                }
            });
        }
    }

    // Helper method to convert display date (MMM dd, yyyy) to storage format (yyyy-MM-dd)
    private String convertDisplayDateToStorage(String displayDate) {
        try {
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            SimpleDateFormat storageFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = displayFormat.parse(displayDate);
            return storageFormat.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Date conversion error: " + e.getMessage());
            return "2024-12-31"; // Default date
        }
    }

    // Handle result from ProductDetailActivity (for deletion)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("deleted_position")) {
                int deletedPosition = data.getIntExtra("deleted_position", -1);

                if (deletedPosition != -1 && deletedPosition < productList.size()) {
                    String productName = productList.get(deletedPosition).split(" - ")[0];

                    // Remove from both lists
                    productList.remove(deletedPosition);
                    if (deletedPosition < expiryDates.size()) {
                        expiryDates.remove(deletedPosition);
                    }

                    adapter.notifyDataSetChanged();
                    updateProductCount();

                    Toast.makeText(this, "Deleted from details: " + productName, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Product deleted from detail screen: " + productName);
                }
            }
        }
    }

    private void addNewProduct() {
        // Sample products to add
        String[] products = {"🍌 Banana", "🥦 Broccoli", "🥩 Steak", "🐟 Fish", "🍇 Grapes", "🥑 Avocado"};
        String[] dates = {"2025-01-05", "2025-01-08", "2025-01-12", "2025-01-15", "2025-01-20", "2025-01-25"};

        // Pick a random product
        int randomIndex = (int) (Math.random() * products.length);

        // Add product with date
        addProductWithDate(products[randomIndex], dates[randomIndex]);

        adapter.notifyDataSetChanged();
        updateProductCount();

        // Show confirmation
        Toast.makeText(this, "Added: " + products[randomIndex], Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Added new product: " + products[randomIndex]);
    }

    private void updateProductCount() {
        if (productCountText != null) {
            productCountText.setText("Total: " + productList.size() + " products");
            Log.d(TAG, "Updated product count: " + productList.size());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update user name if changed
        String userName = preferences.getString("user_name", "User");
        if (titleTextView != null) {
            titleTextView.setText("📦 " + userName + "'s Products");
        }
        Log.d(TAG, "onResume called, title updated");
    }
}