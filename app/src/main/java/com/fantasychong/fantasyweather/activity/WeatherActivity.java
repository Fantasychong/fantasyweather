package com.fantasychong.fantasyweather.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fantasychong.fantasyweather.R;
import com.fantasychong.fantasyweather.gson.Forecast;
import com.fantasychong.fantasyweather.gson.Weather;
import com.fantasychong.fantasyweather.service.AutoUpdateService;
import com.fantasychong.fantasyweather.util.JSONParseUtils;
import com.fantasychong.fantasyweather.util.OkHttpUtils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by lenovo on 2017/12/28.
 */

public class WeatherActivity extends AppCompatActivity{
    private ScrollView sv;
    private TextView nameTitle;
    private TextView updateTimeTitle;
    private TextView degreeTitle;
    private TextView weatherTitle;
    private LinearLayout ll;
    private TextView aqiTitle;
    private TextView pm25Title;
    private TextView comfortTitle;
    private TextView carWashTitle;
    private TextView sportTitle;
    private TextView itemTimeTitle;
    private TextView itemWeatherTitle;
    private TextView itemMaxTitle;
    private TextView itemMinTitle;
    private ImageView iv;
    public SwipeRefreshLayout sl;
    private String mWeatherId;
    public RelativeLayout rl;
    public DrawerLayout dl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT>= 21){
            View decorView= getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        sv= findViewById(R.id.weather_sv);
        nameTitle= findViewById(R.id.title_name);
        updateTimeTitle= findViewById(R.id.title_updateTime);
        degreeTitle= findViewById(R.id.now_degree);
        iv= findViewById(R.id.weather_iv);
        weatherTitle= findViewById(R.id.now_weather);
        ll= findViewById(R.id.forecart_ll);
        aqiTitle= findViewById(R.id.aqi_aqi);
        pm25Title= findViewById(R.id.aqi_pm25);
        comfortTitle= findViewById(R.id.sugg_comfort);
        carWashTitle= findViewById(R.id.sugg_carWash);
        sportTitle= findViewById(R.id.sugg_sport);
        sl= findViewById(R.id.weather_sl);
        sl.setColorSchemeResources(R.color.colorPrimary);
        dl= findViewById(R.id.weather_dl);

        rl= findViewById(R.id.title_rl);
        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dl.openDrawer(GravityCompat.START);
            }
        });
        SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString= pref.getString("weather", null);
        if (weatherString!= null){
            //有缓存直接解析天气数据
            Weather weather= JSONParseUtils.handleWeatherResponse(weatherString);
            mWeatherId= weather.basic.weatherId;
            showWeatherInfo(weather);
        }else {
            //无缓存直接请求网络数据
            mWeatherId= getIntent().getStringExtra("weather_id");
            sv.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }

        sl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
        String bingPic= pref.getString("bing_pic", null);
        if (!TextUtils.isEmpty(bingPic)){
            Glide.with(this).load(bingPic).into(iv);
        }else {
            loadPic();
        }
    }

    private void loadPic() {
        OkHttpUtils.sendOkHttpRequest("http://guolin.tech/api/bing_pic", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "背景图片请求失败，请检查您的网络", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData= response.body().string();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(getApplicationContext()).load(responseData).into(iv);
                    }
                });
            }
        });
    }

    public void requestWeather(String weatherId) {
        String weatherUrl= "http://guolin.tech/api/weather?cityid="+ weatherId+ "&key=1cbe13d582144f95bf9b894af00cf34f";
        OkHttpUtils.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "请求失败，请检查您的网络", Toast.LENGTH_SHORT).show();
                        sl.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData= response.body().string();
                final Weather weather= JSONParseUtils.handleWeatherResponse(responseData);
                SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", responseData);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "请求成功", Toast.LENGTH_SHORT).show();
                        if (weather!= null&& "ok".equals(weather.status)){
                            SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseData);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(getApplicationContext(), "请求失败", Toast.LENGTH_SHORT).show();
                        }
                        sl.setRefreshing(false);
                    }
                });

            }
        });
        loadPic();
    }

    private void showWeatherInfo(Weather weather) {
        if (weather!= null&& "ok".equals(weather.status)){
            String cityName= weather.basic.cityName;
            String updateTime= weather.basic.update.updateTime.split(" ")[1];
            String degree= weather.now.temperature+ "℃";
            String weatherInfo= weather.now.more.info;
            nameTitle.setText(cityName);
            updateTimeTitle.setText(updateTime);
            degreeTitle.setText(degree);
            weatherTitle.setText(weatherInfo);
            ll.removeAllViews();
            for (Forecast forecast: weather.forecastList){
                View view= LayoutInflater.from(this).inflate(R.layout.forecast_item, ll, false);
                itemTimeTitle= view.findViewById(R.id.item_time);
                itemWeatherTitle= view.findViewById(R.id.item_weather);
                itemMaxTitle= view.findViewById(R.id.item_maxDegree);
                itemMinTitle= view.findViewById(R.id.item_minDegree);
                itemTimeTitle.setText(forecast.date);
                itemWeatherTitle.setText(forecast.more.info);
                itemMaxTitle.setText(forecast.temperature.max);
                itemMinTitle.setText(forecast.temperature.min);
                ll.addView(view);
            }
            if (weather.aqi!= null){
                aqiTitle.setText(weather.aqi.city.aqi);
                pm25Title.setText(weather.aqi.city.pm25);
            }
            String comfort= "舒适度："+ weather.suggestion.comfort.info;
            String carWash= "洗车指数："+ weather.suggestion.carWash.info;
            String sport= "运动建议："+ weather.suggestion.sport.info;
            comfortTitle.setText(comfort);
            carWashTitle.setText(carWash);
            sportTitle.setText(sport);
            sv.setVisibility(View.VISIBLE);
            Intent intent= new Intent(this, AutoUpdateService.class);
            startService(intent);
        }else {
            Toast.makeText(getApplicationContext(), "请求天气信息失败", Toast.LENGTH_SHORT).show();
        }

    }
}
