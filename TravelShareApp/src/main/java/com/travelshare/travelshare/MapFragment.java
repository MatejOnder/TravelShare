package com.travelshare.travelshare;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MapFragment extends Fragment {
    MapView mMapView;
    private GoogleMap mMap;
    private final int REQUEST_PERMISSION_PHONE_STATE = 1;
    private Map<Marker, Integer> markerIdMap = new HashMap<>();
    private int markerCount = 0;
    private ArrayList<Marker> markers = new ArrayList<>();

    public void clearMap() {
        mMap.clear();
        markers.clear();
        markerIdMap.clear();
        markerCount = 0;
    }

    public void centerOnUser()
    {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if (location != null)
        {
            if(markerCount <= 1) {
                LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate cup = CameraUpdateFactory.newLatLngZoom(pos, 13);
                mMap.animateCamera(cup);
                Marker m = mMap.addMarker(new MarkerOptions().position(pos).title("You"));
                m.setVisible(true);
                markers.add(m);
                markerCount++;
            }
        }
        else
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, new GPSClass());
        }
    }

    public interface CustomInteractionListener {
         void showExplanation(String title,
                             String message,
                             final String permission,
                             final int permissionRequestCode, final Activity thisInContext);
         void setFragment(MapFragment m);
    }

    private CustomInteractionListener mListener;

    public MapFragment() {
        // Required empty public constructor
    }

    public void zoomOnBounds(LatLng point1, LatLng point2)
    {
        LatLngBounds llb = new LatLngBounds(point1, point2);
        CameraUpdate cup = CameraUpdateFactory.newLatLngBounds(llb, 150);
        mMap.animateCamera(cup);
    }

    public void zoomOnBounds(LatLngBounds llb)
    {
        CameraUpdate cup = CameraUpdateFactory.newLatLngBounds(llb, 150);
        mMap.animateCamera(cup);
    }

    public void DrawMarker(LatLng latlng, String t, boolean clear, float... color)
    {
        Marker m;
        if(clear)
        {
            mMap.clear();
            markers.clear();
            markerCount = 0;
        }
        if(color.length > 0)
        {
            m = mMap.addMarker(new MarkerOptions().position(latlng).title(t).icon(BitmapDescriptorFactory.defaultMarker(color[0])));
            m.setVisible(true);
        }
        else
        {
            m = mMap.addMarker(new MarkerOptions().position(latlng).title(t));
            m.setVisible(true);
        }
        markers.add(m);
        extendBounds();
        markerCount++;
    }

    public void extendBounds()
    {
        if(markers.size() > 2)
        {
            Marker m1 = markers.get(0);
            Marker m2 = markers.get(1);

            LatLngBounds llb = new LatLngBounds(m1.getPosition(), m2.getPosition());

            for(int i = 2; i < markers.size(); i++)
            {
                Marker m = markers.get(i);
                llb = llb.including(m.getPosition());
            }

            zoomOnBounds(llb);
        }
    }

    public void DrawMarkerWithId(LatLng latlng, int id, String t, boolean clear, float... color)
    {
        Marker m;
        if(clear)
        {
            mMap.clear();
            markers.clear();
            markerCount = 0;
        }
        if(color.length > 0)
        {
            m = mMap.addMarker(new MarkerOptions().position(latlng).title(t).icon(BitmapDescriptorFactory.defaultMarker(color[0])));
            m.setVisible(true);
        }
        else
        {
            m = mMap.addMarker(new MarkerOptions().position(latlng).title(t));
            m.setVisible(true);
        }
        markerIdMap.put(m, id);
        markers.add(m);
        extendBounds();
        markerCount++;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void getPhotoFromId(int id) {
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(getPhotoById(id));
    }

    private StringRequest getPhotoById(int id){
        final String TAG = "asdf";

        StringRequest jsonObjReq = new StringRequest(Request.Method.GET,
                "http://10.0.2.2:8080/rest/photo/"+id,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        MainNavigation m = (MainNavigation) getActivity();
                        m.displayPhoto(response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Context context = getActivity().getApplicationContext();
                CharSequence text = "Error fetching ID";
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflat and return the layout
        View v = inflater.inflate(R.layout.fragment_location_info, container,
                false);
        mMapView = v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        if(markerIdMap.containsKey(marker))
                        {
                            getPhotoFromId(markerIdMap.get(marker));
                        }
                        return true;
                    }
                });
                boolean x = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    CustomInteractionListener c = (CustomInteractionListener) getActivity();
                    c.showExplanation("Permission needed", "You need to enable location services to use this app", Manifest.permission.READ_PHONE_STATE, REQUEST_PERMISSION_PHONE_STATE, getActivity());
                }
                else
                {
                    centerOnUser();
                }
            }
        });
        // Perform any camera updates here
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CustomInteractionListener) {
            mListener = (CustomInteractionListener) context;
            mListener.setFragment(this);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public class GPSClass implements LocationListener {

        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            centerOnUser();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}
        public void onProviderEnabled(String provider) {}
        public void onProviderDisabled(String provider) {}
    }
}
