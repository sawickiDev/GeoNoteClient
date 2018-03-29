package com.steveq.geonoteclient.login;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class TokensPersistant {

    private Activity activityContext;
    private SharedPreferences sharedPreferencesHandle;

    public TokensPersistant(Activity activity){
        this.activityContext = activity;
        sharedPreferencesHandle = activity.getPreferences(Context.MODE_PRIVATE);
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
