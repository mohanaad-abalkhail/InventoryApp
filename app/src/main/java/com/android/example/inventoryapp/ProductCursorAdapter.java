package com.android.example.inventoryapp;


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

import com.android.example.inventoryapp.data.ProductContract;


/**
 * Created by MOHANAAD on 4/8/17.
 */

public class ProductCursorAdapter extends CursorAdapter {

    private int mRowsAffected;

    private Context mContext;

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0/* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {

        final TextView mQuantityTextView;

        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView itemsLeftTextView = (TextView) view.findViewById(R.id.items_left);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        mQuantityTextView = (TextView) view.findViewById(R.id.items_left);

        String currencysign = " S.R";

        int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int itemsLeftColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);

        mContext = context;

        final int position = cursor.getPosition();

        Button soldOneButton = (Button) view.findViewById(R.id.list_item_button);
        soldOneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cursor.moveToPosition(position);

                soldOne(cursor.getInt(cursor.getColumnIndex(ProductContract.ProductEntry._ID)), mQuantityTextView);

                return;
            }
        });

        String productName = cursor.getString(nameColumnIndex);
        String productQuantity = cursor.getString(itemsLeftColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);
        productPrice = productPrice + currencysign;

        nameTextView.setText(productName);
        itemsLeftTextView.setText(productQuantity);
        priceTextView.setText(productPrice);
    }

    public int soldOne(int mRowId, TextView mQuantityTextView) {
        String mQuantityString;
        int quantity = Integer.parseInt(mQuantityTextView.getText().toString());

        if (quantity > 0) {
            quantity--;

            mQuantityString = Integer.toString(quantity);

            ContentValues values = new ContentValues();
            values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, mQuantityString);

            Uri currentProductUri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI,
                    mRowId);

            mRowsAffected = mContext.getContentResolver().update(currentProductUri, values,
                    null, null);
        }
        return mRowsAffected;
    }

}
