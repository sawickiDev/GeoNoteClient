package com.steveq.geonoteclient.map;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GeonoteNoteAPI {

    @POST("geonote/create")
    Call<GeoNote> publishNote(@Header("authorization") String token,
                             @Body RequestNote requestNote);

    @GET("geonote/fetch")
    Call<GeoNoteBatch> fetchNotes(@Header("authorization") String token,
                                   @Query("lat") Double lat,
                                   @Query("lng") Double lng,
                                   @Query("radius") Integer radius,
                                   @Query("access") String access);

}
