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
import android.location.LocationProvider;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.steveq.geonoteclient.R;
import com.steveq.geonoteclient.login.AuthError;
import com.steveq.geonoteclient.login.AuthResponse;
import com.steveq.geonoteclient.login.GeonoteAuthController;
import com.steveq.geonoteclient.login.TokensPersistant;
import com.steveq.geonoteclient.services.PermissionChecker;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    private static final String TAG = MapsActivity.class.getSimpleName();

    private static final int LOCATION_REQUEST = 20;
    private static final String[] NEEDED_PERMISSIONS =
            new String[]{
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_COARSE_LOCATION"
            };
    private static final String PROVIDER = "gps";

    @BindView(R.id.parentRelativeLayout)
    View parentRelativeLayout;

    @BindView(R.id.noteEditText)
    EditText noteEditText;

    @BindView(R.id.publishFloatingActionButton)
    FloatingActionButton publishFloatingButton;

    SupportMapFragment mapFragment;

    private GoogleMap googleMap;
    private MarkerOptions currentPosMarker;
    private Marker mapMarker;
    private LocationManager locationManager;
    private GeonoteNoteController geonoteNoteController;
    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        geonoteNoteController = new GeonoteNoteController();

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);

        handlePermissionCheck();
        publishFloatingButton.setOnClickListener(v -> {
            String noteText = noteEditText.getText().toString();
            if(!"".equals(noteText)){
                geonoteNoteController.prepareCreateCall(
                        new RequestNote(
                                        noteText,
                                        this.location.getLatitude(),
                                        this.location.getLongitude())
                ).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if(response.isSuccessful()){
                            MarkerOptions newlyNoteMarker =
                                    new MarkerOptions()
                                            .position(new LatLng(location.getLatitude(), location.getLongitude()))
                                            .snippet(noteText)
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                            googleMap.addMarker(newlyNoteMarker);
                        } else if (response.code() == 409){
                            Snackbar
                                    .make(parentRelativeLayout, response.body(), Snackbar.LENGTH_LONG)
                                    .show();
                        } else {
                            Snackbar
                                    .make(parentRelativeLayout, getResources().getString(R.string.publish_error), Snackbar.LENGTH_SHORT)
                                    .show();

                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.d(TAG, t.getMessage());
                        Snackbar
                                .make(parentRelativeLayout, getResources().getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                                .show();
                    }
                });
            }
        });
    }

    private void handlePermissionCheck(){

        PermissionChecker permissionChecker = new PermissionChecker(this);
        List<String> falsyPermissions = permissionChecker.getFalsyPermissions(NEEDED_PERMISSIONS);
        if(falsyPermissions.isEmpty())
            mapFragment.getMapAsync(this);
        else
            permissionChecker.requestPermissions(falsyPermissions, LOCATION_REQUEST);

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(!locationManager.isProviderEnabled(PROVIDER)){
            promptForGpsEnable();
        } else {
            centerOnCurrentPosition();
            locationManager.requestLocationUpdates(PROVIDER, 400, 300, this);
        }
    }

    private void promptForGpsEnable(){
        Intent gpsOptionsIntent = new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(gpsOptionsIntent);
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();

        if (haveLocationServiceInitialized()) {
            centerOnCurrentPosition();
            locationManager.requestLocationUpdates(PROVIDER, 400, 300, this);
        }
    }

    private boolean haveLocationServiceInitialized(){
        return locationManager != null;
    }

    @SuppressLint("MissingPermission")
    private void centerOnCurrentPosition(){
         location = locationManager.getLastKnownLocation(PROVIDER);

        if(location != null){
            LatLng currentPos = new LatLng(location.getLatitude(), location.getLongitude());

            if(mapMarker != null)
                mapMarker.remove();

            currentPosMarker = new MarkerOptions().position(currentPos);

            mapMarker = googleMap.addMarker(currentPosMarker);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentPos));
            googleMap.setMinZoomPreference(14.0f);
            googleMap.setMaxZoomPreference(16.0f);
        } else {
            Snackbar
                    .make(parentRelativeLayout, getResources().getString(R.string.location_unavailable), Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(haveLocationServiceInitialized())
            locationManager.removeUpdates(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case LOCATION_REQUEST: {
                if(grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    mapFragment.getMapAsync(this);
                } else {
                    finish();
                }
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        LatLng currentPos = new LatLng(location.getLatitude(), location.getLongitude());

        if(mapMarker != null)
            mapMarker.remove();


        currentPosMarker = new MarkerOptions().position(currentPos);

        mapMarker = googleMap.addMarker(currentPosMarker);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentPos));
        googleMap.setMinZoomPreference(12.0f);
        googleMap.setMaxZoomPreference(14.0f);
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
