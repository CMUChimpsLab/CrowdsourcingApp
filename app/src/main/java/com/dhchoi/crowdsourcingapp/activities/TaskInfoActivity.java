package com.dhchoi.crowdsourcingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.task.Task;
import com.dhchoi.crowdsourcingapp.task.TaskManager;
import com.dhchoi.crowdsourcingapp.user.UserManager;
import com.google.gson.Gson;

public class TaskInfoActivity extends AppCompatActivity {

    private String userId;

    private TextView mTaskName;
    private Button mManageTask;

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
        mManageTask = (Button) findViewById(R.id.btn_manage_task);

        Task currentTask = new Gson().fromJson(getIntent().getExtras().getString("task"), Task.class);
        Log.i("TaskInfoActivity", "Task ID: " + currentTask.getId());
        Log.i("TaskInfoActivity", "Task Name: " + currentTask.getName());
        Log.i("TaskInfoActivity", "Task Cost: " + currentTask.getCost());

        mTaskName.setText(currentTask.getName());
        mManageTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TaskInfoActivity.this, "Manage Activity", Toast.LENGTH_SHORT).show();
            }
        });

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
