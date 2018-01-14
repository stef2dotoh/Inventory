package com.example.android.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Patterns;

import com.example.android.inventory.data.InventoryContract.ItemEntry;
/**
 * {@link ContentProvider} for Inventory app
 */

public class InventoryProvider extends ContentProvider {
    
    private static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    /** Database helper object to gain access to database */
    private InventoryDbHelper dbHelper;

    /** SQLiteDatabase object to perform operations on database */
    private SQLiteDatabase db;

    /** Sets integer value for multiple rows in Books table */
    private static final int BOOKS = 100;

    /** Sets integer value for a single row in Books table */
    private static final int BOOK_ID = 101;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer run the first time anything is called from this class
    static {

        // Calls to addURI() go here for all content URI patterns provider should recognize
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_BOOKS,
                BOOKS);

        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY,
                InventoryContract.PATH_BOOKS + "/#", BOOK_ID);
    }

    /**
     * Initialize provider and database helper object
     */
    @Override
    public boolean onCreate() {
        dbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    /**
     * Perform query for given URI using given projection, selection, selection arguments,
     * and sort order
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        // Get readable database
        db = dbHelper.getReadableDatabase();

        // Cursor object to hold results of query
        Cursor cursor;

        // Determine what kind of URI was passed
        final int match = uriMatcher.match(uri);

        switch (match) {
            case BOOKS:
                // Query books table; cursor could contain multiple rows
                cursor = db.query(ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case BOOK_ID:
                // Query books table; cursor will contain single row
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI - " + uri);

        }

        // Set up notification uri to update display automatically
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /**
     * Insert new data into provider with given ContentValues
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        // Determine if valid URI was passed
        final int match = uriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion not supported for URI - " + uri);
        }
    }

    /**
     * Insert a book into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     *
     * @param uri of table
     * @param contentValues to be inserted
     */
    private Uri insertBook(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        // Check that title is not null
        String title = contentValues.getAsString(ItemEntry.COLUMN_TITLE);
        if (title.isEmpty()) {
            throw new IllegalArgumentException("Book requires a title");
        }

        // Check that author is not null
        String author = contentValues.getAsString(ItemEntry.COLUMN_AUTHOR);
        if (author.isEmpty()) {
            throw new IllegalArgumentException("Book requires an author");
        }

        // XML inputType forces positive double input, but just to make sure, check that price is not
        // negative; can be null, but database sets default to 0 in database helper.
        Double price = contentValues.getAsDouble(ItemEntry.COLUMN_PRICE);
        if (price != null && price < 0.00) {
            throw new NumberFormatException("Book requires a price greater than or equal to 0.00");
        }

        // XML inputType forces positive input, but just to make sure, check that quantity is not
        // negative; can be null, but database sets default to 0 in database helper
        Integer quantity = contentValues.getAsInteger(ItemEntry.COLUMN_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new NumberFormatException("Book requires a quantity greater than or equal to 0");
        }

        // Check that supplier name is not null
        String supplierName = contentValues.getAsString(ItemEntry.COLUMN_SUPPLIER_NAME);
        if (supplierName.isEmpty()) {
            throw new IllegalArgumentException("Supplier name required");
        }

        // Check that supplier email is valid; can be null
        String supplierEmail = contentValues.getAsString(ItemEntry.COLUMN_SUPPLIER_EMAIL);
        if (!supplierEmail.isEmpty() &&
                !Patterns.EMAIL_ADDRESS.matcher(supplierEmail).matches()) {
            throw new IllegalArgumentException("Supplier email not valid");
        }

        // Check that supplier phone is not null
        String supplierPhone = contentValues.getAsString(ItemEntry.COLUMN_SUPPLIER_PHONE);
        if (supplierPhone.isEmpty()) {
            throw new IllegalArgumentException("Supplier phone required");
        }

        // Get writable database
        db = dbHelper.getWritableDatabase();

        // Insert a new book into bookstore database books table with the given content values
        long rowId = db.insert(ItemEntry.TABLE_NAME, null, contentValues);

        // If rowId is -1, then insertion failed. Log error and return null.
        if (rowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that data has changed for book content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know ID of new row in the table, return new URI with ID appended to the end
        return ContentUris.withAppendedId(uri, rowId);
    }

    /**
     * Updates books at given selection and selection arguments with new ContentValues
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues,
                      @Nullable String selection, @Nullable String[] selectionArgs) {

        // Determine if valid URI was passed
        final int match = uriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateBook(uri, contentValues, selection, selectionArgs);
            case BOOK_ID:
                // For the BOOK_ID code, extract ID from URI, convert to number, then to string
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateBook(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for URI - " + uri);
        }
    }

    /**
     * Updates books at given selection and selection arguments with new ContentValues
     * Return number of rows successfully updated
     */
    private int updateBook(Uri uri, ContentValues contentValues, String selection,
                           String[] selectionArgs) {

        // Check to see if {@link BookEntry#COLUMN_TITLE} key is present
        // Check that title is not null
        if (contentValues.containsKey(ItemEntry.COLUMN_TITLE)) {
            String title = contentValues.getAsString(ItemEntry.COLUMN_TITLE);
            if (title.isEmpty()) {
                throw new IllegalArgumentException("Book requires a title");
            }
        }

        // Check to see if {@link ItemEntry#COLUMN_AUTHOR} key is present
        // Check that author is not null
        if (contentValues.containsKey(ItemEntry.COLUMN_AUTHOR)) {
            String author = contentValues.getAsString(ItemEntry.COLUMN_AUTHOR);
            if (author.isEmpty()) {
                throw new IllegalArgumentException("Book requires an author");
            }
        }

        // Check to see if {@link ItemEntry#COLUMN_PRICE} key is present
        // XML inputType forces positive double input, but just to make sure, check that price is not
        // negative; can be null, but database sets default to 0 in database helper.
        if (contentValues.containsKey(ItemEntry.COLUMN_PRICE)) {
            Double price = contentValues.getAsDouble(ItemEntry.COLUMN_PRICE);
            if (price != null && price < 0.00) {
                throw new NumberFormatException("Book requires a price greater than or equal to 0.00");
            }
        }

        // Check to see if {@link ItemEntry#COLUMN_QUANTITY} key is present
        // XML inputType forces positive input, but just to make sure, check that quantity is not
        // negative; can be null, but database sets default to 0 in database helper
        if (contentValues.containsKey(ItemEntry.COLUMN_QUANTITY)) {
            Integer quantity = contentValues.getAsInteger(ItemEntry.COLUMN_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new NumberFormatException("Book requires a quantity greater than or equal to 0");
            }
        }

        // Check to see if {@link ItemEntry#COLUMN_SUPPLIER_NAME} key is present
        // Check that supplier name is not null
        if (contentValues.containsKey(ItemEntry.COLUMN_SUPPLIER_NAME)) {
            String supplierName = contentValues.getAsString(ItemEntry.COLUMN_SUPPLIER_NAME);
            if (supplierName.isEmpty()) {
                throw new IllegalArgumentException("Supplier name required");
            }
        }

        // Check to see if {@link ItemEntry#COLUMN_SUPPLIER_EMAIL} key is present
        // Check that supplier email is valid; can be null
        if (contentValues.containsKey(ItemEntry.COLUMN_SUPPLIER_EMAIL)) {
            String supplierEmail = contentValues.getAsString(ItemEntry.COLUMN_SUPPLIER_EMAIL);
            if (!supplierEmail.isEmpty() &&
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(supplierEmail).matches()) {
                throw new IllegalArgumentException("Supplier email not valid");
            }
        }

        // Check to see if {@link ItemEntry#COLUMN_SUPPLIER_PHONE} key is present
        // Check that supplier phone is not null
        if (contentValues.containsKey(ItemEntry.COLUMN_SUPPLIER_PHONE)) {
            String supplierPhone = contentValues.getAsString(ItemEntry.COLUMN_SUPPLIER_PHONE);
            if (supplierPhone.isEmpty()) {
                throw new IllegalArgumentException("Supplier phone required");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (contentValues.size() == 0) {
            return 0;
        }

        // There are values to update, so get writable database
        db = dbHelper.getWritableDatabase();

        // Update books table in bookstore database with the given content values
        // and get number of rows updated
        int rowsUpdated = db.update(ItemEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return number of rows updated
        return rowsUpdated;
    }

    /**
     * Delete data at given selection and selection arguments
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {

        int rowsDeleted;

        // Get writable database
        db = dbHelper.getWritableDatabase();

        // Determine if valid URI was passed
        final int match = uriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // Delete all rows with given selection and selection args
                rowsDeleted = db.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                // For the BOOK_ID code, extract ID from URI, convert to number, then to string
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // Delete single row with given selection and selection args
                rowsDeleted = db.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for URI - " + uri);
        }

        // If row is deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return number of rows deleted
        return rowsDeleted;
    }

    /**
     * Returns MIME type of data for content URI
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        final int match = uriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return ItemEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return ItemEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI - " + uri + " with match " + match);
        }
    }
}
