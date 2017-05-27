package com.harsh.tagbox;


import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Anjan on 5/27/2017.
 */
public class ApiController {

    public static Call login(String userName, String password, Callback responseCallback){

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.connectTimeout(30, TimeUnit.SECONDS);
        clientBuilder.readTimeout(30, TimeUnit.SECONDS);
        clientBuilder.writeTimeout(30, TimeUnit.SECONDS);

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("user", userName);
        formBuilder.add("password", password);
        formBuilder.add("loc","test");
        formBuilder.add("timestamp", String.valueOf(System.currentTimeMillis()));

        Request request = new Request.Builder().post(formBuilder.build()).build();
        Call call = clientBuilder.build().newCall(request);
        call.enqueue(responseCallback);
        return call;
    }


}
