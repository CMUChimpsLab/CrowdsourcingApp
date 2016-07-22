package com.dhchoi.crowdsourcingapp.services;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.dhchoi.crowdsourcingapp.NotificationHelper;
import com.dhchoi.crowdsourcingapp.activities.MainActivity;
import com.dhchoi.crowdsourcingapp.activities.TaskInfoActivity;
import com.dhchoi.crowdsourcingapp.task.TaskManager;
import com.dhchoi.crowdsourcingapp.user.UserManager;
import com.google.android.gms.gcm.GcmListenerService;

public class GcmMessageListenerService extends GcmListenerService {

    private static final String TAG = "GcmMsgListenerService";
    private static NewTaskListener listener;

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

        // in case notification received when user hasn't logged in
        if (!UserManager.isUserLoggedIn(this)) {
            return;
        }

        // update push
        if (data.getString("update", "0").equals("1")) {
            try {
                PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                if (Integer.parseInt(data.getString("version", "0")) > packageInfo.versionCode) {      // new version available
                    Log.i(TAG, "New apk available");
                    NotificationHelper.createDownloadNotification("Update Available", "Click to to download the latest apk", this, "http://ec2-54-221-193-1.compute-1.amazonaws.com:3000/apk");
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return;
        }

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
                if (BackgroundLocationService.isServiceRunning(this, BackgroundLocationService.class)) {
                    if (data.getString("taskId") == null)
                        return;

                    BackgroundLocationService.addTaskToList(data.getString("taskId"));
                } else {
                    TaskManager.syncTasks(getApplicationContext());
                    if (listener != null)
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                ((MainActivity) listener).syncEverything();
                            }
                        });
                }
            } else if (type.equals("updated") && ownerId.equals(UserManager.getUserId(this))) {
                NotificationHelper.createNotification("Someone responded to your task", data.getString("taskName", "Touch to check response"), this, TaskInfoActivity.class, data.getString("taskId", null));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void registerNewTaskListener(Activity activity) {
        listener = (NewTaskListener) activity;
    }

    public interface NewTaskListener {

        void onNewTask();

    }
}
