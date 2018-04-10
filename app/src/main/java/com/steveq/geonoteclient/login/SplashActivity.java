package com.steveq.geonoteclient.login;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.steveq.geonoteclient.map.MapsActivity;
import com.steveq.geonoteclient.services.PermissionChecker;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = SplashActivity.class.getSimpleName();

    private static final int INTERNET_REQUEST = 10;
    private static final String[] NEEDED_PERMISSIONS =
            new String[]{
                    "android.permission.INTERNET",
                    "android.permission.ACCESS_NETWORK_STATE"
            };

    private GeonoteAuthController geonoteAuthController;
    private TokensPersistant tokensPersistant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tokensPersistant = new TokensPersistant();
        geonoteAuthController = new GeonoteAuthController();

        handlePermissionCheck();
    }

    private void handlePermissionCheck(){

        PermissionChecker permissionChecker = new PermissionChecker(this);
        List<String> falsyPermissions = permissionChecker.getFalsyPermissions(NEEDED_PERMISSIONS);
        if(falsyPermissions.isEmpty())
            checkTokenValidity();
        else
            permissionChecker.requestPermissions(falsyPermissions, INTERNET_REQUEST);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case INTERNET_REQUEST: {
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    checkTokenValidity();
                } else {

                }
            }
        }
    }

    private void checkTokenValidity(){
        if(tokensPersistant.hasAccessToken()){
            geonoteAuthController
                    .prepareTokenCheckCall(tokensPersistant.getAccessToken())
                    .enqueue(new Callback<TokenCheckResponse>() {
                        @Override
                        public void onResponse(Call<TokenCheckResponse> call, Response<TokenCheckResponse> response) {
                            if(response.isSuccessful()){
                                TokenCheckResponse tokenCheckResponse = response.body();
                                if(tokenCheckResponse.getActive() != null
                                        && tokenCheckResponse.getActive())
                                    redirectToMap();
                                else
                                    redirectToLogin();
                            } else {
                                redirectToLogin();
                            }
                        }

                        @Override
                        public void onFailure(Call<TokenCheckResponse> call, Throwable t) {
                            //intentionally blank
                            Log.d(TAG, "CHECK FAILURE");
                        }
                    });
        } else {
            redirectToLogin();
        }
    }

    private void redirectToMap(){
        Intent intent =
                new Intent(SplashActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    private void redirectToLogin(){
        Intent intent =
                new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
