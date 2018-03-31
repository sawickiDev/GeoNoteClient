package com.steveq.geonoteclient.login;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.steveq.geonoteclient.App;

public class TokensPersistant {
    private static final String TAG = TokensPersistant.class.getSimpleName();

    private SharedPreferences sharedPreferencesHandle;
    private static final String PREF_FILE_NAME = "tokens_shared";

    public TokensPersistant(){
        sharedPreferencesHandle =
                App.getContext().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }

    public Boolean hasAccessToken(){
        return sharedPreferencesHandle.getString("access_token", null) != null;
    }

    public void saveAccessToken(String accessToken){
        SharedPreferences.Editor editor = sharedPreferencesHandle.edit();
        editor.putString("access_token", accessToken);
        editor.apply();
    }

    public void saveRefreshToken(String refreshToken){
        SharedPreferences.Editor editor = sharedPreferencesHandle.edit();
        editor.putString("refresh_token", refreshToken);
        editor.apply();
    }

    public String getAccessToken(){
        return sharedPreferencesHandle.getString("access_token", "");
    }

    public String getRefreshToken(){
        return sharedPreferencesHandle.getString("refresh_token", "");
    }
}
