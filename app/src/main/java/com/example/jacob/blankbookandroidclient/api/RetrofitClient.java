package com.example.jacob.blankbookandroidclient.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static RetrofitClient instance;
    private final BlankBookAPI blankBookAPI;

    private RetrofitClient() {
        Gson gson = new GsonBuilder().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://prod.blankbook.ca")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        blankBookAPI = retrofit.create(BlankBookAPI.class);
    }

    public static RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public BlankBookAPI getBlankBookAPI() {
        return blankBookAPI;
    }
}
