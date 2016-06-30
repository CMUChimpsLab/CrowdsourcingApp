package com.dhchoi.crowdsourcingapp.fragments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.activities.MainActivity;
import com.dhchoi.crowdsourcingapp.task.Task;
import com.dhchoi.crowdsourcingapp.task.TaskManager;

import java.util.List;

public class TaskAvailableFragment extends Fragment {

    public static final String NAME = "NEARBY";

    public boolean isMapShown = false;
    private TaskAvailableMapFragment mTaskAvailableMapFragment = new TaskAvailableMapFragment();
    private TaskAvailableListFragment mTaskAvailableListFragment = new TaskAvailableListFragment();

    private FloatingActionButton fab;

    public TaskAvailableFragment() {
        // Required empty public constructor
    }

    public static TaskAvailableFragment newInstance() {
        return new TaskAvailableFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_task_available, container, false);
        getChildFragmentManager().beginTransaction().replace(R.id.task_view_container, isMapShown ? mTaskAvailableMapFragment : mTaskAvailableListFragment).commit();

        fab = (FloatingActionButton) mRootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapFragments();
            }
        });

        return mRootView;
    }

    public void swapFragments() {
        getChildFragmentManager().beginTransaction()
                // .setCustomAnimations(R.animator.fade_in, R.animator.fade_out)
                .replace(R.id.task_view_container, !isMapShown ? mTaskAvailableMapFragment : mTaskAvailableListFragment)
                .commit();
        isMapShown = !isMapShown;
        if (fab.isShown())
            fab.hide();
        else
            fab.show();
    }

    public TaskAvailableMapFragment getTaskAvailableMapFragment() {
        return mTaskAvailableMapFragment;
    }

    public TaskAvailableListFragment getTaskAvailableListFragment() {
        return mTaskAvailableListFragment;
    }

}
