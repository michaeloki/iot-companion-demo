package com.inspirati.iotcompanion.viewController;


import com.inspirati.iotcompanion.model.*;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;


interface APIInterface {

    @GET("/rooms")
    Call<GetRooms> getAllRooms();

    @GET("/bedroom/light1/on")
    Call<ResponseBody> turnOnBedLightOne();

    @GET("/bedroom/light2/on")
    Call<ResponseBody> turnOnBedLightTwo();

    @GET("/bedroom/ac/on")
    Call<ResponseBody> turnOnAC();

    @GET("/bedroom/light1/off")
    Call<ResponseBody> turnOffBedLightOne();

    @GET("/bedroom/light2/off")
    Call<ResponseBody> turnOffBedLightTwo();

    @GET("/bedroom/ac/off")
    Call<ResponseBody> turnOffAC();

    @GET("/kitchen/light/on")
    Call<ResponseBody> turnOnKitchenLightOne();

    @GET("/kitchen/music/on")
    Call<ResponseBody> turnOnKitchenMusic();

    @GET("/kitchen/slowcooker/on")
    Call<ResponseBody> turnOnKitchenSlowCooker();

    @GET("/kitchen/light/off")
    Call<ResponseBody> turnOffKitchenLightOne();

    @GET("/kitchen/music/off")
    Call<ResponseBody> turnOffKitchenMusic();

    @GET("/kitchen/slowcooker/off")
    Call<ResponseBody> turnOffKitchenSlowCooker();

    @GET("/living-room/light/on")
    Call<ResponseBody> turnOnLivingLightOne();

    @GET("/living-room/tv/on")
    Call<ResponseBody> turnOnLivingLightTwo();

    @GET("/living-room/light/off")
    Call<ResponseBody> turnOffLivingLightOne();

    @GET("/living-room/tv/off")
    Call<ResponseBody> turnOffLivingLightTwo();
}