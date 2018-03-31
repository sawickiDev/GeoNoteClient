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

    private Gson gson;
    private Retrofit retrofit;
    private GeonoteAuthAPI geonoteAuthAPI;

    public GeonoteAuthController(){
        gson =
                new GsonBuilder()
                        .setLenient()
                        .create();

        retrofit =
                new Retrofit.Builder()
                        .baseUrl(App.getContext().getResources().getString(R.string.base_url))
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();

        geonoteAuthAPI =
                retrofit.create(GeonoteAuthAPI.class);
    }

    public Call<AuthResponse> prepareLoginCall(String username, String password){

        return geonoteAuthAPI.authRequest(
                        Credentials.basic("android_app", "android"),
                        "password",
                        username,
                        password
                );
    }

    public Call<String> prepareRegisterCall(RegisterData registerData){

        return geonoteAuthAPI.registerRequest(registerData);
    }

    public Call<TokenCheckResponse> prepareTokenCheckCall(String token){

        return geonoteAuthAPI.tokenCheckRequest(token);
    }
}
