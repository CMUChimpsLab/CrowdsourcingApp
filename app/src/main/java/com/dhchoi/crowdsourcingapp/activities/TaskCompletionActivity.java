package com.dhchoi.crowdsourcingapp.activities;

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
import android.widget.TextView;
import android.widget.Toast;

import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.HttpClientAsyncTask;
import com.dhchoi.crowdsourcingapp.HttpClientCallable;
import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.task.Task;
import com.dhchoi.crowdsourcingapp.task.TaskAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskCompletionActivity extends AppCompatActivity {

    private List<ViewGroup> mTaskActionLayouts = new ArrayList<ViewGroup>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_task_completion);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Task task = (Task) getIntent().getSerializableExtra(Task.KEY_SERIALIZABLE);
        Log.d(Constants.TAG, "serialized task: " + task);

        final ProgressBar submitResponseProgressBar = (ProgressBar) findViewById(R.id.submitResponseProgressBar);
        final Button submitResponseButton = (Button) findViewById(R.id.submit_button);
        ((TextView) findViewById(R.id.task_name)).setText(task.getName());
        ((TextView) findViewById(R.id.task_location)).setText(task.getLocation().getName());

        final ViewGroup taskActionsLayout = (ViewGroup) findViewById(R.id.task_actions);
        for (TaskAction taskAction : task.getTaskActions()) {
            if (taskAction.getType() == TaskAction.TaskActionType.TEXT) {
                View taskActionLayout = LayoutInflater.from(this).inflate(R.layout.task_action_text, null);
                ((TextView) taskActionLayout.findViewById(R.id.task_action_description)).setText(taskAction.getDescription());
                taskActionLayout.findViewById(R.id.task_action_response).setTag(taskAction.getId());
                mTaskActionLayouts.add((ViewGroup) taskActionLayout);
                taskActionsLayout.addView(taskActionLayout);
            }
        }

        submitResponseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitResponseButton.setEnabled(false);
                submitResponseProgressBar.setVisibility(ProgressBar.VISIBLE);

                new HttpClientAsyncTask(Constants.APP_SERVER_TEST_URL, HttpClientCallable.POST, getUserResponses(task)) {
                    @Override
                    protected void onPostExecute(String response) {
                        submitResponseButton.setEnabled(true);
                        submitResponseProgressBar.setVisibility(ProgressBar.GONE);

                        if (response != null) {
                            Toast.makeText(TaskCompletionActivity.this, "Response submitted!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(TaskCompletionActivity.this, "Failed to submit response!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }.execute();
            }
        });
    }

    private Map<String, String> getUserResponses(Task task) {
        Map<String, String> userResponses = new HashMap<String, String>();
        userResponses.put("taskId", task.getId());

        for (ViewGroup viewGroup : mTaskActionLayouts) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View childView = viewGroup.getChildAt(i);
                if (childView instanceof EditText) {
                    userResponses.put((String) childView.getTag(), ((EditText) childView).getText().toString());
                }
            }
        }

        Log.d(Constants.TAG, "userResponses: " + userResponses);

        return userResponses;
    }
}
