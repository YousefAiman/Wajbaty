package com.developers.wajbaty.PartneredRestaurant.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.developers.wajbaty.Fragments.ProgressDialogFragment;
import com.developers.wajbaty.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class RestaurantLocationActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMapClickListener, View.OnClickListener {

    public static final int MAP_TYPE_CURRENT_LOCATION = 1, MAP_TYPE_MARK_LOCATION = 2;
    private static final int
            REQUEST_CHECK_SETTINGS = 100,
            REQUEST_LOCATION_PERMISSION = 10;

//    private LocationRequester locationRequester;

    private GoogleMap mMap;
    private Marker currentMapMarker;
    private Button confirmLocationBtn;
    private ProgressDialogFragment progressDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_location);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.locateRestaurantMap);

        mapFragment.getMapAsync(this);

        confirmLocationBtn = findViewById(R.id.confirmLocationBtn);

    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        markCurrentPosition();
        mMap.setOnMapClickListener(this);

    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {

        if (currentMapMarker != null)
            currentMapMarker.remove();

        currentMapMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Delivery location"));


    }

    @Override
    public void onClick(View v) {

        if(v.getId() == confirmLocationBtn.getId()){

            final Intent intent = new Intent();
            intent.putExtra("chosenLatLng", currentMapMarker.getPosition());
            setResult(RESULT_OK, intent);
            finish();

        }

    }


    private void markCurrentPosition() {

        final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {

            Log.d("ttt", "requesting location persmission");

            requestPermissions(permissions, REQUEST_LOCATION_PERMISSION);

        } else {

//            showProgressDialog();

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            initializeLocationRequester();
        }


    }

    private void showProgressDialog() {
        sweetAlertDialog = new
                SweetAlertDialog(MapsActivity.this, SweetAlertDialog.PROGRESS_TYPE);

        sweetAlertDialog.show();
    }


    private void initializeLocationRequester() {
//        locationRequester = new LocationRequester(this);
//        locationRequester.getCurrentLocation();
    }


    public void markCurrentLocation(Location location) {

//        sweetAlertDialog.dismiss();

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        currentMapMarker = mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Delivery location"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));

    }

}