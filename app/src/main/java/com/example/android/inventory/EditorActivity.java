package com.example.android.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract.ItemEntry;

/**
 * Handles XML layout item insertion, updating, and deletion
 */
public class EditorActivity extends AppCompatActivity implements View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    private static final int EXISTING_BOOK_LOADER = 0;

    /**
     * EditText field for book title
     */
    private EditText titleEditText;

    /**
     * EditText field for book author
     */
    private EditText authorEditText;

    /**
     * EditText field for book price
     */
    private EditText priceEditText;

    /**
     * EditText field for book quantity
     */
    private EditText quantityEditText;

    /**
     * EditText field for supplier name
     */
    private EditText supplierNameEditText;

    /**
     * EditText field for supplier email
     */
    private EditText supplierEmailEditText;

    /**
     * EditText field for supplier phone
     */
    private EditText supplierPhoneEditText;

    int currentQuantity;

    Intent intent;

    /**
     * Content URI for the existing book (null if it's a new book)
     */
    private Uri currentBookUri;

    /**
     * Whether or not book was updated in editor activity
     */
    private boolean bookHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the bookHasChanged boolean to true.
     */
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            bookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Set up navigation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get intent data
        intent = getIntent();
        currentBookUri = intent.getData();

        titleEditText = findViewById(R.id.et_book_title);
        authorEditText = findViewById(R.id.et_book_author);
        priceEditText = findViewById(R.id.et_book_price);
        quantityEditText = findViewById(R.id.et_quantity);
        supplierNameEditText = findViewById(R.id.et_supplier_name);
        supplierEmailEditText = findViewById(R.id.et_supplier_email);
        supplierPhoneEditText = findViewById(R.id.et_supplier_phone);
        TextView btnIncrease = findViewById(R.id.btn_increase);
        TextView btnDecrease = findViewById(R.id.btn_decrease);
        TextView btnSave = findViewById(R.id.btn_save);
        Button btnDelete = findViewById(R.id.btn_delete);

        // Set up button to increase quantity
        btnIncrease.setOnClickListener(this);

        // Set up button to decrease quantity
        btnDecrease.setOnClickListener(this);

        // Set up button to save entry
        btnSave.setOnClickListener(this);

        // Set up button to delete entry
        btnDelete.setOnClickListener(this);

        // Set up touch listeners for alert dialog
        titleEditText.setOnTouchListener(touchListener);
        authorEditText.setOnTouchListener(touchListener);
        priceEditText.setOnTouchListener(touchListener);
        quantityEditText.setOnTouchListener(touchListener);
        btnIncrease.setOnTouchListener(touchListener);
        btnDecrease.setOnTouchListener(touchListener);
        supplierNameEditText.setOnTouchListener(touchListener);
        supplierEmailEditText.setOnTouchListener(touchListener);
        supplierPhoneEditText.setOnTouchListener(touchListener);

        // Set title and delete button visibility
        if (currentBookUri == null) {
            // We are adding a new item, so change title accordingly
            setTitle(getString(R.string.add_item));

            // Hide delete button because we don't need it if we're adding a new item
            btnDelete.setVisibility(View.GONE);
        } else {
            // Otherwise, we are coming from listView and editing an item
            setTitle(getString(R.string.edit_item));
            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }
    }

    /**
     * Perform the deletion of the book in the database.
     */
    private void deleteBook() {
        int rowDeleted = getContentResolver().delete(
                currentBookUri,   // Current book content URI
                null,              // No selection
                null               // No selection arguments
        );

        // Show a toast message depending on whether or not the deletion was successful
        if (rowDeleted == 0) {
            // If the new content URI is null, then there was an error with deletion.
            Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the deletion was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                    Toast.LENGTH_SHORT).show();
        }

        // Return to catalog activity
        finish();
    }

    /**
     * Get user input to enter new book data into database
     */
    private void saveBook() {

        // Read from EditText fields
        String title = titleEditText.getText().toString().trim();
        String author = authorEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        String quantityString = quantityEditText.getText().toString().trim();
        String supplierName = supplierNameEditText.getText().toString().trim();
        String supplierEmail = supplierEmailEditText.getText().toString().trim();
        String supplierPhone = supplierPhoneEditText.getText().toString().trim();

        // If all fields are empty, don't bother doing anything else
        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(author) && TextUtils.isEmpty(priceString)
                && TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(supplierName)
                && TextUtils.isEmpty(supplierEmail) && TextUtils.isEmpty(supplierPhone)) {
            return;
        }

        // Parse priceString into a double value only if quantity provided.
        // Use 0 by default.
        double price = 0.00;

        if (!TextUtils.isEmpty(priceString)) {
            price = Double.parseDouble(priceString);
        }

        // Parse quantityString into an integer value only if quantity provided and only digits.
        // Use 0 by default.
        int quantity = 0;

        if (!TextUtils.isEmpty(quantityString) && TextUtils.isDigitsOnly(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }

        // Create ContentValues object where column names are the keys,
        // and book attributes from EditText fields are the values.
        ContentValues cv = new ContentValues();
        cv.put(ItemEntry.COLUMN_TITLE, title);
        cv.put(ItemEntry.COLUMN_AUTHOR, author);
        cv.put(ItemEntry.COLUMN_PRICE, price);
        cv.put(ItemEntry.COLUMN_QUANTITY, quantity);
        cv.put(ItemEntry.COLUMN_SUPPLIER_NAME, supplierName);
        cv.put(ItemEntry.COLUMN_SUPPLIER_EMAIL, supplierEmail);
        cv.put(ItemEntry.COLUMN_SUPPLIER_PHONE, supplierPhone);

        if (currentBookUri == null) {
            // We are adding a new item
            Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, cv);

            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_book_successful),
                        Toast.LENGTH_SHORT).show();
            }

        } else {
            // Defines a variable to contain the number of updated rows
            int rowsUpdated = 0;

            rowsUpdated = getContentResolver().update(
                    currentBookUri,    // the book content URI
                    cv,                 // the columns to update
                    null,               // the column to select on
                    null                // the value to compare to
            );

            // Show a toast message depending on whether or not the insertion was successful
            if (rowsUpdated == 0) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_update_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Return to catalog activity
        intent = new Intent(EditorActivity.this, CatalogActivity.class);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Columns we want back from database
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_TITLE,
                ItemEntry.COLUMN_AUTHOR,
                ItemEntry.COLUMN_PRICE,
                ItemEntry.COLUMN_QUANTITY,
                ItemEntry.COLUMN_SUPPLIER_NAME,
                ItemEntry.COLUMN_SUPPLIER_EMAIL,
                ItemEntry.COLUMN_SUPPLIER_PHONE
        };

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the book being displayed.
        return new CursorLoader(this,   // Parent activity context
                currentBookUri,         // Provider content URI to query
                projection,             // Data we want returned
                null,                   // No selection
                null,                   // No selection args
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToLast()) {
            // Find the columns of book attributes we're interested in
            // Extract out the value from the Cursor for the given column index
            String currentTitle = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_TITLE));
            String currentAuthor = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_AUTHOR));
            double currentPrice = cursor.getDouble(cursor.getColumnIndex(ItemEntry.COLUMN_PRICE));
            currentQuantity = cursor.getInt(cursor.getColumnIndex(ItemEntry.COLUMN_QUANTITY));
            String currentSupplierName = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_SUPPLIER_NAME));
            String currentSupplierEmail = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_SUPPLIER_EMAIL));
            String currentSupplierPhone = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_SUPPLIER_PHONE));

            // Update the views on the screen with the values from the database
            titleEditText.setText(currentTitle);
            authorEditText.setText(currentAuthor);
            priceEditText.setText(Double.toString(currentPrice));
            quantityEditText.setText(Integer.toString(currentQuantity));
            supplierNameEditText.setText(currentSupplierName);
            supplierEmailEditText.setText(currentSupplierEmail);
            supplierPhoneEditText.setText(currentSupplierPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Clear all editor fields
        titleEditText.setText("");
        authorEditText.setText("");
        priceEditText.setText("");
        quantityEditText.setText("");
        supplierNameEditText.setText("");
        supplierEmailEditText.setText("");
        supplierPhoneEditText.setText("");
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            // Respond to a click on the "Increase" menu option
            case R.id.btn_increase:
                currentQuantity++;
                quantityEditText.setText(Integer.toString(currentQuantity));
                return;
            case R.id.btn_decrease:
                if (currentQuantity > 1) {
                    currentQuantity--;
                    quantityEditText.setText(Integer.toString(currentQuantity));
                } else
                    Toast.makeText(getBaseContext(), "Quantity must be greater than or equal to 0",
                            Toast.LENGTH_SHORT).show();
                return;
            case R.id.btn_save:
                // Save book to database
                saveBook();
                // Exit activity
                finish();
                break;
            // Respond to a click on the "Delete" button
            case R.id.btn_delete:
                showDeleteConfirmationDialog();
                break;

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on up button
        if (item.getItemId() == android.R.id.home) {
            // If book hasn't changed, continue with navigating up to parent activity
            if (!bookHasChanged) {
                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                return true;
            }

            // Otherwise if there are unsaved changes, setup a dialog to warn the user.
            // Create a click listener to handle the user confirming that
            // changes should be discarded.
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // User clicked "Discard" button, navigate to parent activity.
                            NavUtils.navigateUpFromSameTask(EditorActivity.this);
                        }
                    };

            // Show a dialog that notifies the user they have unsaved changes
            showUnsavedChangesDialog(discardButtonClickListener);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method to warn users about unsaved changes if navigating away from editor activity
     *
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Method to warn users about deleting item
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_delete);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * If back button is pressed, show AlertDialog if there are unsaved changes
     */
    @Override
    public void onBackPressed() {
        // If book hasn't changed, continue with handling back button press
        if (!bookHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }
}
