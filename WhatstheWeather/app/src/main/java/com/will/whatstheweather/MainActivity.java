package com.will.whatstheweather;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    public void search(View view) {
        String cityName = ( (TextView) findViewById(R.id.citySearchText) )
                .getText()
                .toString()
                .replace(" ","");
        String apiString =
                getString(R.string.search_path) +
                cityName +
                "&" +
                getString(R.string.appid);

        DownloaderTask task = new DownloaderTask();
        task.execute(apiString);
    }

    public class DownloaderTask extends AsyncTask<String, Void, String> {

        StringBuilder res = new StringBuilder();

        @Override
        protected String doInBackground(String... urls) {
            try {
                InputStreamReader reader = new InputStreamReader(
                        ( new URL(urls[0]).openConnection() )
                                .getInputStream()
                );

                int data;
                while ( (data = reader.read() ) > -1) {
                    res.append( (char) data);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return res.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.weatherInfoLayout);
            relativeLayout.setVisibility(View.VISIBLE);
            for(int i = 0; i < 13; i++) {
                relativeLayout.getChildAt(i).setVisibility(View.VISIBLE);
            }

            try {
                JSONObject jo = new JSONObject(res.toString() );
                JSONObject sys = new JSONObject(jo.getString("sys") );
                ( (TextView) findViewById(R.id.cityNameText) )
                        .setText(jo.getString("name") + ", " + sys.getString("country") );

                JSONObject weather = new JSONArray(jo.getString("weather") ).getJSONObject(0);
                String description = weather.getString("description");
                ( (TextView) findViewById(R.id.weatherDescText) )
                        .setText(description.substring(0,1).toUpperCase() + description.substring(1) );

                JSONObject main = new JSONObject(jo.getString("main") );
                float tempF = 1.8f * (Float.parseFloat(main.getString("temp") ) - 273.15f ) + 32;
                ( (TextView) findViewById(R.id.tempText) )
                        .setText(String.format("%.1f",tempF) + " \u00b0F" );

                float presInhg = 0.02953f * (Float.parseFloat(main.getString("pressure") ) );
                ( (TextView) findViewById(R.id.pressureText) )
                        .setText(String.format("%.2f",presInhg) + " in." );

                ( (TextView) findViewById(R.id.humidityText) )
                        .setText(main.getString("humidity") + "%" );

                JSONObject wind = new JSONObject(jo.getString("wind") );
                ( (TextView) findViewById(R.id.windText) )
                        .setText(wind.getString("speed") + " kn @ " + wind.getString("deg") + "\u00b0");

                SimpleDateFormat format = new SimpleDateFormat("h:mm a");
                Calendar c = Calendar.getInstance();

                c.setTimeInMillis(Long.parseLong(sys.getString("sunrise") ) * 1000 );
                String sunriseStr = format.format(c.getTime() );

                c.setTimeInMillis(Long.parseLong(sys.getString("sunset") ) * 1000 );
                String sunsetStr = format.format(c.getTime() );

                ( (TextView) findViewById(R.id.sunText) )
                        .setText(sunriseStr + " / " + sunsetStr);



            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.i("Result", res.toString() );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
