package latest.player.music.musiclayer;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import latest.player.music.musiclayer.Adapter.SongAdapter;
import latest.player.music.musiclayer.Adapter.SongSearchAdapter;
import latest.player.music.musiclayer.Fragments.AlbumList;
import latest.player.music.musiclayer.Fragments.FilteredSongs;
import latest.player.music.musiclayer.Fragments.NowPlaying;
import latest.player.music.musiclayer.MediaStore.SongManager;
import latest.player.music.musiclayer.Services.MusicService;
import latest.player.music.musiclayer.utils.MusicUtilities;
import latest.player.music.musiclayer.utils.SongInfo;
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

public class MainActivity extends AppCompatActivity {

    ArrayList<SongInfo> _songs;
    MediaPlayer mPlayer;
    Handler mHandler = new Handler();
    SongAdapter songAdapter;


    SongManager mSongManager = new SongManager();

    RecyclerView songRecyclerView;
    LinearLayout bottomPlayerLayout, topPlayerLayout;
    RelativeLayout mainLayout;
    ImageView albumLayout, searchLayout;
    EditText searchSongEdt;

    FragmentManager Fmanager;
    FragmentTransaction transaction;

    SharedPreferences mPref;

    private static final float BITMAP_SCALE = 0.4f;
    private static final float BLUR_RADIUS = 9.5f;

    public MusicService mService;
    private Intent playIntent;
    public boolean musicBound = false;

    ImageView playNext,playPrevious,pauseMusic,albumArtBottom;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            mService = binder.getService();
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };
    public void mPlayNext(){
        if (musicBound){
            mService.playNext();
        }
    }
    public void mPlayPrevious(){
        if (musicBound){
            mService.playPrevious();
        }
    }
    public void mPauseS(){
        if (musicBound){
            mService.pauseSong();
        }
    }
    public void mLetsPlay(){
        if (musicBound){
            mService.letsPlay();
        }
    }
    public boolean mIsPlaying(){
        return mService.isPlaying();
    }
    public Uri changeAlbumInFrag(){
        return mService.getSongArt();
    }
    public String changeSongTitle(){
        if (musicBound){
            return _songs.get(mService.getSongIndex()).getSongName();
        }
        return null;
    }
    public String changeSongArtist(){
        if (musicBound){
            return _songs.get(mService.getSongIndex()).getArtistName();
        }
        return null;
    }
    public String getSongDuration(){
        return _songs.get(mService.getSongIndex()).getDuration();
    }
    public long getSongId(){
        return _songs.get(mService.getSongIndex()).getSong_id();
    }
    public String getSongAlbumPicasso(){
        return _songs.get(mService.getSongIndex()).getSong_bitmap().toString();
    }
    public Bitmap getAlbumCover(){
        Bitmap cover = MusicUtilities.albumCover(_songs.get(mService.getSongIndex()).getSong_id(),MainActivity.this);
        return cover;
    }
    public void setSongIdService(long id){
        mService.filteredSongs(id);
        mService.playSong();
    }
    /*public Bitmap getSongBitmap(){
        return _songs.get(mService.getSongIndex()).getSong_bitmap();
    }*/

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null){
            playIntent = new Intent(this,MusicService.class);
            bindService(playIntent,mConnection,BIND_AUTO_CREATE);
            startService(playIntent);
        }

        if (musicBound){
            if (mService.isPlaying()){
                //bottomPlayerLayout.setVisibility(View.VISIBLE);
                Picasso.get().load(mService.getSongArt()).error(R.drawable.ic_launcher_background)
                        .placeholder(R.drawable.ic_launcher_background).resize(100,100)
                        .centerCrop().into(albumArtBottom);
            }else {
                //bottomPlayerLayout.setVisibility(View.INVISIBLE);

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        songRecyclerView = findViewById(R.id.song_recyclerview);
        playNext = findViewById(R.id.play_next_btn);
        playPrevious = findViewById(R.id.play_previous_btn);
        pauseMusic = findViewById(R.id.pause_btn);
        albumArtBottom = findViewById(R.id.bottom_player_album_art);
        bottomPlayerLayout = findViewById(R.id.bottom_player_layout);
        mainLayout = findViewById(R.id.main_activity_layout);
        albumLayout = findViewById(R.id.album_list_main_layout);
        searchLayout = findViewById(R.id.search_main_layout);
        searchSongEdt = findViewById(R.id.edt_search_song);
        topPlayerLayout = findViewById(R.id.linear_layout);

        int[] backgroundImages = {R.drawable.background4,R.drawable.background15,R.drawable.background12a,
                                    R.drawable.background5,R.drawable.background6,R.drawable.background7,
                                    R.drawable.background8,R.drawable.background9,R.drawable.background10,
                                    R.drawable.background14,R.drawable.background13,
                R.drawable.background16,R.drawable.background17,R.drawable.background18,R.drawable.background19,
                R.drawable.background21,R.drawable.background22,R.drawable.background23,
                R.drawable.background24a,R.drawable.background26,R.drawable.background27};
        Random rand = new Random();
        int newImge = rand.nextInt(backgroundImages.length);
        _songs = new ArrayList<>();
        checkUserPermission();
        _songs = mSongManager.getMp3Songs(MainActivity.this);
        Collections.sort(_songs, new Comparator<SongInfo>(){
            public int compare(SongInfo a, SongInfo b){
                return a.getSongName().compareTo(b.getSongName());
            }
        });

        //Toast.makeText(this,String.valueOf(_songs.size()),Toast.LENGTH_SHORT).show();
        songAdapter = new SongAdapter(this,_songs);
        if (musicBound){
            mService.setList(mSongManager.getMp3Songs(MainActivity.this));
        }

        //pauseMusic.setClickable(false);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        DividerItemDecoration decoration = new DividerItemDecoration(songRecyclerView.getContext(),
                manager.getOrientation());
        VerticalRecyclerViewFastScroller fastScroller = findViewById(R.id.fast_scroller);
        fastScroller.setRecyclerView(songRecyclerView);

        songRecyclerView.setAdapter(songAdapter);
        songRecyclerView.setLayoutManager(manager);
        songRecyclerView.addItemDecoration(decoration);
        songAdapter.notifyDataSetChanged();



        pauseMusic.setClickable(false);

        Fmanager =getSupportFragmentManager();

        playIntent = new Intent(this,MusicService.class);
        bindService(playIntent,mConnection,BIND_AUTO_CREATE);
        startService(playIntent);

        mainLayout.setBackgroundResource(backgroundImages[newImge]);
        /*if (musicBound){
            Drawable drawable = RoundedBitmapDrawableFactory.create(getResources(), createBlur());
            if (drawable != null){
                mainLayout.setBackground(drawable);
            }
        }*/
        //checkUserPermission();
        searchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //albumLayout.setVisibility(View.GONE);
                transaction = Fmanager.beginTransaction();
                Fragment filtered = new FilteredSongs();

                transaction.add(R.id.main_activity_layout,filtered,"Filtered songs");
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });


        songAdapter.setOnItemClickListener(new SongAdapter.onClickListener() {
            @Override
            public void onItemClick(LinearLayout tv, View v, final SongInfo s, int position) {
                transaction = Fmanager.beginTransaction();
                Fragment nowFrag = new NowPlaying();
                Bundle bundle = new Bundle();
                bundle.putString("song_title",_songs.get(position).getSongName());
                bundle.putString("song_artist",_songs.get(position).getArtistName());
                bundle.putString("image",_songs.get(position).getSong_bitmap().toString());
                nowFrag.setArguments(bundle);
                transaction.add(R.id.main_activity_layout,nowFrag,"Now Playing");
                transaction.addToBackStack(null);
                transaction.commit();
                if (musicBound){
                    mService.setSong(position);
                    //mService.filteredSongs(_songs.get(position).getSong_id());
                    mService.playSong();
                    changeAlbumArt();
                    pauseMusic.setClickable(true);
                    pauseMusic.setImageResource(R.drawable.ic_pause_circle_outline_white);
                    Picasso.get().load(mService.getSongArt()).error(R.drawable.ic_launcher_background)
                            .placeholder(R.drawable.ic_launcher_background).resize(100,100)
                            .centerCrop().into(albumArtBottom);
                    //mService.getTitle(position);
                }
            }
        });
        playNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.playNext();
                Picasso.get().load(mService.getSongArt()).error(R.drawable.ic_audiotrack_white)
                        .placeholder(R.drawable.ic_audiotrack_white).resize(100,100)
                        .centerCrop().into(albumArtBottom);
                pauseMusic.setImageResource(R.drawable.ic_pause_circle_outline_white);

                //Drawable drawable = RoundedBitmapDrawableFactory.create(getResources(), createBlur());
                    //mainLayout.setBackground(drawable);
            }
        });
        playPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.playPrevious();
                Picasso.get().load(mService.getSongArt()).error(R.drawable.ic_launcher_background)
                        .placeholder(R.drawable.ic_launcher_background).resize(100,100)
                        .centerCrop().into(albumArtBottom);
                pauseMusic.setImageResource(R.drawable.ic_pause_circle_outline_white);
            }
        });
        pauseMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mService.isPlaying()){
                    mService.pauseSong();
                    pauseMusic.setImageResource(R.drawable.ic_play_arrow_white);
                }else {
                    mService.letsPlay();
                    pauseMusic.setImageResource(R.drawable.ic_pause_circle_outline_white);
                }

            }
        });
        bottomPlayerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicBound){
                    //if (mService.isPlaying()){
                        transaction = Fmanager.beginTransaction();
                        Fragment nowFrag = new NowPlaying();
                        Bundle bundle = new Bundle();
                        int p = mService.getSongIndex();
                        bundle.putString("song_title",_songs.get(p).getSongName());
                        bundle.putString("song_artist",_songs.get(p).getArtistName());
                        bundle.putString("image",_songs.get(p).getSong_bitmap().toString());
                        nowFrag.setArguments(bundle);
                        transaction.add(R.id.main_activity_layout,nowFrag,"Now Playing");
                        transaction.addToBackStack(null);
                        transaction.commit();
                    //}
                }
            }
        });
        ImageView settings = findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog settingsD = new Dialog(MainActivity.this);
                settingsD.setContentView(R.layout.more_options_settings);
                settingsD.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                ImageView settingsMore = settingsD.findViewById(R.id.more_settings);
                ImageView musicLayer = settingsD.findViewById(R.id.third_option);
                settingsD.show();
                musicLayer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                         final Dialog layoutS = new Dialog(MainActivity.this);
                         layoutS.setContentView(R.layout.layout_selection);
                         TextView firstLay = layoutS.findViewById(R.id.default_layout);
                         TextView amazon = layoutS.findViewById(R.id.amazon_layout);
                         TextView pi = layoutS.findViewById(R.id.pi_layout);
                         layoutS.show();
                        mPref = getSharedPreferences("Now Playing",0);
                        String name = mPref.getString("song_layouts","default");

                         firstLay.setOnClickListener(new View.OnClickListener() {
                             @Override
                             public void onClick(View v) {
                                 mPref.edit().putString("song_layouts","default").apply();
                                 layoutS.dismiss();
                             }
                         });
                         amazon.setOnClickListener(new View.OnClickListener() {
                             @Override
                             public void onClick(View v) {
                                 mPref.edit().putString("song_layouts","amazon").apply();
                                 layoutS.dismiss();
                             }
                         });
                         pi.setOnClickListener(new View.OnClickListener() {
                             @Override
                             public void onClick(View v) {
                                 mPref.edit().putString("song_layouts","pi").apply();
                                 layoutS.dismiss();
                             }
                         });
                    }
                });
            }
        });
        albumLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transaction = Fmanager.beginTransaction();
                Fragment nowFrag = new AlbumList();
                transaction.add(R.id.main_activity_layout,nowFrag,"Album List");
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });



    }


    @Override
    protected void onRestart() {
        super.onRestart();
        if (!mService.isPlaying()){
            pauseMusic.setImageResource(R.drawable.ic_play_arrow_white);
        }
    }


    public  Bitmap createBlur() {

        //long id = ((MainActivity)getActivity()).getSongId();
        long id = _songs.get(mService.getSongIndex()).getSong_id();
        Uri artWorkUri = Uri.parse("content://media/external/audio/albumart");
        Uri albumAppended = ContentUris.withAppendedId(artWorkUri,id);

        Bitmap bmp = null;
        try {
            bmp = MediaStore.Images.Media.getBitmap(getContentResolver(),albumAppended);
            bmp = Bitmap.createScaledBitmap(bmp,100,100,true);
            //albumArt.setImageBitmap(bmp);
            Bitmap outputBitmap = Bitmap.createBitmap(bmp);
            final RenderScript renderScript = RenderScript.create(this);
            Allocation tmpIn = Allocation.createFromBitmap(renderScript, bmp);
            Allocation tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap);

            //Intrinsic Gausian blur filter
            ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
            theIntrinsic.setRadius(BLUR_RADIUS);
            theIntrinsic.setInput(tmpIn);
            theIntrinsic.forEach(tmpOut);
            tmpOut.copyTo(outputBitmap);
            return outputBitmap;

        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmp;

    }

    @Override
    protected void onResume() {
        super.onResume();

        //albumArtBottom.setImageBitmap(mService.getSongArt());
        if (musicBound){
            if (mService.isPlaying()){
                //bottomPlayerLayout.setVisibility(View.VISIBLE);
                Picasso.get().load(mService.getSongArt()).error(R.drawable.ic_audiotrack_white)
                        .placeholder(R.drawable.ic_audiotrack_white).resize(100,100)
                        .centerCrop().into(albumArtBottom);
                pauseMusic.setImageResource(R.drawable.ic_pause_circle_outline_white);
            }else {
                //bottomPlayerLayout.setVisibility(View.INVISIBLE);

            }
        }

    }

    public Bitmap changeAlbumArt(){


            int positionOf = mService.getSongIndex();

            Uri artWorkUri = Uri.parse("content://media/external/audio/albumart");
            Uri albumAppended = ContentUris.withAppendedId(artWorkUri,_songs.get(positionOf).getSong_id());
                    Bitmap bmp = null;
                    try {
                        bmp = MediaStore.Images.Media.getBitmap(getContentResolver(),albumAppended);
                        bmp = Bitmap.createScaledBitmap(bmp,30,30,true);
                    } catch (FileNotFoundException e){
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            return bmp;
               /* Picasso.get().load(_songs.get(positionOf).getSong_bitmap()).error(R.drawable.ic_launcher_background)
                        .placeholder(R.drawable.ic_launcher_background).resize(100,100)
                        .centerCrop().into((ImageView) findViewById(R.id.bottom_player_album_art));*/
    }

    private void checkUserPermission() {
        if (Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},37);
                return;
            }

        }
        //fetchSongs();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case 37:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //fetchSongs();


                }else {
                    Toast.makeText(this,"Permission Denied",Toast.LENGTH_LONG).show();
                    checkUserPermission();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    @Override
    protected void onDestroy() {
        if (!mService.isPlaying()){
            stopService(playIntent);
            mService = null;
            super.onDestroy();
        }

    }
    /*private void fetchSongs(){
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);

        if (cursor != null){
            if (cursor.moveToFirst()){
                do {
                    String songTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    String songArtist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    long album_id= cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

                    Uri sArtworkUri = Uri
                            .parse("content://media/external/audio/albumart");
                    Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, album_id);
                    Bitmap bmp = null;

                    try {
                        bmp = MediaStore.Images.Media.getBitmap(getContentResolver(),albumArtUri);
                        bmp = Bitmap.createScaledBitmap(bmp,95,95,true);
                    } catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }


                    SongInfo info = new SongInfo(songTitle,songArtist,url,bmp);
                    _songs.add(info);
                    //songAdapter = new SongAdapter(MainActivity.this,_songs);
                    //songAdapter.notifyDataSetChanged();
                }while (cursor.moveToNext());
            }
        }



        cursor.close();
        //songAdapter = new SongAdapter(MainActivity.this,_songs);
    }*/



}
