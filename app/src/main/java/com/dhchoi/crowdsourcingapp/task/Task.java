package com.dhchoi.crowdsourcingapp.task;

import com.dhchoi.crowdsourcingapp.SimpleGeofence;
import com.dhchoi.crowdsourcingapp.user.UserManager;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
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
    @SerializedName("refreshRate")
    private int mRefreshRate;
    @SerializedName("expiresAt")
    private double mExpiresAt;
    @SerializedName("answersLeft")
    private int mAnswersLeft;
    @SerializedName("createdAt")
    private String mCreatedAt;
    @SerializedName("updatedAt")
    private String mUpdatedAt;
    @SerializedName("userId")
    private String mUserId;
    @SerializedName("location")
    private SimpleGeofence mLocation;
    @SerializedName("taskresponses")
    private List<TaskResponse> mTaskResponses;
    @SerializedName("taskactions")
    private List<TaskAction> mTaskActions;

    private boolean mIsActivated = false;
    private boolean mIsComplete = false;

    public Task() {
        // necessary for Gson
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
        return (int) mLocation.getRadius();
    }

    public SimpleGeofence getLocation() {
        return mLocation;
    }

    public List<TaskAction> getTaskActions() {
        return mTaskActions;
    }

    public List<TaskResponse> getTaskResponses() {
        return mTaskResponses;
    }

    public boolean isActivated() {
        return mIsActivated;
    }

    public boolean isCompleted() {
        return mIsComplete;
    }

    public Task setActivated(boolean activated) {
        mIsActivated = activated;
        return this;
    }

    public Task setCompleted(boolean completed) {
        mIsComplete = completed;
        return this;
    }

    public List<String> getMyResponses(String answererId) {
        ArrayList<String> myResponseStrings = new ArrayList<>();

        JSONArray jsonArray = TaskManager.getTaskResponses(mId);
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                if (jsonArray.getJSONObject(i).getString("userId").equals(answererId)) {
                    // get all of my responses
                    JSONArray myResponseList = jsonArray.getJSONObject(i).getJSONArray("taskactionresponses");
                    for (int j = 0; j < myResponseList.length(); j++)
                        myResponseStrings.add(myResponseList.getJSONObject(j).getString("response"));
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return myResponseStrings;
    }

    @Override
    public String toString() {
        return mId + "-" + mName.replaceAll(" ", " ") + "-" + mLocation.getName();
    }

}
