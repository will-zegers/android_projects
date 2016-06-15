package com.will.hikerswatch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity implements LocationListener {

    String provider;
    LocationManager locationManager;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        if (provider != null) {
            onLocationChanged(locationManager.getLastKnownLocation(provider) );
        }

        mContentView = findViewById(R.id.fullscreen_content);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        new LocationUpdater().execute(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private class LocationUpdater extends AsyncTask<Location, Void, Void> {

        Double lat, lng, altitude;
        Float accuracy, speed, bearing;
        String address;

        @Override
        protected Void doInBackground(Location... locations) {

            Address a;
            List<Address> listAddresses;

            lat = locations[0].getLatitude();
            lng = locations[0].getLongitude();
            accuracy = locations[0].getAccuracy();
            speed = locations[0].getSpeed();
            bearing = locations[0].getBearing();
            altitude = locations[0].getAltitude();

            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault() );
            try {
                listAddresses = geocoder.getFromLocation(lat, lng, 1);

                if(listAddresses != null && listAddresses.size() > 0) {
                    a = listAddresses.get(0);
                    address = a.getAddressLine(0) + "\n" +  a.getAddressLine(1) + "\n" +
                            a.getAddressLine(2);
                }
            } catch (IOException e) {
                address = "No address available\nfor this location";
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            ( (TextView) findViewById(R.id.latText) ).setText(String.format("%.2f", lat) );
            ( (TextView) findViewById(R.id.lngText) ).setText(String.format("%.2f", lng) );
            ( (TextView) findViewById(R.id.accuracyText) ).setText(String.format("%.2f", accuracy) +" m" );
            ( (TextView) findViewById(R.id.speedText) ).setText(String.format("%.2f", speed) + " m/s");
            ( (TextView) findViewById(R.id.bearingText) ).setText(String.format("%.2f", bearing) );
            ( (TextView) findViewById(R.id.altText) ).setText(String.format("%.2f", altitude) + " m");
            ( (TextView) findViewById(R.id.addressText) )
                    .setText(address);
        }
    }
}
