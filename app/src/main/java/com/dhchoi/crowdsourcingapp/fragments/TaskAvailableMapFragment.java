package com.dhchoi.crowdsourcingapp.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhchoi.crowdsourcingapp.SimpleGeofence;
import com.dhchoi.crowdsourcingapp.services.GeofenceTransitionsIntentService;
import com.dhchoi.crowdsourcingapp.task.Task;
import com.dhchoi.crowdsourcingapp.task.TaskManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class TaskAvailableMapFragment extends SupportMapFragment implements OnMapReadyCallback {

    private OnTaskMapFragmentInteractionListener mListener;

    private GoogleMap mGoogleMap;
    private List<Task> mAllTasks = new ArrayList<Task>();
    private List<Task> mActiveTasks = new ArrayList<Task>();
    private List<Task> mInactiveTasks = new ArrayList<Task>();
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            for (String activatedTaskId : intent.getStringArrayExtra(GeofenceTransitionsIntentService.ACTIVATED_TASK_ID_KEY)) {
                for (int i = 0; i < mInactiveTasks.size(); i++) {
                    Task inactiveTask = mInactiveTasks.get(i);
                    if (inactiveTask.getId().equals(activatedTaskId)) {
                        mInactiveTasks.remove(inactiveTask);
                        mActiveTasks.add(inactiveTask);
                    }
                }
            }

            for (String inactivatedTaskId : intent.getStringArrayExtra(GeofenceTransitionsIntentService.INACTIVATED_TASK_ID_KEY)) {
                for (int i = 0; i < mActiveTasks.size(); i++) {
                    Task activeTask = mActiveTasks.get(i);
                    if (activeTask.getId().equals(inactivatedTaskId)) {
                        mActiveTasks.remove(activeTask);
                        mInactiveTasks.add(activeTask);
                    }
                }
            }

            updateMarkers();
        }
    };

    public TaskAvailableMapFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        getMapAsync(this);

        // get task list
        mAllTasks = TaskManager.getAllTasks(getActivity());
        for (Task t : mAllTasks) {
            if (t.isActivated()) {
                mActiveTasks.add(t);
            } else {
                mInactiveTasks.add(t);
            }
        }

        // Register to receive messages.
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, new IntentFilter(GeofenceTransitionsIntentService.GEOFENCE_TRANSITION_BROADCAST));

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTaskMapFragmentInteractionListener) {
            mListener = (OnTaskMapFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnTaskListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        UiSettings settings = mGoogleMap.getUiSettings();
        settings.setAllGesturesEnabled(true);
        settings.setMyLocationButtonEnabled(true);
        mGoogleMap.setMyLocationEnabled(true);

        // TODO: zoom to current location
//        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(((BaseGoogleApiActivity)getActivity()).getGoogleApiClient());
//        if (lastLocation != null) {
//            LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
//            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
//        }

        final LocationListener mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        long LOCATION_REFRESH_TIME = 1000;
        float LOCATION_REFRESH_DISTANCE = 60f;
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);

        updateMarkers();
    }

    private void updateMarkers() {
        for (Task t : mAllTasks) {
            SimpleGeofence simpleGeofence = t.getLocation();
            LatLng latLng = new LatLng(simpleGeofence.getLatitude(), simpleGeofence.getLongitude());
            mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(t.getName()));
            // TODO: set different markers depending on whether tasks are activated or not
            // TODO: show activation region
            // TODO: set click listeners on markers
            // mGoogleMap.addMarker(new MarkerOptions().position(~).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
        }
    }

    public interface OnTaskMapFragmentInteractionListener {
        void onTaskMapFragmentInteraction(Uri uri);
    }
}
