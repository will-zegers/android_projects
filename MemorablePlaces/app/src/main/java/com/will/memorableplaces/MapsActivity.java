package com.will.memorableplaces;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String provider;
    private Location loc;
    private GoogleMap mMap;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if ( (provider = locationManager.getBestProvider(new Criteria(), false) ) == null ||
                (loc = locationManager.getLastKnownLocation(provider) ) == null) {
            loc = new Location("");
            loc.setLatitude(0);
            loc.setLongitude(0);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        addExistingMarkers();

        zoomToLocation();

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                String name =
                        String.valueOf(latLng.latitude) +", " + String.valueOf(latLng.longitude);
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault() );

                try {
                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addresses != null && addresses.size() > 0) {
                        name = addresses.get(0).getAddressLine(0);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                MainActivity.addLatLng(latLng);
                MainActivity.addPlaceName(name);
                mMap.addMarker(new MarkerOptions().position(latLng).title(name) );
            }
        });
    }

    private void addExistingMarkers() {
        if (MainActivity.getLatLngs().size() > 1) {
            for(int j = 1; j < MainActivity.getLatLngs().size(); j++) {
                mMap.addMarker(
                        new MarkerOptions()
                                .position(MainActivity.getLatLngs().get(j) )
                                .title(MainActivity.getPlaceNames().get(j) )
                );
            }
        }
    }

    private void zoomToLocation() {

        int idx;
        LatLng latLng;
        int zoom = (provider == null) ? 10 : 18;
        if ( (idx = getIntent().getIntExtra("arrayIdx", 0) ) > 0 ) {
            latLng = MainActivity.getLatLngs().get(idx);
        } else {
            latLng = new LatLng(loc.getLatitude(), loc.getLongitude() );
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom) );
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId() ) {
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }
}
