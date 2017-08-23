package com.example.android1.hrms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends Activity {


    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                finish();
                Intent i = new Intent(SplashActivity.this, HomeActivity.class);
                startActivity(i);


            }
        }, SPLASH_TIME_OUT);
    }

}