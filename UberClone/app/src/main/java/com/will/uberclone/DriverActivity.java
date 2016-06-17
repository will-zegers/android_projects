package com.will.uberclone;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class DriverActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private Rider rider;
    private Driver driver;

    private String provider;
    private GoogleMap mMap;
    private ParseObject fareObject;
    private LocationManager locationManager;

    private final int SET_LOCATION = 1;
    private final int REMOVE_UPDATES = 2;
    private final int REQUEST_UPDATES = 3;

    private final int PADDING = 300;
    private final int UPDATE_INTERVAL = 500;

    //region Activity lifecycle methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        driver = new Driver("Current position");
        try {
            driver.username = ParseUser.getCurrentUser().fetch().getUsername();
        } catch (ParseException e) {
            driver.username = "Unavailable";
        }
        rider  = new Rider("Rider");

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestLocationUpdates();
    }

    @Override
    protected void onPause() {
        removeUpdates();
        mHandler.removeCallbacks(mLocationUpdater);
        super.onPause();
    }
    //endregion

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (provider != null) { getUserLocation(driver); }

        rider.setLocation((LatLng) getIntent().getParcelableExtra("requesterLocation"));

        mMap.addMarker(driver.markerOptions);
        mMap.addMarker(rider.markerOptions).showInfoWindow();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        LatLngBounds latLngBounds = LatLngBounds.builder()
                .include(new LatLng(rider.getLatitude(), rider.getLongitude() ) )
                .include(new LatLng(driver.getLatitude(), driver.getLongitude()) )
                .build();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                latLngBounds, metrics.widthPixels, metrics.heightPixels, PADDING) );
    }

    //region Permission check methods
    private void getUserLocation(UberUser user) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    SET_LOCATION);
            return;
        }
        user.setLocation(locationManager, provider);
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_UPDATES);
            return;
        }
        locationManager.requestLocationUpdates(provider, 500, 1, this);
    }

    private void removeUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    REMOVE_UPDATES);
            return;
        }
        locationManager.removeUpdates(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case SET_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getUserLocation(driver);
                }
                break;
            case REMOVE_UPDATES:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    removeUpdates();
                }
                break;
            case REQUEST_UPDATES:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLocationUpdates();
                }
                break;
        }
    }
    //endregion

    //region LocationListener interface methods
    @Override
    public void onLocationChanged(Location location) {
        getUserLocation(driver);
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
    //endregion

    //region onClick methods
    public void goBack(View v) {
        finish();
    }

    public void acceptRequest(View v) {
        new AcceptRequestAsync().execute();
    }

    private class AcceptRequestAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) { String objectId = getIntent().getStringExtra("objectId");
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
            query.whereEqualTo("objectId", objectId);
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (e == null) {
                        fareObject = object;
                        fareObject.put(
                                "driverUsername",
                                driver.username );
                        fareObject.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    mLocationUpdater.run();
                                } else {
                                    Log.i("acceptRequest", "Failure:saveInBackground");
                                    e.printStackTrace();
                                }
                            }
                        });

                    } else {
                        Log.i("acceptRequest", "Failure");
                        e.printStackTrace();
                    }
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            Uri gmmIntentUri = Uri.parse("google.navigation:q="+rider.getLatitude()+","+rider.getLongitude());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);

            super.onPostExecute(aVoid);
        }
    }
    //endregion

    private Handler mHandler = new Handler();
    private Runnable mLocationUpdater = new Runnable() {

        @Override
        public void run() {
        getUserLocation(driver);

        fareObject.put("driverLocation", driver.getParseGeoPoint() );
        fareObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.i("DriverActivity", "updateLocation : saveInBackground - failure");
                    e.printStackTrace();
                }
            }
        });
        mHandler.postDelayed(mLocationUpdater, UPDATE_INTERVAL);
        }
    };
}
