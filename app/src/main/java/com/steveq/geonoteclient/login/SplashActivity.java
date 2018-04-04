package com.steveq.geonoteclient.login;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.steveq.geonoteclient.map.MapFragment;
import com.steveq.geonoteclient.services.PermissionChecker;

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
        PermissionChecker permissionChecker = new PermissionChecker(this);
        if(permissionChecker.handlePermission(NEEDED_PERMISSIONS, INTERNET_REQUEST))
            checkTokenValidity();
    }

    private void redirectToMap(){
        Intent intent =
                new Intent(SplashActivity.this, MapFragment.class);
        startActivity(intent);
    }

    private void redirectToLogin(){
        Intent intent =
                new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case INTERNET_REQUEST: {
                Log.d(TAG, "REQUEST PERMISSION RESULT");
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    checkTokenValidity();
                } else {

                }
            }
        }
    }

    private void checkTokenValidity(){
        Log.d(TAG, "CHECK VALIDITY :: " + tokensPersistant.hasAccessToken());
        if(tokensPersistant.hasAccessToken()){
            Log.d(TAG, "CHECK VALIDITY :: " + tokensPersistant.getAccessToken());
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
}
