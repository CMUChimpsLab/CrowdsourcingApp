package com.dhchoi.crowdsourcingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.HttpClientAsyncTask;
import com.dhchoi.crowdsourcingapp.HttpClientCallable;
import com.dhchoi.crowdsourcingapp.R;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskCreateActivity extends AppCompatActivity {

    private final int PLACE_PICKER_REQUEST = 1;
    private final PlacePicker.IntentBuilder mPlacePickerIntentBuilder = new PlacePicker.IntentBuilder();
    private List<ViewGroup> mTaskActionLayouts = new ArrayList<ViewGroup>();
    private String userId;

    private EditText mTaskName;
    private EditText mTaskCost;
    private EditText mLocationName;
    private EditText mLocationLat;
    private EditText mLocationLng;
    private EditText mLocationRadius;
    private Button mLocationAdd;
    private ViewGroup mTaskActionsContainer;
    private Button mTaskActionAdd;
    private Button mSubmit;
    private ProgressBar mSubmitProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_create);

        // TODO: check why back arrow has different behavior as back button
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userId = getSharedPreferences(Constants.DEFAULT_SHARED_PREF, MODE_PRIVATE).getString(Constants.USER_ID_KEY, "");

        // views
        mTaskName = (EditText) findViewById(R.id.task_name);
        mTaskCost = (EditText) findViewById(R.id.task_cost);
        mLocationName = (EditText) findViewById(R.id.location_name);
        mLocationLat = (EditText) findViewById(R.id.location_lat);
        mLocationLng = (EditText) findViewById(R.id.location_lng);
        mLocationRadius = (EditText) findViewById(R.id.location_radius);
        mLocationAdd = (Button) findViewById(R.id.location_add_btn);
        mTaskActionsContainer = (ViewGroup) findViewById(R.id.task_actions_container);
        mTaskActionAdd = (Button) findViewById(R.id.task_action_add_btn);
        mSubmit = (Button) findViewById(R.id.submit_btn);
        mSubmitProgressBar = (ProgressBar) findViewById(R.id.submit_progress_bar);

        // set OnClickListeners on buttons
        mLocationAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivityForResult(mPlacePickerIntentBuilder.build(TaskCreateActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
        mTaskActionAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View taskActionLayout = LayoutInflater.from(TaskCreateActivity.this).inflate(R.layout.task_action_text_create, null);
                mTaskActionLayouts.add((ViewGroup) taskActionLayout);
                mTaskActionsContainer.addView(taskActionLayout);
            }
        });
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> userEntries = getUserEntries();
                Log.d(Constants.TAG, "User attempting to submit: " + userEntries.toString());

                if (!hasAllFieldsEntered(userEntries)) {
                    Toast.makeText(TaskCreateActivity.this, "Please check if all fields have been completed.", Toast.LENGTH_SHORT).show();
                    return;
                }

                mSubmit.setEnabled(false);
                mSubmitProgressBar.setVisibility(ProgressBar.VISIBLE);

                new HttpClientAsyncTask(Constants.APP_SERVER_TASK_COMPLETE_URL, HttpClientCallable.POST, userEntries) {
                    @Override
                    protected void onPostExecute(String response) {
                        mSubmit.setEnabled(true);
                        mSubmitProgressBar.setVisibility(ProgressBar.GONE);

                        try {
                            JSONObject responseObj = new JSONObject(response);
                            Log.d(Constants.TAG, "Server response: " + responseObj);
                            if (responseObj.getBoolean("result")) {
                                Toast.makeText(TaskCreateActivity.this, "Task created!", Toast.LENGTH_SHORT).show();
                                TaskCreateActivity.this.finish();
                            } else {
                                Toast.makeText(TaskCreateActivity.this, "Your request was ill-formatted. Please check inputs again.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(Constants.TAG, e.getMessage());
                            Toast.makeText(TaskCreateActivity.this, "Failed to submit request.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }.execute();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                mLocationName.setText(place.getName());
                mLocationLat.setText(String.valueOf(place.getLatLng().latitude));
                mLocationLng.setText(String.valueOf(place.getLatLng().longitude));
                mLocationRadius.setText(String.valueOf(60.0f));
            }
        }
    }

    private boolean hasAllFieldsEntered(Map<String, String> userEntries) {
        for (String key : userEntries.keySet()) {
            if (userEntries.get(key).isEmpty()) {
                return false;
            }
        }

        // required fields: userId, taskName, cost, locationName, lat, lng, radius, at least one pair of (taskDescription, taskType)
        if (userEntries.size() < 9) {
            return false;
        }

        return true;
    }

    private Map<String, String> getUserEntries() {
        Map<String, String> userEntries = new HashMap<String, String>();
        userEntries.put("userId", userId);
        userEntries.put("taskName", mTaskName.getText().toString());
        userEntries.put("cost", mTaskCost.getText().toString());
        userEntries.put("locationName", mLocationName.getText().toString());
        userEntries.put("lat", mLocationLat.getText().toString());
        userEntries.put("lng", mLocationLng.getText().toString());
        userEntries.put("radius", mLocationRadius.getText().toString());

        int tagId = 0;
        for (ViewGroup taskActionViewGroup : mTaskActionLayouts) {
            String descriptionKey = "taskActions[" + tagId + "][description]";
            String typeKey = "taskActions[" + tagId + "][type]";
            String descriptionValue = ((EditText) taskActionViewGroup.findViewById(R.id.action_description)).getText().toString();
            String typeValue = ((Spinner) taskActionViewGroup.findViewById(R.id.action_type)).getSelectedItem().toString();
            if (!descriptionValue.isEmpty() && !typeValue.isEmpty()) {
                userEntries.put(descriptionKey, descriptionValue);
                userEntries.put(typeKey, typeValue);
                tagId++;
            }
        }

        return userEntries;
    }
}
