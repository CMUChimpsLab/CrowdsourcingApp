package com.dhchoi.crowdsourcingapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.NotificationHelper;
import com.dhchoi.crowdsourcingapp.services.BackgroundLocationService;
import com.dhchoi.crowdsourcingapp.services.GeofenceIntentService;
import com.dhchoi.crowdsourcingapp.task.TaskManager;
import com.dhchoi.crowdsourcingapp.views.CustomListView;
import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.activities.MainActivity;
import com.dhchoi.crowdsourcingapp.activities.TaskCompleteActivity;
import com.dhchoi.crowdsourcingapp.task.Task;
import com.dhchoi.crowdsourcingapp.views.CustomSwipeRefreshLayout;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskAvailableListFragment extends Fragment implements
        MainActivity.OnTasksUpdatedListener, View.OnClickListener {

    private ArrayAdapter<Task> mActiveTaskListAdapter;
    private ArrayAdapter<Task> mInactiveTaskListAdapter;
    private TextView mActiveTasksNotice;
    private TextView mInactiveTasksNotice;

    private static final String TAG = "TaskAvailableList";
    private static Context context;
    private static boolean firstLaunch;     // whether to display notification

    // task related
    private List<Task> mActiveTasks = new ArrayList<>();
    private List<Task> mInactiveTasks = new ArrayList<>();

    public TaskAvailableListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();

        View rootView = inflater.inflate(R.layout.fragment_task_available_list, container, false);

        mActiveTasksNotice = (TextView) rootView.findViewById(R.id.active_tasks_notice);
        mInactiveTasksNotice = (TextView) rootView.findViewById(R.id.inactive_tasks_notice);
        Button mButtonActiveShowMap = (Button) rootView.findViewById(R.id.btn_show_in_map_active);
        Button mButtonInactiveShowMap = (Button) rootView.findViewById(R.id.btn_show_in_map_inactive);

        // setup task list views and adapters
        CustomListView activeTaskListView = (CustomListView) rootView.findViewById(R.id.active_tasks);
        activeTaskListView.setAdapter(mActiveTaskListAdapter = new TaskListAdapter(getActivity(), mActiveTasks));
        activeTaskListView.setOnItemClickListener(new OnTaskItemClickListener());

        CustomListView inactiveTaskListView = (CustomListView) rootView.findViewById(R.id.inactive_tasks);
        inactiveTaskListView.setAdapter(mInactiveTaskListAdapter = new TaskListAdapter(getActivity(), mInactiveTasks));
        inactiveTaskListView.setOnItemClickListener(new OnTaskItemClickListener());

        updateNoticeTextViews();

        final ScrollView scrollView = (ScrollView) rootView.findViewById(R.id.task_list_scroll_view);
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                scrollView.fullScroll(View.FOCUS_UP);
            }
        });

        final CustomSwipeRefreshLayout swipeRefreshLayout = (CustomSwipeRefreshLayout) rootView.findViewById(R.id.task_list_swipe_refresh);
        swipeRefreshLayout.setScrollView(scrollView);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Void... params) {
                        return TaskManager.syncTasks(getActivity());
                    }

                    @Override
                    protected void onPostExecute(Boolean syncSuccess) {
                        if (syncSuccess) {
                            mActiveTaskListAdapter.notifyDataSetChanged();
                            mInactiveTaskListAdapter.notifyDataSetChanged();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                        Snackbar.make(getView(), "Sync success!", Snackbar.LENGTH_LONG).show();
                    }

                }.execute();
            }
        });

        firstLaunch = true;

        mButtonActiveShowMap.setOnClickListener(this);
        mButtonInactiveShowMap.setOnClickListener(this);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onTasksActivationUpdated(List<Task> activatedTasks, List<Task> inactivatedTasks) {
        if (firstLaunch) {
            firstLaunch = false;
        } else {
            // hope it's not too noisy
            for (Task task : activatedTasks) {
                boolean contains = false;
                for (Task t : mActiveTasks) {
                    if (t.getId().equals(task.getId())) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) {        // new task available
                    NotificationHelper.createNotification(
                            "A Task Just Became Available",
                            "You just entered the active area of another task",
                            context,
                            MainActivity.class);
                    break;
                }
            }
        }

        mActiveTasks.clear();
        mActiveTasks.addAll(activatedTasks);
        mInactiveTasks.clear();
        mInactiveTasks.addAll(inactivatedTasks);

        if (getView() != null) {
            mActiveTaskListAdapter.notifyDataSetChanged();
            mInactiveTaskListAdapter.notifyDataSetChanged();
            updateNoticeTextViews();
        }
        else {
            Log.d(TAG, "onTasksActivationUpdated was called before view was initialized");
        }
    }

    private void updateNoticeTextViews() {
        mActiveTasksNotice.setVisibility(mActiveTaskListAdapter.getCount() > 0 ? TextView.GONE : TextView.VISIBLE);
        mInactiveTasksNotice.setVisibility(mInactiveTaskListAdapter.getCount() > 0 ? TextView.GONE : TextView.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_show_in_map_active:
                ((TaskAvailableFragment) getParentFragment()).swapFragments(TaskAvailableMapFragment.ACTIVE_MARKERS);
                break;
            case R.id.btn_show_in_map_inactive:
                ((TaskAvailableFragment) getParentFragment()).swapFragments(TaskAvailableMapFragment.INACTIVE_MARKERS);
                break;
        }
    }

    class TaskListAdapter extends ArrayAdapter<Task> {

        public TaskListAdapter(Context context, List<Task> tasks) {
            super(context, 0, tasks);
            Log.d(Constants.TAG, "got tasks: " + tasks);
        }

        @SuppressWarnings("all")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Task task = getItem(position);

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_task_available, parent, false);
            }

            // set texts
            ((TextView) convertView.findViewById(R.id.num_submitted_response)).setText(task.getName());
            ((TextView) convertView.findViewById(R.id.task_cost)).setText("$" +
                    new DecimalFormat("#0.00").format(
                            task.getCost()));

            // display either distance of location name
            String locationName = task.getLocation().getName();
            Matcher matcher = Pattern.compile("\\(*\\)").matcher(locationName);
            if (matcher.find()) {
                LatLng currentLocation;
                Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(((MainActivity) getActivity()).getGoogleApiClient());

                // returning from place picker
                if (lastKnownLocation == null) {
                    LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                    lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                if (lastKnownLocation != null) {
                    currentLocation = new LatLng(
                            lastKnownLocation.getLatitude(),
                            lastKnownLocation.getLongitude());

                    // calculate distance to task
                    double distance = GeofenceIntentService.getDistanceFromLatLng(task.getLocation().getLatLng(),
                            currentLocation);
                    ((TextView) convertView.findViewById(R.id.task_location)).setText(
                            new DecimalFormat("#.#").format(distance) + "m away");
                } else {
                    ((TextView) convertView.findViewById(R.id.task_location)).setText(locationName);
                }
            } else {
                ((TextView) convertView.findViewById(R.id.task_location)).setText(locationName);
            }

            // set remaining time
            String expiresAtText = "";
            Calendar currentTime = Calendar.getInstance();
            Calendar expiresAt = Calendar.getInstance();
            expiresAt.setTimeInMillis((long)task.getExpiresAt());
            if (expiresAt.getTimeInMillis() - currentTime.getTimeInMillis() < 60 * 60 * 1000) { // 1 hour
                expiresAtText = (expiresAt.getTimeInMillis() - currentTime.getTimeInMillis()) / (60 * 1000) + " minute(s) left";
            }
            else if (expiresAt.getTimeInMillis() - currentTime.getTimeInMillis() < 24 * 60 * 60 * 1000) { // 24 hours
                expiresAtText = (expiresAt.getTimeInMillis() - currentTime.getTimeInMillis()) / (60 * 60 * 1000) + " hour(s) left";
            }
            else {
                expiresAtText = (expiresAt.getTimeInMillis() - currentTime.getTimeInMillis()) / (60 * 60 * 60 * 1000) + " day(s) left";
            }
            ((TextView) convertView.findViewById(R.id.task_expires_at)).setText(expiresAtText);

            final ImageView taskImage = (ImageView) convertView.findViewById(R.id.task_image);

            if (task.isActivated())
                taskImage.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
            else
                taskImage.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));

            // Return the completed view to render on screen
            return convertView;
        }
    }

    class OnTaskItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Task task = (Task) parent.getItemAtPosition(position);
            Log.d(Constants.TAG, "clicked task: " + task);

            if (task.isCompleted()) {
                Toast.makeText(getActivity(), "You have answered this question", Toast.LENGTH_SHORT).show();
                return;
            }

            BackgroundLocationService.setDoStartService(false);
            Intent intent = new Intent(getActivity(), TaskCompleteActivity.class);
            intent.putExtra(Task.TASK_KEY_SERIALIZABLE, task.getId());

            startActivity(intent);
        }
    }
}
