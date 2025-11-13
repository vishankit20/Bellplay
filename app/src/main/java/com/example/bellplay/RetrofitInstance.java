package com.example.bellplay; // Make sure this matches your package

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance {

    private static final String BASE_URL = "https://api.jamendo.com/v3.0/";

    // We use 'volatile' so that multiple threads can handle the variable correctly
    private static volatile JamendoApiService apiService;

    // Private constructor to prevent anyone from creating an instance
    private RetrofitInstance() {}

    public static JamendoApiService getApiService() {
        // Double-check locking for a thread-safe singleton
        if (apiService == null) {
            synchronized (RetrofitInstance.class) {
                if (apiService == null) {

                    // Create a logger to see request/response in Logcat
                    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                    // Build the OkHttp Client
                    OkHttpClient httpClient = new OkHttpClient.Builder()
                            .addInterceptor(loggingInterceptor)
                            .build();

                    // Build Retrofit
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(httpClient)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    // Create the API service
                    apiService = retrofit.create(JamendoApiService.class);
                }
            }
        }
        return apiService;
    }
}