package com.developers.wajbaty.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.developers.wajbaty.PartneredRestaurant.Activities.RestaurantLocationActivity;
import com.developers.wajbaty.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        startActivity(new Intent(this,RestaurantLocationActivity.class));


    }
}