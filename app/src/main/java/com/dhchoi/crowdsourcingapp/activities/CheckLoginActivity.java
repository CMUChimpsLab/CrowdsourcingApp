package com.dhchoi.crowdsourcingapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.dhchoi.crowdsourcingapp.Constants;

public class CheckLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user first needs to login
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.DEFAULT_SHARED_PREF, CheckLoginActivity.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean(Constants.USER_LOGGED_IN, false);
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
