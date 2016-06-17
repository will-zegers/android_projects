package com.will.uberclone;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.GetCallback;
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
    private Location location;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private Marker driverMarker;

    private final int ZOOM = 16;
    private final int SET_LOCATION = 1;
    private final int REMOVE_UPDATES = 2;
    private final int REQUEST_LOCATION_UPDATES = 3;
    private final int PADDING = 300;
    private final int UPDATE_INTERVAL = 500;

    //region Activity Lifecycle methods
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
        requestLocationUpdates();
    }

    @Override
    protected void onPause() {
        removeUpdates();
        mHandler.removeCallbacks(mDriverUpdater);
        super.onPause();
    }
    //endregion

    //region LocationListener interface methods
    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM));
        mMap.addMarker(new MarkerOptions().position(latLng));
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

    //region Permission check methods
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

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    SET_LOCATION);
            return;
        }
        location = locationManager.getLastKnownLocation(provider);
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_UPDATES);
            return;
        }
        locationManager.requestLocationUpdates(provider, 500, 1, this);
    }

    private void removeUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REMOVE_UPDATES);
            return;
        }
        locationManager.removeUpdates(this);
    }
    //endregion

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getLocation();
        if (location != null) {
            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(currentLatLng));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, ZOOM));
        }
    }

    //region onClick methods and tasks
    public void requestUber(View v) {
        if (hasRequested) {
            Log.i("requestUber", "In");
            mHandler.removeCallbacks(mDriverUpdater);
            new CancelUberAsync().execute();
            Log.i("requestUber", "Out");
        } else {
            new RequestUberAsync().execute();
        }
        hasRequested = !hasRequested;

    }

    private abstract class UberAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

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
            return null;
        }
    }

    private class CancelUberAsync extends UberAsyncTask {

        @Override
        protected Void doInBackground(Void... params) {
            Log.i("UberAsync", "Cancelling");

            return super.doInBackground(params);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ( (Button) findViewById(R.id.acceptRiderButton) ).setText("Request Uber");
            ( (TextView) findViewById(R.id.riderStatusText) ).setText("Uber Cancelled");
            Log.i("UberAsync", "Cancelled");


            super.onPostExecute(aVoid);
        }
    }

    private class RequestUberAsync extends UberAsyncTask {

        @Override
        protected Void doInBackground(Void... params) {

            super.doInBackground(params);

            ParseACL parseACL = new ParseACL();
            parseACL.setPublicReadAccess(true);
            parseACL.setPublicWriteAccess(true);

            ParseObject parseObject = new ParseObject("Request");
            parseObject.setACL(parseACL);
            parseObject.put("requestUsername", username );
            parseObject.put(
                    "requesterLocation",
                    new ParseGeoPoint(location.getLatitude(), location.getLongitude() )
            );
            parseObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        new WaitForDriverAsync().execute();
                    } else {
                        e.printStackTrace();
                    }
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ( (Button) findViewById(R.id.acceptRiderButton) ).setText("Cancel");
            ( (TextView) findViewById(R.id.riderStatusText) ).setText("Finding Uber Driver...");

            super.onPostExecute(aVoid);
        }
    }

    private class WaitForDriverAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            while ( hasRequested && (driverLocation = getDriverLocation() ) == null) {
                try {
                    Thread.sleep(UPDATE_INTERVAL);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(driverLocation != null) {
                ((TextView) findViewById(R.id.riderStatusText)).setText("A driver is on the way!");
                mDriverUpdater.run();
                LatLngBounds latLngBounds = LatLngBounds.builder()
                        .include(new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude()))
                        .include(new LatLng(location.getLatitude(), location.getLongitude()))
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, PADDING));
            }
            super.onPostExecute(aVoid);
        }
    }
    //endregion

    //region Driver position update
    private Handler mHandler = new Handler();
    private ParseGeoPoint driverLocation;
    private Runnable mDriverUpdater = new Runnable() {

        @Override
        public void run() {

            driverLocation = getDriverLocation();
            LatLng driverLatLng = new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude() );

            if (driverMarker != null) { driverMarker.remove(); }
            if (mMap != null) {
                driverMarker = mMap.addMarker(new MarkerOptions()
                        .position(driverLatLng)
                        .title("Your driver")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                );
                driverMarker.showInfoWindow();
            }
            mHandler.postDelayed(mDriverUpdater, UPDATE_INTERVAL);
        }
    };

    private ParseGeoPoint getDriverLocation() {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
        query.whereEqualTo("requestUsername", username);
        query.getFirstInBackground(new GetCallback<ParseObject>() {

            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    driverLocation = (ParseGeoPoint) object.get("driverLocation");
                } else {
                    e.printStackTrace();
                }
            }
        });

        return driverLocation;
    }
    //endregion
}
