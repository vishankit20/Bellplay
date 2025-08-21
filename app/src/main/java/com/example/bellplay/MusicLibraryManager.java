// --- COPY THIS CODE ---
package com.example.bellplay;

import java.util.ArrayList;
import java.util.List;

public class MusicLibraryManager {

    private static final List<Song> songList = new ArrayList<>();

    public static List<Song> getSongs() {
        return songList;
    }

    public static void setSongs(List<Song> songs) {
        songList.clear();
        songList.addAll(songs);
    }

    public static Song getSong(int position) {
        if (position >= 0 && position < songList.size()) {
            return songList.get(position);
        }
        return null;
    }
}