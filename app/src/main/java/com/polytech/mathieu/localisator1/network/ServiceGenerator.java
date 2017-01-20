package com.polytech.mathieu.localisator1.network;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by shafiq on 03/01/2017.
 */

public class ServiceGenerator {

    public static String apiBaseUrl = "http://192.168.1.23:4567";

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(apiBaseUrl)
                    .addConverterFactory(GsonConverterFactory.create());

    public static <S> S createService(Class<S> serviceClass) {
        Retrofit retrofit = builder.client(httpClient.build()).build();
        return retrofit.create(serviceClass);
    }

    public static void changeApiBaseUrl(String newApiBaseUrl) {
        String fullUrl = "http://" + newApiBaseUrl;
        apiBaseUrl = fullUrl;

        builder = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(apiBaseUrl);
    }
}
