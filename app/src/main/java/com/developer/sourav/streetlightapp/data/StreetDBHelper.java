package com.developer.sourav.streetlightapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.developer.sourav.streetlightapp.data.StreetContract.StreetEntry;

/**
 * Created by Sourav on 4/14/2017.
 */

public class StreetDBHelper extends SQLiteOpenHelper {

    /* Name of the database file */
    private static final String DATABASE_NAME = "street.db";

    /**
     * Database version.If you change the database schema, you must increment database version.
     */
    private static final int DATABASE_VERSION = 1;


    public StreetDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String SQL_CREATE_STREET_TABLE = "CREATE TABLE " + StreetEntry.TABLE_NAME + "("
                + StreetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + StreetEntry.COLUMN_METER_ID + " TEXT NOT NULL, "
                + StreetEntry.COLUMN_MODEM_ID + " TEXT NOT NULL, "
                + StreetEntry.COLUMN_MDN + " INTEGER NOT NULL, "
                + StreetEntry.COLUMN_LOCATION + " TEXT NOT NULL, "
                + StreetEntry.COLUMN_ADDRESS + " TEXT NOT NULL);";
        sqLiteDatabase.execSQL(SQL_CREATE_STREET_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
