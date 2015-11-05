package com.dhchoi.crowdsourcingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.SimpleGeofenceManager;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.List;

import static com.dhchoi.crowdsourcingapp.Constants.GEOFENCE_EXPIRATION_TIME;
import static com.dhchoi.crowdsourcingapp.Constants.PLACE_PICKER_REQUEST;

public class ManageLocationsActivity extends AppCompatActivity {

    SimpleGeofenceManager mGeofenceManger;
    ArrayAdapter<Geofence> mListViewAdapter;

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

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(mListViewAdapter);

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
                Toast.makeText(this, "Selected: " + placeName, Toast.LENGTH_LONG).show();

                mGeofenceManger.setGeofence(placeName,
                        place.getLatLng().latitude,
                        place.getLatLng().longitude,
                        60.0f,
                        GEOFENCE_EXPIRATION_TIME,
                        Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);

                mListViewAdapter.clear();
                mListViewAdapter.addAll(mGeofenceManger.getGeofenceList());
                mListViewAdapter.notifyDataSetChanged();
            }
        }
    }
}
