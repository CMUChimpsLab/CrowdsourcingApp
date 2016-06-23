package com.dhchoi.crowdsourcingapp.task;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TaskAction implements Serializable {

    public enum TaskActionType {
        @SerializedName("text")
        TEXT,
        @SerializedName("selection")
        SELECTION,
        @SerializedName("media")
        MEDIA
    }

    @SerializedName("id")
    private String mId;
    @SerializedName("description")
    private String mDescription;
    @SerializedName("type")
    private TaskActionType mTaskActionType;
    @SerializedName("createdAt")
    private String mCreatedAt;
    @SerializedName("updatedAt")
    private String mUpdatedAt;
    @SerializedName("taskId")
    private String mTaskId;

    public TaskAction(String id, String description, TaskActionType taskActionType) {
        mId = id;
        mDescription = description;
        mTaskActionType = taskActionType;
    }

    public String getId() {
        return mId;
    }

    public String getDescription() {
        return mDescription;
    }

    public TaskActionType getType() {
        return mTaskActionType;
    }

    @Override
    public String toString() {
        return mId + "-" + mDescription + "-" + mTaskActionType;
    }
}
