package com.example.bellplay;

import android.content.ContentUris;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = "PlayerActivity";

    private TextView titleTextView, artistTextView;
    private SeekBar seekBar;
    private ImageButton playPauseButton, nextButton, previousButton, favoriteButton;

    private List<Song> songList;
    private int currentSongPosition;
    private MediaPlayer mediaPlayer;
    private final Handler handler = new Handler();
    private boolean isFavoritesList = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        initializeViews();

        String listType = getIntent().getStringExtra("LIST_TYPE");
        if (SongAdapter.LIST_TYPE_FAVORITES.equals(listType)) {
            isFavoritesList = true;
            songList = new ArrayList<>(FavoritesManager.getFavoriteSongs());
        } else {
            isFavoritesList = false;
            songList = MusicLibraryManager.getSongs();
        }

        currentSongPosition = getIntent().getIntExtra("SONG_POSITION", -1);

        if (songList == null || songList.isEmpty() || currentSongPosition == -1) {
            Log.e(TAG, "onCreate: Invalid song list or position.");
            Toast.makeText(this, "Could not play song.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupListeners();
        playNewSong();
    }

    private void initializeViews() {
        titleTextView = findViewById(R.id.song_title_text_view);
        artistTextView = findViewById(R.id.song_artist_text_view);
        seekBar = findViewById(R.id.seek_bar);
        playPauseButton = findViewById(R.id.play_pause_button);
        nextButton = findViewById(R.id.next_button);
        previousButton = findViewById(R.id.previous_button);
        favoriteButton = findViewById(R.id.favorite_button);
    }

    private void setupListeners() {
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        nextButton.setOnClickListener(v -> playNextSong());
        previousButton.setOnClickListener(v -> playPreviousSong());
        favoriteButton.setOnClickListener(v -> toggleFavorite());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void playNewSong() {
        if (!isPositionValid()) {
            Log.e(TAG, "playNewSong: Position is invalid, finishing activity.");
            Toast.makeText(this, "Playlist has changed.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        updateUI();

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        Song currentSong = songList.get(currentSongPosition);
        mediaPlayer = new MediaPlayer();
        try {
            // --- THIS IS THE CRITICAL FIX ---
            // We create a proper Uri using the song's ID, which is the correct way.
            Uri trackUri = ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    currentSong.getId()
            );
            mediaPlayer.setDataSource(this, trackUri);
            // --- END OF FIX ---

            mediaPlayer.prepare();
            mediaPlayer.start();
            playPauseButton.setImageResource(R.drawable.ic_pause);
            seekBar.setMax(mediaPlayer.getDuration());
            updateSeekBar();
        } catch (IOException e) {
            Log.e(TAG, "playNewSong: Could not play song " + currentSong.getPath(), e);
            Toast.makeText(this, "Error playing song", Toast.LENGTH_SHORT).show();
        }

        mediaPlayer.setOnCompletionListener(mp -> playNextSong());
    }

    private void updateUI() {
        if (!isPositionValid()) return;
        Song currentSong = songList.get(currentSongPosition);
        titleTextView.setText(currentSong.getTitle());
        artistTextView.setText(currentSong.getArtist());
        updateFavoriteButtonIcon();
    }

    private void togglePlayPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playPauseButton.setImageResource(R.drawable.ic_play);
            } else {
                mediaPlayer.start();
                playPauseButton.setImageResource(R.drawable.ic_pause);
                updateSeekBar();
            }
        }
    }

    private void playNextSong() {
        if (songList.isEmpty()) return;
        currentSongPosition = (currentSongPosition + 1) % songList.size();
        playNewSong();
    }

    private void playPreviousSong() {
        if (songList.isEmpty()) return;
        currentSongPosition = (currentSongPosition == 0) ? songList.size() - 1 : currentSongPosition - 1;
        playNewSong();
    }

    private void toggleFavorite() {
        if (!isPositionValid()) return;
        Song currentSong = songList.get(currentSongPosition);
        boolean isCurrentlyFavorite = FavoritesManager.isFavorite(currentSong);

        if (isCurrentlyFavorite) {
            FavoritesManager.removeFavorite(currentSong);
            Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show();
            if (isFavoritesList) {
                songList.remove(currentSongPosition);
                if (songList.isEmpty()) {
                    finish();
                    return;
                }
                if (currentSongPosition >= songList.size()) {
                    currentSongPosition = 0;
                }
                playNewSong();
            } else {
                updateFavoriteButtonIcon();
            }
        } else {
            FavoritesManager.addFavorite(currentSong);
            Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show();
            updateFavoriteButtonIcon();
        }
    }

    private void updateFavoriteButtonIcon() {
        if (!isPositionValid()) return;
        Song currentSong = songList.get(currentSongPosition);
        if (FavoritesManager.isFavorite(currentSong)) {
            favoriteButton.setImageResource(R.drawable.ic_favorite);
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    private boolean isPositionValid() {
        return songList != null && !songList.isEmpty() && currentSongPosition >= 0 && currentSongPosition < songList.size();
    }

    private final Runnable updater = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(this, 1000);
            }
        }
    };

    private void updateSeekBar() {
        handler.post(updater);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(updater);
    }
}
