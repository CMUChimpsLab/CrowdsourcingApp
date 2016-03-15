package com.dhchoi.crowdsourcingapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.crowdactivity.CrowdActivityItem;
import com.dhchoi.crowdsourcingapp.crowdactivity.CrowdActivityListViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnActivityListFragmentInteractionListener}
 * interface.
 */
public class CrowdActivityFragment extends Fragment {

    public static final String NAME = "ACTIVITY";

    // TODO: Customize parameter argument names
    private OnActivityListFragmentInteractionListener mListener;
    private List<CrowdActivityItem> crowdActivityItems = new ArrayList<CrowdActivityItem>();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CrowdActivityFragment() {
        for (int i = 1; i <= 25; i++) {
            crowdActivityItems.add(new CrowdActivityItem(String.valueOf(i), "Item " + i, "Details about Item: "+i+"\nMore details information here."));
        }
    }

    // TODO: Customize parameter initialization
    public static CrowdActivityFragment newInstance() {
        return new CrowdActivityFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crowd_activity, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new CrowdActivityListViewAdapter(crowdActivityItems, mListener));
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnActivityListFragmentInteractionListener) {
            mListener = (OnActivityListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnActivityListFragmentInteractionListener");
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
    public interface OnActivityListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onActivityListFragmentInteraction(CrowdActivityItem item);
    }
}
