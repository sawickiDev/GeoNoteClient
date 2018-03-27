package com.steveq.geonoteclient.login;

import android.content.res.Resources;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.steveq.geonoteclient.App;
import com.steveq.geonoteclient.R;

import okhttp3.Credentials;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GeonoteAuthController{
    private static final String TAG = GeonoteAuthController.class.getSimpleName();

    private Callback<AuthResponse> callback;

    public GeonoteAuthController(Callback<AuthResponse> callback){
        this.callback = callback;
    }

    public void start(){
        Gson gson =
                new GsonBuilder()
                    .setLenient()
                    .create();

        Retrofit retrofit =
                new Retrofit.Builder()
                    .baseUrl(App.getContext().getResources().getString(R.string.base_url))
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

        GeonoteAuthAPI geonoteAuthAPI =
                retrofit.create(GeonoteAuthAPI.class);

        Call<AuthResponse> call =
                geonoteAuthAPI.authRequest(
                        Credentials.basic("android_app", "android"),
                        "password",
                        "adamek",
                        "adamek"
                );
        Log.d(TAG, String.valueOf(call.request().url()));
        Log.d(TAG, String.valueOf(call.request().body()));
        call.enqueue(callback);
    }
}
