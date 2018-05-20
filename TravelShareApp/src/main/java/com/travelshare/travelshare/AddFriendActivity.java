package com.travelshare.travelshare;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

public class AddFriendActivity extends AppCompatActivity {

    TextView mEmailSearch;
    public String currentSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mEmailSearch = findViewById(R.id.friendEmail);

        Button searchBtn = findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchFriend();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    private void searchFriend() {
        if(mEmailSearch.getText().toString().length() == 0 || !mEmailSearch.getText().toString().contains("@"))
        {
            Context context = getApplicationContext();
            CharSequence text = "Invalid search parameters";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        currentSearch = mEmailSearch.getText().toString();
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(makeJsonObjReq());
    }

    private void addFriendFunc(String email)
    {
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(addFriendRequest(email));
    }

    private JsonObjectRequest addFriendRequest(String email) {
        final String TAG = "asdf";
        JSONObject toSend = new JSONObject();
        SharedPreferences sp = getSharedPreferences("travelShare", Context.MODE_PRIVATE);
        int userId = sp.getInt("userId", 0);
        try {
            toSend.put("userId", userId);
            toSend.put("friendEmail", email);
        } catch (org.json.JSONException e)
        {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                "http://10.0.2.2:8080/rest/user/AddFriend", toSend,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Context context = getApplicationContext();
                        CharSequence text = "User successfully added to friend list!";
                        int duration = Toast.LENGTH_LONG;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Context context = getApplicationContext();
                CharSequence text = "User not found!";
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
        final String TAG = "asdf";
        JSONObject toSend = new JSONObject();
        final String currSearch = currentSearch;
        try {
            toSend.put("email", currentSearch);
        } catch(org.json.JSONException e)
        {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                "http://10.0.2.2:8080/rest/user/Login", toSend,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        LinearLayout s = findViewById(R.id.friendResultLayout);
                        RelativeLayout friendInfo = (RelativeLayout) getLayoutInflater().inflate(R.layout.fragment_friendinfo, null);
                        TextView friendMail = friendInfo.findViewById(R.id.friendEmailFound);
                        friendMail.setText(currSearch);
                        Button addBtn = friendInfo.findViewById(R.id.addFriendConfirm);
                        addBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                addFriendFunc(currentSearch);
                            }
                        });
                        s.addView(friendInfo);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Context context = getApplicationContext();
                CharSequence text = "User not found!";
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
