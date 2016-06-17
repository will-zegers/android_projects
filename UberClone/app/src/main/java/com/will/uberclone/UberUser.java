package com.will.uberclone;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseGeoPoint;

/**
 * Created by will on 6/16/16.
 */
public abstract class UberUser {

    private Location location;
    private String label;

//    public double latitude, longitude;
    public String username;
    public MarkerOptions markerOptions;
    public Marker marker;

    public UberUser(String label) {
        this.label = label;
    }

    public void setLocation(ParseGeoPoint parseGeoPoint) {
        if (parseGeoPoint == null) {
            location = null;
            return;
        }

        location = new Location("");
        location.setLatitude(parseGeoPoint.getLatitude());
        location.setLongitude(parseGeoPoint.getLongitude());

        createMarkerOptions(label);
    }

    public void setLocation(LatLng latLng) {
        if (latLng == null) {
            location = null;
            return;
        }
        location = new Location("");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);

        createMarkerOptions(label);
    }

    public void setLocation(LocationManager locationManager, String provider) throws SecurityException {

        if (locationManager == null || provider == null) {
            location = null;
            return;
        }
        location  = locationManager.getLastKnownLocation(provider);

        createMarkerOptions(label);
    }

    public Location getLocation() {
        return location;
    }

    public double getLatitude() {
        return location.getLatitude();
    }

    public double getLongitude() {
        return location.getLongitude();
    }

    public ParseGeoPoint getParseGeoPoint() {
        if (location == null) {
            return null;
        }
        return new ParseGeoPoint(location.getLatitude(), location.getLongitude());
    }

    public LatLng getLatLng() {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    protected abstract void createMarkerOptions(String title);
}
