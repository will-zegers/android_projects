package com.will.uberclone;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class DriverActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private String provider;
    private Location location;
    private LocationManager locationManager;
    private LatLng driverLatLng, riderLatLng;

    private final int SET_LOCATION = 1;
    private final int REMOVE_UPDATES = 2;

    //region Activity lifecycle methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            locationManager.requestLocationUpdates(provider, 500, 1, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        removeUpdates();
        super.onPause();
    }
    //endregion

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getLocation();
        riderLatLng = getIntent().getParcelableExtra("requesterLocation");

        mMap.addMarker(new MarkerOptions()
                .position(riderLatLng)
                .title("Rider")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE) )
        ).showInfoWindow();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        LatLngBounds latLngBounds = LatLngBounds.builder()
                .include(new LatLng(riderLatLng.latitude, riderLatLng.longitude ) )
                .include(new LatLng(driverLatLng.latitude, driverLatLng.longitude) )
                .build();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                latLngBounds, metrics.widthPixels, metrics.heightPixels, 250) );
    }

    //region Permission check methods

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    SET_LOCATION);
            return;
        }
        locationManager.requestLocationUpdates(provider, 500, 1, this);
        location = locationManager.getLastKnownLocation(provider);
        driverLatLng = new LatLng(location.getLatitude(), location.getLongitude() );

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(location.getLatitude(), location.getLongitude() ) )
                .title("Current position")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED) )
        );
    }

    private void removeUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    SET_LOCATION);
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
                    getLocation();
                }
                break;
            case REMOVE_UPDATES:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    removeUpdates();
                }
                break;
        }
    }

    //endregion

    //region LocationListener interface methods
    @Override
    public void onLocationChanged(Location location) {
        getLocation();
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
        String objectId = getIntent().getStringExtra("objectId");
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
        query.whereEqualTo("objectId", objectId);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    try {
                        object.put(
                                "driverUsername",
                                ParseUser.getCurrentUser().fetch().getUsername() );
                        object.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Uri gmmIntentUri = Uri.parse("google.navigation:q="+riderLatLng.latitude+","+riderLatLng.longitude);
                                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                    mapIntent.setPackage("com.google.android.apps.maps");
                                    startActivity(mapIntent);
                                }else{
                                    Log.i("acceptRequest", "Failure:saveInBackground");
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }

                } else {
                    Log.i("acceptRequest", "Failure");
                    e.printStackTrace();
                }
            }
        });
    }
    //endregion
}
