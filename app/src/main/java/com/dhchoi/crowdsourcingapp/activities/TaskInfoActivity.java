package com.dhchoi.crowdsourcingapp.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.HttpClientAsyncTask;
import com.dhchoi.crowdsourcingapp.HttpClientCallable;
import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.task.Task;
import com.dhchoi.crowdsourcingapp.task.TaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskInfoActivity extends AppCompatActivity {

    private static final String TAG = "TaskInfoActivity";

    private String taskId;

    private TextView mTaskName;
    private TextView mNumSubmittedResp;
    private TextView mNoResponseNotice;
    private ViewGroup mTaskResponseContainer;

    private Button mDeactivateTask;
    private Button mDeleteTask;

    private JSONArray responseList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_info);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTaskName = (TextView) findViewById(R.id.info_task_name);
        mNumSubmittedResp = (TextView) findViewById(R.id.num_submitted_response);
        mNoResponseNotice = (TextView) findViewById(R.id.no_response_notice);
        mDeactivateTask = (Button) findViewById(R.id.btn_deactivate_task);
        mDeleteTask = (Button) findViewById(R.id.btn_delete_task);

        mTaskResponseContainer = (ViewGroup) findViewById(R.id.task_response_container);

        Task currentTask = TaskManager.getTaskById(this, getIntent().getStringExtra("taskId"));
        taskId = currentTask.getId();
        Log.i(TAG, "Task ID: " + taskId);
        Log.i(TAG, "Task Name: " + currentTask.getName());
        Log.i(TAG, "Task Cost: " + currentTask.getCost());

        Log.d(TAG, "Getting responses");
        responseList = TaskManager.getTaskResponses(taskId);
        displayResponses();

        mTaskName.setText(currentTask.getName());

        mDeactivateTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TaskInfoActivity.this, "Not implemented yet", Toast.LENGTH_SHORT).show();
            }
        });
        mDeleteTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTask();
            }
        });
    }

    private void displayResponses() {
        if (responseList == null || responseList.length() <= 0) {
            mNoResponseNotice.setVisibility(View.VISIBLE);
            mNumSubmittedResp.setText(String.valueOf("0"));
        } else {
            mNoResponseNotice.setVisibility(View.GONE);
            List<JSONObject> allResponsesList = new ArrayList<>();

            try {
                // extract action responses
                for (int i = 0; i < responseList.length(); i++) {
                    JSONArray actionResponseList = ((JSONObject)responseList.get(i)).getJSONArray("taskactionresponses");
                    for (int j = 0; j < actionResponseList.length(); j++) {
                        JSONObject actionResponse = actionResponseList.getJSONObject(j);
                        allResponsesList.add(actionResponse);
                    }
                }

                for (JSONObject jsonObj : allResponsesList) {
                    View taskResponseLayout = LayoutInflater.from(this).inflate(R.layout.task_response_text, null);
                    ((TextView)taskResponseLayout.findViewById(R.id.response_description)).setText(
                            jsonObj.getString("response"));
                    ((TextView)taskResponseLayout.findViewById(R.id.response_user)).setText(
                            jsonObj.getString("userId"));
                    mTaskResponseContainer.addView(taskResponseLayout);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // number of responses
            mNumSubmittedResp.setText(String.valueOf(responseList.length()));
        }
    }

    private void deleteTask() {
        Map<String, String> params = new HashMap<>();
        new HttpClientAsyncTask(Constants.APP_SERVER_TASK_DELETE_URL + "/" + taskId, HttpClientCallable.GET, params) {
            @Override
            protected void onPostExecute(String response) {
                try {
                    if (response != null) {
                        Log.d("TaskInfoActivity", response);
                        onBackPressed();
                    } else {
                        Toast.makeText(TaskInfoActivity.this, "Failed to delete task", Toast.LENGTH_SHORT).show();
                        Log.d("TaskInfoActivity", "Response is null");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(Constants.TAG, e.getMessage());
                    Toast.makeText(TaskInfoActivity.this, "Error deleting task", Toast.LENGTH_LONG).show();
                }

                super.onPostExecute(response);
            }
        }.execute();
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
}
