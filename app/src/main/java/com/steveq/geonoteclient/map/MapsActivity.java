package com.steveq.geonoteclient.map;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.steveq.geonoteclient.R;
import com.steveq.geonoteclient.services.PermissionChecker;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    private static final String TAG = MapsActivity.class.getSimpleName();

    private static final int LOCATION_REQUEST = 20;
    private static final String[] NEEDED_PERMISSIONS =
            new String[]{
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_COARSE_LOCATION"
            };

    @BindView(R.id.parentRelativeLayout)
    View parentRelativeLayout;

    SupportMapFragment mapFragment;

    private GoogleMap googleMap;
    private MarkerOptions currentPosMarker;
    private Marker mapMarker;
    private LocationManager locationManager;
    private String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);

        mapFragment.getMapAsync(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "RESUME :: " + locationManager);
        if (locationManager != null) {
            centerOnCurrentPosition();
            locationManager.requestLocationUpdates(provider, 400, 300, this);
        }
    }

    private void promptForGpsEnable(){
        Intent gpsOptionsIntent = new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(gpsOptionsIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    private void handleLocationInitialization(){
        if(!locationManager.isProviderEnabled(provider)){
            Log.d(TAG, "PROMPT FOR GPS");
            promptForGpsEnable();
        }

        centerOnCurrentPosition();
    }

    @SuppressLint("MissingPermission")
    private void centerOnCurrentPosition(){
        Location location = locationManager.getLastKnownLocation(provider);

        Log.d(TAG, "CENTER :: " + location);
        if(location != null){
            LatLng currentPos = new LatLng(location.getLatitude(), location.getLongitude());

            if(mapMarker != null)
                mapMarker.remove();

            currentPosMarker = new MarkerOptions().position(currentPos);

            mapMarker = googleMap.addMarker(currentPosMarker);
        } else {
            Snackbar
                    .make(parentRelativeLayout, getResources().getString(R.string.location_unavailable), Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    @SuppressLint("MissingPermission")
    private void initializeLocationService(){
        Location location = locationManager.getLastKnownLocation(provider);

        if(location != null){
            Log.d(TAG, "Provider " + provider + " has been selected");
            LatLng currentPos = new LatLng(location.getLatitude(), location.getLongitude());
            currentPosMarker = new MarkerOptions().position(currentPos);
            googleMap.addMarker(currentPosMarker);
        } else {
            Snackbar
                .make(parentRelativeLayout, getResources().getString(R.string.location_unavailable), Snackbar.LENGTH_SHORT)
                .show();
        }
    }

    private Criteria getCriteria() {
        Criteria criteria = new Criteria();

        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);

        return criteria;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        provider = locationManager.getBestProvider(new Criteria(), false);
        Log.d(TAG, "PROVIDER :: " + provider);
        Log.d(TAG, "PROVIDERS :: " + locationManager.getAllProviders());

        PermissionChecker permissionChecker = new PermissionChecker(this);
        if(permissionChecker.handlePermission(NEEDED_PERMISSIONS, LOCATION_REQUEST)){
            Log.d(TAG, "HANDLE");
            handleLocationInitialization();
        }
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        this.googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case LOCATION_REQUEST: {
                Log.d(TAG, "REQUEST PERMISSION RESULT");
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    handleLocationInitialization();
                } else {
                    finish();
                }
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Latitude changed :: " + location.getLatitude());
        Log.d(TAG, "Longitude changed :: " + location.getLongitude());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
