package com.dhchoi.crowdsourcingapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Receiver for data sent from FetchAddressIntentService.
 */
abstract public class FetchAddressResultReceiver extends ResultReceiver {

    public FetchAddressResultReceiver(Handler handler) {
        super(handler);
    }

    /**
     *  Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
     */
    @Override
    abstract protected void onReceiveResult(int resultCode, Bundle resultData);
}
