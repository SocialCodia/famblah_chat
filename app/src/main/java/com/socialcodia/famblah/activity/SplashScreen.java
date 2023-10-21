package com.socialcodia.famblah.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.socialcodia.famblah.R;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendToLogin();
            }
        },1000);
    }

    private void sendToLogin() {
        Intent intent = new Intent(getApplicationContext(),PhoneLoginActivity.class);
        startActivity(intent);
        finish();
    }
}
