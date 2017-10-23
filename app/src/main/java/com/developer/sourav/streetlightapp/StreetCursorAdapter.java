package com.developer.sourav.streetlightapp;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.TextView;

import com.developer.sourav.streetlightapp.data.StreetContract.StreetEntry;

/**
 * Created by Sourav on 4/27/2017.
 */

public class StreetCursorAdapter extends CursorAdapter implements Filterable {



    public StreetCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.location_grid,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView locationTextView = (TextView)view.findViewById(R.id.location_name);
        TextView addressTextView = (TextView)view.findViewById(R.id.address);

        int locationColumnIndex = cursor.getColumnIndex(StreetEntry.COLUMN_LOCATION);
        int addressColumnIndex = cursor.getColumnIndex(StreetEntry.COLUMN_ADDRESS);

        String locationName = cursor.getString(locationColumnIndex);
        String address = cursor.getString(addressColumnIndex);

        if (TextUtils.isEmpty(address)){

            address = context.getString(R.string.address_not_available);
        }

        locationTextView.setText(locationName);
        addressTextView.setText(address);



    }
}
