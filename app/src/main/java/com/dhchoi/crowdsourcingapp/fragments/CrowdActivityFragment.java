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

public class CrowdActivityFragment extends Fragment {

    public static final String NAME = "ACTIVITY";

    private List<CrowdActivityItem> crowdActivityItems = new ArrayList<CrowdActivityItem>();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CrowdActivityFragment() {
        for (int i = 1; i <= 25; i++) {
            crowdActivityItems.add(new CrowdActivityItem(String.valueOf(i), "Item " + i, "Details about Item: " + i + "\nMore details information here."));
        }
    }

    public static CrowdActivityFragment newInstance() {
        return new CrowdActivityFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crowd_activity, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new CrowdActivityListViewAdapter(crowdActivityItems));
        }
        return view;
    }
}
