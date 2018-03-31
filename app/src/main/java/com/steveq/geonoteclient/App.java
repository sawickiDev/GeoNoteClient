package com.steveq.geonoteclient;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class App extends Application{
    private static final String TAG = App.class.getSimpleName();

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "on create App");
        context = this;

    }

    public static Context getContext(){
        return context;
    }
}
