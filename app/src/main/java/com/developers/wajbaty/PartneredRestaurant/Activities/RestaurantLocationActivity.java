package com.developers.wajbaty.PartneredRestaurant.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
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
import com.developers.wajbaty.Utils.GeocoderUtil;
import com.developers.wajbaty.Utils.LocationRequester;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class RestaurantLocationActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMapClickListener, View.OnClickListener {

    private static final int REQUEST_LOCATION_PERMISSION = 10;

    private GoogleMap mMap;
    private Marker currentMapMarker;
    private Button confirmLocationBtn;
    private ProgressDialogFragment progressDialogFragment;
    private LocationRequester locationRequester;


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

//            final Intent intent = new Intent();
//            intent.putExtra("chosenLatLng", currentMapMarker.getPosition());
//            setResult(RESULT_OK, intent);
//            finish();
            GeocoderUtil.getLocationAddress(this,currentMapMarker.getPosition());

            Log.d("ttt","current location is: "+currentMapMarker.getPosition());

        }

    }


    private void markCurrentPosition() {

        final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {

            Log.d("ttt", "requesting location persmission");

            requestPermissions(permissions, REQUEST_LOCATION_PERMISSION);

        } else {

            showProgressDialog();

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            initializeLocationRequester();
        }


    }


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                showProgressDialog();

                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);

                initializeLocationRequester();

            } else {

                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);

                LatLng sydney = new LatLng(-34, 151);
                currentMapMarker = mMap.addMarker(new MarkerOptions().position(sydney).title("Restaurant Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

            }
        }

    }

    private void showProgressDialog() {
        progressDialogFragment = new ProgressDialogFragment();
        progressDialogFragment.show(getSupportFragmentManager(),"progress");
    }


    private void initializeLocationRequester() {
        locationRequester = new LocationRequester(this);
        locationRequester.getCurrentLocation();
    }


    public void markCurrentLocation(Location location) {

        progressDialogFragment.dismiss();
//        sweetAlertDialog.dismiss();

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        currentMapMarker = mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Restaurant Location"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (locationRequester != null) {
            locationRequester.resumeLocationUpdates();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (locationRequester != null) {
            locationRequester.stopLocationUpdates();
        }
    }




}