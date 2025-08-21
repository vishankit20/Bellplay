package com.example.bellplay;

import java.util.ArrayList;
import java.util.List;

public class FavoritesManager {

    private static final List<Song> favoriteSongs = new ArrayList<>();

    public static List<Song> getFavoriteSongs() {
        return favoriteSongs;
    }

    public static void addFavorite(Song song) {
        if (!isFavorite(song)) {
            favoriteSongs.add(song);
        }
    }

    public static void removeFavorite(Song song) {
        favoriteSongs.removeIf(s -> s.getId() == song.getId());
    }

    public static boolean isFavorite(Song song) {
        for (Song favSong : favoriteSongs) {
            if (favSong.getId() == song.getId()) {
                return true;
            }
        }
        return false;
    }
}
