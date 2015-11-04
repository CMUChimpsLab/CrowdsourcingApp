package com.dhchoi.crowdsourcingapp.activities;

import static com.dhchoi.crowdsourcingapp.Constants.LOCATION_REQUEST;
import static com.dhchoi.crowdsourcingapp.Constants.PLACE_PICKER_REQUEST;
import static com.dhchoi.crowdsourcingapp.Constants.TAG;
import static com.dhchoi.crowdsourcingapp.Constants.GEOFENCE_EXPIRATION_TIME;
import static com.dhchoi.crowdsourcingapp.Constants.CONNECTION_FAILURE_RESOLUTION_REQUEST;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.FetchAddressResultReceiver;
import com.dhchoi.crowdsourcingapp.services.FetchAddressIntentService;
import com.dhchoi.crowdsourcingapp.services.GeofenceTransitionsIntentService;
import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.SimpleGeofence;
import com.dhchoi.crowdsourcingapp.SimpleGeofenceStore;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        LocationListener {

    // google api
    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError;

    // Stores the PendingIntent used to request geofence monitoring.
    private PendingIntent mGeofenceRequestIntent;

    // Persistent storage for geofences.
    private SimpleGeofenceStore mGeofenceStorage;

    // Internal List of Geofence objects. In a real app, these might be provided by an API based on
    // locations within the user's proximity.
    List<Geofence> mGeofenceList;

    // Locations
    Location mCurrentLocation;
    String mLastUpdateTime;
    // Receiver registered with this activity to get the response from FetchAddressIntentService.
    FetchAddressResultReceiver mFetchAddressResultReceiver;
    TextView mCurrentLocationTextView;
    TextView mLocationAddressTextView;

    // Notifiations
    NotificationManager mNotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Rather than displayng this activity, simply display a toast indicating that the geofence service is being created.
        // This should happen in less than a second.
        if (!isGooglePlayServicesAvailable()) {
            Log.e(TAG, "Google Play services unavailable.");
            finish();
            return;
        }

        // connect to google api
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // check permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
        } else {
            initializeGeofenceService();
        }

        // create result receiver for FetchAddressIntentService
        mFetchAddressResultReceiver = new FetchAddressResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                // Display the address string or an error message sent from the intent service.
                mLocationAddressTextView.setText(resultData.getString(Constants.RESULT_DATA_KEY));

                // Show a toast message if an address was found.
//            if (resultCode == Constants.SUCCESS_RESULT) {
//                showToast(getString(R.string.address_found));
//            }
            }
        };

        // setup notification manager
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // setup views
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
//                createNotification("test");
                try {
                    startActivityForResult(new PlacePicker.IntentBuilder().build(MainActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mCurrentLocationTextView = (TextView) findViewById(R.id.location_text);
        mLocationAddressTextView = (TextView) findViewById(R.id.address_text);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_available_tasks) {
            Intent intent = new Intent(this, AvailableTasksActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_gallery) {
        }
        else if (id == R.id.nav_slideshow) {
        }
        else if (id == R.id.nav_manage) {
        }
        else if (id == R.id.nav_share) {
        }
        else if (id == R.id.nav_send) {
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_REQUEST) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We can now safely use the API we requested access to
                initializeGeofenceService();
            } else {
                // Permission was denied or request was cancelled
                Toast.makeText(this, "The app needs location services to run properly!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Once the connection is available, send a request to add the Geofences.
     */
    @Override
    public void onConnected(Bundle bundle) {
        mGeofenceRequestIntent = getGeofenceTransitionPendingIntent();
        LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, mGeofenceList, mGeofenceRequestIntent);
        Toast.makeText(this, getString(R.string.start_geofence_service), Toast.LENGTH_SHORT).show();

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, createLocationRequest(), this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs until onConnected() is called.

        if (null != mGeofenceRequestIntent) {
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, mGeofenceRequestIntent);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // This callback is important for handling errors that may occur while attempting to connect with Google.
        // More about this in the 'Handle Connection Failures' section.
        // https://developers.google.com/android/guides/api-client

        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (connectionResult.hasResolution()) {
            try {
                mResolvingError = true;
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                String errMsg = "Exception while resolving connection error.";
                Log.e(TAG, errMsg, e);
                Toast.makeText(this, errMsg, Toast.LENGTH_SHORT).show();
            }
        } else {
            int errorCode = connectionResult.getErrorCode();
            String errMsg = "Connection to Google Play services failed with error code " + errorCode;
            Log.e(TAG, errMsg);
            Toast.makeText(this, errMsg, Toast.LENGTH_SHORT).show();
            mResolvingError = false;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        mCurrentLocationTextView.setText(String.valueOf(mCurrentLocation.toString()) + " (" + mLastUpdateTime + ")");

        if(mCurrentLocation != null) {
            // Determine whether a Geocoder is available.
            if(!Geocoder.isPresent()) {
                Toast.makeText(this, R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
                return;
            }

            /**
             * Creates an intent, adds location data to it as an extra, and starts the intent service for fetching an address.
             */
            // Create an intent for passing to the intent service responsible for fetching the address.
            Intent intent = new Intent(this, FetchAddressIntentService.class);
            // Pass the result receiver as an extra to the service.
            intent.putExtra(Constants.RECEIVER, mFetchAddressResultReceiver);
            // Pass the location data as an extra to the service.
            intent.putExtra(Constants.LOCATION_DATA_EXTRA, mCurrentLocation);
            // Start the service. If the service isn't already running, it is instantiated and started
            // (creating a process for it if needed); if it is running then it remains running. The
            // service kills itself automatically once all intents are processed.
            startService(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                Toast.makeText(this, String.format("Place: %s", place.getName()), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initializeGeofenceService() {
        // Instantiate a new geofence storage area.
        mGeofenceStorage = new SimpleGeofenceStore(this);
        // Instantiate the current List of geofences.
        mGeofenceList = new ArrayList<Geofence>();
        createGeofences();
    }

    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return mLocationRequest;
    }

    /**
     * In this sample, the geofences are predetermined and are hard-coded here.
     * A real app might dynamically create geofences based on the user's location.
     */
    public void createGeofences() {
        // These will store hard-coded geofences in this sample app.

        // Create internal "flattened" objects containing the geofence data.
        String mHomeGeofenceId = "home";
        SimpleGeofence mHomeGeofence = new SimpleGeofence(
                mHomeGeofenceId,
                40.447222,
                -79.946714,
                60.0f,
                GEOFENCE_EXPIRATION_TIME,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
        );
        mGeofenceStorage.setGeofence(mHomeGeofenceId, mHomeGeofence);
        mGeofenceList.add(mHomeGeofence.toGeofence());

        String mGatesCenterGeofenceId = "gatesCenter";
        SimpleGeofence mGatesCenterGeofence = new SimpleGeofence(
                mGatesCenterGeofenceId,
                40.443336,
                -79.944675,
                60.0f,
                GEOFENCE_EXPIRATION_TIME,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
        );
        mGeofenceStorage.setGeofence(mGatesCenterGeofenceId, mGatesCenterGeofence);
        mGeofenceList.add(mGatesCenterGeofence.toGeofence());
    }

    /**
     * Checks if Google Play services is available.
     *
     * @return true if it is.
     */
    private boolean isGooglePlayServicesAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == resultCode) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Google Play services is available.");
            }
            return true;
        } else {
            Log.e(TAG, "Google Play services is unavailable.");
            return false;
        }
    }

    /**
     * Create a PendingIntent that triggers GeofenceTransitionIntentService
     * when a geofence transition occurs.
     */
    private PendingIntent getGeofenceTransitionPendingIntent() {
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
