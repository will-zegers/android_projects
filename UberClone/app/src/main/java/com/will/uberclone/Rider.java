package com.will.uberclone;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseGeoPoint;

/**
 * Created by will on 6/16/16.
 */
public class Rider extends UberUser{

    public Rider(String label) {
        super(label);
    }
    @Override
    protected void createMarkerOptions(String title) {
        markerOptions = new MarkerOptions()
                .position(getLatLng())
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
    }
}
