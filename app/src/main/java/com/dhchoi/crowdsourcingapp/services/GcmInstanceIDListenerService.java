package com.dhchoi.crowdsourcingapp.services;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Class to handle Instance ID service notifications on token refresh.
 * Any app using Instance ID or GCM must include a class extending InstanceIDListenerService and implement onTokenRefresh().
 */
public class GcmInstanceIDListenerService extends InstanceIDListenerService {

    private static final String TAG = "GcmInstanceIDListenerService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Intent intent = new Intent(this, GcmRegistrationIntentService.class);
        startService(intent);
    }
    // [END refresh_token]
}
