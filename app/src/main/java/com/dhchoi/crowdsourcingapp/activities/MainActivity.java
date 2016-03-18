package com.dhchoi.crowdsourcingapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.fragments.CrowdActivityFragment;
import com.dhchoi.crowdsourcingapp.fragments.TaskAvailableFragment;
import com.dhchoi.crowdsourcingapp.fragments.UserInfoFragment;
import com.dhchoi.crowdsourcingapp.services.GeofenceTransitionsIntentService;
import com.dhchoi.crowdsourcingapp.task.Task;
import com.dhchoi.crowdsourcingapp.task.TaskManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends BaseGoogleApiActivity implements
        UserInfoFragment.OnInfoFragmentInteractionListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ProgressBar mSyncProgressBar;
    private ViewPager mViewPager; // The {@link ViewPager} that will host the section contents.

    // task related
    private List<OnTasksUpdatedListener> onTasksUpdatedListeners = new ArrayList<OnTasksUpdatedListener>();
    private List<Task> mActiveTasks = new ArrayList<Task>();
    private List<Task> mInactiveTasks = new ArrayList<Task>();
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO: what if geofence trigger activated first before syncing for first time

            String[] activatedTaskIds = intent.getStringArrayExtra(GeofenceTransitionsIntentService.ACTIVATED_TASK_ID_KEY);
            Log.d(Constants.TAG, "activatedTaskIds: " + Arrays.toString(activatedTaskIds));
            for (String activatedTaskId : intent.getStringArrayExtra(GeofenceTransitionsIntentService.ACTIVATED_TASK_ID_KEY)) {
                for (int i = 0; i < mInactiveTasks.size(); i++) {
                    Task inactiveTask = mInactiveTasks.get(i);
                    if (inactiveTask.getId().equals(activatedTaskId)) {
                        mInactiveTasks.remove(inactiveTask);
                        mActiveTasks.add(inactiveTask);
                    }
                }
            }

            String[] inactivatedTaskIds = intent.getStringArrayExtra(GeofenceTransitionsIntentService.INACTIVATED_TASK_ID_KEY);
            Log.d(Constants.TAG, "inactivatedTaskIds: " + Arrays.toString(inactivatedTaskIds));
            for (String inactivatedTaskId : inactivatedTaskIds) {
                for (int i = 0; i < mActiveTasks.size(); i++) {
                    Task activeTask = mActiveTasks.get(i);
                    if (activeTask.getId().equals(inactivatedTaskId)) {
                        mActiveTasks.remove(activeTask);
                        mInactiveTasks.add(activeTask);
                    }
                }
            }

            for (OnTasksUpdatedListener onTasksUpdatedListener : onTasksUpdatedListeners) {
                onTasksUpdatedListener.onTasksActivationUpdated(mActiveTasks, mInactiveTasks);
            }
        }
    };

    private TaskAvailableFragment mTaskAvailableFragment = TaskAvailableFragment.newInstance();
    private CrowdActivityFragment mCrowdActivityFragment = CrowdActivityFragment.newInstance();
    private UserInfoFragment mUserInfoFragment = UserInfoFragment.newInstance();
    {
        onTasksUpdatedListeners.add(mTaskAvailableFragment.getTaskAvailableListFragment());
        onTasksUpdatedListeners.add(mTaskAvailableFragment.getTaskAvailableMapFragment());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // ProgressBar to show sync status
        mSyncProgressBar = (ProgressBar) findViewById(R.id.sync_progress_bar);

        // Set up the Tab Layout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // Register to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(GeofenceTransitionsIntentService.GEOFENCE_TRANSITION_BROADCAST));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will automatically handle clicks on the Home/Up button,
        // so long as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
//        } else if (id == R.id.action_check_current_location) {
//            startActivity(new Intent(this, CurrentLocationActivity.class));
//            return true;
        } else if (id == R.id.action_logout) {
            SharedPreferences sharedPreferences = getSharedPreferences(Constants.DEFAULT_SHARED_PREF, this.MODE_PRIVATE);
            sharedPreferences.edit().putBoolean(Constants.USER_LOGGED_IN, false).apply();
            startActivity(new Intent(this, CheckLoginActivity.class));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);

        // sync tasks with server
        mSyncProgressBar.setVisibility(ProgressBar.VISIBLE);
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                return TaskManager.syncTasks(MainActivity.this, getGoogleApiClient());
            }

            @Override
            protected void onPostExecute(Boolean syncSuccess) {
                mSyncProgressBar.setVisibility(ProgressBar.GONE);
                if (syncSuccess) {
                    Fragment currentFragment = mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem());
                    Snackbar.make(currentFragment.getView().findViewById(R.id.fragment_content), "Sync success!", Snackbar.LENGTH_LONG).show();

                    // broadcast tasks to listeners
                    List<Task> allTasks = TaskManager.getAllTasks(MainActivity.this);
                    mActiveTasks = new ArrayList<Task>();
                    mInactiveTasks = new ArrayList<Task>();
                    for (Task t : allTasks) {
                        if (t.isActivated()) {
                            mActiveTasks.add(t);
                        } else {
                            mInactiveTasks.add(t);
                        }
                    }

                    for (OnTasksUpdatedListener onTasksUpdatedListener : onTasksUpdatedListeners) {
                        onTasksUpdatedListener.onTasksActivationUpdated(mActiveTasks, mInactiveTasks);
                    }

                } else {
                    Snackbar.make(findViewById(android.R.id.content), "Failed to sync with server.", Snackbar.LENGTH_LONG).show();
                }
            }
        }.execute();

        mTaskAvailableFragment.getTaskAvailableMapFragment().updateCurrentLocation(this);
    }

    @Override
    public void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onInfoFragmentInteraction(Uri uri) {

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        List<FragmentWrapper> fragmentWrappers = new ArrayList<FragmentWrapper>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            fragmentWrappers.add(new FragmentWrapper(TaskAvailableFragment.NAME, mTaskAvailableFragment));
            // fragmentWrappers.add(new FragmentWrapper(CrowdActivityFragment.NAME, mCrowdActivityFragment));
            fragmentWrappers.add(new FragmentWrapper(UserInfoFragment.NAME, mUserInfoFragment));
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentWrappers.get(position).fragment;
        }

        @Override
        public int getCount() {
            return fragmentWrappers.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentWrappers.get(position).fragmentName;
        }

        class FragmentWrapper {
            String fragmentName;
            Fragment fragment;

            FragmentWrapper(String fragmentName, Fragment fragment) {
                this.fragmentName = fragmentName;
                this.fragment = fragment;
            }
        }
    }

    public interface OnTasksUpdatedListener {
        void onTasksActivationUpdated(List<Task> activatedTasks, List<Task> inactivatedTasks);
    }
}
