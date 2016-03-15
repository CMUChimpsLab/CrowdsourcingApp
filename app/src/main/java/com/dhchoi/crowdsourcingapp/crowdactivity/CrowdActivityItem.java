package com.dhchoi.crowdsourcingapp.crowdactivity;

public class CrowdActivityItem {
    public final String id;
    public final String content;
    public final String details;

    public CrowdActivityItem(String id, String content, String details) {
        this.id = id;
        this.content = content;
        this.details = details;
    }

    @Override
    public String toString() {
        return content;
    }
}