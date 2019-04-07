package com.inspirati.iotcompanion;


import com.inspirati.iotcompanion.model.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.*;


interface APIInterface {

    @GET("/rooms")
    Call<GetRooms> getAllRooms();


    /*@POST("/getModels?")
    Call<CarModels> doGetModels(@Body CarModels carModels);*/
    /*
    @GET("/getUserDetail")
    Call<UserDetail> getDetails(@Header("email") String email,
                                @Header("name") String name,
                                @Header("player_id") String playerId,
                                @Header("deviceId") String deviceId);*/
}