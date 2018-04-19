package com.steveq.geonoteclient.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.steveq.geonoteclient.App;
import com.steveq.geonoteclient.R;
import com.steveq.geonoteclient.login.LoginActivity;
import com.steveq.geonoteclient.services.PermissionChecker;
import com.steveq.geonoteclient.services.RadarService;
import com.steveq.geonoteclient.services.TokensPersistant;
import com.steveq.geonoteclient.settings.SettingsActivity;

import java.time.Instant;
import java.util.ArrayList;
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

    @BindView(R.id.drawerContainer)
    DrawerLayout drawerContainer;

    @BindView(R.id.drawerNavigationView)
    NavigationView drawerNavigationView;

    @BindView(R.id.burgerImageButton)
    ImageButton burgerImageButton;

    @BindView(R.id.parentRelativeLayout)
    View parentRelativeLayout;

    @BindView(R.id.noteEditText)
    EditText noteEditText;

    @BindView(R.id.publishFloatingActionButton)
    FloatingActionButton publishFloatingButton;

    @BindView(R.id.expirationSeekBar)
    SeekBar expirationSeekBar;

    @BindView(R.id.unitRadioGroup)
    RadioGroup unitRadioGroup;

    @BindView(R.id.minutesRadioButton)
    RadioButton minutesRadioButton;

    @BindView(R.id.hoursRadioButton)
    RadioButton hoursRadioButton;

    @BindView(R.id.daysRadioButton)
    RadioButton daysRadioButton;

    @BindView(R.id.timeTextView)
    TextView timeTextView;

    SupportMapFragment mapFragment;

    private GoogleMap googleMap;
    private MarkerOptions currentPosMarker;
    private Marker currentLocationMarker;
    private List<Marker> notesMarkers = new ArrayList<>();
    private LocationManager locationManager;
    private GeonoteNoteController geonoteNoteController;
    private Location location;
    private String bestProvider;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private TokensPersistant tokensPersistant;
    private int seekBarMin;
    private int seekBarIncrement = 1;
    private int seekBarValue;
    private int seekConverterValue;
    private int unitIndicator;

    View.OnClickListener publishListener = v -> {
        String noteText = noteEditText.getText().toString();
        hideKeyboard();
        if(noteWithinConstraints(noteText)){
            RequestNote rn =
                    new RequestNote(
                        noteText,
                        this.location.getLatitude(),
                        this.location.getLongitude(),
                        this.seekBarValue * this.seekConverterValue
                    );
            Log.d(TAG, "REQUEST NOTE :: " + rn);
            geonoteNoteController.prepareCreateCall(
                rn
            ).enqueue(new Callback<GeoNote>() {
                @Override
                public void onResponse(Call<GeoNote> call, Response<GeoNote> response) {
                    if(response.isSuccessful()){

                        GeoNote geoNote = response.body();
                        Log.d(TAG, "CREATED GEONOTE :: " + geoNote);
                        googleMap.addMarker(geoNoteMarkerOptions(geoNote));
                        noteEditText.setText("");
                        showSimpleSnackbar(getResources().getString(R.string.note_created));

                    } else if (response.code() == 406){
                        showSimpleSnackbar(getResources().getString(R.string.location_occupied));
                    } else if (response.code() == 409){
                        showSimpleSnackbar(getResources().getString(R.string.spam_warning));
                    } else {
                        showSimpleSnackbar(getResources().getString(R.string.publish_error));
                    }
                }

                @Override
                public void onFailure(Call<GeoNote> call, Throwable t) {
                    showSimpleSnackbar(getResources().getString(R.string.connection_error));
                }
            });
        }
    };

    NavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            menuItem -> {
                menuItem.setChecked(true);
                drawerContainer.closeDrawers();
                String itemName = menuItem.getTitle().toString();
                switch(itemName){
                    case "Current Location" :
                        centerOnCurrentPosition();
                        break;
                    case "Settings" :
                        Intent intent = new Intent(this, SettingsActivity.class);
                        this.startActivity(intent);
                        break;
                    case "Logout" :
                        performLogout();
                        break;
                    default:
                        break;
                }
                return true;
            };

    private void performLogout() {
        tokensPersistant.removeAccessToken();
        if(!tokensPersistant.hasAccessToken())
            redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        this.startActivity(intent);
    }

    private void hideKeyboard(){
        View view = this.getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private boolean noteWithinConstraints(String note) {
        return !note.isEmpty()
                && note.length() <= this.getResources().getInteger(R.integer.max_chars);
    }

    private void showSimpleSnackbar(String message){
        Snackbar
            .make(parentRelativeLayout, message, Snackbar.LENGTH_LONG)
            .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        geonoteNoteController = new GeonoteNoteController(this);
        tokensPersistant = new TokensPersistant();

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);

        handlePermissionCheck();
        fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this);

        publishFloatingButton.setOnClickListener(publishListener);
        drawerNavigationView.setNavigationItemSelectedListener(navigationItemSelectedListener);

        burgerImageButton.setOnClickListener(v -> {
            drawerContainer.openDrawer(GravityCompat.START);
        });

        initializeMinutesSeekBar();
        expirationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int progress = (i + seekBarMin) / seekBarIncrement;
                progress = progress * seekBarIncrement;
                seekBarValue = progress;
                timeTextView.setText(App.getContext().getString(unitIndicator, String.valueOf(seekBarValue)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        unitRadioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            switch(i){
                //minutes
                case 2131296371:
                    initializeMinutesSeekBar();
                    break;
                //hours
                case 2131296345:
                    initializeHoursSeekBar();
                    break;
                //days
                case 2131296314:
                    initializeDaysSeekBar();
                    break;
            }
        });
    }

    private void initializeMinutesSeekBar() {
        unitIndicator = R.string.minutes_choose_label;
        seekConverterValue = 1;
        seekBarMin = 5;
        seekBarIncrement = 5;
        seekBarValue = seekBarMin;
        expirationSeekBar.setMax(60 - seekBarMin);
        expirationSeekBar.setProgress(seekBarValue - seekBarMin);
        timeTextView.setText(App.getContext().getString(R.string.minutes_choose_label, String.valueOf(seekBarValue)));
    }

    private void initializeHoursSeekBar() {
        unitIndicator = R.string.hours_choose_label;
        seekConverterValue = 60;
        seekBarMin = 1;
        seekBarIncrement = 1;
        seekBarValue = seekBarMin;
        expirationSeekBar.setMax(24 - seekBarMin);
        expirationSeekBar.setProgress(seekBarValue - seekBarMin);
        timeTextView.setText(App.getContext().getString(R.string.hours_choose_label, String.valueOf(seekBarValue)));
    }

    private void initializeDaysSeekBar() {
        unitIndicator = R.string.days_choose_label;
        seekConverterValue = 60 * 24;
        seekBarMin = 1;
        seekBarIncrement = 1;
        seekBarValue = seekBarMin;
        expirationSeekBar.setMax(30 - seekBarMin);
        expirationSeekBar.setProgress(seekBarValue - seekBarMin);
        timeTextView.setText(App.getContext().getString(R.string.days_choose_label, String.valueOf(seekBarValue)));
    }

    private void handlePermissionCheck(){

        PermissionChecker permissionChecker = new PermissionChecker(this);
        List<String> falsyPermissions = permissionChecker.getFalsyPermissions(NEEDED_PERMISSIONS);
        if(falsyPermissions.isEmpty())
            mapFragment.getMapAsync(this);
        else
            permissionChecker.requestPermissions(falsyPermissions, LOCATION_REQUEST);

    }

    private MarkerOptions geoNoteMarkerOptions(GeoNote geoNote) {
        return new MarkerOptions()
                        .position(new LatLng(geoNote.getLat(), geoNote.getLng()))
                        .title(geoNote.getOwner() + " (Expired in " + getExpirationString(geoNote) + ")")
                        .snippet(geoNote.getNote())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
    }

    @SuppressLint("NewApi")
    private String getExpirationString(GeoNote geoNote) {

        String result;
        long createdTs = geoNote.getCreatedTimestamp();
        long expirationTs = createdTs + geoNote.getExpirationMinutes() * 60 * 1000;
        long millisToExpiration = expirationTs - System.currentTimeMillis();
        long secsToExpiration = millisToExpiration/1000;

        if (secsToExpiration >= 60*60*24)
            result = secsToExpiration / 60 / 60 / 24 + "[d]";
        else if(secsToExpiration >= 60*60)
            result = secsToExpiration / 60 / 60 + "[hr]";
        else
            result = secsToExpiration / 60 + "[min]";

        return result;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);
        this.googleMap.getUiSettings().setZoomGesturesEnabled(true);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        bestProvider =
                locationManager.getBestProvider(new Criteria(), false);

        if(!locationManager.isProviderEnabled(bestProvider)){
            promptForGpsEnable();
        } else {
            centerOnCurrentPosition();
            showNearbyNotes(this.location);
            locationManager.requestLocationUpdates(bestProvider, 400, 300, this);
        }
    }

    private void showNearbyNotes(Location location){
        geonoteNoteController.prepareFetchCall(location.getLatitude(), location.getLongitude())
                .enqueue(new Callback<GeoNoteBatch>() {
                    @Override
                    public void onResponse(Call<GeoNoteBatch> call, Response<GeoNoteBatch> response) {
                        clearNotesMarkers();
                        placeNotesOnMap(response.body().getNotes());
                    }

                    @Override
                    public void onFailure(Call<GeoNoteBatch> call, Throwable t) {
                        Snackbar
                                .make(parentRelativeLayout, getResources().getString(R.string.connection_error), Snackbar.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void clearNotesMarkers() {
        notesMarkers.stream().forEach(m -> m.remove());
        notesMarkers.clear();
    }

    private void placeNotesOnMap(List<GeoNote> geoNotes) {
        geoNotes.stream().forEach(gf -> {
            notesMarkers.add(
                googleMap.addMarker(geoNoteMarkerOptions(gf))
            );
        });
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

        Intent intent = new Intent(this, RadarService.class);
        stopService(intent);

        if (haveLocationServiceInitialized()) {
            centerOnCurrentPosition();
            showNearbyNotes(this.location);
            locationManager.requestLocationUpdates(bestProvider, 400, 300, this);
        }
    }

    private boolean haveLocationServiceInitialized(){
        return locationManager != null;
    }

    @SuppressLint("MissingPermission")
    private void centerOnCurrentPosition(){
        location = locationManager.getLastKnownLocation(bestProvider);

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(l -> {
                    location = l;
                    moveMapToLocation(l);
                })
                .addOnFailureListener(e -> {
                    Snackbar
                        .make(parentRelativeLayout, getResources().getString(R.string.location_unavailable), Snackbar.LENGTH_SHORT)
                        .show();
                });
    }

    private void moveMapToLocation(Location location){
        LatLng currentPos = new LatLng(location.getLatitude(), location.getLongitude());

        if(currentLocationMarker != null)
            currentLocationMarker.remove();

        currentPosMarker = new MarkerOptions().position(currentPos);

        currentLocationMarker = googleMap.addMarker(currentPosMarker);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 18.0f));
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

//    @Override
//    public void onBackPressed() {
//        //do nothing on back pressed
//        finish();
//    }

    @Override
    protected void onStop() {
        super.onStop();
        if(tokensPersistant.hasAccessToken())
            bootstrapRadarService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(tokensPersistant.hasAccessToken())
            bootstrapRadarService();
    }

    private void bootstrapRadarService(){
        Intent intent = new Intent(this, RadarService.class);
        this.startService(intent);
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        LatLng currentPos = new LatLng(location.getLatitude(), location.getLongitude());

        if(currentLocationMarker != null)
            currentLocationMarker.remove();

        moveMapToLocation(location);
        showNearbyNotes(location);
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
