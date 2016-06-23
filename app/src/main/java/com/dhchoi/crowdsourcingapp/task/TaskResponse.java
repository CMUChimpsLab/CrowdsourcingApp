package com.dhchoi.crowdsourcingapp.task;

import com.google.gson.annotations.SerializedName;

public class TaskResponse {

    @SerializedName("id")
    private String mId;
    @SerializedName("createdAt")
    private String mCreatedAt;
    @SerializedName("updatedAt")
    private String mUpdatedAt;
    @SerializedName("taskId")
    private String mTaskId;
    @SerializedName("userId")
    private String mUserId;

    public String getTaskId() {
        return mTaskId;
    }

    public String getUserId() {
        return mUserId;
    }

}
