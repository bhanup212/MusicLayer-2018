package latest.player.music.musiclayer.Fragments;


import android.app.AlarmManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import latest.player.music.musiclayer.Adapter.AlbumAdapter;
import latest.player.music.musiclayer.MediaStore.AlbumManager;
import latest.player.music.musiclayer.R;
import latest.player.music.musiclayer.utils.AlbumInfo;
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumList extends Fragment {

    private ArrayList<AlbumInfo> albumList = new ArrayList<>();
    RecyclerView albumRecycler;
    AlbumAdapter mAlbumAdapter;


    public AlbumList() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.album_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AlbumManager manager = new AlbumManager();
        albumList = manager.getAlbumList(getContext());

        albumRecycler = view.findViewById(R.id.album_list_recycler_view);
        GridLayoutManager gridView = new GridLayoutManager(getContext(),2,1,false);
        albumRecycler.setLayoutManager(gridView);
        mAlbumAdapter = new AlbumAdapter(albumList,getContext());
        VerticalRecyclerViewFastScroller albumScroll = view.findViewById(R.id.album_fast_scroller);
        albumScroll.setRecyclerView(albumRecycler);
        albumRecycler.setHasFixedSize(true);
        albumRecycler.setAdapter(mAlbumAdapter);

    }
}
