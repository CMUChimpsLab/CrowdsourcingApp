package com.dhchoi.crowdsourcingapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.HttpClientAsyncTask;
import com.dhchoi.crowdsourcingapp.HttpClientCallable;
import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.services.GeofenceTransitionsIntentService;
import com.dhchoi.crowdsourcingapp.task.Task;
import com.dhchoi.crowdsourcingapp.task.TaskAction;
import com.dhchoi.crowdsourcingapp.task.TaskManager;
import com.dhchoi.crowdsourcingapp.user.UserManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskCompleteActivity extends AppCompatActivity {

    private List<ViewGroup> mTaskActionLayouts = new ArrayList<ViewGroup>();
    private SharedPreferences mSharedPreferences;
    private Button mSubmitResponseButton;
    private TextView mSubmissionNotice;
    private Task mTask;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<String> inactivatedTaskIds = Arrays.asList(intent.getStringArrayExtra(GeofenceTransitionsIntentService.INACTIVATED_TASK_ID_KEY));
            List<String> activatedTaskIds = Arrays.asList(intent.getStringArrayExtra(GeofenceTransitionsIntentService.ACTIVATED_TASK_ID_KEY));
            if (inactivatedTaskIds.contains(mTask.getId()) || activatedTaskIds.contains(mTask.getId())) {
                updateSubmitResponseButtonStatus();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_task_complete);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSharedPreferences = getSharedPreferences(Constants.DEFAULT_SHARED_PREF, MODE_PRIVATE);

        mTask = TaskManager.getTaskById(this, getIntent().getStringExtra(Task.TASK_KEY_SERIALIZABLE));
        Log.d(Constants.TAG, "creating activity with task: " + mTask);

        final ProgressBar submitResponseProgressBar = (ProgressBar) findViewById(R.id.submitResponseProgressBar);
        mSubmitResponseButton = (Button) findViewById(R.id.submit_button);
        mSubmissionNotice = (TextView) findViewById(R.id.submission_notice);
        ((TextView) findViewById(R.id.task_name)).setText(mTask.getName());
        ((TextView) findViewById(R.id.task_location)).setText(mTask.getLocation().getName());

        final ViewGroup taskActionsLayout = (ViewGroup) findViewById(R.id.task_actions);
        for (TaskAction taskAction : mTask.getTaskActions()) {
            if (taskAction.getType() == TaskAction.TaskActionType.TEXT) {
                View taskActionLayout = LayoutInflater.from(this).inflate(R.layout.task_action_text_complete, null);
                ((TextView) taskActionLayout.findViewById(R.id.task_action_description)).setText(taskAction.getDescription());
                taskActionLayout.findViewById(R.id.task_action_response).setTag(taskAction.getId());
                mTaskActionLayouts.add((ViewGroup) taskActionLayout);
                taskActionsLayout.addView(taskActionLayout);
            }
        }

        updateSubmitResponseButtonStatus();

        mSubmitResponseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSubmitResponseButton.setEnabled(false);
                submitResponseProgressBar.setVisibility(ProgressBar.VISIBLE);

                new HttpClientAsyncTask(Constants.APP_SERVER_TASK_COMPLETE_URL, HttpClientCallable.POST, getUserResponses()) {
                    @Override
                    protected void onPostExecute(String response) {
                        mSubmitResponseButton.setEnabled(true);
                        submitResponseProgressBar.setVisibility(ProgressBar.GONE);

                        try {
                            JSONObject responseObj = new JSONObject(response);
                            if (responseObj.getBoolean("result")) {
                                Toast.makeText(TaskCompleteActivity.this, "Response submitted!", Toast.LENGTH_SHORT).show();
                                // update the time when the task was completed
                                mTask.setCompletionTime(new Date().getTime()); // TODO: use same value from server
                                TaskManager.updateTask(TaskCompleteActivity.this, mTask);
                                // exit activity
                                TaskCompleteActivity.this.finish();
                            } else {
                                Toast.makeText(TaskCompleteActivity.this, "Your request was ill-formatted. Please check inputs again.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(Constants.TAG, e.getMessage());
                            Toast.makeText(TaskCompleteActivity.this, "Failed to submit request.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }.execute();
            }
        });

        // Register to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(GeofenceTransitionsIntentService.GEOFENCE_TRANSITION_BROADCAST));
    }

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    private void updateSubmitResponseButtonStatus() {
        // TODO: think about what to do if user leaves region on this screen

        if (!mTask.isActivated()) {
            mSubmitResponseButton.setEnabled(false);
            mSubmissionNotice.setVisibility(TextView.VISIBLE);
            mSubmissionNotice.setText("Please be located where the task can be activated.");
            return;
        }

        mSubmitResponseButton.setEnabled(true);
        mSubmissionNotice.setVisibility(TextView.GONE);
    }

    private Map<String, String> getUserResponses() {
        Map<String, String> userResponses = new HashMap<String, String>();
        userResponses.put("userId", UserManager.getUserId(this));
        userResponses.put("taskId", mTask.getId());
        String taskActionIds = "["; // [id, id, ..., id]
        String taskActionResponses = "{"; // {id: response, id: response, ..., id: response}

        for (ViewGroup viewGroup : mTaskActionLayouts) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View childView = viewGroup.getChildAt(i);
                if (childView instanceof EditText) {
                    String taskActionId = (String) childView.getTag();
                    String taskActionResponse = ((EditText) childView).getText().toString();

                    taskActionIds += taskActionId + ",";
                    taskActionResponses += "\"" + taskActionId + "\": \"" + taskActionResponse + "\",";
                }
            }
        }

        taskActionIds = taskActionIds.substring(0, taskActionIds.length() - 1);
        taskActionIds += "]";
        userResponses.put("taskActionIds", taskActionIds);

        taskActionResponses = taskActionResponses.substring(0, taskActionResponses.length() - 1);
        taskActionResponses += "}";
        userResponses.put("responses", taskActionResponses);

        Log.d(Constants.TAG, "userResponses: " + userResponses);

        return userResponses;
    }
}
