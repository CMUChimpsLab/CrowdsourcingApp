package com.dhchoi.crowdsourcingapp.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.task.Task;
import com.dhchoi.crowdsourcingapp.task.TaskManager;

public class ResponseInfoActivity extends AppCompatActivity {

    private static final String TAG = "ResponseInfoActivity";

    private String taskId;

    private TextView mTaskName;
    private Button mReturnBtn;
    private Button mDeleteTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resp_info);

        mTaskName = (TextView) findViewById(R.id.resp_task_name);
        mReturnBtn = (Button) findViewById(R.id.btn_return);
        mDeleteTask = (Button) findViewById(R.id.btn_delete_task);

        Task currentTask = TaskManager.getTaskById(this, getIntent().getStringExtra("taskId"));
        taskId = currentTask.getId();
        Log.i(TAG, "Task ID: " + taskId);
        Log.i(TAG, "Task Name: " + currentTask.getName());
        Log.i(TAG, "Task Cost: " + currentTask.getCost());

        mTaskName.setText(currentTask.getName());
        mReturnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
