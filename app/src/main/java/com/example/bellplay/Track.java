package com.example.bellplay; // Make sure this matches your package

import com.google.gson.annotations.SerializedName;

public class Track {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("artist_name")
    private String artistName;

    @SerializedName("album_image")
    private String albumImage;

    @SerializedName("audio")
    private String audioUrl;

    // --- Getters ---
    // Gson will use these to set the private fields

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getAlbumImage() {
        return albumImage;
    }

    public String getAudioUrl() {
        return audioUrl;
    }
}