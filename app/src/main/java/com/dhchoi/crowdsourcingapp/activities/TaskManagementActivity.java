package com.dhchoi.crowdsourcingapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.task.Task;
import com.dhchoi.crowdsourcingapp.task.TaskManager;

import java.util.List;

public class TaskManagementActivity extends BaseGoogleApiActivity {

    ArrayAdapter<Task> mTaskListViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTaskListViewAdapter = new TaskListAdapter(this, TaskManager.getAllTasks(this));

        // setup views
        setContentView(R.layout.activity_task_management);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ListView taskListView = (ListView) findViewById(R.id.listView);
        taskListView.setAdapter(mTaskListViewAdapter);
        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                Task task = (Task) taskListView.getItemAtPosition(position);
                Log.d(Constants.TAG, "clicked task: " + task);

                Intent intent = new Intent(TaskManagementActivity.this, TaskCompletionActivity.class);
                intent.putExtra(Task.KEY_SERIALIZABLE, task);

                startActivity(intent);
            }
        });
    }

    private void updateListViewAdapter() {
//        mTaskListViewAdapter.clear();
//        mTaskListViewAdapter.addAll(mTaskManager.getTasksList());
//        mTaskListViewAdapter.notifyDataSetChanged();
    }

    class TaskListAdapter extends ArrayAdapter<Task> {

        public TaskListAdapter(Context context, List<Task> tasks) {
            super(context, 0, tasks);
            Log.d(Constants.TAG, "got tasks: " + tasks);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Task task = getItem(position);

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.task_line, parent, false);
            }

            ((TextView) convertView.findViewById(R.id.task_name)).setText(task.getName());
            ((TextView) convertView.findViewById(R.id.task_location)).setText(task.getLocation().getName());
            ((TextView) convertView.findViewById(R.id.task_cost)).setText("$" + task.getCost());

            // Return the completed view to render on screen
            return convertView;
        }
    }

}
