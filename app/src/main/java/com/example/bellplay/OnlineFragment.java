package com.example.bellplay;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OnlineFragment extends Fragment {

    private YouTubePlayerView youTubePlayerView;
    private YouTubePlayer youTubePlayer;
    private RecyclerView recyclerView;
    private VideoAdapter videoAdapter;
    private List<YouTubeVideo> videoList = new ArrayList<>();
    private RequestQueue requestQueue;

    public OnlineFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_online, container, false);

        requestQueue = Volley.newRequestQueue(requireContext());
        youTubePlayerView = view.findViewById(R.id.youtube_player_view);
        recyclerView = view.findViewById(R.id.videos_recycler_view);

        getLifecycle().addObserver(youTubePlayerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        videoAdapter = new VideoAdapter(getContext(), videoList, video -> {
            if (youTubePlayer != null) {
                youTubePlayerView.setVisibility(View.VISIBLE);
                youTubePlayer.loadVideo(video.getVideoId(), 0);
            }
        });
        recyclerView.setAdapter(videoAdapter);

        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer player) {
                youTubePlayer = player;
            }
        });

        return view;
    }

    public void searchYouTube(String query) {
        // Use the API key from BuildConfig
        String apiKey = BuildConfig.YOUTUBE_API_KEY;
        String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&q=" + query + "&type=video&maxResults=20&key=" + apiKey;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        videoList.clear();
                        JSONArray items = response.getJSONArray("items");
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            JSONObject id = item.getJSONObject("id");
                            String videoId = id.getString("videoId");
                            JSONObject snippet = item.getJSONObject("snippet");
                            String title = snippet.getString("title");
                            String thumbnailUrl = snippet.getJSONObject("thumbnails").getJSONObject("high").getString("url");
                            videoList.add(new YouTubeVideo(videoId, title, thumbnailUrl));
                        }
                        videoAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e("OnlineFragment", "Volley Error: ", error);
                    Toast.makeText(getContext(), "Error fetching videos", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(request);
    }
}
