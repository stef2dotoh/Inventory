package com.example.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventory.data.InventoryContract.ItemEntry;

/**
 * Database helper for Inventory app
 * Manages database creation and version management
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    /** Name of the database file */
    private static final String DB_NAME = "bookstore.db";

    /** Database version. Increment when changing schema. */
    private static final int DB_VERSION = 1;

    /**
     * Constructs a new instance of {@link InventoryDbHelper}.
     * @param context of the app
     */
    public InventoryDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Called when database is first created.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // String to create table and columns
        String SQL_CREATE_TABLE_BOOKS =
                "CREATE TABLE " + ItemEntry.TABLE_NAME + " ("
                        + ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + ItemEntry.COLUMN_TITLE + " TEXT NOT NULL, "
                        + ItemEntry.COLUMN_AUTHOR + " TEXT, "
                        + ItemEntry.COLUMN_PRICE + " DOUBLE NOT NULL DEFAULT 0.00, "
                        + ItemEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                        + ItemEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
                        + ItemEntry.COLUMN_SUPPLIER_EMAIL + " TEXT, "
                        + ItemEntry.COLUMN_SUPPLIER_PHONE + " TEXT NOT NULL);";

        // Create the table
        db.execSQL(SQL_CREATE_TABLE_BOOKS);
        
    }

    /**
     * Called when database schema changes.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Do nothing right now

    }

    /**
     * This method empties the database every time app starts for demo purposes.
     * Remove for live application
     */
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        // Delete database
        String SQL_DROP_TABLE_BOOKS =
                "DROP TABLE IF EXISTS " + ItemEntry.TABLE_NAME + ";";

        db.execSQL(SQL_DROP_TABLE_BOOKS);

        onCreate(db);
    }
}
