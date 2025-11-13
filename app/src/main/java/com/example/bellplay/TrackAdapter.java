package com.example.bellplay; // Make sure this matches your package

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // Import Glide
import java.util.ArrayList;
import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    private List<Track> tracks;
    private Context context;
    private OnTrackClickListener listener;

    // Interface for handling clicks
    public interface OnTrackClickListener {
        void onTrackClick(Track track);
    }

    public TrackAdapter(Context context, OnTrackClickListener listener) {
        this.context = context;
        this.tracks = new ArrayList<>(); // Start with an empty list
        this.listener = listener;
    }

    // Called when RecyclerView needs a new "row"
    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_track, parent, false);
        return new TrackViewHolder(view);
    }

    // Called to display the data at a specific position
    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        Track currentTrack = tracks.get(position);

        // Set the text
        holder.titleTextView.setText(currentTrack.getName());
        holder.artistTextView.setText(currentTrack.getArtistName());

        // Set the image using Glide
        Glide.with(context)
                .load(currentTrack.getAlbumImage()) // The URL of the image
                .placeholder(R.mipmap.ic_launcher) // A default placeholder
                .error(R.mipmap.ic_launcher)       // An error placeholder
                .into(holder.albumArtImageView);

        // Set the click listener for the whole row
        holder.itemView.setOnClickListener(v -> {
            listener.onTrackClick(currentTrack);
        });
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    // A method to update the list of tracks and refresh the RecyclerView
    public void updateTracks(List<Track> newTracks) {
        tracks.clear();
        tracks.addAll(newTracks);
        notifyDataSetChanged(); // Tell the adapter to refresh
    }

    // This "ViewHolder" class holds the views for a single row
    public static class TrackViewHolder extends RecyclerView.ViewHolder {
        ImageView albumArtImageView;
        TextView titleTextView;
        TextView artistTextView;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            albumArtImageView = itemView.findViewById(R.id.albumArtImageView);
            titleTextView = itemView.findViewById(R.id.songTitleTextView);
            artistTextView = itemView.findViewById(R.id.artistNameTextView);
        }
    }
}