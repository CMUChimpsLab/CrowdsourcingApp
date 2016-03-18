package com.dhchoi.crowdsourcingapp.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.SimpleGeofence;
import com.dhchoi.crowdsourcingapp.activities.BaseGoogleApiActivity;
import com.dhchoi.crowdsourcingapp.activities.TaskCompleteActivity;
import com.dhchoi.crowdsourcingapp.services.GeofenceTransitionsIntentService;
import com.dhchoi.crowdsourcingapp.task.Task;
import com.dhchoi.crowdsourcingapp.task.TaskManager;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskAvailableMapFragment extends SupportMapFragment implements OnMapReadyCallback, LocationListener {

    // map related
    private final int BLUE_TRANSPARENT = 0x220000ff;
    private final int RED_TRANSPARENT = 0x22ff0000;
    private final int ZOOM_LEVEL = 15;
    private GoogleMap mGoogleMap;
    private Circle mCurrentVisibleCircle = null;
    private Map<Marker, Task> mMarkerToTask = new HashMap<Marker, Task>();

    // task related
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
    public void onDetach() {
        super.onDetach();

        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        UiSettings settings = mGoogleMap.getUiSettings();
        settings.setAllGesturesEnabled(true);
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // remove any previous circle drawn
                if (mCurrentVisibleCircle != null) {
                    mCurrentVisibleCircle.remove();
                }

                // add circle to show region for which task can be activated
                Task task = mMarkerToTask.get(marker);
                CircleOptions circleOptions = new CircleOptions()
                        .center(marker.getPosition())
                        .radius(task.getLocation().getRadius())
                        .strokeColor(Color.TRANSPARENT)
                        .fillColor(BLUE_TRANSPARENT);
                if (mInactiveTasks.contains(task)) {
                    circleOptions.fillColor(RED_TRANSPARENT);
                }
                mCurrentVisibleCircle = mGoogleMap.addCircle(circleOptions);

                return false;
            }
        });
        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                // start activity for task
                Intent intent = new Intent(getActivity(), TaskCompleteActivity.class);
                intent.putExtra(Task.TASK_KEY_SERIALIZABLE, mMarkerToTask.get(marker));
                startActivity(intent);
            }
        });
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // remove any circles drawn
                if (mCurrentVisibleCircle != null) {
                    mCurrentVisibleCircle.remove();
                }
            }
        });

        // if ACCESS_FINE_LOCATION is granted
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mGoogleMap.setMyLocationEnabled(true);

            LocationManager locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            Location lastLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (lastLocation != null) {
                Log.d(Constants.TAG, "Moving map camera to lastLocation.");
                LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL));
            } else {
                Log.d(Constants.TAG, "No lastLocation found.");
            }

            if (((BaseGoogleApiActivity) getActivity()).getGoogleApiClient().isConnected()) {
                updateCurrentLocation();
            }
        } else {
            Log.d(Constants.TAG, "ACCESS_FINE_LOCATION not granted and will not perform setMyLocationEnabled(true)");
        }

        updateMarkers();
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL));
    }

    public void updateCurrentLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = new LocationRequest()
                    .setNumUpdates(1)
                    .setInterval(100)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(((BaseGoogleApiActivity) getActivity()).getGoogleApiClient(), locationRequest, this);
        } else {
            Log.d(Constants.TAG, "ACCESS_FINE_LOCATION not granted and will not recenter map to current location");
        }
    }

    private void updateMarkers() {
        // TODO: take care for case of too much clustered tasks in one region
        for (Task t : mAllTasks) {
            SimpleGeofence simpleGeofence = t.getLocation();
            LatLng latLng = new LatLng(simpleGeofence.getLatitude(), simpleGeofence.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(t.getName() + " ($" + t.getCost() + ") ->");
            if (mInactiveTasks.contains(t)) {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }
            mMarkerToTask.put(mGoogleMap.addMarker(markerOptions), t);
        }
    }
}
