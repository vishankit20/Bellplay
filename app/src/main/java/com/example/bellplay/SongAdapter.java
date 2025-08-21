package com.example.bellplay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    public static final String LIST_TYPE_ALL_SONGS = "ALL_SONGS";
    public static final String LIST_TYPE_FAVORITES = "FAVORITES";

    private final Context context;
    private ArrayList<Song> songList; // Make it non-final to allow filtering
    private final String listType;

    public SongAdapter(Context context, ArrayList<Song> songList, String listType) {
        this.context = context;
        this.songList = songList;
        this.listType = listType;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song currentSong = songList.get(position);
        holder.title.setText(currentSong.getTitle());
        holder.artist.setText(currentSong.getArtist());
        holder.albumArt.setImageResource(R.drawable.ic_music_note);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlayerActivity.class);
            intent.putExtra("SONG_POSITION", position);
            intent.putExtra("LIST_TYPE", listType);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    // --- NEW METHOD FOR FILTERING ---
    @SuppressLint("NotifyDataSetChanged")
    public void filterList(ArrayList<Song> filteredList) {
        songList = filteredList;
        notifyDataSetChanged(); // Refresh the list with filtered results
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist;
        ImageView albumArt;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.song_title);
            artist = itemView.findViewById(R.id.song_artist);
            albumArt = itemView.findViewById(R.id.song_album_art);
        }
    }
}
