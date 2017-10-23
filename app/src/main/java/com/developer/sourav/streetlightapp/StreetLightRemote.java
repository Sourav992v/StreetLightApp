package com.developer.sourav.streetlightapp;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.sourav.streetlightapp.data.StreetContract;

public class StreetLightRemote extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int REMOTE_STREET_LOADER = 1;

    private Uri mCurrentRemoteUri;

    private CoordinatorLayout rCoordinatorLayout;
    private BroadcastReceiver mReceiver;
    private IntentFilter mFilter;

    public static final String MDN_PREFERENCE = "mdnKey";
    public static final String LOCATION_PREFERENCE = "locKey";

    private Snackbar snackbar;

    Button energyBtn, restoreBtn, loadCurrentBtn, powerBtn;
    Switch switchBtn;

    private String mdn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_street_light_remote);

        rCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.remoteCoordinator);

        Intent currentRemoteIntent = getIntent();
        mCurrentRemoteUri = currentRemoteIntent.getData();

        switchBtn = (Switch) findViewById(R.id.switchButton);
        restoreBtn = (Button) findViewById(R.id.buttonRestore);
        loadCurrentBtn = (Button) findViewById(R.id.buttonLoadStatus);
        energyBtn = (Button) findViewById(R.id.buttonEnergy);
        powerBtn = (Button) findViewById(R.id.buttonPower);

        SharedPreferences locPref = PreferenceManager.getDefaultSharedPreferences(this);
        locPref.registerOnSharedPreferenceChangeListener(this);

        mFilter = new IntentFilter("com.developer.sourav.streetlightapp.StreetLightRemote");
        mFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        mFilter.setPriority(5555);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String messageStr = SmsReceiver(context,intent);
                showSnackbar(messageStr);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        remote();

        getLoaderManager().initLoader(REMOTE_STREET_LOADER, null, this);
            // Otherwise this is a new info, so change the app bar to say "Add a Street Info"
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a street info that hasn't been created yet.)
        registerReceiver(mReceiver,mFilter);
    }

    private String SmsReceiver(Context mContext,Intent sIntent){

        Bundle bundle = sIntent.getExtras();

        SmsMessage[] message = null;

        String str = "";

        if (bundle != null) {
            // Retrieve the SMS Messages received
            Object[] pdus = (Object[]) bundle.get("pdus");

            message = new SmsMessage[pdus.length];

            // For every SMS message received
            for (int i = 0; i < message.length; i++) {

                        /*String format = bundle.getString("format");

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            message[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                        } else { */
                message[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                //}

                if (message[i].getOriginatingAddress().equals("+91" + mdn)) {
                    {
                        str += message[i].getMessageBody();
                        str += "\n";

                    }
                }
            }
        }
        return str;
    }

    @Override
    protected void onPause() {

        super.onPause();
        
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        preference.unregisterOnSharedPreferenceChangeListener(this);

        unregisterReceiver(mReceiver);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                StreetContract.StreetEntry.COLUMN_METER_ID,
                StreetContract.StreetEntry.COLUMN_MODEM_ID,
                StreetContract.StreetEntry.COLUMN_MDN,
                StreetContract.StreetEntry.COLUMN_LOCATION,
                StreetContract.StreetEntry.COLUMN_ADDRESS
        };
        return new CursorLoader(this,
                mCurrentRemoteUri,
                projection,
                null,
                null,
                null
        );
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null && cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)

        if (cursor.moveToFirst()) {

            // Find the columns of pet attributes that we're interested in
            int meterIdColumnIndex = cursor.getColumnIndex(StreetContract.StreetEntry.COLUMN_METER_ID);
            int modemIdColumnIndex = cursor.getColumnIndex(StreetContract.StreetEntry.COLUMN_MODEM_ID);
            int mdnColumnIndex = cursor.getColumnIndex(StreetContract.StreetEntry.COLUMN_MDN);
            int locationColumnIndex = cursor.getColumnIndex(StreetContract.StreetEntry.COLUMN_LOCATION);

            // Extract out the value from the Cursor for the given column index
            String meterId = cursor.getString(meterIdColumnIndex);
            String modemId = cursor.getString(modemIdColumnIndex);
            String mdn = cursor.getString(mdnColumnIndex);
            String location = cursor.getString(locationColumnIndex);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            Editor editor = preferences.edit();
            editor.putString(MDN_PREFERENCE,mdn);
            editor.putString(LOCATION_PREFERENCE,location);
            editor.apply();

            TextView meterTextView = (TextView) findViewById(R.id.textViewMeterId);
            meterTextView.setText(meterId);

            TextView modemTextView = (TextView) findViewById(R.id.textViewmodem);
            modemTextView.setText(modemId);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.edit_entries, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;

            case R.id.action_edit:
                Intent intent = new Intent(StreetLightRemote.this, EditorActivity.class);
                // Set URI on the data field of the intent
                intent.setData(mCurrentRemoteUri);

                startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        String locName = sharedPreferences.getString(LOCATION_PREFERENCE, "location");
        setTitle(locName + " " + getString(R.string.street_light_location));
        mdn = sharedPreferences.getString(MDN_PREFERENCE,"mdn");

    }

    private void showSnackbar(String str) {
        snackbar = Snackbar.make(rCoordinatorLayout, str, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Clear", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.setActionTextColor(Color.GREEN);
        View snackView = snackbar.getView();
        TextView textView = (TextView) snackView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        textView.setMaxLines(8);
        if (str.isEmpty()) {
            snackbar.dismiss();
        }else{
            snackbar.show();
        }
    }

    public void remote() {

        switchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String connect = "Connect::00000000";
                String disconnect = "Disconnect::00000000";


                if (isChecked) {

                    String SMS_SENT = "Communicating.....";
                    String SMS_DELIVERED = "Connection Established !";

                    PendingIntent sentPendingIntent = PendingIntent.getBroadcast(StreetLightRemote.this, 0, new Intent(SMS_SENT), 0);
                    PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(StreetLightRemote.this, 0, new Intent(SMS_DELIVERED), 0);

                    try {

                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(mdn, null, connect, sentPendingIntent, deliveredPendingIntent);

                        registerReceiver(new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                switch (getResultCode()) {
                                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                        Toast.makeText(context, "Generic failure cause", Toast.LENGTH_SHORT).show();
                                        break;
                                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                                        Toast.makeText(context, "Service is currently unavailable", Toast.LENGTH_SHORT).show();
                                        break;
                                    case SmsManager.RESULT_ERROR_NULL_PDU:
                                        Toast.makeText(context, "No pdu provided", Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            }
                        }, new IntentFilter(SMS_SENT));


                        registerReceiver(new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                switch (getResultCode()) {
                                    case Activity.RESULT_CANCELED:
                                        Toast.makeText(getBaseContext(), "Connection Failure, Try Again !", Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            }
                        }, new IntentFilter(SMS_DELIVERED));


                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Please try again !", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                } else {

                    String SMS_SENT = "Communicating .....";
                    String SMS_DELIVERED = "Disconnected !";

                    PendingIntent sentPendingIntent = PendingIntent.getBroadcast(StreetLightRemote.this, 0, new Intent(SMS_SENT), 0);
                    PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(StreetLightRemote.this, 0, new Intent(SMS_DELIVERED), 0);

                    try {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(mdn, null, disconnect, sentPendingIntent, deliveredPendingIntent);

                        registerReceiver(new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                switch (getResultCode()) {
                                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                        Toast.makeText(context, "Generic failure cause", Toast.LENGTH_SHORT).show();
                                        break;
                                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                                        Toast.makeText(context, "Service is currently unavailable", Toast.LENGTH_SHORT).show();
                                        break;
                                    case SmsManager.RESULT_ERROR_NULL_PDU:
                                        Toast.makeText(context, "No pdu provided", Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            }
                        }, new IntentFilter(SMS_SENT));


                        registerReceiver(new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                switch (getResultCode()) {
                                    case Activity.RESULT_CANCELED:
                                        Toast.makeText(getBaseContext(), "Failed !, Please Try Again...", Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            }
                        }, new IntentFilter(SMS_DELIVERED));

                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Please try again !", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
        });

        restoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reset = "Restore::00000000";

                String SMS_SENT = " Communicating ..... ";
                String SMS_DELIVERED = " Restored !  ";

                PendingIntent sentPendingIntent = PendingIntent.getBroadcast(StreetLightRemote.this, 0, new Intent(SMS_SENT), 0);
                PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(StreetLightRemote.this, 0, new Intent(SMS_DELIVERED), 0);


                registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        switch (getResultCode()) {
                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                Toast.makeText(context, "Generic failure cause", Toast.LENGTH_SHORT).show();
                                break;
                            case SmsManager.RESULT_ERROR_NO_SERVICE:
                                Toast.makeText(context, "Service is currently unavailable", Toast.LENGTH_SHORT).show();
                                break;
                            case SmsManager.RESULT_ERROR_NULL_PDU:
                                Toast.makeText(context, "No pdu provided", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }, new IntentFilter(SMS_SENT));


                registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        switch (getResultCode()) {
                            case Activity.RESULT_CANCELED:
                                Toast.makeText(getBaseContext(), "Failed, Please Try again !", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }, new IntentFilter(SMS_DELIVERED));
                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(mdn, null, reset, sentPendingIntent, deliveredPendingIntent);

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Please try again !", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();

                }
            }
        });



        energyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String load = "energy;;";

                String SMS_SENT = "Communicating .....";
                String SMS_DELIVERED = " Showing Energy ..... ";

                PendingIntent sentPendingIntent = PendingIntent.getBroadcast(StreetLightRemote.this, 0, new Intent(SMS_SENT), 0);
                PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(StreetLightRemote.this, 0, new Intent(SMS_DELIVERED), 0);


                registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        switch (getResultCode()) {
                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                Toast.makeText(context, "Generic failure cause", Toast.LENGTH_SHORT).show();
                                break;
                            case SmsManager.RESULT_ERROR_NO_SERVICE:
                                Toast.makeText(context, "Service is currently unavailable", Toast.LENGTH_SHORT).show();
                                break;
                            case SmsManager.RESULT_ERROR_NULL_PDU:
                                Toast.makeText(context, "No pdu provided", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }, new IntentFilter(SMS_SENT));


                registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        switch (getResultCode()) {
                            case Activity.RESULT_CANCELED:
                                Toast.makeText(getBaseContext(), "Not Available !, Try Again", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }, new IntentFilter(SMS_DELIVERED));
                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(mdn, null, load, sentPendingIntent, deliveredPendingIntent);


                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Please try again !", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loadCurrentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String load = "load;;";

                String SMS_SENT = "Communicating .....";
                String SMS_DELIVERED = " Showing Current Load ..... ";

                PendingIntent sentPendingIntent = PendingIntent.getBroadcast(StreetLightRemote.this, 0, new Intent(SMS_SENT), 0);
                PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(StreetLightRemote.this, 0, new Intent(SMS_DELIVERED), 0);


                registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        switch (getResultCode()) {
                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                Toast.makeText(context, "Generic failure cause", Toast.LENGTH_SHORT).show();
                                break;
                            case SmsManager.RESULT_ERROR_NO_SERVICE:
                                Toast.makeText(context, "Service is currently unavailable", Toast.LENGTH_SHORT).show();
                                break;
                            case SmsManager.RESULT_ERROR_NULL_PDU:
                                Toast.makeText(context, "No pdu provided", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }, new IntentFilter(SMS_SENT));


                registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        switch (getResultCode()) {
                            case Activity.RESULT_CANCELED:
                                Toast.makeText(getBaseContext(), "Not Available !, Try Again", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }, new IntentFilter(SMS_DELIVERED));
                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(mdn, null, load, sentPendingIntent, deliveredPendingIntent);


                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Please try again !", Toast.LENGTH_SHORT).show();
                }
            }
        });

        powerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                String power = "power;;";

                String SMS_SENT = " Communicating ..... ";
                String SMS_DELIVERED = " Fetching Power.....  ";

                PendingIntent sentPendingIntent = PendingIntent.getBroadcast(StreetLightRemote.this, 0, new Intent(SMS_SENT), 0);
                PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(StreetLightRemote.this, 0, new Intent(SMS_DELIVERED), 0);


                registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        switch (getResultCode()) {
                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                Toast.makeText(context, "Generic failure cause", Toast.LENGTH_SHORT).show();
                                break;
                            case SmsManager.RESULT_ERROR_NO_SERVICE:
                                Toast.makeText(context, "Service is currently unavailable", Toast.LENGTH_SHORT).show();
                                break;
                            case SmsManager.RESULT_ERROR_NULL_PDU:
                                Toast.makeText(context, "No pdu provided", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }, new IntentFilter(SMS_SENT));


                registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        switch (getResultCode()) {
                            case Activity.RESULT_CANCELED:
                                Toast.makeText(getBaseContext(), "Failed, Please Try again !", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }, new IntentFilter(SMS_DELIVERED));
                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(mdn, null, power, sentPendingIntent, deliveredPendingIntent);


                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Please try again !", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
