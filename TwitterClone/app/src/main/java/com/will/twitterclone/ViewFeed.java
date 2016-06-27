package com.will.twitterclone;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewFeed extends AppCompatActivity {

    ListView listView;
    SimpleAdapter simpleAdapter;
    List<Map<String, String>> tweetData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.feedListView);

        tweetData = new ArrayList<Map<String, String>>();

        simpleAdapter = new SimpleAdapter(this, tweetData, android.R.layout.simple_list_item_2, new String[] {"content", "username"}, new int[] {android.R.id.text1, android.R.id.text2});

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Tweet");
        query.whereContainedIn("username", ParseUser.getCurrentUser().getList("following"));
        query.orderByDescending("createdAt");
        query.setLimit(20);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        for (ParseObject o : objects) {
                            Map<String, String> tweet = new HashMap<String, String>(2);
                            tweet.put("content", o.getString("content"));
                            tweet.put("username", o.getString("username"));

                            tweetData.add(tweet);
                        }
                        listView.setAdapter(simpleAdapter);
                    }
                } else {
                    Log.i("findInBackground", "Failure");
                    e.printStackTrace();
                }
            }
        });
    }

}
