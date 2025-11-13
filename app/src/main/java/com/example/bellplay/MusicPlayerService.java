package com.example.bellplay; // Make sure this matches your package

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

import java.util.HashMap;


public class MusicPlayerService extends MediaSessionService {

    private static final String NOTIFICATION_CHANNEL_ID = "bellplay_channel";
    private static final int NOTIFICATION_ID = 101;

    private MediaSession mediaSession;
    private ExoPlayer player;

    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_TOGGLE = "ACTION_TOGGLE";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    private String currentSongTitle = "Unknown Title";
    private String currentArtist = "Unknown Artist";
    private Bitmap currentAlbumArt = null;

    // **FIX 2: Add this annotation to opt-in to the unstable API**
    @androidx.annotation.OptIn(markerClass = UnstableApi.class)
    @Override
    public void onCreate() {
        super.onCreate();

        // Create the ExoPlayer instance
        player = new ExoPlayer.Builder(this).build();

        // Create the MediaSession, linking it to the player
        mediaSession = new MediaSession.Builder(this, player).build();

        // Create a notification channel (required for Android 8.0+)
        createNotificationChannel();

        // Setup the foreground notification directly (MediaNotificationProvider is not part of Media3 API)
        Notification notification = buildSimpleNotification();
        startForeground(NOTIFICATION_ID, notification);

        // Add a listener to stop the service when playback stops
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                // **FIX 3: Changed STATE_STOPPED to STATE_IDLE**
                if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED) {
                    stopSelf(); // Stop the service when music ends
                }
            }
        });
    }

    private Notification buildSimpleNotification() {
        boolean isPlaying = player != null && player.isPlaying();
        int playPauseIcon = isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;
        String playPauseAction = isPlaying ? ACTION_PAUSE : ACTION_PLAY;

        // Load album art if available, else fallback to default
        Bitmap largeIcon = currentAlbumArt != null
                ? currentAlbumArt
                : BitmapFactory.decodeResource(getResources(), R.drawable.ic_music_note);

        // Play/Pause Intent
        Intent toggleIntent = new Intent(this, MusicPlayerService.class);
        toggleIntent.setAction(playPauseAction);
        PendingIntent togglePendingIntent = PendingIntent.getService(this, 0, toggleIntent, PendingIntent.FLAG_IMMUTABLE);

        // Stop Intent
        Intent stopIntent = new Intent(this, MusicPlayerService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE);

        // Next Intent
        Intent nextIntent = new Intent(this, MusicPlayerService.class);
        nextIntent.setAction(ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 2, nextIntent, PendingIntent.FLAG_IMMUTABLE);

        // Previous Intent
        Intent previousIntent = new Intent(this, MusicPlayerService.class);
        previousIntent.setAction(ACTION_PREVIOUS);
        PendingIntent previousPendingIntent = PendingIntent.getService(this, 3, previousIntent, PendingIntent.FLAG_IMMUTABLE);

        // Optional: Song details (you can replace with dynamic metadata)
        String songTitle = "Sample Song";
        String artistName = "Unknown Artist";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(currentSongTitle)
                .setContentText(currentArtist)
                .setLargeIcon(largeIcon)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .addAction(android.R.drawable.ic_media_previous, "Previous", previousPendingIntent)
                .addAction(playPauseIcon, isPlaying ? "Pause" : "Play", togglePendingIntent)
                .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
                .setStyle(new MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(mediaSession.getSessionCompatToken()))
                .setOngoing(isPlaying);

        return builder.build();
    }

    @Nullable
    @Override
    public MediaSession onGetSession(@NonNull MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();

            switch (action) {
                case ACTION_PLAY:
                    if (intent.hasExtra("TRACK_URL")) {
                        String trackUrl = intent.getStringExtra("TRACK_URL");
                        String songTitle = intent.getStringExtra("TRACK_TITLE");
                        String artistName = intent.getStringExtra("TRACK_ARTIST");

                        currentSongTitle = songTitle != null ? songTitle : "Unknown Title";
                        currentArtist = artistName != null ? artistName : "Unknown Artist";

                        // 🎵 Try to extract album art from the song
                        try {
                            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                            mmr.setDataSource(trackUrl, new HashMap<>()); // use the version with headers param for URL safety
                            byte[] artBytes = mmr.getEmbeddedPicture();
                            if (artBytes != null) {
                                currentAlbumArt = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
                            } else {
                                currentAlbumArt = null;
                            }
                            mmr.release();
                        } catch (Exception e) {
                            e.printStackTrace();
                            currentAlbumArt = null;
                        }

                        MediaItem mediaItem = MediaItem.fromUri(trackUrl);
                        player.setMediaItem(mediaItem);
                        player.prepare();
                        player.play();
                    } else {
                        player.play();
                    }
                    break;

                case ACTION_PAUSE:
                    player.pause();
                    break;

                case ACTION_TOGGLE:
                    if (player.isPlaying()) {
                        player.pause();
                    } else {
                        player.play();
                    }
                    break;

                case ACTION_STOP:
                    stopForeground(true);
                    stopSelf();
                    break;
                case ACTION_NEXT:
                    // Implement logic for next song
                    // (for now, just show a Toast or Log)
                    Log.d("BellPlay", "Next button pressed");
                    break;

                case ACTION_PREVIOUS:
                    // Implement logic for previous song
                    Log.d("BellPlay", "Previous button pressed");
                    break;
            }

            // Refresh the notification to update the play/pause icon
            Notification notification = buildSimpleNotification();
            startForeground(NOTIFICATION_ID, notification);
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mediaSession != null) {
            mediaSession.release();
        }
        if (player != null) {
            player.release();
        }
        super.onDestroy();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "BellPlay Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Notification for BellPlay background music");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}