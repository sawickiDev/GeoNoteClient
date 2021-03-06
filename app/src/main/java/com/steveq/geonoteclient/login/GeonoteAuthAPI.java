package com.steveq.geonoteclient.login;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface GeonoteAuthAPI {

    @POST("oauth/token")
    @FormUrlEncoded
    Call<AuthResponse> authRequest( @Header("Authorization") String credentials,
                                    @Field("grant_type") String grantType,
                                    @Field("username") String username,
                                    @Field("password") String password);

    @POST("user/register")
    Call<String> registerRequest(@Body RegisterData registerData);

    @POST("oauth/check_token")
    @FormUrlEncoded
    Call<TokenCheckResponse> tokenCheckRequest(@Field("token") String token);
}
