package com.will.basicphrases;

import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    MediaPlayer mPlayer;

    public void playPhrase(View view) {
        int id, resourceId;
        String resourceName;

        id = view.getId();
        resourceName = view.getResources().getResourceEntryName(id);
        resourceId = getResources().getIdentifier(resourceName, "raw", getPackageName() );

        Log.i("Info", String.valueOf(id) + " " + resourceName + " " + String.valueOf(resourceId) );

        mPlayer = MediaPlayer.create(this, resourceId);
        mPlayer.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPlayer = new MediaPlayer();
    }
}
