package com.dhchoi.crowdsourcingapp.fragments;

import android.animation.ValueAnimator;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.animation.AnimatorCompatHelper;
import android.support.v4.animation.ValueAnimatorCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.activities.MainActivity;
import com.dhchoi.crowdsourcingapp.task.Task;
import com.dhchoi.crowdsourcingapp.task.TaskManager;

import java.util.List;

public class TaskAvailableFragment extends Fragment {

    public static final String NAME = "NEARBY TASKS";

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
                swapFragments(TaskAvailableMapFragment.ALL_MARKERS);
            }
        });

        return mRootView;
    }

    public void swapFragments(int flag) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int centerTranslation = -size.x / 2 + fab.getWidth() / 2 + fab.getPaddingRight();
        if (!isMapShown) {
            ValueAnimator valueAnimator = ValueAnimator.ofInt(0, centerTranslation);
            valueAnimator.setDuration(1500)
                    .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            fab.setTranslationX((Integer) animation.getAnimatedValue());
                        }
                    });
            valueAnimator.start();
        } else {
            ValueAnimator valueAnimator = ValueAnimator.ofInt(centerTranslation, 0);
            valueAnimator.setDuration(300)
                    .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            fab.setTranslationX((Integer) animation.getAnimatedValue());
                        }
                    });
            valueAnimator.start();
        }

        mTaskAvailableMapFragment.FLAG = flag;
        getChildFragmentManager().beginTransaction()
                // .setCustomAnimations(R.animator.fade_in, R.animator.fade_out)
                .replace(R.id.task_view_container, !isMapShown ? mTaskAvailableMapFragment : mTaskAvailableListFragment)
                .commit();
        isMapShown = !isMapShown;
    }

    public TaskAvailableMapFragment getTaskAvailableMapFragment() {
        return mTaskAvailableMapFragment;
    }

    public TaskAvailableListFragment getTaskAvailableListFragment() {
        return mTaskAvailableListFragment;
    }

}
