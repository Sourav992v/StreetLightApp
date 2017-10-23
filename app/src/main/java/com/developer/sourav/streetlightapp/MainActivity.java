package com.developer.sourav.streetlightapp;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.sourav.streetlightapp.data.StreetContract.StreetEntry;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int STREET_LOADER = 0;
    private static final int RC_SIGN_IN = 1;
    StreetCursorAdapter mCursorAdapter;

    GridView streetGridView;
    View emptyView;
    public static final String ANONYMOUS = "anonymous";
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private String mUserName;

    public static int TYPE_NOT_CONNECTED = 0;
    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public String internetStatus = "";

    private boolean internetConnected = true;

    private BroadcastReceiver mReceiver;
    private IntentFilter mFilter;

    private Snackbar snackbar;
    private CoordinatorLayout coordinatorLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        coordinatorLayout = (CoordinatorLayout)findViewById(R.id.coordinatorLayout);

        mUserName = ANONYMOUS;

        mFirebaseAuth = FirebaseAuth.getInstance();

        // Find the ListView which will be populated with the Street data
        streetGridView = (GridView)findViewById(R.id.location_grid_view);

        mCursorAdapter = new StreetCursorAdapter(this,null);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        emptyView = findViewById(R.id.empty_view);
        streetGridView.setEmptyView(emptyView);
        streetGridView.setAdapter(mCursorAdapter);

        // Set up item click listener
        streetGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                // Create new intent to go to {@Link EditorActivity}
                Intent remoteIntent = new Intent(MainActivity.this,StreetLightRemote.class);

                Uri remoteUri = ContentUris.withAppendedId(StreetEntry.CONTENT_URI,id);

                remoteIntent.setData(remoteUri);

                startActivity(remoteIntent);

            }
        });

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.add_location_fab_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,EditorActivity.class);
                startActivity(intent);
            }
        });


        mFilter = new IntentFilter("com.developer.sourav.streetlightapp");
        mFilter.addAction("android.net.wifi.STATE_CHANGE");
        mFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String status = getConnectivityStatusString(context);
                setSnackbarMessage(status);
            }
        };

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null){
                    // User is signed in
                    onSignedInInitialized(user.getDisplayName());
                }else{
                    // User is signed out
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setLogo(R.drawable.ic_street_light_logo)
                            .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                    new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()))
                            .setTheme(R.style.LoginTheme)
                            .build(),
                            RC_SIGN_IN);
                }
            }
        };

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        SharedPreferences userPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mUserName = userPref.getString("userKey", "mUserName");

        if (requestCode == RC_SIGN_IN){
            if (resultCode == RESULT_OK){
                Toast.makeText(this,"Signed in!" + " " + mUserName ,Toast.LENGTH_SHORT).show();
            }else if (resultCode == RESULT_CANCELED){
                Toast.makeText(this,"Signed in canceled!",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu options from the res/menu/delete_all_entries.xml
        //This adds menu items to the app bar

        getMenuInflater().inflate(R.menu.menu_main,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //User clicks on a menu option in the app bar overflow menu

        switch (item.getItemId()){
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onSignedInInitialized(String userName) {
        mUserName = userName;

        SharedPreferences userPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor mdnEditor = userPref.edit().putString("userKey", mUserName);
        mdnEditor.apply();


    }

    private void onSignedOutCleanup() {
        mUserName = ANONYMOUS;
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        mCursorAdapter.swapCursor(null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null){
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        getLoaderManager().destroyLoader(STREET_LOADER);
        unregisterReceiver(mReceiver);
    }


    @Override
    protected void onResume() {
        super.onResume();

        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        if (mAuthStateListener != null) {
            //Kick off the loader
            getLoaderManager().initLoader(STREET_LOADER, null, this);
        }

        registerReceiver(mReceiver,mFilter);
    }

    public static int getConnectivityStatus(Context context) {

        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork){
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    public static String getConnectivityStatusString(Context context){
        int conn = getConnectivityStatus(context);
        String status = null;
        if (conn == TYPE_WIFI){
            status = "Wifi enabled";

        } else if (conn == TYPE_MOBILE) {

            status = "Mobile data enabled";

        }else if (conn == TYPE_NOT_CONNECTED){
            status = "No internet connection";
        }
        return status;

    }

    private void setSnackbarMessage(String status){

        if (status.equalsIgnoreCase("Wifi enabled")|| status.equalsIgnoreCase("Mobile data enabled")){
            internetStatus = "Internet Connected";
        }else{
            internetStatus= "Check your internet connection !" ;
        }
        snackbar = Snackbar.make(coordinatorLayout,internetStatus,Snackbar.LENGTH_INDEFINITE)
                .setAction("Clear", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                    }
                });
        // Changing message text color
        snackbar.setActionTextColor(Color.RED);

        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);

        if(internetStatus.equalsIgnoreCase("Check your internet connection !")){
            if(internetConnected){
                snackbar.show();
                internetConnected=false;
            }
        }else{
            if(!internetConnected){
                internetConnected=true;
                snackbar.setDuration(2000).show();
            }
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                StreetEntry._ID,
                StreetEntry.COLUMN_METER_ID,
                StreetEntry.COLUMN_MODEM_ID,
                StreetEntry.COLUMN_MDN,
                StreetEntry.COLUMN_LOCATION,
                StreetEntry.COLUMN_ADDRESS
        };
        return new CursorLoader(this,
                StreetEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);

    }
}
