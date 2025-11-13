package com.example.bellplay; // Make sure this matches your package

// Import statements you will need

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OnlineFragment extends Fragment implements TrackAdapter.OnTrackClickListener {

    private static final String TAG = "OnlineFragment"; // Changed tag for clarity

    // Use the test ID for now. Replace with your real key when you get it.
    private static final String JAMENDO_CLIENT_ID = "bfb65408";
    private RecyclerView recyclerView;
    private TrackAdapter trackAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // This is where you inflate your XML layout file for this fragment
        // For example:
        View view = inflater.inflate(R.layout.fragment_online, container, false);
        recyclerView = view.findViewById(R.id.tracksRecyclerView);
        return view;

        // Make sure to replace 'R.layout.fragment_online' with your actual layout file
//        return inflater.inflate(R.layout.fragment_online, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- SETUP RECYCLERVIEW ---
        setupRecyclerView();

        // Fetch tracks as before
        fetchPopularTracks();
    }

    private void setupRecyclerView() {
        // 'this' refers to the Fragment itself, because we implemented the listener
        trackAdapter = new TrackAdapter(getContext(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(trackAdapter);
    }

    /**
     * This is the main function for calling the Jamendo API.
     */
    private void fetchPopularTracks() {
        Log.d(TAG, "Fetching popular tracks from Jamendo...");

        // 1. Get the API service instance
        JamendoApiService apiService = RetrofitInstance.getApiService();

        // 2. Create the API call
        Call<JamendoResponse> call = apiService.getPopularTracks(
                JAMENDO_CLIENT_ID,
                "json",
                50,
                "popularity_week"
        );

        // 3. Execute the call asynchronously (in the background)
        call.enqueue(new Callback<JamendoResponse>() {

            @Override
            public void onResponse(@NonNull Call<JamendoResponse> call, @NonNull Response<JamendoResponse> response) {
                // IMPORTANT: Check if the Fragment is still attached to the activity
                if (!isAdded()) {
                    return; // Fragment is gone, do nothing.
                }

                if (response.isSuccessful() && response.body() != null) {

                    List<Track> tracks = response.body().getTracks();

                    // --- IT WORKS! ---
                    Log.i(TAG, "Successfully fetched " + tracks.size() + " tracks.");

                    trackAdapter.updateTracks(tracks);
                    // *** NEXT STEP: Pass this 'tracks' list to your RecyclerView adapter ***
                    // We'll write this method next
                    // setupRecyclerView(tracks);

                } else {
                    Log.e(TAG, "API Error Response: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<JamendoResponse> call, @NonNull Throwable t) {
                // IMPORTANT: Check if the Fragment is still attached
                if (!isAdded()) {
                    return; // Fragment is gone, do nothing.
                }
                Log.e(TAG, "Network Failure: " + t.getMessage());
            }
        });
    }

    /**
     * This is the NEW public search method that MainActivity will call.
     */
    public void searchJamendoTracks(String query) {
        Log.d(TAG, "Searching Jamendo for: " + query);

        // 1. Get the API service
        JamendoApiService apiService = RetrofitInstance.getApiService();

        // 2. Create the search call
        Call<JamendoResponse> call = apiService.searchTracks(
                JAMENDO_CLIENT_ID,
                "json",
                50,     // limit
                query   // the search term
        );

        // 3. Execute the call
        call.enqueue(new Callback<JamendoResponse>() {
            @Override
            public void onResponse(@NonNull Call<JamendoResponse> call, @NonNull Response<JamendoResponse> response) {
                if (!isAdded()) return; // Safety check

                if (response.isSuccessful() && response.body() != null) {
                    List<Track> tracks = response.body().getTracks();
                    Log.i(TAG, "Search returned " + tracks.size() + " tracks.");
                    trackAdapter.updateTracks(tracks);

                    // *** NEXT STEP: Update your RecyclerView adapter with these new results ***
                    // We'll need to pass this 'tracks' list to the adapter
                    // adapter.updateList(tracks);

                } else {
                    Log.e(TAG, "Search API Error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<JamendoResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return; // Safety check
                Log.e(TAG, "Search Network Failure: " + t.getMessage());
            }
        });
    }

    @Override
    public void onTrackClick(Track track) {
        // This method is called when a user taps on a song
        Log.i(TAG, "User clicked on: " + track.getName());
        Log.i(TAG, "Audio URL: " + track.getAudioUrl());
        // --- THIS IS THE NEW CODE ---
        // Create an Intent to start our MusicPlayerService
        Intent serviceIntent = new Intent(getContext(), MusicPlayerService.class);

        // Put the audio URL into the Intent
        serviceIntent.putExtra("TRACK_URL", track.getAudioUrl());
        serviceIntent.setAction("ACTION_PLAY"); // Add an action to tell the service what to do

        // Start the service
        // Use startForegroundService for modern Android versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getContext().startForegroundService(serviceIntent);
        } else {
            getContext().startService(serviceIntent);
        }

        // --- NEXT STEP will be to play this URL ---
        // playMusic(track.getAudioUrl());
    }

    // We will create this method in the next step
    // private void setupRecyclerView(List<Track> tracks) {
    //    ...
    // }
}