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
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
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
        if (location.equals("")) {
            Log.d(TAG, "Empty Location");
        } else if (location == null) {
            Log.d(TAG, "Null Location");
        } else {
            Log.d(TAG, location);
        }
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                "https://www.metaweather.com/api/location/search/?query="+location,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(final JSONArray response) {
                        Log.d(TAG, response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(final VolleyError error) {
                Log.w(TAG, error.toString());
            }
        });
        Log.d(TAG, jsonArrayRequest.toString());
        requestQueue.add(jsonArrayRequest); //TODO: Understand how to use this
    }
}
