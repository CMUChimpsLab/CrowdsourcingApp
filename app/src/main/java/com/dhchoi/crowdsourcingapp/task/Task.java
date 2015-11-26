package com.dhchoi.crowdsourcingapp.task;

import com.dhchoi.crowdsourcingapp.SimpleGeofence;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.dhchoi.crowdsourcingapp.Constants.PACKAGE_NAME;

public class Task implements Serializable {

    public static final String KEY_SERIALIZABLE = PACKAGE_NAME + ".TASK_KEY_SERIALIZABLE";

    @SerializedName("id")
    private String mId;
    @SerializedName("name")
    private String mName;
    @SerializedName("cost")
    private int mCost;
    @SerializedName("location")
    private SimpleGeofence mLocation;
    @SerializedName("actions")
    private List<TaskAction> mTaskActions = new ArrayList<TaskAction>();

    public Task(String id, String name, int cost, SimpleGeofence location) {
        mId = id;
        mName = name;
        mCost = cost;
        mLocation = location;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public int getCost() {
        return mCost;
    }

    public SimpleGeofence getLocation() {
        return mLocation;
    }

    public List<TaskAction> getTaskActions() {
        return mTaskActions;
    }

    @Override
    public String toString() {
        return mId + "-" + mName + "-" + mLocation.getName();
    }

}
