package com.dhchoi.crowdsourcingapp;

import android.os.AsyncTask;

import java.util.Map;

public class HttpClientAsyncTask extends AsyncTask<Void, Void, String> {
    private HttpClientCallable mHttpClientCallable;

    public HttpClientAsyncTask(String url, String method, Map<String, String> params) {
        mHttpClientCallable = new HttpClientCallable(url, method, params);
    }

    @Override
    protected String doInBackground(Void... params) {
        return HttpClientCallable.Executor.execute(mHttpClientCallable);
    }
}
