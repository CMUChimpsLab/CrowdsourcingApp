package com.dhchoi.crowdsourcingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.dhchoi.crowdsourcingapp.R;

/**
 * Created by Peter on 7/20/16.
 *
 */
public class SplashScreen extends AppCompatActivity {

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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this, CheckLoginActivity.class);
                startActivity(intent);

                finish();
            }
        }, 2000);       // 2 seconds
    }
}
