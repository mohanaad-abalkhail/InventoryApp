package com.android.example.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.example.inventoryapp.data.ProductContract;
import java.io.ByteArrayOutputStream;

/**
 * Created by MOHANAAD on 4/8/17.
 */

public class EditorInventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 0;

    static final int REQUEST_IMAGE_GET = 1;

    private Uri mCurrentProductUri;

    private EditText mVendorNameEditText;

    private EditText mVendorPhoneEditText;

    private EditText mProductNameEditText;

    private TextView mProductQuantityTextView;

    private EditText mProductPriceEditText;

    private ImageView mAddQuantityButton;

    private ImageView mReduceQuantityButton;

    private Button mReOrderButton;

    private Button mAddPictureButton;

    private ImageView mAddedPicture;

    private Bitmap mBitmap;

    private boolean mProductHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_item);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.add_product));

            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.edit_product));

            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mVendorNameEditText = (EditText) findViewById(R.id.edit_vendor_name);
        mVendorPhoneEditText = (EditText) findViewById(R.id.edit_vendor_number);
        mProductNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mProductQuantityTextView = (TextView) findViewById(R.id.edit_product_quantity);
        mProductPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mAddQuantityButton = (ImageView) findViewById(R.id.increase_button);
        mReduceQuantityButton = (ImageView) findViewById(R.id.reduce_button);
        mReOrderButton = (Button) findViewById(R.id.reorder_button);
        mAddPictureButton = (Button) findViewById(R.id.add_picture_button);
        mAddedPicture = (ImageView) findViewById(R.id.image_placeholder);

        mVendorNameEditText.setOnTouchListener(mTouchListener);
        mVendorPhoneEditText.setOnTouchListener(mTouchListener);
        mProductNameEditText.setOnTouchListener(mTouchListener);
        mAddQuantityButton.setOnTouchListener(mTouchListener);
        mReduceQuantityButton.setOnTouchListener(mTouchListener);
        mProductPriceEditText.setOnTouchListener(mTouchListener);
        mAddPictureButton.setOnTouchListener(mTouchListener);

        mAddQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String quantityString = mProductQuantityTextView.getText().toString().trim();
                int quantity = Integer.parseInt(quantityString);
                quantity = quantity + 1;
                mProductQuantityTextView.setText(String.valueOf(quantity));
            }
        });

        mReduceQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String quantityString = mProductQuantityTextView.getText().toString().trim();
                int quantity = Integer.parseInt(quantityString);
                if (quantity > 0) {
                    quantity = quantity - 1;
                    mProductQuantityTextView.setText(String.valueOf(quantity));
                } else {
                    return;
                }
            }
        });

        mReOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String vendorPhoneString = mVendorPhoneEditText.getText().toString().trim();
                String uri = "tel:" + vendorPhoneString;
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(uri));
                startActivity(intent);
            }
        });

        mAddPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
    }

    public void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            Uri fullPhotoUri = data.getData();

            mAddedPicture.setImageURI(fullPhotoUri);
            mBitmap = ((BitmapDrawable) mAddedPicture.getDrawable()).getBitmap();

        }
    }

    private void saveProduct() {

        String vendorNameString = mVendorNameEditText.getText().toString().trim();
        String vendorPhoneString = mVendorPhoneEditText.getText().toString().trim();
        String productNameString = mProductNameEditText.getText().toString().trim();
        String quantityString = mProductQuantityTextView.getText().toString().trim();
        String priceString = mProductPriceEditText.getText().toString().trim();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] imageInByte = baos.toByteArray();
        if (mBitmap != null ) {
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            imageInByte = baos.toByteArray();
        }
        if (TextUtils.isEmpty(vendorNameString) || TextUtils.isEmpty(vendorPhoneString) ||
                TextUtils.isEmpty(productNameString) || TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(priceString)|| (imageInByte.length <= 0)) {

            Toast.makeText(this,"Please fill all required information.",Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_VENDOR_NAME, vendorNameString);
        values.put(ProductContract.ProductEntry.COLUMN_VENDOR_PHONE, vendorPhoneString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, productNameString);

        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE, imageInByte);

        if (mCurrentProductUri == null) {

            Uri newUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);

            if (newUri == null) {

                Toast.makeText(this, getString(R.string.failed),
                        Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(this, getString(R.string.succesful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {

            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            if (rowsAffected == 0) {

                Toast.makeText(this, getString(R.string.failed),
                        Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(this, getString(R.string.succesful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_save:

                saveProduct();

                finish();
                return true;

            case R.id.action_delete:

                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:

                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorInventoryActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                NavUtils.navigateUpFromSameTask(EditorInventoryActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_VENDOR_NAME,
                ProductContract.ProductEntry.COLUMN_VENDOR_PHONE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE};

        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {

            int vendorNameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_VENDOR_NAME);
            int vendorPhoneColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_VENDOR_PHONE);
            int productNameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int productQuantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int productPriceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            int productImageColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE);

            String vendorName = cursor.getString(vendorNameColumnIndex);
            String vendorPhone = cursor.getString(vendorPhoneColumnIndex);
            String productName = cursor.getString(productNameColumnIndex);
            int quantity = cursor.getInt(productQuantityColumnIndex);
            int price = cursor.getInt(productPriceColumnIndex);
            byte[] image = cursor.getBlob(productImageColumnIndex);
            mBitmap = BitmapFactory.decodeByteArray(image, 0, image.length);

            mVendorNameEditText.setText(vendorName);
            mVendorPhoneEditText.setText(vendorPhone);
            mProductNameEditText.setText(productName);
            mProductQuantityTextView.setText(Integer.toString(quantity));
            mProductPriceEditText.setText(Integer.toString(price));
            mAddedPicture.setImageBitmap(mBitmap);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mVendorNameEditText.setText("");
        mVendorPhoneEditText.setText("");
        mProductNameEditText.setText("");
        mProductQuantityTextView.setText("");
        mProductPriceEditText.setText("");
        mAddedPicture.setImageURI(null);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {

        if (mCurrentProductUri != null) {

            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            if (rowsDeleted == 0) {

                Toast.makeText(this, getString(R.string.didnt_delet),
                        Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(this, getString(R.string.deleted),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

}
