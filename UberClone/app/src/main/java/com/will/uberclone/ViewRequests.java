package com.will.uberclone;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class ViewRequests extends AppCompatActivity implements LocationListener {

    String provider;
    Location location;
    LocationManager locationManager;
    ParseGeoPoint currentGeoPoint;

    private final int SET_LOCATION = 1;
    private final int REMOVE_UPDATES = 2;

    //region Activity lifecycle methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (provider != null) {
            getLocation();
        }

        currentGeoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude() );
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
        query.whereNear(
                "requesterLocation",
                currentGeoPoint
        );
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    new GetDistancesAsync().execute(objects);
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        removeUpdates();
        super.onPause();
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

    private class GetDistancesAsync extends AsyncTask<List<ParseObject>, Void, ArrayList<ParseGeoPoint> > {

        private ArrayList<String> objectIds;

        @Override
        protected ArrayList<ParseGeoPoint> doInBackground(List<ParseObject>... objects) {

            final ArrayList<ParseGeoPoint> geoPointList = new ArrayList<>();
            objectIds = new ArrayList<>();

            for (ParseObject o : objects[0]) {
                geoPointList.add(o.getParseGeoPoint("requesterLocation") );
                objectIds.add(o.getObjectId() );
            }

            return geoPointList;
        }

        @Override
        protected void onPostExecute(final ArrayList<ParseGeoPoint> geoPointList) {

            ListView listView = (ListView) findViewById(R.id.requestsListView);
            GeoPointAdapter geoPointAdapter = new GeoPointAdapter(
                    ViewRequests.this,
                    android.R.layout.simple_list_item_1,
                    geoPointList);
            listView.setAdapter(geoPointAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent i = new Intent(getApplicationContext(), DriverMapActivity.class);
                    i.putExtra("requesterLocation",
                            new LatLng(
                                    geoPointList.get(position).getLatitude(),
                                    geoPointList.get(position).getLongitude() )
                    );
                    i.putExtra("objectId", objectIds.get(position) );
                    startActivity(i);
                }
            });
            super.onPostExecute(geoPointList);
        }

        private class GeoPointAdapter extends ArrayAdapter<ParseGeoPoint> {

            public GeoPointAdapter(Context context, int resource, List<ParseGeoPoint> objects) {
                super(context, resource, objects);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ParseGeoPoint gp = getItem(position);

                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext() )
                            .inflate(R.layout.item_geopoint, parent, false);
                }

                TextView textView = (TextView) convertView.findViewById(R.id.geoPointTextView);
                textView.setText(
                        String.format("%.2f miles", gp.distanceInMilesTo(
                                new ParseGeoPoint(location.getLatitude(), location.getLongitude() ) ) )
                );

                return convertView;
            }
        }
    }
}
