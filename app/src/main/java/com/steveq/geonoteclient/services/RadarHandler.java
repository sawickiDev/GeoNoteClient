package com.steveq.geonoteclient.services;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.steveq.geonoteclient.R;
import com.steveq.geonoteclient.map.GeoNote;
import com.steveq.geonoteclient.map.GeoNoteBatch;
import com.steveq.geonoteclient.map.GeonoteNoteController;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RadarHandler extends Handler implements LocationListener {
    private static final String TAG = RadarHandler.class.getSimpleName();

    private Service parentService;
    private LocationManager locationManager;
    private String bestProvider;
    private GeonoteNoteController geonoteNoteController;
    private int currentNotification;
    private Random randomGen;

    private RadarHandler(Looper looper) {
        super(looper);
    }

    @SuppressLint("MissingPermission")
    public RadarHandler(Looper looper, Service service) {
        this(looper);

        randomGen = new Random();
        parentService = service;
        this.geonoteNoteController = new GeonoteNoteController(parentService);
        locationManager = (LocationManager) parentService.getSystemService(Context.LOCATION_SERVICE);
        bestProvider =
                locationManager.getBestProvider(new Criteria(), false);

        if(bestProvider != null)
            locationManager.requestLocationUpdates(bestProvider, 400, 100, this);

    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Log.d(TAG, "HANDLE MESSAGE");
        parentService.stopSelf();
    }

    @Override
    public void onLocationChanged(Location location) {
        geonoteNoteController.prepareFetchCall(location.getLatitude(), location.getLongitude())
                .enqueue(new retrofit2.Callback<GeoNoteBatch>() {
                    @Override
                    public void onResponse(Call<GeoNoteBatch> call, Response<GeoNoteBatch> response) {
                        List<GeoNote> fetchedNotes = response.body().getNotes();
                        Optional<GeoNote> closest = fetchedNotes.stream().min((n1, n2) -> {
                            Location loc1 = new Location("");
                            loc1.setLatitude(n1.getLat());
                            loc1.setLongitude(n1.getLng());

                            Location loc2 = new Location("");
                            loc2.setLatitude(n2.getLat());
                            loc2.setLongitude(n2.getLng());

                            float dist1 = location.distanceTo(loc1);
                            float dist2 = location.distanceTo(loc2);

                            return Float.compare(dist1, dist2);
                        });

                        closest.ifPresent(v -> {

                            NotificationManagerCompat notificationManagerCompat =
                                    NotificationManagerCompat.from(parentService);

                            if(currentNotification != 0)
                                notificationManagerCompat.cancel(currentNotification);

                            NotificationCompat.Builder builder = new NotificationCompat.Builder(parentService, TAG)
                                    .setSmallIcon(R.drawable.vec_logo_small)
                                    .setContentTitle("You got note from " + v.getOwner())
                                    .setContentText(v.getNote())
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                            currentNotification = randomGen.nextInt();
                            notificationManagerCompat.notify(currentNotification, builder.build());

                        });
                    }

                    @Override
                    public void onFailure(Call<GeoNoteBatch> call, Throwable t) {
                        Log.d(TAG, "ERROR FETCHING");
                    }
                });
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
