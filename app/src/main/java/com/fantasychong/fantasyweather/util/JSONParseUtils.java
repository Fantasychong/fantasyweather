package com.fantasychong.fantasyweather.util;

import android.text.TextUtils;
import android.webkit.WebView;

import com.fantasychong.fantasyweather.db.City;
import com.fantasychong.fantasyweather.db.County;
import com.fantasychong.fantasyweather.db.Province;
import com.fantasychong.fantasyweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

/**
 * Created by fantasychong on 2017/12/27.
 * json解析封装工具类
 */

public class JSONParseUtils {

    /**
     * 解析和处理服务器返回的省级数据
     */
    public static boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvinces= new JSONArray(response);
                for (int i= 0; i< allProvinces.length(); i++){
                    JSONObject provinceObject= allProvinces.getJSONObject(i);
                    Province province= new Province();
                    province.setProvinceCode(provinceObject.optInt("id"));
                    province.setProvinceName(provinceObject.optString("name"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据
     */
    public static boolean handleCityResponse(String response, int provinceId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCities= new JSONArray(response);
                for(int i= 0; i< allCities.length(); i++){
                    JSONObject cityObject= allCities.getJSONObject(i);
                    City city= new City();
                    city.setCityName(cityObject.optString("name"));
                    city.setCityCode(cityObject.optInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的区级数据
     */
    public static boolean handleCountyResponse(String response, int cityId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCountits= new JSONArray(response);
                for(int i= 0; i< allCountits.length(); i++){
                    County county= new County();
                    JSONObject countyObject= allCountits.getJSONObject(i);
                    county.setCountyName(countyObject.optString("name"));
                    county.setWeatherId(countyObject.optString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 将返回的JSON数据解析成Weather实体类
     */
    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject= new JSONObject(response);
            JSONArray jsonArray= jsonObject.optJSONArray("HeWeather");
            String weatherContent= jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }






}
