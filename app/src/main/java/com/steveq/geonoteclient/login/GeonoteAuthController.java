package com.steveq.geonoteclient.login;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.steveq.geonoteclient.App;
import com.steveq.geonoteclient.R;

import okhttp3.Credentials;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GeonoteAuthController{
    private static final String TAG = GeonoteAuthController.class.getSimpleName();

    public GeonoteAuthController(){}

    public Call<AuthResponse> prepareLoginCall(String username, String password){
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

        return geonoteAuthAPI.authRequest(
                        Credentials.basic("android_app", "android"),
                        "password",
                        username,
                        password
                );
    }

    public Call<String> prepareRegisterCall(RegisterData registerData){
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

        Call<String> call =
                geonoteAuthAPI.registerRequest(registerData);
        Log.d(TAG, String.valueOf(call.request().url()));
        Log.d(TAG, String.valueOf(call.request().body().toString()));
        Log.d(TAG, registerData.toString());
        return call;
    }
}
