package com.fantasychong.fantasyweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.fantasychong.fantasyweather.gson.Weather;
import com.fantasychong.fantasyweather.util.JSONParseUtils;
import com.fantasychong.fantasyweather.util.OkHttpUtils;

import java.io.IOException;
import java.nio.file.WatchEvent;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        AlarmManager manager= (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour= 8* 60* 60* 1000;
        long triggerAtTime= SystemClock.elapsedRealtime()+ anHour;
        Intent i= new Intent(this, AutoUpdateService.class);
        PendingIntent pi= PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateBingPic() {
        final String requestBingBic= "http://guolin.tech/api/bing_pic";
        OkHttpUtils.sendOkHttpRequest(requestBingBic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic= response.body().string();
                SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
            }
        });
    }

    private void updateWeather() {
        SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString= pref.getString("weather", null);
        if (weatherString!= null){
            //有缓存直接解析天气数据
            Weather weather= JSONParseUtils.handleWeatherResponse(weatherString);
            final String weatherId= weather.basic.weatherId;
            String weatherUrl= "http://guolin.tech/api/weather?cityid="+ weatherId+ "&key=1cbe13d582144f95bf9b894af00cf34f";
            OkHttpUtils.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseData= response.body().string();
                    Weather weather= JSONParseUtils.handleWeatherResponse(responseData);
                    if (weather!= null&& "ok".equals(weather.status)){
                        SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather", responseData);
                        editor.apply();
                    }
                }
            });
        }
    }
}
