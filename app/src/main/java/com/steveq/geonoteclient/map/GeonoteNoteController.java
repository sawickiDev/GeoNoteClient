package com.steveq.geonoteclient.map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.steveq.geonoteclient.App;
import com.steveq.geonoteclient.R;
import com.steveq.geonoteclient.login.AuthResponse;
import com.steveq.geonoteclient.login.GeonoteAuthAPI;
import com.steveq.geonoteclient.login.RegisterData;
import com.steveq.geonoteclient.login.TokenCheckResponse;
import com.steveq.geonoteclient.login.TokensPersistant;

import java.util.StringJoiner;

import okhttp3.Credentials;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GeonoteNoteController {
    private static final String TAG = GeonoteNoteController.class.getSimpleName();

    private Gson gson;
    private Retrofit retrofit;
    private GeonoteNoteAPI geonoteNoteAPI;
    private TokensPersistant tokensPersistant;

    public GeonoteNoteController(){
        gson =
                new GsonBuilder()
                        .setLenient()
                        .create();

        retrofit =
                new Retrofit.Builder()
                        .baseUrl(App.getContext().getResources().getString(R.string.base_url))
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();

        geonoteNoteAPI =
                retrofit.create(GeonoteNoteAPI.class);

        tokensPersistant = new TokensPersistant();
    }

    public Call<String> prepareCreateCall(RequestNote requestNote){
        StringJoiner joiner = new StringJoiner(" ");
        joiner
            .add("Bearer")
            .add(tokensPersistant.getAccessToken());
        return geonoteNoteAPI.publishNote(joiner.toString(), requestNote);
    }

}
