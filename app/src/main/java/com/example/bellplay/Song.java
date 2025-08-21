package com.example.bellplay;

import java.io.Serializable;

public class Song implements Serializable {

    private long id;
    private String title;
    private String artist;
    private String path;
    private long duration;
    private boolean isFavorite;

    public Song(long id, String title, String artist, String path, long duration) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.path = path;
        this.duration = duration;
        this.isFavorite = false; // Default to not favorite
    }

    // Getters
    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getPath() {
        return path;
    }

    public long getDuration() {
        return duration;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    // Setter for favorite status
    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}
