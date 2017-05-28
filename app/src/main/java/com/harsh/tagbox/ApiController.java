package com.harsh.tagbox;


import android.location.Location;
import android.os.AsyncTask;

import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by Anjan on 5/27/2017.
 */
public class ApiController {
    public static final MediaType TYPE_TEXT = MediaType.parse("text/plain; charset=utf-8");

    public static Call login(String userName, String password, Callback responseCallback, Location loc) {

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.connectTimeout(30, TimeUnit.SECONDS);
        clientBuilder.readTimeout(30, TimeUnit.SECONDS);
        clientBuilder.writeTimeout(30, TimeUnit.SECONDS);

        RequestBody requestBody = RequestBody.create(TYPE_TEXT, getLoginReqBody(userName, password, loc));

        Request request = new Request.Builder()
                .post(requestBody)
                .url(ApiURL.LOGIN)
                .build();
        Call call = clientBuilder.build().newCall(request);
        executeAsync(call, responseCallback);
        return call;
    }

    private static void executeAsync(Call call, Callback callback) {
        AsyncApiTask apiTask = new AsyncApiTask(call, callback);
        apiTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class AsyncApiTask extends AsyncTask<Void, Void, Void> {

        private final Call call;
        private final Callback callback;

        AsyncApiTask(Call call, Callback callback) {
            this.call = call;
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            call.enqueue(callback);
            return null;
        }
    }

    private static String getLoginReqBody(String userName, String password, Location loc) {
        return "{"
                + "\"user\" : \"" + userName + "\","
                + "\"pwd\" : \"" + password + "\","
                + "\"loc\" : \"" + loc.getLatitude() + ", " + loc.getLongitude() + "\","
                + "\"timestamp\" : \"" + String.valueOf(System.currentTimeMillis()) + "\"" +
                "}";
    }

    public interface ApiURL {
        String LOGIN = "http://104.215.248.40:8080/restservice/v1/d2c/login";
    }
}

