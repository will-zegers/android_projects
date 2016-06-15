package com.will.uberclone;

import android.app.Application;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

public class ParseService extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);

        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext() )
            .applicationId(getString(R.string.app_id) )
            .clientKey(null)
            .server(getString(R.string.server_url) )
            .build()
        );

//        ParseUser.enableAutomaticUser();

        ParseACL defaultACL = new ParseACL();
        defaultACL.setDefaultACL(defaultACL, true);
    }
}
