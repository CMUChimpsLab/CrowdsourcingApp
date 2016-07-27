package com.dhchoi.crowdsourcingapp.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.activities.MainActivity;
import com.dhchoi.crowdsourcingapp.activities.TaskCreateActivity;
import com.dhchoi.crowdsourcingapp.activities.TaskInfoActivity;
import com.dhchoi.crowdsourcingapp.services.BackgroundLocationService;
import com.dhchoi.crowdsourcingapp.task.Task;
import com.dhchoi.crowdsourcingapp.task.TaskManager;
import com.dhchoi.crowdsourcingapp.user.UserManager;
import com.dhchoi.crowdsourcingapp.views.CustomSwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserInfoFragment extends Fragment implements MainActivity.OnTasksUpdatedListener {

    public static final String NAME = "MY INFO";
    private final int COLOR_ON = 0xffffffff;
    private final int COLOR_OFF = 0x22000000;

    // task related
    private List<Task> mCreatedTasks = new ArrayList<>();
    private List<Task> mCompletedTasks = new ArrayList<>();

    private ArrayAdapter<Task> mCreatedTaskListAdapter;
    private ArrayAdapter<Task> mCompletedTaskListAdapter;

    private TextView mUserId;
    private TextView mUserBalance;
    private TextView mCreatedTasksNotice;
    private TextView mCompletedTasksNotice;
    private ListView mListCreatedTasks;
    private ListView mListCompletedTasks;
    private TextView mNumCreatedTasks;
    private TextView mNumCompletedTasks;
    private LinearLayout mNumCreatedTasksTitle;
    private LinearLayout mNumCompletedTasksTitle;
    private CustomSwipeRefreshLayout mSwipeRefresh;

    private static final int TIME_OFFSET = -1000 * 3600 * 4;        // UTC to EST

    public UserInfoFragment() {
    }

    public static UserInfoFragment newInstance() {
        return new UserInfoFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_user_info, container, false);

        final FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // warn about region
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                if (!sharedPreferences.getBoolean("boundary_warning_shown", false)) {
                    android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(getActivity())
                            .setPositiveButton("GOT IT", null)
                            .setMessage("We are still in testing phase so please kindly limit your tasks inside the CMU campus")
                            .create();
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.show();

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("boundary_warning_shown", true)
                            .apply();
                    return;
                }

                BackgroundLocationService.setDoStartService(false);
                Intent intent = new Intent(getActivity(), TaskCreateActivity.class);
                startActivity(intent);
            }
        });

        final LinearLayout createdTasksContainer = (LinearLayout) rootView.findViewById(R.id.created_tasks_container);
        final LinearLayout completedTasksContainer = (LinearLayout) rootView.findViewById(R.id.completed_tasks_container);

        // setup created-tasks related lists
        mListCreatedTasks = (ListView) rootView.findViewById(R.id.list_created_tasks);
        mListCreatedTasks.setAdapter(mCreatedTaskListAdapter = new CreatedTaskListAdapter(getActivity(), mCreatedTasks));
        mListCreatedTasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BackgroundLocationService.setDoStartService(false);
                Intent intent = new Intent(getActivity(), TaskInfoActivity.class);
                intent.putExtra("taskId", mCreatedTaskListAdapter.getItem(position).getId());
                startActivity(intent);
            }
        });
        mNumCreatedTasksTitle = (LinearLayout) rootView.findViewById(R.id.num_created_tasks_title_layout);
        mNumCreatedTasksTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (createdTasksContainer.getVisibility() == View.GONE) {
                    // enable task created
                    createdTasksContainer.setVisibility(View.VISIBLE);
                    mNumCreatedTasksTitle.setBackgroundColor(COLOR_ON);
                    // disable task completed
                    completedTasksContainer.setVisibility(View.GONE);
                    mNumCompletedTasksTitle.setBackgroundColor(COLOR_OFF);

                    mSwipeRefresh.setListView(mListCreatedTasks);
                }
            }
        });

        // setup completed-tasks related lists
        mListCompletedTasks = (ListView) rootView.findViewById(R.id.list_completed_tasks);
        mListCompletedTasks.setAdapter(mCompletedTaskListAdapter = new CompletedTaskListAdapter(getActivity(), mCompletedTasks));
        mListCompletedTasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    String taskId = mCompletedTaskListAdapter.getItem(position).getId();
                    Bundle args = new Bundle();
                    args.putString("taskId", taskId);

                    DialogFragment fragment = ResponseInfoDialogFragment.newInstance(args);
                    fragment.show(getFragmentManager(), taskId);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mNumCompletedTasksTitle = (LinearLayout) rootView.findViewById(R.id.num_completed_tasks_title_layout);
        mNumCompletedTasksTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (completedTasksContainer.getVisibility() == View.GONE) {
                    // disable task created
                    createdTasksContainer.setVisibility(View.GONE);
                    mNumCreatedTasksTitle.setBackgroundColor(COLOR_OFF);
                    // enable task completed
                    completedTasksContainer.setVisibility(View.VISIBLE);
                    mNumCompletedTasksTitle.setBackgroundColor(COLOR_ON);

                    mSwipeRefresh.setListView(mListCompletedTasks);
                }
            }
        });

        // update views
        mNumCreatedTasks = (TextView) rootView.findViewById(R.id.num_created_tasks);
        mNumCompletedTasks = (TextView) rootView.findViewById(R.id.num_completed_tasks);
        mCreatedTasksNotice = (TextView) rootView.findViewById(R.id.created_tasks_notice);
        mCompletedTasksNotice = (TextView) rootView.findViewById(R.id.completed_tasks_notice);

        String userId = UserManager.getUserId(getActivity());
        mUserId = (TextView) rootView.findViewById(R.id.user_id);
        mUserId.setText(userId);

        mUserBalance = (TextView) rootView.findViewById(R.id.available_balance);

        // swipe refresh layout
        mSwipeRefresh = (CustomSwipeRefreshLayout) rootView.findViewById(R.id.user_info_swipe_refresh);
        mSwipeRefresh.setListView(mListCreatedTasks);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        // update the two layouts
                        TaskManager.syncTasks(getActivity());
                        UserManager.syncUser(getActivity());
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        mCreatedTaskListAdapter.notifyDataSetChanged();
                        updateUserTextViews();

                        mSwipeRefresh.setRefreshing(false);
                        Snackbar.make(getView(), "Sync success!", Snackbar.LENGTH_LONG).show();
                    }
                }.execute();
            }
        });

        updateNoticeTextViews();

        return rootView;
    }

    private void updateNoticeTextViews() {
        mCreatedTasksNotice.setVisibility(mCreatedTaskListAdapter.getCount() > 0 ? TextView.GONE : TextView.VISIBLE);
        mCompletedTasksNotice.setVisibility(mCompletedTaskListAdapter.getCount() > 0 ? TextView.GONE : TextView.VISIBLE);
    }

    @SuppressWarnings("All")
    public void updateUserTextViews() {
        mUserBalance.setText(String.format("%.2f", UserManager.getUserBalance(getActivity())));
    }

    /**
     * Get all tasks from local storage
     */
    private void fetchTasks() {
        if (getActivity() == null)
            return;

        // fetch tasks
        mCreatedTasks.clear();
        mCreatedTasks.addAll(TaskManager.getAllOwnedTasks(getActivity()));
        mCreatedTaskListAdapter.notifyDataSetChanged();
        mNumCreatedTasks.setText(String.valueOf(mCreatedTasks.size()));
        if (mCreatedTaskListAdapter.getCount() > 0)
            mCreatedTasksNotice.setVisibility(View.GONE);
        else
            mCreatedTasksNotice.setVisibility(View.VISIBLE);

        mCompletedTasks.clear();
        mCompletedTasks.addAll(TaskManager.getAllUnownedCompletedTasks(getActivity()));
        mCompletedTaskListAdapter.notifyDataSetChanged();
        mNumCompletedTasks.setText(String.valueOf(mCompletedTasks.size()));
        if (mCompletedTaskListAdapter.getCount() > 0)
            mCompletedTasksNotice.setVisibility(View.GONE);
        else
            mCompletedTasksNotice.setVisibility(View.VISIBLE);

        if (mSwipeRefresh.isRefreshing())
            mSwipeRefresh.setRefreshing(false);
    }

    @Override
    public void onTasksActivationUpdated(List<Task> activatedTasks, List<Task> inactivatedTasks) {
        fetchTasks();
    }

    class CreatedTaskListAdapter extends ArrayAdapter<Task> {

        private View mConvertView;

        public CreatedTaskListAdapter(Context context, List<Task> tasks) {
            super(context, android.R.layout.simple_list_item_1, tasks);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            mConvertView = convertView;
            Task task = getItem(position);

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_task_created, parent, false);
            }

            updateTaskInfoDisplay(task, convertView);

            return convertView;
        }

        public void updateTaskInfoDisplay(Task task, final View convertView) {
            ((TextView) convertView.findViewById(R.id.num_submitted_response)).setText(task.getName());
            new AsyncTask<String, Void, JSONArray>() {
                @Override
                protected JSONArray doInBackground(String... params) {
                    return TaskManager.getTaskResponses(params[0]);
                }

                @Override
                protected void onPostExecute(JSONArray jsonArray) {
                    try {
                        if (jsonArray != null && jsonArray.length() > 0) {
                            ((TextView)convertView.findViewById(R.id.num_answers)).setText(
                                    String.valueOf(jsonArray.length()));

                            // "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
                            String dateString = jsonArray.getJSONObject(jsonArray.length() - 1)
                                    .get("createdAt").toString();
                            dateString = dateString.substring(0, dateString.length() - 1).replace('T', ' ');
                            long last = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
                                    .parse(dateString).getTime() + TIME_OFFSET;
                            long now = new Date().getTime();

                            ((TextView)convertView.findViewById(R.id.last_answer_time)).setText(
                                    String.valueOf((now - last) / (1000 * 60)) + " minutes ago");
                        } else
                            ((TextView)convertView.findViewById(R.id.last_answer_time)).setText("N/A");
                    } catch (JSONException | ParseException e) {
                        e.printStackTrace();
                    }
                }
            }.execute(task.getId());
        }

    }

    class CompletedTaskListAdapter extends ArrayAdapter<Task> {

        public CompletedTaskListAdapter(Context context, List<Task> tasks) {
            super(context, 0, tasks);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Task task = getItem(position);

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_task_completed, parent, false);
            }

            ((TextView) convertView.findViewById(R.id.num_submitted_response)).setText(task.getName());
            ((TextView) convertView.findViewById(R.id.task_location)).setText(task.getLocation().getName());
            ((TextView) convertView.findViewById(R.id.task_cost)).setText("$" + task.getCost());

            return convertView;
        }
    }

    /***
     * to display user's own response to a task
     */
    public static class ResponseInfoDialogFragment extends DialogFragment {

        public static ResponseInfoDialogFragment newInstance(Bundle args) {
            ResponseInfoDialogFragment fragment = new ResponseInfoDialogFragment();
            fragment.setArguments(args);

            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = getArguments();
            Task task = TaskManager.getTaskById(getActivity(), args.getString("taskId"));

            View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_task_info, null);
            ((TextView)dialogView.findViewById(R.id.dialog_task_name)).setText(task.getName() + " - $" + task.getCost());

            // query for all owned answers and display
            ArrayList<String> taskResponses = (ArrayList<String>) task.getMyResponses(UserManager.getUserId(getActivity()));
            String[] responseArray = new String[taskResponses.size()];
            for (int i = 0; i < responseArray.length; i++)
                responseArray[i] = taskResponses.get(i);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.listitem_dialog_task_response, responseArray);
            ListView listResponses = (ListView) dialogView.findViewById(R.id.dialog_task_response_list);
            listResponses.setAdapter(adapter);
            listResponses.setDivider(new ColorDrawable(ContextCompat.getColor(getContext(), R.color.colorPrimary)));
            listResponses.setDividerHeight(1);
            listResponses.setPadding(0, 10, 0, 10);

            return new AlertDialog.Builder(getActivity())
                    .setView(dialogView)
                    .setPositiveButton("Dismiss", null)
                    .create();
        }
    }
}
