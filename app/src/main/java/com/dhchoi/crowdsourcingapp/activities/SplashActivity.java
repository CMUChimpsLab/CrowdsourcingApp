package com.dhchoi.crowdsourcingapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.dhchoi.crowdsourcingapp.R;

/**
 * Created by Peter on 7/20/16.
 *
 */
public class SplashActivity extends BaseGoogleApiActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView splashImage = (ImageView) findViewById(R.id.splash_image);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        splashImage.getLayoutParams().width = (int) (metrics.widthPixels * 0.5);
        splashImage.getLayoutParams().height = (int) (metrics.heightPixels * 0.4);
        splashImage.setPadding(0, (int) (metrics.heightPixels * 0.1), 0, 0);

        if (!isGooglePlayServicesAvailable()) {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setMessage("The app requires Google Play Services to run properly!")
                    .create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    finish();
                }
            });

            alertDialog.show();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashActivity.this, CheckLoginActivity.class);
                    startActivity(intent);

                    finish();
                }
            }, 2000);       // 2 seconds
        }
    }
}
