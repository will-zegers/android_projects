package com.will.uberclone;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    public void getStarted(View v) {
        boolean isDriver;
        if ( ( (Switch) findViewById(R.id.isDriverSwitch) ).isChecked() ) {
            isDriver = true;
        } else {
            isDriver = false;
        }
        ParseUser.getCurrentUser().put("isDriver", isDriver);
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.i("LoginActivity", "Failure");
                    e.printStackTrace();
                }
            }
        });
        redirectUser();
    }

    private void redirectUser() {
        Class userActivity;
        if ( (Boolean) ParseUser.getCurrentUser().get("isDriver") ) {
            userActivity = ViewRequests.class;
        } else {
            userActivity = RiderActivity.class;
        }

        Intent i = new Intent(getApplicationContext(), userActivity);
        startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseAnalytics.trackAppOpenedInBackground(getIntent() );
        if (ParseUser.getCurrentUser() == null) {
            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (e != null) {
                        Log.i("LoginActivity", "Failure");
                        e.printStackTrace();
                    }
                }
            });
        } else if (ParseUser.getCurrentUser().get("isDriver") != null) {
            redirectUser();
        }
    }

}
