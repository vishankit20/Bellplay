package com.example.bellplay;

public class YouTubeVideo {
    private final String videoId;
    private final String title;
    private final String thumbnailUrl;

    public YouTubeVideo(String videoId, String title, String thumbnailUrl) {
        this.videoId = videoId;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getTitle() {
        return title;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}