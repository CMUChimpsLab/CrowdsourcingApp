package com.dhchoi.crowdsourcingapp.task;

import com.dhchoi.crowdsourcingapp.simplegeofence.SimpleGeofence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.dhchoi.crowdsourcingapp.Constants.PACKAGE_NAME;

public class Task implements Serializable {

    public static final String KEY_SERIALIZABLE = PACKAGE_NAME + ".TASK_KEY_SERIALIZABLE";

    private String mId;
    private String mName;
    private int mCost;
    private SimpleGeofence mLocation;
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

    public void addTaskAction(TaskAction taskAction) {
        this.mTaskActions.add(taskAction);
    }

    @Override
    public String toString() {
        return mId + "-" + mName + "-" + mLocation.getName();
    }

}
