package com.travelshare.travelshare;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainNavigation extends AppCompatActivity  implements MapFragment.CustomInteractionListener {

    enum AppMode {
        MODE_TO_RECORD, MODE_RECORDING, MODE_NEWSFEED, MODE_PROFILE, MODE_MYTRIPS
    }

    private TextView mTextMessage;
    private final int REQUEST_PERMISSION_PHONE_STATE = 1;
    private MapFragment fragment;
    private AppMode currAppMode = AppMode.MODE_TO_RECORD;
    private View newsFeedView;
    private View myTripsView;
    private View recordTripView;
    private View recordingTripView;
    private View[] views;

    private EditText mAddressOne;
    private EditText mAddressTwo;

    private double latStart;
    private double lonStart;

    private double latEnd;
    private double lonEnd;

    @Override
    public void setFragment(MapFragment m)
    {
        fragment = m;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_PHONE_STATE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fragment.centerOnUser();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Permission needed")
                            .setMessage("You need to enable location services to use this app")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            });
                    builder.create().show();
                }
        }
    }

    public void displayPhoto(String photoContent)
    {
        Intent i = new Intent(MainNavigation.this, PhotoDisplayActivity.class);
        i.putExtra("photoContent", photoContent);
        startActivity(i);
    }

    @Override
    public void showExplanation(String title,
                                 String message,
                                 final String permission,
                                 final int permissionRequestCode, final Activity thisInContext) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ActivityCompat.requestPermissions(thisInContext, new String[]{permission}, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_navigation);

        SharedPreferences sp = getSharedPreferences("travelShare", Context.MODE_PRIVATE);

        mTextMessage = (TextView) findViewById(R.id.message);
        final BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_recordtrip);
        BottomNavigationViewHelper.removeShiftMode(navigation);
        final BottomNavigationItemView newsFeedButton = findViewById(R.id.navigation_newsfeed);
        BottomNavigationItemView myTripsButton = findViewById(R.id.navigation_mytrips);
        BottomNavigationItemView recordTripButton = findViewById(R.id.navigation_recordtrip);
        BottomNavigationItemView profileButton = findViewById(R.id.navigation_profile);
        Button startRecordingButton = findViewById(R.id.buttonStartTrip);
        Button finishRecordingButton = findViewById(R.id.FinishTripButton);
        ImageButton cameraButton = findViewById(R.id.cameraButton);

        newsFeedView = findViewById(R.id.NewsFeedLayout);
        myTripsView = findViewById(R.id.MyTripsLayout);
        recordTripView = findViewById(R.id.RecordTripLayout);
        recordingTripView = findViewById(R.id.RecordingTripLayout);

        views = new View[4];
        views[0] = newsFeedView;
        views[1] = myTripsView;
        views[2] = recordTripView;
        views[3] = recordingTripView;

        boolean isRecording = sp.getBoolean("isRecording", false);
        if(isRecording)
        {
            muteTabs();
            recordingTripView.setVisibility(View.VISIBLE);
            zoomToTrip();
        }
        else
        {
            muteTabs();
            recordTripView.setVisibility(View.VISIBLE);
        }

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activateCamera();
            }
        });

        finishRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigation.setSelectedItemId(R.id.navigation_recordtrip);
                finishTrip();
            }
        });

        newsFeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigation.setSelectedItemId(R.id.navigation_newsfeed);
                activateNewsFeed();
            }
        });

        myTripsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigation.setSelectedItemId(R.id.navigation_mytrips);
                activateMyTrips();
            }
        });

        recordTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigation.setSelectedItemId(R.id.navigation_recordtrip);
                activateRecordTrip();
            }
        });

        startRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activateRecordingTrip();
            }
        });

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activateProfile();
            }
        });
    }

    private void finishTrip() {
        muteTabs();
        recordTripView.setVisibility(View.VISIBLE);
        SharedPreferences sp = getSharedPreferences("travelShare", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("isRecording", false);
        editor.remove("tripId");
        editor.apply();
        fragment.clearMap();
        fragment.centerOnUser();
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(finishCurrentTrip());
    }

    private void zoomToTrip() {
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(getCurrentTrip());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (1) : {
                if (resultCode == Activity.RESULT_OK) {
                    Double latValue = data.getDoubleExtra("latValue", 0);
                    Double lonValue = data.getDoubleExtra("lonValue", 0);
                    int photoId = data.getIntExtra("photoId", 0);
                    fragment.DrawMarkerWithId(new LatLng(latValue, lonValue), photoId, "Photo", false, BitmapDescriptorFactory.HUE_BLUE);
                }
                break;
            }
        }
    }

    private void activateCamera()
    {
        Intent camera = new Intent(MainNavigation.this, CameraActivity.class);
        startActivityForResult(camera, 1);
    }

    private void activateNewsFeed()
    {
        muteTabs();
        fragment.clearMap();
        newsFeedView.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(getTripsByUserFriends());
    }

    private void activateMyTrips()
    {
        muteTabs();
        fragment.clearMap();
        myTripsView.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(getTripsByUser());
    }

    private void activateRecordTrip()
    {
        muteTabs();
        SharedPreferences sp = getSharedPreferences("travelShare", Context.MODE_PRIVATE);
        boolean isRecording = sp.getBoolean("isRecording", false);
        fragment.clearMap();
        if(isRecording)
        {
            recordingTripView.setVisibility(View.VISIBLE);
            zoomToTrip();
        }
        else
        {
            recordTripView.setVisibility(View.VISIBLE);
            fragment.centerOnUser();
        }
    }

    private void activateRecordingTrip()
    {
        if(!checkAddressValid())
        {
            Context context = getApplicationContext();
            CharSequence text = "The addresses are not valid!";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }
        else
        {
            muteTabs();
            recordingTripView.setVisibility(View.VISIBLE);
            fragment.DrawMarker(new LatLng(latStart, lonStart), "Start", true, BitmapDescriptorFactory.HUE_GREEN);
            fragment.DrawMarker(new LatLng(latEnd, lonEnd), "End", false, BitmapDescriptorFactory.HUE_RED);
            fragment.zoomOnBounds(new LatLng(latStart, lonStart), new LatLng(latEnd, lonEnd));
            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(makeJsonObjReq());
        }
    }

    private void displayTripInList(final JSONObject j, LinearLayout scrollView)
    {
        fragment.clearMap();
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        RelativeLayout rl = new RelativeLayout(this);
        rl.setBackgroundColor(0xFFAAAAAA);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(0, 0, 0, 10);
        rl.setLayoutParams(params);
        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayTrip(j);
            }
        });

        try {
            RelativeLayout.LayoutParams newParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            LinearLayout tripStartLayout = new LinearLayout(this);
            int startId = View.generateViewId();
            tripStartLayout.setId(startId);
            TextView tripStart = (TextView) getLayoutInflater().inflate(R.layout.fragment_tripinfo, null);
            addresses = geocoder.getFromLocation(j.getDouble("startlat"), j.getDouble("startlon"), 1);
            tripStart.setText("Starting point: " +addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getLocality());
            tripStartLayout.addView(tripStart);
            rl.addView(tripStartLayout);

            LinearLayout tripEndLayout = new LinearLayout(this);
            int endId = View.generateViewId();
            tripEndLayout.setId(endId);
            TextView tripEnd = (TextView) getLayoutInflater().inflate(R.layout.fragment_tripinfo, null);
            addresses = geocoder.getFromLocation(j.getDouble("endlat"), j.getDouble("endlon"), 1);
            tripEnd.setText("Ending point: " +addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getLocality());
            tripEndLayout.addView(tripEnd);
            newParams.addRule(RelativeLayout.BELOW, startId);
            rl.addView(tripEndLayout, newParams);

            newParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            LinearLayout photoCountLayout = new LinearLayout(this);
            TextView photoCount = (TextView) getLayoutInflater().inflate(R.layout.fragment_tripinfo, null);
            photoCount.setText("Photos: "+j.getJSONArray("photosById").length());
            photoCountLayout.addView(photoCount);
            newParams.addRule(RelativeLayout.BELOW, endId);
            newParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            rl.addView(photoCountLayout, newParams);

            scrollView.addView(rl);
        } catch (org.json.JSONException e)
        {
            e.printStackTrace();
        } catch (java.io.IOException e)
        {
            e.printStackTrace();
        }
    }

    private void displayTrip(JSONObject j)
    {
        fragment.clearMap();
        LatLng startPoint;
        LatLng endPoint;
        Map<LatLng, Integer> photos = new HashMap<>();

        try {
            startPoint = new LatLng(j.getDouble("startlat"), j.getDouble("startlon"));
            endPoint = new LatLng(j.getDouble("endlat"), j.getDouble("endlon"));

            JSONArray photoArr = j.getJSONArray("photosById");
            for(int i = 0; i < photoArr.length(); i++)
            {
                JSONObject p = photoArr.getJSONObject(i);
                photos.put(new LatLng(p.getDouble("latitude"), p.getDouble("longitude")), p.getInt("id"));
            }

            fragment.DrawMarker(startPoint, "Start", true, BitmapDescriptorFactory.HUE_GREEN);
            fragment.DrawMarker(endPoint, "End", false, BitmapDescriptorFactory.HUE_RED);

            for(LatLng l: photos.keySet())
            {
                int id = photos.get(l);
                fragment.DrawMarkerWithId(l, id, "Photo", false, BitmapDescriptorFactory.HUE_BLUE);
            }
        } catch (org.json.JSONException e)
        {
            e.printStackTrace();
        }
    }

    private void displayUsersTrips(JSONArray response)
    {
        View scrollView = findViewById(R.id.LinearLayoutMyTrips);
        if(((LinearLayout) scrollView).getChildCount() > 0)
        {
            ((LinearLayout) scrollView).removeAllViews();
        }
        for(int i = 0; i < response.length(); i++)
        {
            try {
                JSONObject j = (JSONObject) response.get(i);
                displayTripInList(j, (LinearLayout) scrollView);

            } catch(org.json.JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void displayTripInFriendList(final JSONObject j, LinearLayout scrollView)
    {
        fragment.clearMap();
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        RelativeLayout rl = new RelativeLayout(this);
        rl.setBackgroundColor(0xFFAAAAAA);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(0, 0, 0, 10);
        rl.setLayoutParams(params);
        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    displayTrip(j.getJSONObject("t"));
                } catch (org.json.JSONException e)
                {
                    e.printStackTrace();
                }
            }
        });

        try {
            RelativeLayout.LayoutParams newParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            LinearLayout tripStartLayout = new LinearLayout(this);
            int startId = View.generateViewId();
            tripStartLayout.setId(startId);
            TextView tripStart = (TextView) getLayoutInflater().inflate(R.layout.fragment_tripinfo, null);
            addresses = geocoder.getFromLocation(j.getJSONObject("t").getDouble("startlat"), j.getJSONObject("t").getDouble("startlon"), 1);
            tripStart.setText("Starting point: " +addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getLocality());
            tripStartLayout.addView(tripStart);
            rl.addView(tripStartLayout);

            LinearLayout tripEndLayout = new LinearLayout(this);
            int endId = View.generateViewId();
            tripEndLayout.setId(endId);
            TextView tripEnd = (TextView) getLayoutInflater().inflate(R.layout.fragment_tripinfo, null);
            addresses = geocoder.getFromLocation(j.getJSONObject("t").getDouble("endlat"), j.getJSONObject("t").getDouble("endlon"), 1);
            tripEnd.setText("Ending point: " +addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getLocality());
            tripEndLayout.addView(tripEnd);
            newParams.addRule(RelativeLayout.BELOW, startId);
            rl.addView(tripEndLayout, newParams);

            newParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            LinearLayout photoCountLayout = new LinearLayout(this);
            int photoId = View.generateViewId();
            photoCountLayout.setId(photoId);
            TextView photoCount = (TextView) getLayoutInflater().inflate(R.layout.fragment_tripinfo, null);
            photoCount.setText("Photos: "+j.getJSONObject("t").getJSONArray("photosById").length());
            photoCountLayout.addView(photoCount);
            newParams.addRule(RelativeLayout.BELOW, endId);
            newParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            rl.addView(photoCountLayout, newParams);

            newParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            LinearLayout userEmailLayout = new LinearLayout(this);
            TextView userEmail = (TextView) getLayoutInflater().inflate(R.layout.fragment_tripinfo, null);
            userEmail.setText("User: "+j.getJSONObject("u").getString("email"));
            userEmailLayout.addView(userEmail);
            newParams.addRule(RelativeLayout.BELOW, photoId);
            newParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            rl.addView(userEmailLayout, newParams);

            scrollView.addView(rl);
        } catch (org.json.JSONException e)
        {
            e.printStackTrace();
        } catch (java.io.IOException e)
        {
            e.printStackTrace();
        }
    }

    private void displayUsersFriendsTrips(JSONArray response)
    {
        View scrollView = findViewById(R.id.LinearLayoutNewsFeed);
        if(((LinearLayout) scrollView).getChildCount() > 0)
        {
            ((LinearLayout) scrollView).removeAllViews();
        }
        for(int i = 0; i < response.length(); i++)
        {
            try {
                JSONObject j = (JSONObject) response.get(i);
                displayTripInFriendList(j, (LinearLayout) scrollView);
            } catch(org.json.JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    private JsonArrayRequest getTripsByUserFriends(){
        SharedPreferences sp = getSharedPreferences("travelShare", Context.MODE_PRIVATE);
        JSONObject toSend = new JSONObject();
        int userId = sp.getInt("userId", 0);
        final String TAG = "asdf";

        JsonArrayRequest jsonObjReq = new JsonArrayRequest(Request.Method.GET,
                "http://10.0.2.2:8080/rest/trip/findAllByFriends/"+userId, null,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        displayUsersFriendsTrips(response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Context context = getApplicationContext();
                CharSequence text = "You currently don't have any finished trips!";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }) {

            /**
             * Passing some request headers
             * */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }



        };

        jsonObjReq.setTag(TAG);
        // Adding request to request queue
        return jsonObjReq;

        // Cancelling request
    /* if (queue!= null) {
    queue.cancelAll(TAG);
    } */

    }

    private JsonArrayRequest getTripsByUser(){
        SharedPreferences sp = getSharedPreferences("travelShare", Context.MODE_PRIVATE);
        JSONObject toSend = new JSONObject();
        int userId = sp.getInt("userId", 0);
        final String TAG = "asdf";

        JsonArrayRequest jsonObjReq = new JsonArrayRequest(Request.Method.GET,
                "http://10.0.2.2:8080/rest/trip/findAll/"+userId, null,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        displayUsersTrips(response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Context context = getApplicationContext();
                CharSequence text = "You currently don't have any finished trips!";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }) {

            /**
             * Passing some request headers
             * */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }



        };

        jsonObjReq.setTag(TAG);
        // Adding request to request queue
        return jsonObjReq;

        // Cancelling request
    /* if (queue!= null) {
    queue.cancelAll(TAG);
    } */

    }

    private JsonObjectRequest finishCurrentTrip(){
        SharedPreferences sp = getSharedPreferences("travelShare", Context.MODE_PRIVATE);
        JSONObject toSend = new JSONObject();
        int userId = sp.getInt("userId", 0);
        final String TAG = "asdf";

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                "http://10.0.2.2:8080/rest/trip/finish/"+userId, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Context context = getApplicationContext();
                CharSequence text = "Invalid login/password";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }) {

            /**
             * Passing some request headers
             * */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }



        };

        jsonObjReq.setTag(TAG);
        // Adding request to request queue
        return jsonObjReq;

        // Cancelling request
    /* if (queue!= null) {
    queue.cancelAll(TAG);
    } */

    }

    private JsonArrayRequest getCurrentTripPhotos(int tripId){
        final String TAG = "asdf";

        JsonArrayRequest jsonObjReq = new JsonArrayRequest(Request.Method.GET,
                "http://10.0.2.2:8080/rest/trip/photos/"+tripId, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for(int i = 0; i < response.length(); i++)
                        {
                            try {
                                JSONObject j = (JSONObject) response.get(i);
                                fragment.DrawMarkerWithId(new LatLng(j.getDouble("latitude"), j.getDouble("longitude")), j.getInt("id"), "Photo", false, BitmapDescriptorFactory.HUE_BLUE);
                            }catch(org.json.JSONException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Context context = getApplicationContext();
                CharSequence text = "Invalid login/password";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }) {

            /**
             * Passing some request headers
             * */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }



        };

        jsonObjReq.setTag(TAG);
        // Adding request to request queue
        return jsonObjReq;

        // Cancelling request
    /* if (queue!= null) {
    queue.cancelAll(TAG);
    } */

    }

    private JsonObjectRequest getCurrentTrip(){
            SharedPreferences sp = getSharedPreferences("travelShare", Context.MODE_PRIVATE);
        final Activity thisInContext = this;
            JSONObject toSend = new JSONObject();
            int userId = sp.getInt("userId", 0);
            final String TAG = "asdf";

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                    "http://10.0.2.2:8080/rest/trip/"+userId, null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                RequestQueue queue = Volley.newRequestQueue(thisInContext);
                                queue.add(getCurrentTripPhotos(response.getInt("id")));
                                fragment.DrawMarker(new LatLng(response.getDouble("startlat"), response.getDouble("startlon")), "Start", true, BitmapDescriptorFactory.HUE_GREEN);
                                fragment.DrawMarker(new LatLng(response.getDouble("endlat"), response.getDouble("endlon")), "End", false, BitmapDescriptorFactory.HUE_RED);
                                fragment.zoomOnBounds(new LatLng(response.getDouble("startlat"), response.getDouble("startlon")), new LatLng(response.getDouble("endlat"), response.getDouble("endlon")));
                            } catch(org.json.JSONException e)
                            {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                    Context context = getApplicationContext();
                    CharSequence text = "Invalid login/password";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }) {

                /**
                 * Passing some request headers
                 * */
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    return headers;
                }



            };

            jsonObjReq.setTag(TAG);
            // Adding request to request queue
            return jsonObjReq;

            // Cancelling request
    /* if (queue!= null) {
    queue.cancelAll(TAG);
    } */

        }

    private JsonObjectRequest makeJsonObjReq() {
        SharedPreferences sp = getSharedPreferences("travelShare", Context.MODE_PRIVATE);
        JSONObject toSend = new JSONObject();
        try {
            int userId = sp.getInt("userId", 0);
            String userEmail = sp.getString("userEmail", "");
            String userPassword = sp.getString("userPassword", "");
            toSend.put("startlat", Double.toString(latStart));
            toSend.put("startlon", Double.toString(lonStart));
            toSend.put("endlat", Double.toString(latEnd));
            toSend.put("endlon", Double.toString(lonEnd));
            toSend.put("isActive", "true");
            Map<String, String> userMap = new HashMap<>();
            userMap.put("id", Integer.toString(userId));
            /*userMap.put("email", userEmail);
            userMap.put("password", userPassword);*/
            toSend.put("usersByUserId", userId);
        } catch (org.json.JSONException e)
        {
            e.printStackTrace();
            finish();
        }

        final String TAG = "asdf";

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                "http://10.0.2.2:8080/rest/trip", toSend,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        SharedPreferences sp = getSharedPreferences("travelShare", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putBoolean("isRecording", true);
                        try
                        {
                            editor.putInt("tripId", response.getInt("id"));
                        } catch (org.json.JSONException e)
                        {
                            e.printStackTrace();
                        }
                        editor.apply();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Context context = getApplicationContext();
                CharSequence text = "Invalid login/password";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }) {

            /**
             * Passing some request headers
             * */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }



        };

        jsonObjReq.setTag(TAG);
        // Adding request to request queue
        return jsonObjReq;

        // Cancelling request
    /* if (queue!= null) {
    queue.cancelAll(TAG);
    } */

    }

    private void activateProfile()
    {
        Intent activityPage = new Intent(MainNavigation.this, ProfileActivity.class);
        startActivity(activityPage);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    private boolean checkAddressValid()
    {
        mAddressOne = findViewById(R.id.StartPoint);
        mAddressTwo = findViewById(R.id.EndPoint);
        String addressOne = mAddressOne.getText().toString();
        String addressTwo = mAddressTwo.getText().toString();
        if(addressOne.length() == 0 || addressTwo.length() == 0)
        {
            return false;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(addressOne, 1);
            latStart = addresses.get(0).getLatitude();
            lonStart = addresses.get(0).getLongitude();

            addresses = geocoder.getFromLocationName(addressTwo, 1);
            latEnd = addresses.get(0).getLatitude();
            lonEnd = addresses.get(0).getLongitude();
            return true;
        } catch (java.io.IOException e)
        {
            e.printStackTrace();
            return false;
        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    private void muteTabs()
    {
        for(View v: views)
        {
            v.setVisibility(View.GONE);
        }
    }
}
