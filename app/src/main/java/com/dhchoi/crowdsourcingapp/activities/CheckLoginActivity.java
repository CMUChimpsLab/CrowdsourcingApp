package com.dhchoi.crowdsourcingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.user.UserManager;
import io.fabric.sdk.android.Fabric;

public class CheckLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        // Check if user first needs to login
        boolean isLoggedIn = UserManager.isUserLoggedIn(this);
        if (isLoggedIn) {
            Log.d(Constants.TAG, "Already logged in. Directing to MainActivity.");
            startActivity(new Intent(this, MainActivity.class));
        } else {
            Log.d(Constants.TAG, "Not logged in. Directing to LoginActivity.");
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish();
    }
}
