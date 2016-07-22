package com.dhchoi.crowdsourcingapp.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.HttpClientAsyncTask;
import com.dhchoi.crowdsourcingapp.HttpClientCallable;
import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.services.BackgroundLocationService;
import com.dhchoi.crowdsourcingapp.user.UserManager;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class TaskCreateActivity extends AppCompatActivity {

    private static final String TAG = "TaskCreate";

    private final int PLACE_PICKER_REQUEST = 1;
    private final PlacePicker.IntentBuilder mPlacePickerIntentBuilder = new PlacePicker.IntentBuilder();
    private List<ViewGroup> mTaskActionLayouts = new ArrayList<>();
    private String userId;

    private MaterialEditText mTaskName;
    private MaterialEditText mTaskCost;
    private EditText mLocationName;
    private EditText mLocationLat;
    private EditText mLocationLng;
    private MaterialEditText mLocationRadius;
    private Button mDateAdd;
    private MaterialEditText mDateText;
    private Button mTimeAdd;
    private MaterialEditText mTimeText;
    private MaterialEditText mRefreshRate;
    private MaterialEditText mAnswersLeft;
    private CheckBox mEndlessAnswers;
    private Button mLocationAdd;
    private ViewGroup mTaskActionsContainer;
    private Button mTaskActionAdd;
    private Button mSubmit;
    private ProgressBar mSubmitProgressBar;

    private int mExpirationYear;
    private int mExpirationMonth;
    private int mExpirationDay;
    private int mExpirationHour;
    private int mExpirationMinute;

    private static final String SHOWCASE_ID = "TaskCreateActivityShowcase";

    @SuppressWarnings("all")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_create);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userId = UserManager.getUserId(this);

        // views
        mTaskName = (MaterialEditText) findViewById(R.id.task_name);
        mTaskCost = (MaterialEditText) findViewById(R.id.task_cost);
        mLocationName = (EditText) findViewById(R.id.location_name);
        mLocationLat = (EditText) findViewById(R.id.location_lat);
        mLocationLng = (EditText) findViewById(R.id.location_lng);
        mLocationRadius = (MaterialEditText) findViewById(R.id.location_radius);
        mLocationAdd = (Button) findViewById(R.id.location_add_btn);
        mTaskActionsContainer = (ViewGroup) findViewById(R.id.task_actions_container);
        mTaskActionAdd = (Button) findViewById(R.id.task_action_add_btn);
        mSubmit = (Button) findViewById(R.id.submit_btn);
        mSubmitProgressBar = (ProgressBar) findViewById(R.id.submit_progress_bar);
        mDateAdd = (Button) findViewById(R.id.date_add_btn);
        mDateText = (MaterialEditText) findViewById(R.id.date_add_text);
        mTimeAdd = (Button) findViewById(R.id.time_add_btn);
        mTimeText = (MaterialEditText) findViewById(R.id.time_add_text);
        mRefreshRate = (MaterialEditText) findViewById(R.id.refresh_rate);
        mAnswersLeft = (MaterialEditText) findViewById(R.id.answers_left);
        mEndlessAnswers = (CheckBox) findViewById(R.id.endless_answers_check);

        // make date & time not editable
        mDateText.setKeyListener(null);
        mTimeText.setKeyListener(null);
        mLocationName.setKeyListener(null);

        // validate cost decimal
        mTaskCost.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {        // user has left this blank
                    MaterialEditText editText = (MaterialEditText) v;
                    try {
                        if (editText.getText() != null) {
                            double cost = Double.valueOf(editText.getText().toString());
                            editText.setText(
                                    String.valueOf(
                                            new DecimalFormat("#.##").format(
                                                    Math.floor(cost * 100 + .5) / 100)));
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        editText.setText("0");
                    }
                }
            }
        });

        // radius cap 100
        mLocationRadius.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    if (Integer.parseInt(mLocationRadius.getText().toString()) > 100)
                        mLocationRadius.setText(String.valueOf(100));
                }
            }
        });

        // set OnClickListeners on buttons
        mDateAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get current date
                final Calendar c = Calendar.getInstance();
                final int currentYear = c.get(Calendar.YEAR);
                final int currentMonth = c.get(Calendar.MONTH);
                final int currentDay = c.get(Calendar.DAY_OF_MONTH);
                // create and show dialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(TaskCreateActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        mExpirationYear = year;
                        mExpirationMonth = monthOfYear + 1;
                        mExpirationDay = dayOfMonth;
                        mDateText.setText(mExpirationYear + "/" + mExpirationMonth + "/" + mExpirationDay);
                    }
                }, currentYear, currentMonth, currentDay);
                datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
                datePickerDialog.getDatePicker().updateDate(mExpirationYear, mExpirationMonth - 1, mExpirationDay);
                datePickerDialog.setTitle(null);
                datePickerDialog.show();
            }
        });
        mTimeAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get current time
                final Calendar c = Calendar.getInstance();
                final int currentHour = c.get(Calendar.HOUR_OF_DAY);
                final int currentMinute = c.get(Calendar.MINUTE);
                // create and show dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(TaskCreateActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeZone(TimeZone.getDefault());
                        long currentTimeInMillis = calendar.getTimeInMillis();
                        calendar.set(Calendar.YEAR, mExpirationYear);
                        calendar.set(Calendar.MONTH, mExpirationMonth - 1);
                        calendar.set(Calendar.DAY_OF_MONTH, mExpirationDay);
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        if (calendar.getTimeInMillis() < currentTimeInMillis) {
                            Toast.makeText(TaskCreateActivity.this, "Cannot set expiration time in the past", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        mExpirationHour = hourOfDay;
                        mExpirationMinute = minute;
                        mTimeText.setText(mExpirationHour + ":" + (mExpirationMinute < 10 ? "0" + mExpirationMinute : mExpirationMinute + ""));    // add padding
                    }
                }, currentHour, currentMinute, false);
                timePickerDialog.updateTime(mExpirationHour, mExpirationMinute);
                timePickerDialog.show();
            }
        });
        mLocationAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivityForResult(mPlacePickerIntentBuilder.build(TaskCreateActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
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

                Location createLocation;
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    createLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (createLocation == null)
                        createLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                } else
                    createLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                userEntries.put("createLat", createLocation == null ? null : String.valueOf(createLocation.getLatitude()));
                userEntries.put("createLng", createLocation == null ? null : String.valueOf(createLocation.getLongitude()));

                mSubmit.setEnabled(false);
                mSubmitProgressBar.setVisibility(ProgressBar.VISIBLE);

                new HttpClientAsyncTask(Constants.APP_SERVER_TASK_CREATE_URL, HttpClientCallable.POST, userEntries) {
                    @Override
                    protected void onPostExecute(String response) {
                        mSubmit.setEnabled(true);
                        mSubmitProgressBar.setVisibility(ProgressBar.GONE);

                        try {
                            JSONObject responseObj = new JSONObject(response);
                            Log.d(Constants.TAG, "Server response: " + responseObj);
                            if (!responseObj.getString("createdTaskId").isEmpty()) {
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
        mEndlessAnswers.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    mAnswersLeft.setEnabled(false);
                else
                    mAnswersLeft.setEnabled(true);
            }
        });

        hideSoftKeyboard();

        // showcase the views
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(300);

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);
        sequence.setConfig(config);

        sequence.addSequenceItem(mTaskName, "Task Name", "e.g. Is the CHIMPS Lab crowded right now?", "GOT IT")
                .addSequenceItem(mRefreshRate, "Refresh Rate", "Interval between each answer", "GOT IT")
                .addSequenceItem(mLocationRadius, "Location Radius", "Radius of area where people can do this task", "GOT IT")
                .addSequenceItem(mAnswersLeft, "Total Answers", "How many answers you expect to receive, check endless if no limit", "GOT IT")
                .start();

        setDefaultTime();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            Log.d(TAG, "Returned from PlacePicker");
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                mLocationName.setText(place.getName());
                mLocationLat.setText(String.valueOf(place.getLatLng().latitude));
                mLocationLng.setText(String.valueOf(place.getLatLng().longitude));
            }
        }
    }

    private boolean hasAllFieldsEntered(Map<String, String> userEntries) {
        for (String key : userEntries.keySet()) {
            if (userEntries.get(key).isEmpty()) {
                return false;
            }
        }

        // required fields: (with at least one pair of [taskDescription, taskType])
        String[] requiredFields = {"userId", "taskName", "cost", "expiresAt", "refreshRate", "locationName", "lat", "lng", "radius", "taskDescription", "taskType"};
        return userEntries.size() >= requiredFields.length;

    }

    private void setDefaultTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(calendar.getTimeInMillis() + 1000 * 60 * 60 * 24);
        mExpirationYear = calendar.get(Calendar.YEAR);
        mExpirationMonth = calendar.get(Calendar.MONTH) + 1;
        mExpirationDay = calendar.get(Calendar.DAY_OF_MONTH);
        mExpirationHour = calendar.get(Calendar.HOUR_OF_DAY);
        mExpirationMinute = calendar.get(Calendar.MINUTE);

        mDateText.setText(mExpirationYear + "/" + mExpirationMonth + "/" + mExpirationDay);
        mTimeText.setText(mExpirationHour + ":" + (mExpirationMinute < 10 ? "0" + mExpirationMinute : mExpirationMinute + ""));    // add padding
    }

    private String getExpiresAt() {
        long expirationTime = 0;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        dateFormat.setTimeZone(TimeZone.getDefault());
        try {
            Date expirationDate = dateFormat.parse(mExpirationYear + "/" + mExpirationMonth + "/" + mExpirationDay + " " + mExpirationHour + ":" + mExpirationMinute);
            Log.d(Constants.TAG, "Expiration date: " + expirationDate.toString());
            expirationTime = expirationDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return expirationTime == 0 ? null : Long.toString(expirationTime);
    }

    private Map<String, String> getUserEntries() {
        Map<String, String> userEntries = new HashMap<>();
        userEntries.put("userId", userId);
        userEntries.put("taskName", mTaskName.getText().toString());
        userEntries.put("cost", mTaskCost.getText().toString());
        String expiresAt = getExpiresAt();
        if (expiresAt != null)
            userEntries.put("expiresAt", expiresAt);
        userEntries.put("refreshRate", mRefreshRate.getText().toString());
        userEntries.put("locationName", mLocationName.getText().toString());
        userEntries.put("lat", mLocationLat.getText().toString());
        userEntries.put("lng", mLocationLng.getText().toString());
        userEntries.put("radius", mLocationRadius.getText().toString());
        userEntries.put("answersLeft", mEndlessAnswers.isChecked() ? "-1" : mAnswersLeft.getText().toString());

        int tagId = 0;
        for (ViewGroup taskActionViewGroup : mTaskActionLayouts) {
            String descriptionKey = "taskActions[" + tagId + "][description]";
            String typeKey = "taskActions[" + tagId + "][type]";
            String descriptionValue = ((MaterialEditText) taskActionViewGroup.findViewById(R.id.action_description)).getText().toString();
            String typeValue = ((Spinner) taskActionViewGroup.findViewById(R.id.action_type)).getSelectedItem().toString();
            if (!descriptionValue.isEmpty() && !typeValue.isEmpty()) {
                userEntries.put(descriptionKey, descriptionValue);
                userEntries.put(typeKey, typeValue);
                tagId++;
            }
        }

        return userEntries;
    }

    private void hideSoftKeyboard() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        BackgroundLocationService.setDoStartService(false);
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        if (BackgroundLocationService.isServiceRunning(getApplicationContext(), BackgroundLocationService.class))
            stopService(new Intent(getApplicationContext(), BackgroundLocationService.class));
        super.onResume();
    }

    @Override
    protected void onStop() {
        if (BackgroundLocationService.whetherStartService())
            BackgroundLocationService.startLocationService(getApplicationContext());
        BackgroundLocationService.setDoStartService(true);
        super.onStop();
    }
}
