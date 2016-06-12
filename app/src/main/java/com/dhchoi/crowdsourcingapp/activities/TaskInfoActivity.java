package com.dhchoi.crowdsourcingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.task.Task;
import com.dhchoi.crowdsourcingapp.user.UserManager;
import com.google.gson.Gson;

public class TaskInfoActivity extends AppCompatActivity {

    private String userId;

    private TextView mTaskName;

    public static final int TASK_REMOVED = 201;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_info);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userId = UserManager.getUserId(this);

        mTaskName = (TextView) findViewById(R.id.info_task_name);

        Task currentTask = new Gson().fromJson(getIntent().getExtras().getString("task"), Task.class);
        Log.i("TaskInfoActivity", "Task ID: " + currentTask.getId());
        Log.i("TaskInfoActivity", "Task Name: " + currentTask.getName());
        Log.i("TaskInfoActivity", "Task Cost: " + currentTask.getCost());

        mTaskName.setText(currentTask.getName());
    }
}
