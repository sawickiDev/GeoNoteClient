package com.steveq.geonoteclient.map;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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
    private Context context;

    public GeonoteNoteController(Context context){
        this.context = context;

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
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(this.context);
        String radiusPref = this.context.getString(R.string.settings_radius_key);
        String ownedPref = this.context.getString(R.string.settings_owned_key);
        String othersPref = this.context.getString(R.string.settings_others_key);

        StringJoiner joiner = new StringJoiner(" ");
        joiner
            .add("Bearer")
            .add(tokensPersistant.getAccessToken());

        StringJoiner accessJoiner = new StringJoiner(",");

        if(sh.getBoolean(ownedPref, true))
            accessJoiner.add("owned");
        if(sh.getBoolean(othersPref, false))
            accessJoiner.add("others");

        return geonoteNoteAPI.fetchNotes(
                joiner.toString(),
                lat,
                lng,
                sh.getInt(radiusPref, 100),
                accessJoiner.toString());
    }

}
