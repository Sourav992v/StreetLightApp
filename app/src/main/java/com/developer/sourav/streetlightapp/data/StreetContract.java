package com.developer.sourav.streetlightapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Sourav on 4/14/2017.
 */

public final class StreetContract {

    private StreetContract(){}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.developer.sourav.streetlightapp";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.pets/pets/ is a valid path for
     * looking at pet data. content://com.example.android.pets/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     * */
    public static final String PATH_STREET = "street";


    public static final class StreetEntry implements BaseColumns{

        /** The content URI to access the pet data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI,PATH_STREET);


        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of pets.
         */

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STREET;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STREET;

        public static final String TABLE_NAME = "street";

        public static final String _ID = BaseColumns._ID;

        public static final String COLUMN_LOCATION = "location";

        public static final String COLUMN_ADDRESS = "address";

        public static final String COLUMN_METER_ID = "meterId";

        public static final String COLUMN_MODEM_ID = "modemId";

        public static final String COLUMN_MDN = "mdn";




    }
}
