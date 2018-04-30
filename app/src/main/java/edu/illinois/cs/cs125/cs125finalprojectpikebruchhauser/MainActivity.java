package edu.illinois.cs.cs125.cs125finalprojectpikebruchhauser;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    /** Default logging tag for messages from the main activity. */
    private static final String TAG = "Final Project:Main";

    /** Request queue for our network requests. */
    private static RequestQueue requestQueue;

    /** String variable for the location user inputs */
    private String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "On create ran");

        requestQueue = Volley.newRequestQueue(this);

        super.onCreate(savedInstanceState);

        // Load the main layout for our activity
        setContentView(R.layout.activity_main);

        /*
         * Set up handlers for each button in our UI. These run when the buttons are clicked.
         */
        final EditText inputText = (EditText) findViewById(R.id.locationInput);

        final Button submitButton = (Button) findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d(TAG, "Submit button clicked");
                onSubmitButtonClick(inputText.getText().toString());
            }
        });
    }

    /**
     * Starts the API call when clicked if possible.
     * @param loc the inputted text.
     */
    public void onSubmitButtonClick(String loc) {
        location = loc;
        StringRequest StringRequest = new StringRequest(
                Request.Method.GET,
                "https://www.metaweather.com/api/location/search/?query="+location,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        Log.d(TAG, response);
                        JsonObject result = checkIfValid(response);
                        if (result != null) {
                            Log.d(TAG, "IT KINDA WORKS!");
                            TextView temp = findViewById(R.id.city);
                            temp.setText(result.get("title").getAsString());
                            displayInfo(result);
                        } else {
                            Log.d(TAG, "Invalid query");
                            Toast toast = Toast.makeText(getApplicationContext(), "Search was invalid or not specific enough",
                                    Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(final VolleyError error) {
                Log.w(TAG, error.toString());
            }
        });
        requestQueue.add(StringRequest);
    }

    /**
     * Will check if the query is valid.
     * @param json The json given from the API.
     * @return Returns JsonObject if query is specific enough and null if no response or not specific
     * enough
     */
    public JsonObject checkIfValid(String json) {
        try {
            JsonParser parser = new JsonParser();
            JsonArray rootObj = parser.parse(json).getAsJsonArray();
            if (rootObj.size() != 1) {
                return null;
            } else {
                return rootObj.get(0).getAsJsonObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @param json
     */
    public void displayInfo(JsonObject json) {
        String woeid = json.get("woeid").getAsString();
        String url = "https://www.metaweather.com/api/location/"+woeid+"/";

        StringRequest StringRequest = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        Log.d(TAG, response);
                        String[] results = getInfo(response);
                        TextView theTemp = findViewById(R.id.temperature);
                        TextView minTemp = findViewById(R.id.minTemp);
                        TextView maxTemp = findViewById(R.id.maxTemp);
                        TextView state = findViewById(R.id.weatherState);
                        TextView windSpeed = findViewById(R.id.windSpeed);
                        TextView windDir = findViewById(R.id.windDirection);

                        theTemp.setText(results[0]);
                        minTemp.setText(results[1]);
                        maxTemp.setText(results[2]);
                        state.setText(results[3]);
                        windSpeed.setText(results[4]);
                        windDir.setText(results[5]);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(final VolleyError error) {
                Log.w(TAG, error.toString());
            }
        });
        requestQueue.add(StringRequest);
    }

    /**
     *
     * @param json
     * @return
     */
    public String[] getInfo(String json) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject rootObj = parser.parse(json).getAsJsonObject();
            JsonArray consolWeather = rootObj.getAsJsonArray("consolidated_weather");
            JsonObject mostRecent = consolWeather.get(0).getAsJsonObject();
            String weatherState = mostRecent.get("weather_state_name").getAsString();
            String windSpeed = mostRecent.get("wind_speed").getAsString();
            String windDir = mostRecent.get("wind_direction_compass").getAsString();
            String minTemp = mostRecent.get("min_temp").getAsString();
            String maxTemp = mostRecent.get("max_temp").getAsString();
            String theTemp = mostRecent.get("the_temp").getAsString();
            String[] toReturn = {theTemp, minTemp, maxTemp, weatherState, windSpeed, windDir};
            return toReturn;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
