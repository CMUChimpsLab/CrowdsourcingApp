package com.dhchoi.crowdsourcingapp;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;



/**
 * Created by alex on 11/22/15.
 */
public class LocationListAdapter extends ArrayAdapter<SimpleGeofence> {

    private static final String TAG = "LocationListAdapter";

    public LocationListAdapter(Context context, List<SimpleGeofence> locations) {
        super(context, 0, locations);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position


        SimpleGeofence location = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.location_line, parent, false);
        }
        // Lookup view for data population
        TextView locationQuestionView = (TextView) convertView.findViewById(R.id.locationQuestion);
        TextView locationNameView = (TextView) convertView.findViewById(R.id.locationName);
        // Populate the data into the template view using the data object
        locationQuestionView.setText(location.getQuestion());
        locationNameView.setText(location.getName());
        // Return the completed view to render on screen
        return convertView;
    }
}