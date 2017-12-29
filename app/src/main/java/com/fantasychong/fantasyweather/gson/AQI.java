package com.fantasychong.fantasyweather.gson;


/**
 * Created by lenovo on 2017/12/27.
 */

public class AQI {
    public AQICity city;

    public class AQICity {
        public String aqi;
        public String pm25;
    }
}
