package com.dhchoi.crowdsourcingapp.services;

import android.os.Bundle;
import android.util.Log;

import com.dhchoi.crowdsourcingapp.NotificationHelper;
import com.dhchoi.crowdsourcingapp.activities.MainActivity;
import com.dhchoi.crowdsourcingapp.activities.TaskInfoActivity;
import com.dhchoi.crowdsourcingapp.user.UserManager;
import com.google.android.gms.gcm.GcmListenerService;

public class GcmMessageListenerService extends GcmListenerService {

    private static final String TAG = "GcmMsgListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Bundle Data: " + data);

        // taskOwnerId
        // taskName
        // taskStatus
        // taskId
        try {
            String ownerId = data.getString("taskOwnerId");
            String type = data.getString("taskStatus");

            assert ownerId != null;
            assert type != null;

            if (type.equals("created") && !ownerId.equals(UserManager.getUserId(this))) {
                NotificationHelper.createNotification("New Task Available", data.getString("taskName", "Touch to check new task."), this, MainActivity.class);
            } else if (type.equals("updated") && ownerId.equals(UserManager.getUserId(this))) {
                NotificationHelper.createNotification("Someone responded to your task", data.getString("taskName", "Touch to check response"), this, TaskInfoActivity.class, data.getString("taskId", null));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
