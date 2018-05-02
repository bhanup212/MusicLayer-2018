package latest.player.music.musiclayer.Fragments;


import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.blurry.Blurry;
import latest.player.music.musiclayer.MainActivity;
import latest.player.music.musiclayer.R;
import latest.player.music.musiclayer.Services.MusicService;
import latest.player.music.musiclayer.utils.BlurImage;
import latest.player.music.musiclayer.utils.MusicUtilities;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class NowPlaying extends Fragment implements SeekBar.OnSeekBarChangeListener{

    TextView songTitle, songArtist;
    RelativeLayout fragBackground;
    ImageView pause,previous,next, albumArt,repeat_btn,shuffle_btn;
    TextView songMax,songMin;
    SeekBar mSeekBar;

    private static final float BITMAP_SCALE = 0.4f;
    private static final float BLUR_RADIUS = 9.5f;

    SharedPreferences mPref, mpp;
    SharedPreferences.Editor editor;

    private GestureDetector mDetectorD;
    private Handler myHandler = new Handler();


    public NowPlaying() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment,  fragment_now_playing
        mPref = getActivity().getSharedPreferences("Now Playing",0);
        mpp = getContext().getSharedPreferences("isCompletedSong",0);
        String name = mPref.getString("song_layouts","default");

        View v = inflater.inflate(R.layout.fragment_now_playing, container, false);
        if (name.equals("amazon")){
            v = inflater.inflate(R.layout.amazon_music_layout, container, false);
        }else if (name.equals("pi")){
            v = inflater.inflate(R.layout.pi_layout,container,false);
        }


        final GestureDetector mGesture = new GestureDetector(getActivity(),
                new GestureDetector.SimpleOnGestureListener(){
                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                        final int SWIPE_MIN_DISTANCE = 200;
                        final int SWIPE_MAX_DISTANCE = 450;
                        final int SWIPE_THRESHOLD_VELOCITY = 400;

                        if (Math.abs(e1.getY()-e2.getX()) > SWIPE_MAX_DISTANCE) return false;
                        if (e1.getX() - e2.getX() > 100){
                            //Toast.makeText(getContext(),"right to left",Toast.LENGTH_SHORT).show();
                            ((MainActivity)getActivity()).mPlayNext();
                            onSongChange();
                            pause.setImageResource(R.drawable.ic_pause_circle_outline_white);

                        }else if (e2.getX()-e1.getX()>100){
                            //Toast.makeText(getContext(),"left to right",Toast.LENGTH_SHORT).show();
                            ((MainActivity)getActivity()).mPlayPrevious();
                            onSongChange();

                            pause.setImageResource(R.drawable.ic_pause_circle_outline_white);

                        }
                        return super.onFling(e1, e2, velocityX, velocityY);
                    }

                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }
                });
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGesture.onTouchEvent(event);
            }
        });
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String title = getArguments().getString("song_title");
        String artist = getArguments().getString("song_artist");

        //Intent playIntent = new Intent(getContext(),MusicService.class);
        //getActivity().bindService(playIntent,mConnection,BIND_AUTO_CREATE);
        //getActivity().startService(playIntent);

        songTitle = view.findViewById(R.id.song_title);
        songArtist = view.findViewById(R.id.song_artist);
        albumArt = view.findViewById(R.id.album_playing_art);
        pause = view.findViewById(R.id.now_playing_pause_btn);
        previous = view.findViewById(R.id.now_playing_previous_btn);
        next = view.findViewById(R.id.now_playing_next_btn);
        songMax = view.findViewById(R.id.songMaxDuration);
        songMin = view.findViewById(R.id.songMinDuration);
        mSeekBar = view.findViewById(R.id.song_seekbar);
        repeat_btn = view.findViewById(R.id.repeat_btn);
        shuffle_btn = view.findViewById(R.id.shuffle_btn);

        String max = ((MainActivity)getActivity()).getSongDuration();
        songMax.setText(max);

        mSeekBar.setOnSeekBarChangeListener(this);

        Typeface custom_title_font = Typeface.createFromAsset(getContext().getAssets(),"fonts/Prime Regular.otf");
        songTitle.setTypeface(custom_title_font);

        Typeface custom_artist_name = Typeface.createFromAsset(getContext().getAssets(),"fonts/Prime Light.otf");
        songArtist.setTypeface(custom_artist_name);


        fragBackground = (RelativeLayout) getView().findViewById(R.id.now_playing_background_layout);

        long _id  = ((MainActivity)getActivity()).mService.getSongId();
        long songId = ((MainActivity) getActivity()).getSongId();

        Bitmap cover = ((MainActivity)getActivity()).getAlbumCover();
        Bitmap blurImage = null;
        if (cover != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                blurImage = BlurImage.blur(getActivity(), cover);
                fragBackground.setBackgroundDrawable(new BitmapDrawable(getResources(), blurImage));
            }
        } else {
            cover = BitmapFactory.decodeResource(getResources(), R.drawable.background4);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                blurImage = BlurImage.blur(getActivity(), cover);
                fragBackground.setBackgroundDrawable(new BitmapDrawable(getResources(), blurImage));
            }

        }



        songTitle.setText(title);
        songArtist.setText(artist);
        final String path = getArguments().getString("image");
        Picasso.get().load(path).error(R.drawable.ic_audiotrack_white).fit().into(albumArt);

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((MainActivity)getActivity()).mPlayPrevious();
                onSongChange();

                pause.setImageResource(R.drawable.ic_pause_circle_outline_white);

            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                    if (((MainActivity)getActivity()).mIsPlaying()){
                        ((MainActivity)getActivity()).mPauseS();
                        pause.setImageResource(R.drawable.ic_play_arrow_white);
                    }else {
                        ((MainActivity)getActivity()).mLetsPlay();
                        pause.setImageResource(R.drawable.ic_pause_circle_outline_white);

                    }

            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).mPlayNext();

                onSongChange();
                pause.setImageResource(R.drawable.ic_pause_circle_outline_white);

            }
        });
        shuffle_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isShuffle = ((MainActivity)getActivity()).mService.isShuffle();
                if (!isShuffle){
                    shuffle_btn.setImageResource(R.drawable.ic_shuffle_white);
                    ((MainActivity)getActivity()).mService.setShuffle(true);
                    Toast.makeText(getContext(),"Shuffle on",Toast.LENGTH_SHORT).show();
                }else {
                    shuffle_btn.setImageResource(R.drawable.ic_shuffle_black_24dp);
                    ((MainActivity)getActivity()).mService.setShuffle(false);
                    Toast.makeText(getContext(),"Shuffle off",Toast.LENGTH_SHORT).show();
                }
            }
        });
        repeat_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isRepeatOne = ((MainActivity)getActivity()).mService.isRepeatOne();
                if (isRepeatOne){
                    repeat_btn.setImageResource(R.drawable.ic_repeat_white);
                    ((MainActivity)getActivity()).mService.setRepeatOne(false);
                    Toast.makeText(getContext(),"Repeat All",Toast.LENGTH_SHORT).show();
                }else {
                    repeat_btn.setImageResource(R.drawable.ic_repeat_one_white);
                    ((MainActivity)getActivity()).mService.setRepeatOne(true);
                    Toast.makeText(getContext(),"Repeat One",Toast.LENGTH_SHORT).show();
                }
            }
        });
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mSeekBar.setProgress(0);
                int max = ((MainActivity)getActivity()).mService.getDuration();
                mSeekBar.setMax(max);

                String max2 = ((MainActivity)getActivity()).getSongDuration();
                songMax.setText(max2);
            }
        };
        myHandler.postDelayed(runnable,100);
        Thread t = new runThread();
        t.start();



    }
    public void onSongChange(){
        Uri uri = ((MainActivity)getActivity()).changeAlbumInFrag();
        String tit= ((MainActivity)getActivity()).changeSongTitle();
        String arti = ((MainActivity)getActivity()).changeSongArtist();

        songTitle.setText(tit);
        songArtist.setText(arti);

        String max = ((MainActivity)getActivity()).getSongDuration();
        songMax.setText(max);
        songMin.setText("00:00");
        //albumArt.setImageResource(R.drawable.ic_audiotrack_white);
        String path = ((MainActivity)getActivity()).getSongAlbumPicasso();
        Picasso.get().load(path).error(R.drawable.ic_audiotrack_white).fit().into(albumArt);



    }

    @Override
    public void onResume() {
        super.onResume();
        boolean isPlaying = ((MainActivity)getActivity()).mService.isPlaying();
        if (!isPlaying){
            pause.setImageResource(R.drawable.ic_play_arrow_white);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //SystemClock.sleep(100);
        //((MainActivity)getActivity()).mService.mPlayer.stop();
        //((MainActivity)getActivity()).mService.seekTo(progress);
        //((MainActivity)getActivity()).mService.letsPlay();
        if (fromUser){
            ((MainActivity)getActivity()).mService.seekTo(progress);
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //((MainActivity)getActivity()).mService.seekTo(seekBar.getMax());
    }
    public  Bitmap createBlur(Context context) {

        long id = ((MainActivity)getActivity()).getSongId();
        Uri artWorkUri = Uri.parse("content://media/external/audio/albumart");
        Uri albumAppended = ContentUris.withAppendedId(artWorkUri,id);

        Bitmap bmp = null;
        try {
            bmp = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(),albumAppended);
            bmp = Bitmap.createScaledBitmap(bmp,100,100,true);
            //albumArt.setImageBitmap(bmp);

        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (null == bmp) return null;
        int width = Math.round(bmp.getWidth() * BITMAP_SCALE);
        int height = Math.round(bmp.getHeight() * BITMAP_SCALE);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(bmp, 25, 100, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }

    @Override
    public void onStart() {
        super.onStart();

        boolean isRepeatOne = ((MainActivity)getActivity()).mService.isRepeatOne();
        if (isRepeatOne){
            repeat_btn.setImageResource(R.drawable.ic_repeat_one_white);
        }else {
            repeat_btn.setImageResource(R.drawable.ic_repeat_white);
        }
        boolean isShuffle = ((MainActivity)getActivity()).mService.isShuffle();
        if (isShuffle) shuffle_btn.setImageResource(R.drawable.ic_shuffle_white);
    }

    public class runThread extends Thread{
        @Override
        public void run() {
            while (true){
                try {


                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mSeekBar.post(new Runnable() {
                    @Override
                    public void run() {
                        mSeekBar.setProgress(((MainActivity)getActivity()).mService.getCurrentPosition());
                        int min = ((MainActivity)getActivity()).mService.getCurrentPosition();
                        SharedPreferences mpp = getContext().getSharedPreferences("isCompletedSong",0);
                        boolean isCompleted = mpp.getBoolean("isCompleted",false);
                        if (isCompleted){
                            onSongChange();
                        }
                        //Toast.makeText(getContext(),String.valueOf(min),Toast.LENGTH_LONG).show();
                        String songDuration = String.format("%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(min),
                                TimeUnit.MILLISECONDS.toSeconds(min) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(min))
                        );
                        songMin.setText(songDuration);
                    }
                });
            }
        }
    }
}
