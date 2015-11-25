package com.dhchoi.crowdsourcingapp.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TaskAction implements Serializable {

    public enum ResponseType {
        TEXT, SELECTION, MEDIA
    }

    private String mDescription;
    private ResponseType mResponseType;
    private List<String> mPossibleResponses = new ArrayList<String>();

    public TaskAction(String description, ResponseType responseType) {
        mDescription = description;
        mResponseType = responseType;
    }

    public String getDescription() {
        return mDescription;
    }

    public ResponseType getResponseType() {
        return mResponseType;
    }

    public void addPossibleResponse(String response) {
        mPossibleResponses.add(response);
    }

    public List<String> getPossibleResponses() {
        return mPossibleResponses;
    }
}
