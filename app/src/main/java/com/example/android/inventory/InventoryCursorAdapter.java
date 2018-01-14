package com.example.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventory.data.InventoryContract.ItemEntry;

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    /** Method to inflate a new view and return it */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
    }

    /* Method to bind all data to a given view such as setting the text on a TextView */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        // Find fields to populate in inflated template
        TextView tvName = view.findViewById(R.id.tv_name);
        TextView tvPrice = view.findViewById(R.id.tv_price);
        TextView tvQuantity = view.findViewById(R.id.tv_quantity);

        // Get sale button
        Button btnSale = view.findViewById(R.id.btn_sale);

        // Get column index for book attributes we want
        // Extract properties from cursor
        final int rowIndex = cursor.getInt(cursor.getColumnIndex(ItemEntry._ID));
        String name = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_TITLE));
        String price = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_PRICE));
        final String quantity = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_QUANTITY));


        // Populate fields with extracted properties
        tvName.setText(name);
        tvPrice.setText(price);
        tvQuantity.setText(quantity);

        // Decrease item quantity on click of sale button
        btnSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, rowIndex);
                int qty = Integer.parseInt(quantity);
                if (qty > 0) {
                    int newQuantity = qty - 1;

                    ContentValues cv = new ContentValues();
                    cv.put(ItemEntry.COLUMN_QUANTITY, newQuantity);

                    String selection = ItemEntry._ID + "=?";
                    String[] selectionArgs = new String[]{String.valueOf(ContentUris
                            .parseId(uri))};

                    view.getContext().getContentResolver().update(uri, cv, selection, selectionArgs);
                }
            }
        });
    }
}
