package com.dhchoi.crowdsourcingapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.SimpleGeofenceManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.List;

import static com.dhchoi.crowdsourcingapp.Constants.GEOFENCE_EXPIRATION_TIME;
import static com.dhchoi.crowdsourcingapp.Constants.PLACE_PICKER_REQUEST;
import static com.dhchoi.crowdsourcingapp.Constants.TAG;

public class ManageLocationsActivity extends BaseGoogleApiActivity implements
        ResultCallback<Status> {

    SimpleGeofenceManager mGeofenceManger;
    ArrayAdapter<Geofence> mListViewAdapter;
    Geofence mGeofenceToRemove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGeofenceManger = new SimpleGeofenceManager(this);
        mListViewAdapter = new ArrayAdapter<Geofence>(this, android.R.layout.simple_list_item_1, mGeofenceManger.getGeofenceList());

        // setup views
        setContentView(R.layout.activity_manage_locations);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startActivityForResult(new PlacePicker.IntentBuilder().build(ManageLocationsActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        final ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(mListViewAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                // Customize AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(ManageLocationsActivity.this);
                builder.setTitle("Delete Geofence");
                builder.setMessage("Do you wish to delete the Geofence?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        mGeofenceToRemove = (Geofence) listView.getItemAtPosition(position);
                        LocationServices.GeofencingApi.removeGeofences(
                                getGoogleApiClient(),
                                // This is the same pending intent that was used in addGeofences().
                                mGeofenceManger.getGeofenceTransitionPendingIntent()
                        ).setResultCallback(ManageLocationsActivity.this);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

                // Create the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the message from the intent
        // Intent intent = getIntent();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                String placeName = String.valueOf(place.getName());
                Toast.makeText(this, "Added: " + placeName, Toast.LENGTH_LONG).show();

                mGeofenceManger.setGeofence(placeName,
                        place.getLatLng().latitude,
                        place.getLatLng().longitude,
                        60.0f,
                        GEOFENCE_EXPIRATION_TIME,
                        Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);

                updateListViewAdapter();
            }
        }
    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            mGeofenceManger.removeGeofence(mGeofenceToRemove);
            updateListViewAdapter();

            Toast.makeText(ManageLocationsActivity.this, "Removed: " + mGeofenceToRemove.getRequestId(), Toast.LENGTH_SHORT).show();
        }
        else {
            // Get the status code for the error and log it using a user-friendly message.
            Log.e(TAG, "Could not remove geofence. StatusCode=" + status.getStatusCode());
        }
    }

    private void updateListViewAdapter() {
        mListViewAdapter.clear();
        mListViewAdapter.addAll(mGeofenceManger.getGeofenceList());
        mListViewAdapter.notifyDataSetChanged();
    }
}
