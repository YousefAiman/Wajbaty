package com.developers.wajbaty.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.developers.wajbaty.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        new Thread(() -> {
            try {
                Thread.sleep(4000);
                startActivity(new Intent(this, WelcomeActivity.class));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();



    }
}