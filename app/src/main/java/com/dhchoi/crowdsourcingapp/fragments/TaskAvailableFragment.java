package com.dhchoi.crowdsourcingapp.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhchoi.crowdsourcingapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnNearbyFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TaskAvailableFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TaskAvailableFragment extends Fragment {

    public static final String NAME = "NEARBY";

    private OnNearbyFragmentInteractionListener mListener;
    private boolean isMapShown = true;
    // private TaskAvailableMapFragment mTaskMapFragment = new TaskAvailableMapFragment();
    // private TaskAvailableListFragment mTaskListFragment = new TaskAvailableListFragment();

    public TaskAvailableFragment() {
        // Required empty public constructor
    }

    public static TaskAvailableFragment newInstance() {
        return new TaskAvailableFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: check better fragment inflation method (and dynamic swap method)
//        if (mRootView != null) {
//            ViewGroup parent = (ViewGroup) mRootView.getParent();
//            if (parent != null) {
//                parent.removeView(mRootView);
//            }
//        }
//
//        try {
//            // Inflate the layout for this fragment
//            mRootView = inflater.inflate(R.layout.fragment_task_available, container, false);
//        } catch (InflateException e) {
//            Log.e(Constants.TAG, e.getMessage());
//        }
        View mRootView = inflater.inflate(R.layout.fragment_task_available, container, false);

//        final SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
//        if (mapFragment != null) {
//            mapFragment.getMapAsync(this);
//        }
//        if (savedInstanceState != null) {
////            isMapShown = savedInstanceState.getBoolean(IS_MAP_SHOWN);
//            if (!isMapShown) {
//                swapFragments();
//            }
//        }
//        else {
//            getFragmentManager().beginTransaction().add(R.id.task_view_container, mTaskMapFragment).commit();
//        }

//        if (!mTaskMapFragment.isAdded()) {
//            getFragmentManager().beginTransaction().add(R.id.task_view_container, mTaskMapFragment).commit();
//        }
//        if (!isMapShown) {
//            swapFragments();
//        }

        getFragmentManager().beginTransaction().add(R.id.task_view_container, isMapShown ? new TaskAvailableMapFragment() : new TaskAvailableListFragment()).commit();

        FloatingActionButton fab = (FloatingActionButton) mRootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapFragments();
                isMapShown = !isMapShown;
            }
        });

        return mRootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onNearbyFragmentInteraction(uri);
        }
    }

    private void swapFragments() {
        // Fragment fragmentToSwapTo = isMapShown ? mTaskListFragment : mTaskMapFragment;
        getFragmentManager().beginTransaction()
                    // .setCustomAnimations(R.animator.fade_in, R.animator.fade_out)
                    .replace(R.id.task_view_container, !isMapShown ? new TaskAvailableMapFragment() : new TaskAvailableListFragment())
                    .commit();
        // fragmentTransaction.hide();
        // fragmentTransaction.add(R.id.task_view_container, new TaskAvailableListFragment())
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // outState.putBoolean(IS_MAP_SHOWN, isMapShown);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNearbyFragmentInteractionListener) {
            mListener = (OnNearbyFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnNearbyFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnNearbyFragmentInteractionListener {
        // TODO: Update argument type and name
        void onNearbyFragmentInteraction(Uri uri);
    }
}
