package com.will.uberclone;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class RiderActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private boolean hasRequested = false;
    private String provider, username;
    private Location loc;
    private GoogleMap mMap;
    private LocationManager locationManager;

    final int ZOOM = 16;
    final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

        try {
            username = ParseUser.getCurrentUser().fetch().getUsername();
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        new GetLocationAsync().execute();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                } else {
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (provider != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                return;
            } else {
                new GetLocationAsync().execute();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude() );
        mMap.clear();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM ) );
        mMap.addMarker(new MarkerOptions().position(latLng) );
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

    private class GetLocationAsync extends AsyncTask<Void, Void, Void> {

        LatLng currentLatLng;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                loc = locationManager.getLastKnownLocation(provider);
                currentLatLng = new LatLng(loc.getLatitude(), loc.getLongitude() );
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mMap.addMarker(new MarkerOptions().position(currentLatLng) );
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, ZOOM) );

            super.onPostExecute(aVoid);
        }
    }

    public void requestUber(View v) {
        if (hasRequested) {
            ( (Button) findViewById(R.id.requestUberButton) ).setText("Request Uber");
            ( (TextView) findViewById(R.id.riderStatusText) ).setText("Uber Cancelled");

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
            query.whereEqualTo("requestUsername", username);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        for (ParseObject object : objects) {
                            try {
                                object.delete();
                                object.saveInBackground();
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            });
        } else {
            ( (Button) findViewById(R.id.requestUberButton) ).setText("Cancel");
            ( (TextView) findViewById(R.id.riderStatusText) ).setText("Finding Uber Driver...");

            ParseACL parseACL = new ParseACL();
            parseACL.setPublicReadAccess(true);
            parseACL.setPublicWriteAccess(true);

            ParseObject parseObject = new ParseObject("Request");
            parseObject.setACL(parseACL);
            parseObject.put("requestUsername", username );
            parseObject.put(
                    "requesterLocation",
                    new ParseGeoPoint(loc.getLatitude(), loc.getLongitude() )
            );
            parseObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.i("RiderActivity", "Failure");
                        e.printStackTrace();
                    }
                }
            });

        }
        hasRequested = !hasRequested;
    }
}
