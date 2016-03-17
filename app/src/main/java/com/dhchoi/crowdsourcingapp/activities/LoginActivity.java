package com.dhchoi.crowdsourcingapp.activities;

import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.dhchoi.crowdsourcingapp.Constants;
import com.dhchoi.crowdsourcingapp.HttpClientAsyncTask;
import com.dhchoi.crowdsourcingapp.HttpClientCallable;
import com.dhchoi.crowdsourcingapp.R;
import com.dhchoi.crowdsourcingapp.services.GcmRegistrationIntentService;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private static final int USER_EMAIL_REQUEST = 100;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        // Set SignIn button OnClickListener
        findViewById(R.id.email_sign_in_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        // Set EmailChoose button OnClickListener
        findViewById(R.id.email_choose_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailRequestIntent = AccountPicker.newChooseAccountIntent(null, null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false, null, null, null, null);
                startActivityForResult(emailRequestIntent, USER_EMAIL_REQUEST);
            }
        });

        // start IntentService to register this application with GCM
        startService(new Intent(this, GcmRegistrationIntentService.class));
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to perform the user login attempt.
            showProgress(true);

            // Register user to server.
            Map<String, String> params = new HashMap<String, String>();
            params.put("userId", email);
            params.put("gcmToken", getSharedPreferences(Constants.DEFAULT_SHARED_PREF, MODE_PRIVATE).getString(Constants.USER_GCM_TOKEN_KEY, ""));
            new HttpClientAsyncTask(Constants.APP_SERVER_TASK_RESPOND_URL, HttpClientCallable.POST, params) {
                @Override
                protected void onPostExecute(String response) {
                    showProgress(false);

                    try {
                        if (response != null) {
                            JSONObject responseObj = new JSONObject(response);
                            String errorMsg = responseObj.getString("error");
                            if (errorMsg.isEmpty()) {
                                SharedPreferences sharedPreferences = getSharedPreferences(Constants.DEFAULT_SHARED_PREF, LoginActivity.MODE_PRIVATE);
                                sharedPreferences.edit().putBoolean(Constants.USER_LOGGED_IN, true).apply();
                                sharedPreferences.edit().putString(Constants.USER_ID_KEY, "").apply();
                                Log.d(Constants.TAG, "Successfully logged in. Directing to MainActivity.");
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Email registration was rejected.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Server response was unexpected.", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(Constants.TAG, e.getMessage());
                        Toast.makeText(LoginActivity.this, "Error while communicating with server.", Toast.LENGTH_LONG).show();
                    }
                }
            }.execute();
        }
    }

    //TODO: Replace this with your own logic
    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    //TODO: Replace this with your own logic
    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow for very easy animations.
        // If available, use these APIs to fade-in the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == USER_EMAIL_REQUEST && resultCode == RESULT_OK) {
            String userEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            Log.d(Constants.TAG, "received user email: " + userEmail);
            mEmailView.setText(userEmail);
        }
    }

}
