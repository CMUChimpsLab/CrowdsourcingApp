package com.dhchoi.crowdsourcingapp.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.HttpClientCallable;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterUserIntentService extends IntentService {

    private static final String TAG = "RegUserIntentService";

    public static final String RESULT_RECEIVER_KEY = TAG + ".RESULT_RECEIVER_KEY";

    private Context mContext;
    private Handler mMainThread;

    public RegisterUserIntentService() {
        super(TAG); // Used to name the worker thread, important only for debugging.
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.DEFAULT_SHARED_PREF, MODE_PRIVATE);
        mContext = getApplicationContext();
        mMainThread = new Handler(Looper.getMainLooper());

        Log.d(TAG, "Initiate user registration");

        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("userId", sharedPreferences.getString(Constants.USER_ID_KEY, ""));
            params.put("gcmToken", sharedPreferences.getString(Constants.USER_GCM_TOKEN_KEY, ""));

            String response = HttpClientCallable.Executor.execute(new HttpClientCallable(Constants.APP_SERVER_USER_CREATE_URL, HttpClientCallable.POST, params));
            if (response != null) {
                JSONObject responseObj = new JSONObject(response);
                String errorMsg = responseObj.getString("error");
                if (errorMsg.isEmpty()) {
                    showToast("Email was successfully registered!");
                } else {
                    showToast("Email registration was rejected.");
                }
            }

            // Store a boolean that indicates whether the user has been registered with the server.
            // If the boolean is false, register the user to the server,
            // otherwise the server should have already received the user info.
            sharedPreferences.edit().putBoolean(Constants.USER_REGISTERED_KEY, true).apply();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            showToast("Failed to register email.");

            // If an exception happens while updating registration data on a third-party server,
            // this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(Constants.USER_REGISTERED_KEY, false).apply();
        }

        ResultReceiver resultReceiver = intent.getParcelableExtra(RESULT_RECEIVER_KEY);
        if (resultReceiver != null) {
            resultReceiver.send(
                    sharedPreferences.getBoolean(Constants.USER_REGISTERED_KEY, false) ? Constants.RESULT_RECEIVER_SUCCESS : Constants.RESULT_RECEIVER_FAIL, null);
        }
    }

    private void showToast(final String message) {
        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
