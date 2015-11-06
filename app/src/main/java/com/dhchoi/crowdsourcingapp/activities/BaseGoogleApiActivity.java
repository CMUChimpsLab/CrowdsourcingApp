package com.dhchoi.crowdsourcingapp.activities;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

import static com.dhchoi.crowdsourcingapp.Constants.CONNECTION_FAILURE_RESOLUTION_REQUEST;
import static com.dhchoi.crowdsourcingapp.Constants.PERMISSION_REQUEST;
import static com.dhchoi.crowdsourcingapp.Constants.TAG;

public class BaseGoogleApiActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError;
    private String requiredPermission = Manifest.permission.ACCESS_FINE_LOCATION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isGooglePlayServicesAvailable()) {
            Log.e(TAG, "Google Play services unavailable.");
            finish();
            return;
        }

        // check permissions
        if (ActivityCompat.checkSelfPermission(this, requiredPermission) != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this, new String[]{requiredPermission}, PERMISSION_REQUEST);
        }
        else {
            buildGoogleApiClient();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        if(mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) {
            if (!(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission was denied or request was cancelled
                Toast.makeText(this, "The app needs the proper permissions to run properly!", Toast.LENGTH_SHORT).show();
            }
            else {
                // else, we can now safely use the API we requested access to
                buildGoogleApiClient();
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs until onConnected() is called.

        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
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

    protected GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    protected void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /**
     * Checks if Google Play services is available.
     *
     * @return true if it is.
     */
    protected boolean isGooglePlayServicesAvailable() {
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
}
