package com.dhchoi.crowdsourcingapp.activities;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.services.GcmRegistrationIntentService;
import com.dhchoi.crowdsourcingapp.services.RegisterUserIntentService;
import com.dhchoi.crowdsourcingapp.task.TaskManager;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

public class MainActivity extends BaseGoogleApiActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int USER_EMAIL_REQUEST = 100;
    private static final String USER_EMAIL_PROVIDED_KEY = Constants.PACKAGE_NAME + "USER_EMAIL_PROVIDED_KEY";

    private SharedPreferences mSharedPreferences;
    private TextView mNoticeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setup views
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mNoticeText = (TextView) findViewById(R.id.notice_text);

        // request user for email if it wasn't provided yet
        mSharedPreferences = getSharedPreferences(Constants.DEFAULT_SHARED_PREF, MODE_PRIVATE);
        if (!mSharedPreferences.getBoolean(USER_EMAIL_PROVIDED_KEY, false)) {
            Log.d(Constants.TAG, "start requesting user email");
            Intent emailRequestIntent = AccountPicker.newChooseAccountIntent(
                    null, null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false, null, null, null, null);
            startActivityForResult(emailRequestIntent, USER_EMAIL_REQUEST);
        } else {
            String userEmail = mSharedPreferences.getString(Constants.USER_ID_KEY, "");
            Log.d(Constants.TAG, "user email already provided: " + userEmail);
            mNoticeText.setText(String.format(getResources().getString(R.string.notice_welcome), userEmail));

            // register user if hasn't already been done so
            if (!mSharedPreferences.getBoolean(Constants.USER_REGISTERED_KEY, false)) {
                startService(new Intent(this, RegisterUserIntentService.class));
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will automatically handle clicks on the Home/Up button,
        // so long as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_manage_tasks) {
            Intent intent = new Intent(this, TaskManagementActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_check_location) {
            Intent intent = new Intent(this, CheckLocationActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);

        // sync tasks with server
        TaskManager.syncTasks(this, getGoogleApiClient());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == USER_EMAIL_REQUEST && resultCode == RESULT_OK) {
            String userEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            Log.d(Constants.TAG, "received user email: " + userEmail);

            mSharedPreferences.edit().putBoolean(USER_EMAIL_PROVIDED_KEY, true).apply();
            mSharedPreferences.edit().putString(Constants.USER_ID_KEY, userEmail).apply();

            mNoticeText.setText(String.format(getResources().getString(R.string.notice_welcome), mSharedPreferences.getString(Constants.USER_ID_KEY, "")));

            // start IntentService to register this application with GCM
            startService(new Intent(this, GcmRegistrationIntentService.class));
        }
    }
}
