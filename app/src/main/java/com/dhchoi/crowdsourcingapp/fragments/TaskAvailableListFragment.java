package com.dhchoi.crowdsourcingapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.views.CustomListView;
import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.activities.MainActivity;
import com.dhchoi.crowdsourcingapp.activities.TaskCompleteActivity;
import com.dhchoi.crowdsourcingapp.task.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TaskAvailableListFragment extends Fragment implements MainActivity.OnTasksUpdatedListener {

    private ArrayAdapter<Task> mActiveTaskListAdapter;
    private ArrayAdapter<Task> mInactiveTaskListAdapter;
    private TextView mActiveTasksNotice;
    private TextView mInactiveTasksNotice;

    // task related
    private List<Task> mActiveTasks = new ArrayList<Task>();
    private List<Task> mInactiveTasks = new ArrayList<Task>();

    public TaskAvailableListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task_available_list, container, false);

        mActiveTasksNotice = (TextView) rootView.findViewById(R.id.active_tasks_notice);
        mInactiveTasksNotice = (TextView) rootView.findViewById(R.id.inactive_tasks_notice);

        // setup task list views and adapters
        CustomListView activeTaskListView = (CustomListView) rootView.findViewById(R.id.active_tasks);
        activeTaskListView.setAdapter(mActiveTaskListAdapter = new TaskListAdapter(getActivity(), mActiveTasks));
        activeTaskListView.setOnItemClickListener(new OnTaskItemClickListener());

        CustomListView inactiveTaskListView = (CustomListView) rootView.findViewById(R.id.inactive_tasks);
        inactiveTaskListView.setAdapter(mInactiveTaskListAdapter = new TaskListAdapter(getActivity(), mInactiveTasks));
        inactiveTaskListView.setOnItemClickListener(new OnTaskItemClickListener());

        updateNoticeTextViews();

        final ScrollView scrollView = (ScrollView) rootView.findViewById(R.id.scroll_view);
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                scrollView.fullScroll(View.FOCUS_UP);
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onTasksActivationUpdated(List<Task> activatedTasks, List<Task> inactivatedTasks) {
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
            Log.d(Constants.TAG, "onTasksActivationUpdated was called before view was initialized");
        }
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
            ((TextView) convertView.findViewById(R.id.task_cost)).setText("$" + task.getCost());
            final ImageView taskImage = (ImageView) convertView.findViewById(R.id.task_image);
            Random random = new Random();
            taskImage.setBackgroundColor(0xff000000 + 256 * 256 * random.nextInt(256) + 256 * random.nextInt(256) + random.nextInt(256));
//            new AsyncTask<Void, Void, Bitmap>() {
//                @Override
//                protected Bitmap doInBackground(Void... params) {
//                    Bitmap image = null;
//                    try {
//                        URL url = new URL("https://c2.staticflickr.com/4/3713/10988185013_26082c04a4_b.jpg");
//                        image = BitmapFactory.decodeStream(url.openStream());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return image;
//                }
//
//                @Override
//                protected void onPostExecute(Bitmap image) {
//                    if (image != null) {
//                        taskImage.setImageBitmap(image);
//                    }
//                }
//            }.execute();

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
