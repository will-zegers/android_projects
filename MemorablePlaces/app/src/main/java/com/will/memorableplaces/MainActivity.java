package com.will.memorableplaces;

import android.app.Activity;
import android.content.Intent;
import android.graphics.MaskFilter;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static private ArrayList<LatLng> latLngs;
    static private ArrayList<String> placeNames;

    static public void addPlaceName(String placeName) {
        placeNames.add(placeName);
    }

    static public ArrayList<String> getPlaceNames() {
        return placeNames;
    }

    static public void addLatLng(LatLng latLng) {
        latLngs.add(latLng);
    }

    static public ArrayList<LatLng> getLatLngs() {
        return latLngs;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("onCreate", "onCreate called");

        if (latLngs == null || placeNames == null) {
            Log.i("Nulls", "null");
            latLngs = new ArrayList<LatLng>() {{
                add(null);
                add(new LatLng(32.881137, -117.237467));
            }};
            placeNames = new ArrayList<String>() {{
                add("Add new places...");
                add("Geisel");
            }};
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateListView();
    }

    private void updateListView() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, placeNames
        );

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent maps = new Intent(getApplicationContext(), MapsActivity.class);
                maps.putExtra("arrayIdx", position);
                startActivity(maps);
            }
        });
    }
}
