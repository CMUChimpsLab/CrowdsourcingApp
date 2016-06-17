package com.dhchoi.crowdsourcingapp.task;

import com.dhchoi.crowdsourcingapp.SimpleGeofence;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.dhchoi.crowdsourcingapp.Constants.PACKAGE_NAME;

public class Task implements Serializable {

    public static final String TASK_KEY_SERIALIZABLE = PACKAGE_NAME + ".TASK_KEY_SERIALIZABLE";

    @SerializedName("id")
    private String mId;
    @SerializedName("userId")
    private String mUserId;
    @SerializedName("name")
    private String mName;
    @SerializedName("cost")
    private double mCost;
    @SerializedName("expiresAt")
    private double mExpiresAt;
    @SerializedName("refreshRate")
    private int mRefreshRate;
    @SerializedName("radius")
    private int mRadius;
    @SerializedName("location")
    private SimpleGeofence mLocation;
    @SerializedName("taskactions")
    private List<TaskAction> mTaskActions = new ArrayList<TaskAction>();
    private boolean mIsActivated = false;
    private long mCompletionTime = 0;

    public Task(String id, String name, int cost, SimpleGeofence location) {
        this(id, name, cost, 1000, location);
    }

    public Task(String id, String name, int cost, int radius, SimpleGeofence location) {
        mId = id;
        mName = name;
        mCost = cost;
        mRadius = radius;
        mLocation = location;
    }

    public String getId() {
        return mId;
    }

    public String getOwner() {
        return mUserId;
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

    public int getRadius() {
        // TODO: remove when fixed the server radius code
        return 1000;
//        return mRadius;
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

    public boolean isCompleted() {
        long currentTime = new Date().getTime();
        return mCompletionTime > 0 && (currentTime - mCompletionTime) < mRefreshRate;
    }

    public void setActivated(boolean activated) {
        mIsActivated = activated;
    }

    public void setCompletionTime(long completionTime) {
        mCompletionTime = completionTime;
    }

    @Override
    public String toString() {
        return mId + "-" + mName + "-" + mLocation.getName();
    }

}
