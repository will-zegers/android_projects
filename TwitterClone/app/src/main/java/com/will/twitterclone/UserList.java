package com.will.twitterclone;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class UserList extends AppCompatActivity {

    String tweetText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ArrayList<String> users = new ArrayList();

        final ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_checked, users);
        final ListView listView = (ListView) findViewById(R.id.listView);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView checkedTextView = (CheckedTextView) view;
                if (checkedTextView.isChecked()) {
                    ParseUser.getCurrentUser().getList("following").add(users.get(position).toString());
                } else {
                    ParseUser.getCurrentUser().getList("following").remove(users.get(position).toString());
                }
                ParseUser.getCurrentUser().saveInBackground();
            }
        });

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {

                if (e == null) {
                    users.clear();
                    for (ParseUser user : objects) {
                        users.add(user.getUsername());
                    }
                    arrayAdapter.notifyDataSetChanged();

                    List following = ParseUser.getCurrentUser().getList("following");
                    for (String user : users) {
                        if (following.contains(user)) {
                            listView.setItemChecked(users.indexOf(user), true);
                        }
                    }
                } else {
                    Log.i("findInBackground", "Failure");
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.tweet:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Send a tweet");
                final EditText tweetContent = new EditText(this);
                tweetContent.setText(tweetText);
                builder.setView(tweetContent);

                builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ParseObject tweet = new ParseObject("Tweet");
                        tweet.put("content", tweetContent.getText().toString());
                        tweet.put("username", ParseUser.getCurrentUser().getUsername());
                        tweet.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.tweet_success), Toast.LENGTH_SHORT).show();
                                    tweetText = "";
                                } else {
                                    Toast.makeText(getApplicationContext(), getString(R.string.tweet_failure), Toast.LENGTH_SHORT).show();
                                    tweetText = tweetContent.getText().toString();
                                }
                            }
                        });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
