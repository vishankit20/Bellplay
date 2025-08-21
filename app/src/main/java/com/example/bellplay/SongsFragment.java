package com.example.bellplay;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class SongsFragment extends Fragment {

    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private ArrayList<Song> fullSongList; // Keep a copy of the full list

    public SongsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_songs, container, false);
        recyclerView = view.findViewById(R.id.songs_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (MusicLibraryManager.getSongs().isEmpty()) {
            loadSongs();
        }
        // Store the full list and pass a copy to the adapter
        fullSongList = new ArrayList<>(MusicLibraryManager.getSongs());
        songAdapter = new SongAdapter(getContext(), new ArrayList<>(fullSongList), SongAdapter.LIST_TYPE_ALL_SONGS);
        recyclerView.setAdapter(songAdapter);
    }

    // --- NEW PUBLIC METHOD TO BE CALLED FROM MAINACTIVITY ---
    public void filterSongs(String text) {
        ArrayList<Song> filteredList = new ArrayList<>();
        for (Song song : fullSongList) {
            if (song.getTitle().toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))) {
                filteredList.add(song);
            }
        }
        if (songAdapter != null) {
            songAdapter.filterList(filteredList);
        }
    }

    private void loadSongs() {
        // ... (The rest of this method remains the same)
        ArrayList<Song> localSongList = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        int idColumn = -1;
        int titleColumn = -1;
        int artistColumn = -1;
        int dataColumn = -1;
        int durationColumn = -1;

        Cursor cursor = getContext().getContentResolver().query(uri, projection, selection, null, null);

        if (cursor != null) {
            idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                String title = cursor.getString(titleColumn);
                String artist = cursor.getString(artistColumn);
                String path = cursor.getString(dataColumn);
                long duration = cursor.getLong(durationColumn);

                if (artist == null || artist.equals("<unknown>")) {
                    artist = "Unknown Artist";
                }

                if (path != null && !path.equals("") && title != null) {
                    Song song = new Song(id, title, artist, path, duration);
                    localSongList.add(song);
                }
            }
            cursor.close();
        }
        MusicLibraryManager.setSongs(localSongList);
    }
}
