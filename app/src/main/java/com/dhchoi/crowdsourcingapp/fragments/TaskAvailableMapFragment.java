package com.dhchoi.crowdsourcingapp.fragments;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.GeofenceLocation;
import com.dhchoi.crowdsourcingapp.activities.BaseGoogleApiActivity;
import com.dhchoi.crowdsourcingapp.activities.MainActivity;
import com.dhchoi.crowdsourcingapp.activities.TaskCompleteActivity;
import com.dhchoi.crowdsourcingapp.services.BackgroundLocationService;
import com.dhchoi.crowdsourcingapp.task.Task;
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

public class TaskAvailableMapFragment extends SupportMapFragment implements
        OnMapReadyCallback,
        LocationListener,
        MainActivity.OnTasksUpdatedListener {

    // map related
    private final int BLUE_TRANSPARENT = 0x220000ff;
    private final int RED_TRANSPARENT = 0x22ff0000;
    private final int ZOOM_LEVEL = 13;
    private GoogleMap mGoogleMap;
    private Circle mCurrentVisibleCircle = null;
    private Map<Marker, Task> mMarkerToTask = new HashMap<>();

    // task related
    private List<Task> mActiveTasks = new ArrayList<>();
    private List<Task> mInactiveTasks = new ArrayList<>();

    public static final int ALL_MARKERS = 0x010;
    public static final int ACTIVE_MARKERS = 0x011;
    public static final int INACTIVE_MARKERS = 0x012;

    public TaskAvailableMapFragment() {
        super();
    }

    public int FLAG = ALL_MARKERS;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        getMapAsync(this);

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        UiSettings settings = mGoogleMap.getUiSettings();
        settings.setAllGesturesEnabled(true);
        settings.setCompassEnabled(true);
        settings.setMyLocationButtonEnabled(true);
        settings.setZoomControlsEnabled(true);
        settings.setIndoorLevelPickerEnabled(true);
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
                if (mActiveTasks.contains(task)) {
                    circleOptions.fillColor(RED_TRANSPARENT);
                }
                else {
                    circleOptions.fillColor(BLUE_TRANSPARENT);
                }
                mCurrentVisibleCircle = mGoogleMap.addCircle(circleOptions);

                return false;
            }
        });
        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                // start activity for task
                BackgroundLocationService.setDoStartService(false);
                Intent intent = new Intent(getActivity(), TaskCompleteActivity.class);
                intent.putExtra(Task.TASK_KEY_SERIALIZABLE, mMarkerToTask.get(marker).getId());
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

            BaseGoogleApiActivity baseGoogleApiActivity = (BaseGoogleApiActivity) getActivity();
            if (baseGoogleApiActivity.getGoogleApiClient().isConnected()) {
                updateCurrentLocation(baseGoogleApiActivity);
            }
        } else {
            Log.d(Constants.TAG, "ACCESS_FINE_LOCATION not granted and will not perform setMyLocationEnabled(true)");
        }

        updateMarkers(FLAG);
    }

    @Override
    public void onLocationChanged(Location location) {
        if(getView() != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL));
        }
    }

    public void updateCurrentLocation(@NonNull BaseGoogleApiActivity baseGoogleApiActivity) {
        if (ContextCompat.checkSelfPermission(baseGoogleApiActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = new LocationRequest()
                    .setNumUpdates(1)
                    .setInterval(100)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(baseGoogleApiActivity.getGoogleApiClient(), locationRequest, this);
        } else {
            Log.d(Constants.TAG, "ACCESS_FINE_LOCATION not granted and will not recenter map to current location");
        }
    }

    public void updateMarkers(int flag) {
        // remove previous markers
        if (mGoogleMap != null)
            mGoogleMap.clear();
        else
            return;

        List<Task> allTasks = new ArrayList<>();

        switch (flag) {
            case ACTIVE_MARKERS:
                allTasks.addAll(mActiveTasks);
                break;
            case INACTIVE_MARKERS:
                allTasks.addAll(mInactiveTasks);
                break;
            default:
                allTasks.addAll(mActiveTasks);
                allTasks.addAll(mInactiveTasks);
        }

        for (Task t : allTasks) {
            GeofenceLocation simpleGeofence = t.getLocation();
            LatLng latLng = new LatLng(simpleGeofence.getLatitude(), simpleGeofence.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(t.getName() + " ($" + t.getCost() + ") ->");
            if (mInactiveTasks.contains(t)) {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
            }
            mMarkerToTask.put(mGoogleMap.addMarker(markerOptions), t);
        }
    }

    @Override
    public void onTasksActivationUpdated(List<Task> activatedTasks, List<Task> inactivatedTasks) {
        mActiveTasks = activatedTasks;
        mInactiveTasks = inactivatedTasks;

        if (getView() != null) {
            updateMarkers(FLAG);
        }
    }
}
