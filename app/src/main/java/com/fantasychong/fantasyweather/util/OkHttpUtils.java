package com.fantasychong.fantasyweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by fantasychong on 2017/12/27.
 * Okhttp网络框架封装工具类
 */

public class OkHttpUtils {
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback){
        OkHttpClient client= new OkHttpClient();
        Request request= new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }

}
