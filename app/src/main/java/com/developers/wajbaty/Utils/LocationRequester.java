package com.developers.wajbaty.Utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.developers.wajbaty.PartneredRestaurant.Activities.RestaurantLocationActivity;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Map;

public class LocationRequester {

    private static final int REQUEST_CHECK_SETTINGS = 100;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean mRequestingLocationUpdates;
    private final Activity activity;
    private final LocationRequestAction locationRequestAction;

    public interface LocationRequestAction{
        void locationFetched(LatLng latLng);
    }

    public LocationRequester(Activity activity,LocationRequestAction locationRequestAction) {
        this.activity = activity;
        this.locationRequestAction = locationRequestAction;
    }



    @SuppressLint("MissingPermission")
    public void getCurrentLocation() {

        if (fusedLocationClient != null && locationCallback != null) {

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,
                    Looper.myLooper());

            return;
        }


        locationRequest = LocationRequest.create().
                setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(10000).setFastestInterval(5000);

        final LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        LocationServices.getSettingsClient(activity)
                .checkLocationSettings(builder.build())
                .addOnSuccessListener(locationSettingsResponse -> {
                    Log.d("ttt", "location is enabled");

                    getLastKnownLocation();

                }).addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                Log.d("ttt", "location is not enabled");
                try {
                    final ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(activity,
                            REQUEST_CHECK_SETTINGS);

                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });

    }

    @SuppressLint("MissingPermission")
    void getLastKnownLocation() {

        Log.d("ttt", "getting last known location");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) {

                            Log.d("ttt", "last location is null");

                            mRequestingLocationUpdates = true;

                            fusedLocationClient.requestLocationUpdates(locationRequest,
                                    locationCallback = addLocationCallback(),
                                    Looper.getMainLooper());

                        } else {

                            Log.d("ttt", "last known location: " + location);

                            locationRequestAction.locationFetched(new LatLng(location.getLatitude(),
                                    location.getLongitude()));

                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                fusedLocationClient.requestLocationUpdates(locationRequest,
                        locationCallback = addLocationCallback(),
                        Looper.getMainLooper());

                Log.d("ttt", "last known failed: " + e.getMessage());
            }
        });

    }


    @SuppressLint("MissingPermission")
    public void resumeLocationUpdates() {

        if (!mRequestingLocationUpdates && locationCallback != null && fusedLocationClient != null) {
            mRequestingLocationUpdates = true;

            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback = addLocationCallback(),
                    Looper.getMainLooper());
        }

    }

    public void stopLocationUpdates() {
        Log.d("ttt", "stopping location updates");
        if (mRequestingLocationUpdates) {
            if (locationCallback != null && fusedLocationClient != null) {
                mRequestingLocationUpdates = false;

                fusedLocationClient.removeLocationUpdates(locationCallback);
            }
        }
    }

    LocationCallback addLocationCallback() {

        return new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                Log.d("ttt", "onLocationResult");

                if (locationResult == null) {
                    Log.d("ttt", "onLocationResult locationResult is null");
                    return;
                }

                for (Location location : locationResult.getLocations()) {

                    if (location != null) {

                        Log.d("ttt", "location result is not null");

                        locationRequestAction.locationFetched(new LatLng(location.getLatitude(),location.getLongitude()));

                        stopLocationUpdates();

                        break;
                    }
                }

            }

        };

    }

    public static boolean areLocationPermissionsEnabled(Context context){

        boolean permissionsGranted;

        @SuppressLint("InlinedApi") final String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
        };

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {

            permissionsGranted = checkPermissionGranted(permissions[0],context) &&
                    checkPermissionGranted(permissions[1],context) &&
                    checkPermissionGranted(permissions[2],context);

        } else {

            permissionsGranted = checkPermissionGranted(permissions[0],context) &&
                    checkPermissionGranted(permissions[1],context);
        }

        Log.d("ttt","permissions granted: "+permissionsGranted);

        return permissionsGranted;
    }


    private static boolean checkPermissionGranted(String permission,Context context){
        return ContextCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_GRANTED;
    }



}
