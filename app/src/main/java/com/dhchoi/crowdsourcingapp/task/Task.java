package com.dhchoi.crowdsourcingapp.task;

import com.dhchoi.crowdsourcingapp.SimpleGeofence;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.dhchoi.crowdsourcingapp.Constants.PACKAGE_NAME;

public class Task implements Serializable {

    public static final String TASK_KEY_SERIALIZABLE = PACKAGE_NAME + ".TASK_KEY_SERIALIZABLE";

    @SerializedName("id")
    private String mId;
    @SerializedName("name")
    private String mName;
    @SerializedName("cost")
    private double mCost;
    @SerializedName("expiresAt")
    private double mExpiresAt;
    @SerializedName("refreshRate")
    private int mRefreshRate;
    @SerializedName("location")
    private SimpleGeofence mLocation;
    @SerializedName("taskactions")
    private List<TaskAction> mTaskActions = new ArrayList<TaskAction>();
    private boolean mIsActivated = false;

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

    public double getCost() {
        return mCost;
    }

    public double getExpiresAt() {
        return mExpiresAt;
    }

    public int getRefreshRate() {
        return mRefreshRate;
    }

    public SimpleGeofence getLocation() {
        return mLocation;
    }

    public List<TaskAction> getTaskActions() {
        return mTaskActions;
    }

    public boolean isActivated() {
        return mIsActivated;
    }

    public void setActivated(boolean activated) {
        mIsActivated = activated;
    }

    @Override
    public String toString() {
        return mId + "-" + mName + "-" + mLocation.getName();
    }

}
