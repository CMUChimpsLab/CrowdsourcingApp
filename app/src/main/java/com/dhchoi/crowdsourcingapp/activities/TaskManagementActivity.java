package com.dhchoi.crowdsourcingapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
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
import com.dhchoi.crowdsourcingapp.services.GeofenceTransitionsIntentService;
import com.dhchoi.crowdsourcingapp.task.Task;
import com.dhchoi.crowdsourcingapp.task.TaskManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TaskManagementActivity extends BaseGoogleApiActivity {

    private ArrayAdapter<Task> mActiveTaskListAdapter;
    private ArrayAdapter<Task> mInactiveTaskListAdapter;
    private TextView mActiveTasksNotice;
    private TextView mInactiveTasksNotice;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            for (String activatedTaskId : intent.getStringArrayExtra(GeofenceTransitionsIntentService.ACTIVATED_TASK_ID_KEY)) {
                for (int i = 0; i < mInactiveTaskListAdapter.getCount(); i++) {
                    Task inactiveTask = mInactiveTaskListAdapter.getItem(i);
                    if (inactiveTask.getId().equals(activatedTaskId)) {
                        mInactiveTaskListAdapter.remove(inactiveTask);
                        mActiveTaskListAdapter.add(inactiveTask);
                    }
                }
            }

            for (String inactivatedTaskId : intent.getStringArrayExtra(GeofenceTransitionsIntentService.INACTIVATED_TASK_ID_KEY)) {
                for (int i = 0; i < mActiveTaskListAdapter.getCount(); i++) {
                    Task activeTask = mActiveTaskListAdapter.getItem(i);
                    if (activeTask.getId().equals(inactivatedTaskId)) {
                        mActiveTaskListAdapter.remove(activeTask);
                        mInactiveTaskListAdapter.add(activeTask);
                    }
                }
            }

            mActiveTaskListAdapter.notifyDataSetChanged();
            mInactiveTaskListAdapter.notifyDataSetChanged();
            updateNoticeTextViews();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setup views
        setContentView(R.layout.activity_task_management);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mActiveTasksNotice = (TextView) findViewById(R.id.active_tasks_notice);
        mInactiveTasksNotice = (TextView) findViewById(R.id.inactive_tasks_notice);

        // get task list
        List<Task> allTasks = TaskManager.getAllTasks(this);
        List<Task> activeTasks = new ArrayList<Task>();
        List<Task> inactiveTasks = new ArrayList<Task>();
        for (Task t : allTasks) {
            if (t.isActivated()) {
                activeTasks.add(t);
            } else {
                inactiveTasks.add(t);
            }
        }

        // setup task list views and adapters
        ListView activeTaskListView = (ListView) findViewById(R.id.active_tasks);
        activeTaskListView.setAdapter(mActiveTaskListAdapter = new TaskListAdapter(this, activeTasks));
        activeTaskListView.setOnItemClickListener(new OnTaskItemClickListener());

        ListView inactiveTaskListView = (ListView) findViewById(R.id.inactive_tasks);
        inactiveTaskListView.setAdapter(mInactiveTaskListAdapter = new TaskListAdapter(this, inactiveTasks));
        inactiveTaskListView.setOnItemClickListener(new OnTaskItemClickListener());

        updateNoticeTextViews();

        // Register to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(GeofenceTransitionsIntentService.GEOFENCE_TRANSITION_BROADCAST));
    }

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    private void updateNoticeTextViews() {
        mActiveTasksNotice.setVisibility(mActiveTaskListAdapter.getCount() > 0 ? TextView.GONE : TextView.VISIBLE);
        mInactiveTasksNotice.setVisibility(mInactiveTaskListAdapter.getCount() > 0 ? TextView.GONE : TextView.VISIBLE);
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
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.task_list_line, parent, false);
            }

            ((TextView) convertView.findViewById(R.id.task_name)).setText(task.getName());
            ((TextView) convertView.findViewById(R.id.task_location)).setText(task.getLocation().getName());
            ((TextView) convertView.findViewById(R.id.task_cost)).setText("$" + task.getCost());

            // Return the completed view to render on screen
            return convertView;
        }
    }

    class OnTaskItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Task task = (Task) parent.getItemAtPosition(position);
            Log.d(Constants.TAG, "clicked task: " + task);

            Intent intent = new Intent(TaskManagementActivity.this, TaskCompletionActivity.class);
            intent.putExtra(Task.TASK_KEY_SERIALIZABLE, task);

            startActivity(intent);
        }
    }
}
