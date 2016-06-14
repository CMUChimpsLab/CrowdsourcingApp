package com.dhchoi.crowdsourcingapp.activities;

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

import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.HttpClientAsyncTask;
import com.dhchoi.crowdsourcingapp.HttpClientCallable;
import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.task.Task;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class TaskInfoActivity extends AppCompatActivity {

    private String taskId;

    private TextView mTaskName;
    private Button mManageTask;
    private Button mDeleteTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_info);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTaskName = (TextView) findViewById(R.id.info_task_name);
        mManageTask = (Button) findViewById(R.id.btn_manage_task);
        mDeleteTask = (Button) findViewById(R.id.btn_delete_task);

        Task currentTask = new Gson().fromJson(getIntent().getExtras().getString("task"), Task.class);
        taskId = currentTask.getId();
        Log.i("TaskInfoActivity", "Task ID: " + taskId);
        Log.i("TaskInfoActivity", "Task Name: " + currentTask.getName());
        Log.i("TaskInfoActivity", "Task Cost: " + currentTask.getCost());

        mTaskName.setText(currentTask.getName());
        mManageTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TaskInfoActivity.this, "Manage Activity", Toast.LENGTH_SHORT).show();
            }
        });
        mDeleteTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTask();
            }
        });

    }

    private void deleteTask() {
        Map<String, String> params = new HashMap<>();
        new HttpClientAsyncTask(Constants.APP_SERVER_USER_DELETE_URL + "/" + taskId, HttpClientCallable.GET, params) {
            @Override
            protected void onPostExecute(String response) {
                // showProgress(false);
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
