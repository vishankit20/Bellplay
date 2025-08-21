package com.example.bellplay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FavoritesFragment extends Fragment {

    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private ArrayList<Song> favoriteList;
    private TextView noFavoritesTextView;

    public FavoritesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        recyclerView = view.findViewById(R.id.favorites_recycler_view);
        noFavoritesTextView = view.findViewById(R.id.no_favorites_text);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        favoriteList = new ArrayList<>(FavoritesManager.getFavoriteSongs());
        if (favoriteList.isEmpty()) {
            noFavoritesTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noFavoritesTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            songAdapter = new SongAdapter(getContext(), favoriteList, SongAdapter.LIST_TYPE_FAVORITES);
            recyclerView.setAdapter(songAdapter);
        }
    }
}