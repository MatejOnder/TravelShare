package com.travelshare.travelshare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    TextView mTextEmail;
    TextView mTextTripCount;
    TextView mTextPhotoCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mTextEmail = findViewById(R.id.emailText);
        mTextTripCount = findViewById(R.id.tripsCount);
        mTextPhotoCount = findViewById(R.id.photoCount);

        Button changePassBtn = findViewById(R.id.changePasswordButton);
        changePassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

        Button addFriendBtn = findViewById(R.id.addFriendButton);
        addFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend();
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(makeJsonObjReq());
    }

    private void addFriend() {
        Intent i = new Intent(ProfileActivity.this, AddFriendActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    private void changePassword() {
        Intent i = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    private JsonObjectRequest makeJsonObjReq() {
        SharedPreferences sp = getSharedPreferences("travelShare", Context.MODE_PRIVATE);
        JSONObject toSend = new JSONObject();
        int userId = sp.getInt("userId", 0);
        try {
            toSend.put("id", userId);
        } catch (org.json.JSONException e)
        {
            e.printStackTrace();
        }

        final String TAG = "asdf";

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                "http://10.0.2.2:8080/rest/user/detail/"+userId, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            mTextEmail.setText(response.get("email").toString());
                            mTextPhotoCount.setText(response.get("photoCount").toString());
                            mTextTripCount.setText(response.get("tripCount").toString());
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
                CharSequence text = "Error fetching user details";
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
