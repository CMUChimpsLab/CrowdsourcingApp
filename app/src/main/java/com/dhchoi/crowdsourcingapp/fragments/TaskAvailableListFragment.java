package com.dhchoi.crowdsourcingapp.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
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
import com.dhchoi.crowdsourcingapp.activities.TaskCompleteActivity;
import com.dhchoi.crowdsourcingapp.services.GeofenceTransitionsIntentService;
import com.dhchoi.crowdsourcingapp.task.Task;
import com.dhchoi.crowdsourcingapp.task.TaskManager;

import java.util.ArrayList;
import java.util.List;

public class TaskAvailableListFragment extends Fragment {

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

    public TaskAvailableListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task_available_list, container, false);

        mActiveTasksNotice = (TextView) rootView.findViewById(R.id.active_tasks_notice);
        mInactiveTasksNotice = (TextView) rootView.findViewById(R.id.inactive_tasks_notice);

        // get task list
        List<Task> allTasks = TaskManager.getAllTasks(getActivity());
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
        ListView activeTaskListView = (ListView) rootView.findViewById(R.id.active_tasks);
        activeTaskListView.setAdapter(mActiveTaskListAdapter = new TaskListAdapter(getActivity(), activeTasks));
        activeTaskListView.setOnItemClickListener(new OnTaskItemClickListener());

        ListView inactiveTaskListView = (ListView) rootView.findViewById(R.id.inactive_tasks);
        inactiveTaskListView.setAdapter(mInactiveTaskListAdapter = new TaskListAdapter(getActivity(), inactiveTasks));
        inactiveTaskListView.setOnItemClickListener(new OnTaskItemClickListener());

        updateNoticeTextViews();

        // Register to receive messages.
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, new IntentFilter(GeofenceTransitionsIntentService.GEOFENCE_TRANSITION_BROADCAST));

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
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
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_task_available, parent, false);
            }

            ((TextView) convertView.findViewById(R.id.task_name)).setText(task.getName());
            ((TextView) convertView.findViewById(R.id.task_location)).setText(task.getLocation().getName());
            // BitmapDescriptorFactory.fromResource(R.drawable.marker))
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

            Intent intent = new Intent(getActivity(), TaskCompleteActivity.class);
            intent.putExtra(Task.TASK_KEY_SERIALIZABLE, task);

            startActivity(intent);
        }
    }
}
