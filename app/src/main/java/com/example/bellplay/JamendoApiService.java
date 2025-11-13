package com.example.bellplay; // Make sure this matches your package

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface JamendoApiService {

    @GET("tracks")
    Call<JamendoResponse> getPopularTracks(
            @Query("client_id") String clientId,
            @Query("format") String format,
            @Query("limit") int limit,
            @Query("order") String order
    );

    @GET("tracks")
    Call<JamendoResponse> searchTracks(
            @Query("client_id") String clientId,
            @Query("format") String format,
            @Query("limit") int limit,
            @Query("search") String searchTerm // This is the new part
    );

    // We can add other calls here later, like for searching
    // @GET("tracks")
    // Call<JamendoResponse> searchTracks(...);
}