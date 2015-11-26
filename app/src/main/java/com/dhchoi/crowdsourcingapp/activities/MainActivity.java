package com.dhchoi.crowdsourcingapp.activities;

import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.FetchAddressResultReceiver;
import com.dhchoi.crowdsourcingapp.HttpClientAsyncTask;
import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.services.FetchAddressIntentService;
import com.dhchoi.crowdsourcingapp.services.GcmRegistrationIntentService;
import com.dhchoi.crowdsourcingapp.task.Task;
import com.dhchoi.crowdsourcingapp.task.TaskAction;
import com.dhchoi.crowdsourcingapp.task.TaskManager;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import static com.dhchoi.crowdsourcingapp.Constants.PLACE_PICKER_REQUEST;
import static com.dhchoi.crowdsourcingapp.Constants.TAG;

public class MainActivity extends BaseGoogleApiActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        LocationListener {

    // Locations
    Location mCurrentLocation;
    String mLastUpdateTime;
    // Receiver registered with this activity to get the response from FetchAddressIntentService.
    FetchAddressResultReceiver mFetchAddressResultReceiver;

    // TextViews
    TextView mCurrentLocationTextView;
    TextView mLocationAddressTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create result receiver for FetchAddressIntentService
        mFetchAddressResultReceiver = new FetchAddressResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                // Show a toast message if an address was found.
                if (resultCode == Constants.SUCCESS_RESULT) {
                    // Display the address string or an error message sent from the intent service.
                    mLocationAddressTextView.setText(resultData.getString(Constants.RESULT_DATA_KEY));
                } else {
                    mLocationAddressTextView.setText(getString(R.string.no_address_found));
                }
            }
        };

        // start IntentService to register this application with GCM
        startService(new Intent(this, GcmRegistrationIntentService.class));

        // setup views
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mCurrentLocationTextView = (TextView) findViewById(R.id.task_location);
        mLocationAddressTextView = (TextView) findViewById(R.id.address_text);
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
        // Handle action bar item clicks here. The action bar will automatically handle clicks on the Home/Up button,
        // so long as you specify a parent activity in AndroidManifest.xml.
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

        if (id == R.id.nav_manage_locations) {
            Intent intent = new Intent(this, TaskManagementActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Once the connection is available, send a request to add the Geofences.
     */
    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);

        // For displaying current location
        LocationServices.FusedLocationApi.requestLocationUpdates(getGoogleApiClient(), createLocationRequest(), this);

        /**
         *
         */
        new HttpClientAsyncTask() {
            @Override
            public void onHttpResponse(String response) {
                try {
                    JSONObject responseObj = new JSONObject(response);
                    String lastUpdated = (String) responseObj.get("lastUpdated");
                    JSONArray taskIds = responseObj.getJSONArray("taskIds");
                    Log.d(TAG, "lastUpdated: " + lastUpdated);
                    Log.d(TAG, "taskIds: " + taskIds.toString());
                    for(int i = 0; i < taskIds.length(); i++) {
                        Log.d(TAG, taskIds.getString(i));
                    }

                    new HttpClientAsyncTask() {
                        @Override
                        public void onHttpResponse(String response) {
                            List<Task> tasks = TaskManager.setTasks(response, MainActivity.this, getGoogleApiClient());
                            for(Task task : tasks) {
                                Log.d(TAG, "task: " + task);
                                Log.d(TAG, "location: " + task.getLocation());
                                for(TaskAction taskAction : task.getTaskActions()) {
                                    Log.d(TAG, "action: " + taskAction);
                                }
                            }
                        }
                    }.execute(Constants.APP_SERVER_FETCH_TASKS_URL, HttpClientAsyncTask.GET, "taskIds=1,2");

                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }.execute(Constants.APP_SERVER_SYNC_URL, HttpClientAsyncTask.GET, "time=now");
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        mCurrentLocationTextView.setText(String.valueOf(mCurrentLocation.toString()) + " (" + mLastUpdateTime + ")");

        if (mCurrentLocation != null) {
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
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

    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return mLocationRequest;
    }
}
