package com.developers.wajbaty.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class GeocoderUtil {

    private static int retries = 0;

    public static void getLocationAddress(Context context,LatLng latLng){

            final Geocoder geocoder = new Geocoder(context, new Locale("ar"));

            try {
                final List<Address> addresses = geocoder.getFromLocation(latLng.latitude,
                        latLng.longitude, 1);

                if (!addresses.isEmpty() && addresses.get(0).getCountryName() != null) {

                    final Address a = addresses.get(0);

                    Log.d("ttt", "address to string: " + a.toString());

                    final String cityName = a.getLocality();

                    final String country = a.getCountryName();
                    final String countryCode = a.getCountryCode();

                    String currency = Currency.getInstance(new Locale("en", countryCode))
                            .getCurrencyCode();

                    if (currency.contains(".")) {
                        if (currency.substring(currency.length() - 2, currency.length() - 1)
                                .equals(".")) {
                            currency = currency.substring(0, currency.length() - 2);
                        }
                    }

                    Log.d("ttt", "currency: " + currency);

                    Log.d("ttt", "from geocoder: " + country + countryCode.toLowerCase() + currency);


                } else {
                    Log.d("ttt", "no address so fetching from api");

                    fetchFromApi(context,latLng.latitude, latLng.longitude);
                }
            } catch (IOException e) {
                fetchFromApi(context,latLng.latitude, latLng.longitude);
                Log.d("ttt", "geocoder error:" + e.getLocalizedMessage());
            }


    }

    private static void fetchFromApi(Context context,double latitude, double longitude) {


        final String url =
                "https://api.opencagedata.com/geocode/v1/json?key=078648c6ff684a8e851e63cbb1c8f6d8&q="
                        + latitude + "+" + longitude + "&pretty=1&no_annotations=1";

        final RequestQueue queue = Volley.newRequestQueue(context.getApplicationContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, response -> {
            try {
                if (response.getJSONObject("status").getString("message")
                        .equalsIgnoreCase("ok")) {

                    final JSONObject address = response.getJSONArray("results")
                            .getJSONObject(0).getJSONObject("components");

                    Log.d("ttt", "address: " + address.toString());
                    final String country = address.getString("country");
                    final String countryCode = address.getString("country_code");

                    String cityName = null;

                    if(address.has("village")){
                        cityName = address.getString("village");
                    } else if (address.has("region")) {
                        cityName = address.getString("region");
                    } else if (address.has("city")) {
                        cityName = address.getString("city");
                    } else if (address.has("county")) {
                        cityName = address.getString("county");
                    }


                    Log.d("ttt", "country:+ " + country);
                    Log.d("ttt", "code:+ " + countryCode);

                    String currency = Currency.getInstance(new Locale("en", countryCode))
                            .getCurrencyCode();

                    if (currency.contains(".")) {
                        if (currency.substring(currency.length() - 2, currency.length() - 1).equals(".")) {
                            currency = currency.substring(0, currency.length() - 2);
                        }
                    }

                    Log.d("ttt", "currency: " + currency);

                    Log.d("ttt", "from api: " + country + countryCode + currency);

                } else {
                    Log.d("ttt", "error here man 3: " +
                            response.getJSONObject("status").getString("message"));
                }
            } catch (JSONException e) {
                Log.d("ttt", "error here man 1: " + e.getMessage());
                e.printStackTrace();
            }
        }, error -> {

            if (retries < 3) {
                retries++;

                fetchFromApi(context,latitude, longitude);
            } else {

            }

            Log.d("ttt", "error here man 2: " + error.getMessage());
        });
        queue.add(jsonObjectRequest);
        queue.start();
    }


}
