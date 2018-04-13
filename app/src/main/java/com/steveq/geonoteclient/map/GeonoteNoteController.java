package com.steveq.geonoteclient.map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.steveq.geonoteclient.App;
import com.steveq.geonoteclient.R;
import com.steveq.geonoteclient.services.TokensPersistant;

import java.util.StringJoiner;

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
                        .serializeNulls()
                        .create();

        retrofit =
                new Retrofit.Builder()
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .baseUrl(App.getContext().getResources().getString(R.string.base_url))
                        .build();

        geonoteNoteAPI =
                retrofit.create(GeonoteNoteAPI.class);

        tokensPersistant = new TokensPersistant();
    }

    public Call<GeoNote> prepareCreateCall(RequestNote requestNote){
        StringJoiner joiner = new StringJoiner(" ");
        joiner
            .add("Bearer")
            .add(tokensPersistant.getAccessToken());
        return geonoteNoteAPI.publishNote(joiner.toString(), requestNote);
    }

    public Call<GeoNoteBatch> prepareFetchCall(Double lat, Double lng){
        StringJoiner joiner = new StringJoiner(" ");
        joiner
            .add("Bearer")
            .add(tokensPersistant.getAccessToken());
        return geonoteNoteAPI.fetchNotes(
                joiner.toString(),
                lat,
                lng,
                1000,
                "owned,others");
    }

}
