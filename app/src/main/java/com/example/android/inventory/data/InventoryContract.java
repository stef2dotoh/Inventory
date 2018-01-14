package com.example.android.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract for Inventory app
 */

public class InventoryContract {

    /** Content Authority to help identify Content Provider */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";

    /**
     * Concatenate CONTENT_AUTHORITY constant with “content://” scheme to create
     * BASE_CONTENT_URI which will be shared by every URI associated with the contract
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Constant for Books table
    public static final String PATH_BOOKS = "books";



    // Empty constructor to prevent accidental instantiation of contract class
    private InventoryContract() {}

    /**
     * Inner class for Books table
     */

    public static final class ItemEntry implements BaseColumns {

        /** Full URI for ItemEntry class to access data in provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        /** Name of database table for books */
        public static final String TABLE_NAME = "books";

        /**
         * Unique ID number for the book
         *
         * Type: INTEGER
         */
        public static final String _ID = BaseColumns._ID;

        /**
         * Name of book
         *
         * Type: TEXT
         */
        public static final String COLUMN_TITLE = "title";

        /**
         * Author of book
         *
         * Type: TEXT
         */
        public static final String COLUMN_AUTHOR = "author";

        /**
         * Price of book
         *
         * Type: DOUBLE
         */
        public static final String COLUMN_PRICE = "price";

        /**
         * Quantity of book
         *
         * Type: INTEGER
         */
        public static final String COLUMN_QUANTITY = "quantity";

        /**
         * Book supplier name
         *
         * Type: TEXT
         */
        public static final String COLUMN_SUPPLIER_NAME = "supplier_name";

        /**
         * Book supplier email
         *
         * Type: TEXT
         */
        public static final String COLUMN_SUPPLIER_EMAIL = "supplier_email";

        /**
         * Book supplier phone
         *
         * Type: TEXT
         */
        public static final String COLUMN_SUPPLIER_PHONE = "supplier_phone";

        /**
         * MIME type of {@link #CONTENT_URI} for a list of books
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        /**
         * MIME type of {@link #CONTENT_URI} for a single book
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

    }
}
