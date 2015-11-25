package com.dhchoi.crowdsourcingapp.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.task.Task;
import com.dhchoi.crowdsourcingapp.task.TaskAction;

public class TaskCompletionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_task_completion);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Task task = (Task) getIntent().getSerializableExtra(Task.KEY_SERIALIZABLE);
        Log.d(Constants.TAG, "serialized task: " + task);

        ((TextView) findViewById(R.id.task_name)).setText(task.getName());
        ((TextView) findViewById(R.id.task_location)).setText(task.getLocation().getName());

        ViewGroup taskActionsLayout = (ViewGroup) findViewById(R.id.task_actions);
        for (TaskAction taskAction : task.getTaskActions()) {
            if (taskAction.getResponseType() == TaskAction.ResponseType.TEXT) {
                View taskActionLayout = LayoutInflater.from(this).inflate(R.layout.task_action_text, null);
                ((TextView) taskActionLayout.findViewById(R.id.task_action_description)).setText(taskAction.getDescription());
                taskActionsLayout.addView(taskActionLayout);
            }
        }
    }

}
