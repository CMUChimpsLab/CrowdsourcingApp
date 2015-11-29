package com.dhchoi.crowdsourcingapp.activities;

import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.services.FetchAddressIntentService;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;

public class CheckLocationActivity extends BaseGoogleApiActivity implements LocationListener {

    // Receiver registered with this activity to get the response from FetchAddressIntentService.
    ResultReceiver mFetchAddressResultReceiver;

    // TextViews
    TextView mCurrentLocationRaw;
    TextView mCurrentLocationAddress;
    TextView mCurrentLocationLastUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Receiver for data sent from FetchAddressIntentService
        mFetchAddressResultReceiver = new ResultReceiver(new Handler()) {
            // Receives data sent from FetchAddressIntentService and updates the UI in MainActivity
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                // Show a toast message if an address was found.
                if (resultCode == FetchAddressIntentService.FETCH_ADDRESS_SUCCESS_RESULT) {
                    // Display the address string or an error message sent from the intent service.
                    mCurrentLocationAddress.setText(resultData.getString(FetchAddressIntentService.FETCH_ADDRESS_RESULT_DATA_KEY));
                } else {
                    mCurrentLocationAddress.setText(getString(R.string.no_address_found));
                }
            }
        };

        // setup views
        setContentView(R.layout.activity_check_location);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCurrentLocationRaw = (TextView) findViewById(R.id.current_location_raw);
        mCurrentLocationAddress = (TextView) findViewById(R.id.current_location_address);
        mCurrentLocationLastUpdated = (TextView) findViewById(R.id.current_location_last_updated);
    }

    /**
     * Once the connection is available, send a request to update location.
     */
    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);
        LocationServices.FusedLocationApi.requestLocationUpdates(getGoogleApiClient(), createLocationRequest(), this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocationRaw.setText(String.valueOf(location.toString()));
        mCurrentLocationLastUpdated.setText(DateFormat.getTimeInstance().format(new Date()));

        if (location != null) {
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
            intent.putExtra(FetchAddressIntentService.FETCH_ADDRESS_RESULT_RECEIVER, mFetchAddressResultReceiver);
            // Pass the location data as an extra to the service.
            intent.putExtra(FetchAddressIntentService.FETCH_ADDRESS_LOCATION_DATA_EXTRA, location);
            // Start the service. If the service isn't already running, it is instantiated and started
            // (creating a process for it if needed); if it is running then it remains running. The
            // service kills itself automatically once all intents are processed.
            startService(intent);
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
