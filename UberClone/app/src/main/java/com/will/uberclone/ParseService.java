package com.will.uberclone;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;

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

        ParseACL defaultACL = new ParseACL();
        defaultACL.setDefaultACL(defaultACL, true);
    }
}
