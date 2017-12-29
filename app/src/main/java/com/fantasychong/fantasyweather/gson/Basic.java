package com.fantasychong.fantasyweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lenovo on 2017/12/27.
 */

public class Basic {

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }





}
