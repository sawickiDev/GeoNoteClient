package com.steveq.geonoteclient.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.os.Message;

public class RadarService extends Service {
    private static final String TAG = RadarService.class.getSimpleName();

    private Looper radarLooper;
    private HandlerThread radarThread;
    private RadarHandler radarHandler;
    private int startId;


    @Override
    public void onCreate() {
        radarThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        radarThread.start();

        radarLooper = radarThread.getLooper();
        radarHandler = new RadarHandler(radarLooper, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Message msg = radarHandler.obtainMessage();
        msg.arg1 = startId;
        radarHandler.sendMessage(msg);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        this.startId = startId;
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
