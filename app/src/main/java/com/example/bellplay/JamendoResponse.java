package com.example.bellplay; // Make sure this matches your package

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class JamendoResponse {

    @SerializedName("results")
    private List<Track> tracks;

    // --- Getter ---

    public List<Track> getTracks() {
        return tracks;
    }
}