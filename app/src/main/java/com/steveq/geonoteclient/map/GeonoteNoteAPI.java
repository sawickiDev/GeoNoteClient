package com.steveq.geonoteclient.map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface GeonoteNoteAPI {

    @POST("geonote/create")
    Call<String> publishNote(@Header("authorization") String token,
                             @Body RequestNote requestNote);

}
