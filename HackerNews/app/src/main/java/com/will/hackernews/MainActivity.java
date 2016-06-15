package com.will.hackernews;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private String storiesUrl;

    private static ArrayList<String> titles;
    private static ArrayList<String> targets;
    private static int currentItemIdx;
    private int currentListSize;

    final static int ENTRIES_PER_LOAD = 50;
    final static String TOP = "https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty";
    final static String BEST = "https://hacker-news.firebaseio.com/v0/beststories.json?print=pretty";
    final static String NEW = "https://hacker-news.firebaseio.com/v0/newstories.json?print=pretty";
    final static String FIRST = "https://hacker-news.firebaseio.com/v0/item/";
    final static String LAST = ".json?print=pretty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        storiesUrl = TOP;
        loadStories();
    }

    @Override
    protected void onResume() {
        ( (ListView) findViewById(R.id.listView) ).setSelection(currentItemIdx);
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId() ) {
            case R.id.best_stories:
                storiesUrl = BEST;
                break;
            case R.id.new_stories:
                storiesUrl = NEW;
                break;
            default:
                storiesUrl = TOP;
        }
        targets = null;
        loadStories();
        return super.onOptionsItemSelected(item);
    }

    public void loadStories() {

        if (targets == null || titles == null) {
            titles = new ArrayList<String>();
            targets = new ArrayList<String>();

            currentItemIdx = 0;
            currentListSize = 0;
        }
        while (titles.size() < currentListSize + ENTRIES_PER_LOAD) {titles.add(""); }
        while (targets.size() < currentListSize + ENTRIES_PER_LOAD) {targets.add(""); }

        new StoriesDownloader().execute(storiesUrl);
    }

    public class StoriesDownloader extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            ( (ListView) findViewById(R.id.listView) ).setAlpha(0.20f);
            ( (ProgressBar) findViewById(R.id.progressBar) ).setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... urls) {

            if (titles.get(currentListSize) != "") {
                Log.i("doInBackground", "Arrays not null");
                return null;
            }
            StringBuilder result = new StringBuilder();
            try {
                InputStreamReader reader = new InputStreamReader(
                        ((HttpsURLConnection) new URL(urls[0])
                                .openConnection()
                        ).getInputStream());

                int data;
                while ((data = reader.read()) != -1) {
                    result.append((char) data);
                }
                JSONArray arr = new JSONArray(result.toString() );

                ExecutorService executorService = Executors.newFixedThreadPool(
                        8*Runtime.getRuntime().availableProcessors()
                );
                for (int i = currentListSize; i < currentListSize + ENTRIES_PER_LOAD; i++) {
                    Runnable task = new JSONDownloader(arr.getString(i), i);
                    executorService.execute(task);
                }
                executorService.shutdown();
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                currentListSize += ENTRIES_PER_LOAD;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            final ListView listView = (ListView) findViewById(R.id.listView);
            ArrayAdapter arrayAdapter = new ArrayAdapter(
                    MainActivity.this, android.R.layout.simple_list_item_1, titles
            );
            listView.setAdapter(arrayAdapter);

            listView.setAlpha(1);
            listView.setVisibility(View.VISIBLE);
            listView.setSelection(currentItemIdx);
            ( (ProgressBar) findViewById(R.id.progressBar) ).setVisibility(View.INVISIBLE);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    currentItemIdx = position;
                    Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                    intent.putExtra("url", targets.get(position) );
                    startActivity(intent);
                }
            });

            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    currentItemIdx = firstVisibleItem;
                    if (firstVisibleItem + visibleItemCount == titles.size() ) {
                        currentItemIdx = currentListSize;
                        loadStories();
                    }
                }
            });
            super.onPostExecute(aVoid);
        }
    }

    public class JSONDownloader implements Runnable {

        private int idx;
        private String target;
        private StringBuilder result;

        final private String nourl_first = "https://news.ycombinator.com/item?id=";

        JSONDownloader(String target, int idx) {
            this.target = target;
            this.idx = idx;
            this.result = new StringBuilder();
        }

        @Override
        public void run() {
            try {
                InputStreamReader reader = new InputStreamReader(
                        ( (HttpsURLConnection) new URL(FIRST + target + LAST).openConnection() )
                                .getInputStream()
                );

                int data;
                while ( (data = reader.read() ) != -1) {
                    result.append( (char) data);
                }
                JSONObject obj = new JSONObject(result.toString() );

                titles.set(idx, obj.getString("title") );
                if (obj.has("url") ) {
                    targets.set(idx, obj.getString("url") );
                } else {
                    targets.set(idx, nourl_first+target );
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
