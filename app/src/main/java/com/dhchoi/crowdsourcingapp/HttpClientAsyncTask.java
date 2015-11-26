package com.dhchoi.crowdsourcingapp;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public abstract class HttpClientAsyncTask extends AsyncTask<String, Void, String> {

    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String CHARSET = "UTF-8";

    private static final String TAG = "HttpClientAsyncTask";

    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection urlConnection = null;
        String response = null;

        try {
            String url = params[0];
            String method = params.length > 1 ? params[1].toUpperCase() : GET;
            byte[] data = params.length > 2 ? params[2].getBytes(CHARSET) : null;

            // TODO: may need to do url encoding for data part later on
            Log.d(TAG, "Received params: url: " + url + ", method: " + method + ", data: " + (data != null ? params[2] : null));

            if (method.equals(POST)) {
                urlConnection = (HttpURLConnection) new URL(url).openConnection();
                urlConnection.setRequestMethod(POST);
                urlConnection.setRequestProperty("Accept-Charset", CHARSET);
                if (data != null) {
                    urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    urlConnection.setRequestProperty("Content-Length", Integer.toString(data.length));
                    urlConnection.getOutputStream().write(data);
                }
            } else {
                urlConnection = (HttpURLConnection) new URL(data != null ? url+"?"+new String(data) : url).openConnection();
                urlConnection.setRequestMethod(GET);
                urlConnection.setRequestProperty("Accept-Charset", CHARSET);
                urlConnection.setRequestProperty("Content-Type", "text/plain");
            }

            if (urlConnection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                response = "";
                String line;
                while ((line = in.readLine()) != null) {
                    response += line;
                }
            } else {
                // See documentation for more info on response handling
                Log.e(TAG, "Bad http response code: " + urlConnection.getResponseCode());
            }

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            // Handle problems
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        Log.d(TAG, "Returning http response: " + response);
        return response;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        onHttpResponse(result);
    }

    public abstract void onHttpResponse(String response);
}
