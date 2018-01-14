package com.example.android.inventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.inventory.data.InventoryContract.ItemEntry;

/**
 * Main activity that lists inventory
 */
public class CatalogActivity extends AppCompatActivity
        implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static Context context;

    private static final String LOG_TAG = CatalogActivity.class.getSimpleName();

    private static final int BOOK_LOADER = 0;

    // This is the Adapter being used to display the list's data.
    private InventoryCursorAdapter cursorAdapter;

    private static Uri currentBookUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        context = getApplicationContext();

        // Set up dummy data button to add data for demo when database is empty
        TextView btnDummyData = findViewById(R.id.btn_dummy_data);
        btnDummyData.setOnClickListener(this);

        // Set up inventory add button listener to open EditorActivity
        TextView btnAdd = findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(this);

        // Get listview from layout
        ListView bookListView = findViewById(R.id.listview);

        // Create empty view to show when database is empty
        View emptyView = findViewById(R.id.empty_view);
        bookListView.setEmptyView(emptyView);

        // Initialize adapter and attach to listview
        cursorAdapter = new InventoryCursorAdapter(this, null);
        bookListView.setAdapter(cursorAdapter);

        // Setup onClickListener to open EditorActivity
        // Position is location of item in listview
        // Id is ID of item clicked on
        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                    currentBookUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);
                    intent.setData(currentBookUri);
                    startActivity(intent);
            }
        });

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(BOOK_LOADER, null, this);
    }

    private void insertDummyData() {
        ContentValues cv = new ContentValues();

        cv.put(ItemEntry.COLUMN_TITLE, "Harry Potter");
        cv.put(ItemEntry.COLUMN_AUTHOR, "JK Rowlilng");
        cv.put(ItemEntry.COLUMN_PRICE, 9.99);
        cv.put(ItemEntry.COLUMN_QUANTITY, 100);
        cv.put(ItemEntry.COLUMN_SUPPLIER_NAME, "Scholastic Books");
        cv.put(ItemEntry.COLUMN_SUPPLIER_EMAIL, "info@scholastic.com");
        cv.put(ItemEntry.COLUMN_SUPPLIER_PHONE, "8005551212");

        // Insert data into database using ContentProvider class
        getContentResolver().insert(ItemEntry.CONTENT_URI, cv);

        cv.put(ItemEntry.COLUMN_TITLE, "Chicken Soup for the Soul");
        cv.put(ItemEntry.COLUMN_AUTHOR, "Jack Canfield, Mark Victor Hansen");
        cv.put(ItemEntry.COLUMN_PRICE, 5.99);
        cv.put(ItemEntry.COLUMN_QUANTITY, 10);
        cv.put(ItemEntry.COLUMN_SUPPLIER_NAME, "Health Communications, Inc");
        cv.put(ItemEntry.COLUMN_SUPPLIER_EMAIL, "info@healthcomm.com");
        cv.put(ItemEntry.COLUMN_SUPPLIER_PHONE, "8005551234");

        // Insert data into database using ContentProvider class
        getContentResolver().insert(ItemEntry.CONTENT_URI, cv);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Columns we want back from database
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_TITLE,
                ItemEntry.COLUMN_PRICE,
                ItemEntry.COLUMN_QUANTITY
        };

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the book being displayed.
        return new CursorLoader(this,       // Parent activity context
                ItemEntry.CONTENT_URI,      // Provider content URI to query
                projection,                 // Data we want returned
                null,                       // No selection
                null,                       // No selection args
                null);                      // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Update {@link InventoryCursorAdapter} with this new cursor containing updated item data
        cursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        cursorAdapter.swapCursor(null);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_add:
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_dummy_data:
                for (int index = 0; index < 5; index++) {
                    insertDummyData();
                }
                break;
        }
    }
}
