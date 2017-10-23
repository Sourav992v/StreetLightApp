package com.developer.sourav.streetlightapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.developer.sourav.streetlightapp.data.StreetContract.StreetEntry;


public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int EXISTING_STREET_LOADER = 1;

    /** Content URI for the existing Street info (null if it's a new info) */
    StreetCursorAdapter mCursorAdapter;

    private Uri mCurrentStreetUri;

    private EditText mMeterId;
    private EditText mModemId;
    private EditText mMDN;
    private EditText mLocation;
    private EditText mAddress;

    private boolean mStreetInfoHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            mStreetInfoHasChanged = true ;

            return false;
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mMeterId = (EditText) findViewById(R.id.edit_meter_id);
        mModemId = (EditText) findViewById(R.id.edit_modem_id);
        mMDN = (EditText) findViewById(R.id.edit_mdn);
        mLocation = (EditText) findViewById(R.id.edit_location);
        mAddress = (EditText) findViewById(R.id.edit_address);
        mMeterId.requestFocus();

        mMeterId.setOnTouchListener(mTouchListener);
        mModemId.setOnTouchListener(mTouchListener);
        mMDN.setOnTouchListener(mTouchListener);
        mLocation.setOnTouchListener(mTouchListener);
        mAddress.setOnTouchListener(mTouchListener);

        Intent intent = getIntent();
        mCurrentStreetUri = intent.getData();

        if (mCurrentStreetUri == null) {
            // This is a new info, so change the app bar to say "Add a Street Info"
            setTitle(getString(R.string.editor_activity_title_add_new_street_info));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a street info that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {

            // Otherwise this is an existing info, so change app bar to say "Edit Street"
            setTitle(getString(R.string.editor_activity_title_edit_street_info));
            getLoaderManager().initLoader(EXISTING_STREET_LOADER, null, this);

        }
    }


    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener){
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialogInterface != null){
                    dialogInterface.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }


    private void saveStreet(){

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String meterId = mMeterId.getText().toString().trim();
        String modemId = mModemId.getText().toString().trim();
        String mdnNo = mMDN.getText().toString().trim();
        String location = mLocation.getText().toString().trim();
        String address = mAddress.getText().toString().trim();

        if (mCurrentStreetUri == null &&
                TextUtils.isEmpty(meterId) && TextUtils.isEmpty(modemId)
                && TextUtils.isEmpty(location) && TextUtils.isEmpty(address)){

            // Since no fields were modified, we can return early without creating a new street info.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(StreetEntry.COLUMN_METER_ID,meterId);
        values.put(StreetEntry.COLUMN_MODEM_ID,modemId);
        values.put(StreetEntry.COLUMN_LOCATION,location);
        values.put(StreetEntry.COLUMN_ADDRESS,address);

        long deviceNo = +91;
        if (!TextUtils.isEmpty(mdnNo)){
            deviceNo = Long.parseLong(mdnNo);
        }
        values.put(StreetEntry.COLUMN_MDN,deviceNo);

        if (mCurrentStreetUri == null){

            // This is a NEW pet, so insert a new pet into the provider,
            // returning the content URI for the new pet.
            Uri newUri = getContentResolver().insert(StreetEntry.CONTENT_URI,values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_street_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_street_successful),
                        Toast.LENGTH_SHORT).show();
            }

        }else {
            // Otherwise this is an EXISTING street info, so update the pet with content URI: mCurrentStreetUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentStreetUri will already identify the correct row in the database that
            // we want to modify.

            int rowsAffected = getContentResolver().update(mCurrentStreetUri,values,null,null);

            if (rowsAffected == 0){
                Toast.makeText(this, getString(R.string.editor_insert_street_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_street_successful),
                        Toast.LENGTH_SHORT).show();

            }
        }
    }



    /**
     * Perform the deletion of the pet in the database.
     */
    private void deletePet() {
        // Only perform the delete if this is an existing info.
        if (mCurrentStreetUri != null){
            // Call ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection args because the mCurrentPetUri
            // content URI already identifies the info that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentStreetUri,null,null);

            // Show a toast message depending on weather or not the delete was successful.
            if (rowsDeleted  == 0){
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this,getString(R.string.editor_insert_street_failed),
                        Toast.LENGTH_SHORT).show();
            }else{
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this,getString(R.string.editor_delete_info_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
       super.onPrepareOptionsMenu(menu);

        // If this is a new info, hide the "Delete" menu item.
        if (mCurrentStreetUri == null){
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveStreet();
                if (mCurrentStreetUri != null) {
                    Intent intent = new Intent(this, StreetLightRemote.class);
                    intent.setData(mCurrentStreetUri);
                    startActivity(intent);
                }else{
                    finish();
                }
                return true;
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            case R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link MainActivity}

                if (!mStreetInfoHasChanged){
                    NavUtils.navigateUpFromSameTask(this);

                return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        // If the info hasn't changed, continue with handling back button press
        if (!mStreetInfoHasChanged) {
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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                StreetEntry.COLUMN_METER_ID,
                StreetEntry.COLUMN_MODEM_ID,
                StreetEntry.COLUMN_MDN,
                StreetEntry.COLUMN_LOCATION,
                StreetEntry.COLUMN_ADDRESS
        };
        return new CursorLoader(this,
                mCurrentStreetUri,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null && cursor.getCount() < 1){
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)


        if (cursor.moveToFirst()){

            // Find the columns of pet attributes that we're interested in
            int meterIdColumnIndex = cursor.getColumnIndex(StreetEntry.COLUMN_METER_ID);
            int modemIdColumnIndex = cursor.getColumnIndex(StreetEntry.COLUMN_MODEM_ID);
            int mdnColumnIndex = cursor.getColumnIndex(StreetEntry.COLUMN_MDN);
            int locationColumnIndex = cursor.getColumnIndex(StreetEntry.COLUMN_LOCATION);
            int addressColumnIndex = cursor.getColumnIndex(StreetEntry.COLUMN_ADDRESS);

            // Extract out the value from the Cursor for the given column index
            String meterId = cursor.getString(meterIdColumnIndex);
            String modemId = cursor.getString(modemIdColumnIndex);
            String mdn = cursor.getString(mdnColumnIndex);
            String location = cursor.getString(locationColumnIndex);
            String address = cursor.getString(addressColumnIndex);

            // Update the views on the screen with the values from the database
            mMeterId.setText(meterId);
            mModemId.setText(modemId);
            mMDN.setText(mdn);
            mLocation.setText(location);
            mAddress.setText(address);

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // If the loader is invalidated, clear out all the data from the input fields.

    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
