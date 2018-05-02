package latest.player.music.musiclayer.Fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import latest.player.music.musiclayer.Adapter.SongAdapter;
import latest.player.music.musiclayer.Adapter.SongSearchAdapter;
import latest.player.music.musiclayer.MainActivity;
import latest.player.music.musiclayer.MediaStore.SongManager;
import latest.player.music.musiclayer.R;
import latest.player.music.musiclayer.utils.SongInfo;
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

/**
 * A simple {@link Fragment} subclass.
 */
public class FilteredSongs extends Fragment implements SearchView.OnQueryTextListener {

    ArrayList<SongInfo> filteredSongs = new ArrayList<>();
    ArrayList<SongInfo> _songsF;
    SongSearchAdapter searchAdapter;

    RecyclerView filteredRecyclerview;
    EditText searchEdt;
    SearchView sv;
    public int searchCunt=0;


    public FilteredSongs() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.filtered_layout_songs, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        _songsF = new SongManager().getMp3Songs(getActivity());
        filteredRecyclerview = view.findViewById(R.id.filtered_song_recyclerview);
        searchEdt = view.findViewById(R.id.filtered_song_edt);
        sv = view.findViewById(R.id.song_searchView);
        filteredSongs = _songsF;
        searchAdapter = new SongSearchAdapter(getContext(),filteredSongs);
        filteredRecyclerview.setAdapter(searchAdapter);

        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        DividerItemDecoration decoration = new DividerItemDecoration(filteredRecyclerview.getContext(),
                manager.getOrientation());
        //VerticalRecyclerViewFastScroller fastScroller = findViewById(R.id.fast_scroller);
        //fastScroller.setRecyclerView(songRecyclerView);

        filteredRecyclerview.setAdapter(searchAdapter);
        filteredRecyclerview.setLayoutManager(manager);
        filteredRecyclerview.addItemDecoration(decoration);
        searchAdapter.notifyDataSetChanged();

        sv.setOnQueryTextListener(this);

        searchAdapter.setOnItemClickListener(new SongSearchAdapter.onClickListener() {
            @Override
            public void onItemClick(LinearLayout tv, View v, SongInfo s, int position) {
                Toast.makeText(getContext(),filteredSongs.get(position).getSongName().toString(),Toast.LENGTH_LONG).show();
                long id = filteredSongs.get(position).getSong_id();
                ((MainActivity)getActivity()).setSongIdService(id);


            }
        });


    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        //searchAdapter.filter(query);
        searchAdapter.getFilter().filter(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        /*String queryOf = newText.toLowerCase();
        for (SongInfo info: _songsF){
            final String title = info.getSongName().toLowerCase();
            if (title.contains(queryOf)){
                filteredSongs.clear();
                filteredSongs.add(info);
                searchAdapter.filteredSongs(filteredSongs);
            }
        }*/
        //searchAdapter.filter(newText);
        searchAdapter.getFilter().filter(newText);
        return true;
    }
}
