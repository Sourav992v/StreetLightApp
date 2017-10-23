package com.developer.sourav.streetlightapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.developer.sourav.streetlightapp.data.StreetContract.StreetEntry;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Sourav on 4/24/2017.
 */

public class StreetContentProvider extends ContentProvider {

    private static final String LOG_TAG = StreetContentProvider.class.getName();



    Uri contentUris = null;

    private static final int STREETS = 100;
    private static final int STREETS_ID = 101;


    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(StreetContract.CONTENT_AUTHORITY,StreetContract.PATH_STREET,STREETS);
        sUriMatcher.addURI(StreetContract.CONTENT_AUTHORITY,StreetContract.PATH_STREET + "/#", STREETS_ID);
    }

    private StreetDBHelper mStreetDBHelper;
    @Override
    public boolean onCreate() {
        mStreetDBHelper = new StreetDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        // Get readable database
        SQLiteDatabase database = mStreetDBHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match){
            case STREETS:
                cursor = database.query(StreetEntry.TABLE_NAME,null,null,null,null,null,null);
                break;
            case STREETS_ID:
                selection = StreetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(StreetEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Can't query unknown uri" + uri);
        }

        // Set notification URI on the cursor
        // So we know what CONTENET URI the cursor was created for.
        // If the data at this URI changes, then we khow we need to update the cursor.

        cursor.setNotificationUri(getContext().getContentResolver(),uri);


        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match){
            case STREETS:
                return StreetEntry.CONTENT_LIST_TYPE;
            case STREETS_ID:
                return StreetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown Uri" + uri + "with match" + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);

        switch (match){
            case STREETS:
                return insertStreet(uri,contentValues);
            default:
                throw new IllegalArgumentException("Isertion is not supported for" + uri);
                
        }
    }

    private Uri insertStreet(Uri uri, ContentValues contentValues) {

        SQLiteDatabase database = mStreetDBHelper.getWritableDatabase();

        String meterId = contentValues.getAsString(StreetEntry.COLUMN_METER_ID);
        if (meterId == null){
            throw new IllegalArgumentException("Requires valid Id");
        }


        long id = database.insert(StreetEntry.TABLE_NAME,null,contentValues);

        if (id == -1){
            Log.e(LOG_TAG,"Failed to insert row for" + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the Street Content URI.
        // uri: com.developer.sourav.streetlightapp/streets;
        getContext().getContentResolver().notifyChange(uri,null);

        // Return the new URI with the ID(of the newly inserted row) append at the end.
        return contentUris.withAppendedPath(uri, String.valueOf(id));
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        // Track the number of rows that were deleted
        int rowsDeleted;

        SQLiteDatabase database = mStreetDBHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);

        switch (match){
            case STREETS:
                // Delete all rows that match the selection and selection args

                rowsDeleted = database.delete(StreetEntry.TABLE_NAME,selection,selectionArgs);
                // If 1 or more rows were deleted, then notify all listeners that the data at the
                // given URI has changed
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                // Return the number of rows deleted
                return rowsDeleted;

            case STREETS_ID:
                // Delete a single row given by the ID in the URI
                selection = StreetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(StreetEntry.TABLE_NAME,selection,selectionArgs);
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                // Return the number of rows deleted
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for" + uri);
        }

    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match){
            case STREETS:
                return updateStreet(uri,contentValues,selection,selectionArgs);
            case STREETS_ID:

                // For the STREET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = StreetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateStreet(uri,contentValues,selection,selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for" + uri);
        }
    }

    private int updateStreet(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        // If the {@link StreetEntry#COLUMN_METER_ID} key is present,
        // check that the name value is not null.

        if (contentValues.containsKey(StreetEntry.COLUMN_METER_ID)){
            String meterId = contentValues.getAsString(StreetEntry.COLUMN_METER_ID);
            if (meterId == null){
                throw new IllegalArgumentException("Requires an Id");
            }
        }

        if (contentValues.containsKey(StreetEntry.COLUMN_MODEM_ID)){
            String modemId = contentValues.getAsString(StreetEntry.COLUMN_MODEM_ID);
            if (modemId == null){
                throw new IllegalArgumentException("Requires an Id");
            }
        }

        if (contentValues.containsKey(StreetEntry.COLUMN_MDN)){
            Long mdn = contentValues.getAsLong(StreetEntry.COLUMN_MDN);
            if (mdn != null && mdn < 0){
                throw new IllegalArgumentException("Requires a value");
            }
        }

        if (contentValues.containsKey(StreetEntry.COLUMN_LOCATION)){
            String location = contentValues.getAsString(StreetEntry.COLUMN_LOCATION);
            if (location == null){
                throw new IllegalArgumentException("Requires a Location");
            }
        }

        if (contentValues.containsKey(StreetEntry.COLUMN_ADDRESS)){
            String address = contentValues.getAsString(StreetEntry.COLUMN_ADDRESS);
            if (address == null){
                throw new IllegalArgumentException("Requires an Address");
            }
        }

        if (contentValues.size() == 0){
            return 0;
        }

        SQLiteDatabase database = mStreetDBHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(StreetEntry.TABLE_NAME,contentValues,selection,selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated !=0){
            getContext().getContentResolver().notifyChange(uri,null);
        }

        return rowsUpdated;




    }
}
