package latest.player.music.musiclayer.Services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import latest.player.music.musiclayer.R;
import latest.player.music.musiclayer.MediaStore.SongManager;
import latest.player.music.musiclayer.utils.SongInfo;

/**
 * Created by Bhanupro on 4/17/2018.
 */

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener{

    public MediaPlayer mPlayer;
    private int songPosition;
    private ArrayList<SongInfo> _songs = new ArrayList<>();
    private Random rand;
    public boolean Shuffle = false;
    public boolean repeatNone = false;
    public boolean repeatAll = false;
    public boolean repeatOne = false;
    PendingIntent pIntent;
    private long filteredSongId = 0;

    SharedPreferences mPref;

    private IntentFilter noisy;
    //private BecomingNoisyReceiver myNoisyReceiver;
    private Handler audioHandler = new Handler();
    private Handler notificationHandler = new Handler();
    AudioManager am;
    //AudioManager.OnAudioFocusChangeListener afl;

    private SongManager mSongManager = new SongManager();
    public final IBinder MusicBind = new MusicBinder();

    @Override
    public void onCreate() {
        super.onCreate();

        songPosition = 0;
        rand = new Random();
        _songs = mSongManager.getMp3Songs(MusicService.this);
        Collections.sort(_songs, new Comparator<SongInfo>(){
            public int compare(SongInfo a, SongInfo b){
                return a.getSongName().compareTo(b.getSongName());
            }
        });
        am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        int result = am.requestAudioFocus(this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
        /*if (Build.VERSION.SDK_INT>=26){

                        mPlayer.setAudioAttributes(new AudioAttributes.Builder()
                         .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build());
        }*/
        if(result ==AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            mPlayer = new MediaPlayer();
            initMusicPlayer();
        }else if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED){
            Toast.makeText(this,"Failed to start audio service",Toast.LENGTH_SHORT).show();
        }
        noisy = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        mPref = getSharedPreferences("isCompletedSong",0);

    }
    public void initMusicPlayer(){
        mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);


        //BecomingNoisyReceiver myNoisyReceiver = new BecomingNoisyReceiver();

    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:

                if (!mPlayer.isPlaying()) {
                    mPlayer.start();
                    mPlayer.setVolume(1.0f, 1.0f);
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mPlayer.isPlaying()) mPlayer.setVolume(0.2f, 0.2f);
                audioHandler.postDelayed(audioRunnable,TimeUnit.SECONDS.toMillis(5));
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mPlayer.isPlaying()) mPlayer.pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mPlayer.isPlaying()) mPlayer.setVolume(0.2f, 0.2f);
                notificationHandler.postDelayed(notificationRunnable,TimeUnit.SECONDS.toMillis(4));

                break;
            default:
                break;
        }
    }
    private Runnable audioRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPlayer.isPlaying()) mPlayer.stop();
            //mPlayer.release();
            //mPlayer = null;
            //unregisterReceiver(BecomingNoisyReceiver);
            if (Build.VERSION.SDK_INT>=26){
                am.abandonAudioFocusRequest(null);
            }
        }
    };
    private Runnable notificationRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPlayer.isPlaying())mPlayer.setVolume(1.0f, 1.0f);
        }
    };

    public class MusicBinder extends Binder{
        public MusicService getService(){
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle data = intent.getExtras();
        if (data!= null){
           // _songs = (ArrayList<SongInfo>) data.getSerializable("songList");
        }


        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return MusicBind;
    }
    public boolean unBind(Intent intent){
        mPlayer.stop();
        mPlayer.release();
        return false;
    }
    public void setList(ArrayList<SongInfo> songs){
        _songs = songs;
    }
    public void setSong(int index){
        songPosition = index;
    }
    public int getSongIndex(){
        return songPosition;
    }
    public long getSongId(){
        return _songs.get(songPosition).getSong_id();
    }
    public void filteredSongs(long id){
        filteredSongId = id;
    }
    public void getSongPostionOf(){
       if (filteredSongId != 0){
           for (int ii=0;ii<_songs.size();ii++){
               if (filteredSongId == _songs.get(ii).getSong_id()){
                   songPosition = ii;
               }
           }
       }
    }
    public void playSong(){
        mPlayer.reset();
        registerReceiver(BecomingNoisyReceiver, noisy);

        SongInfo info = _songs.get(songPosition);
        long currentSongId = info.getSong_id();
        if (filteredSongId != 0){
            currentSongId = filteredSongId;
            getSongPostionOf();
        }

        if (isPlaying()){
            mPlayer.stop();
            Uri songUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,currentSongId);
            try {
                mPlayer.setDataSource(getApplicationContext(),songUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            Uri songUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,currentSongId);
            try {
                mPlayer.setDataSource(getApplicationContext(),songUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        filteredSongId = 0;
        mPref.edit().putBoolean("isCompleted",false).apply();

        mPlayer.prepareAsync();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mPref.edit().putBoolean("isCompleted",true).apply();
        if (mPlayer.getCurrentPosition()>0){
            mp.reset();
            playNext();
        }
    }

    public void playNext() {
        mPlayer.reset();

        if (repeatOne){
            int repeatOne = songPosition;
            songPosition = repeatOne;
        }else {
            if(Shuffle){
                int newSong = songPosition;
                while(newSong==songPosition){
                    newSong=rand.nextInt(_songs.size());
                }
                songPosition=newSong;
            }
            else{
                songPosition++;
                if(songPosition>=_songs.size()) songPosition=0;
            }
        }
        playSong();
    }
    public void playPrevious(){
        mPlayer.reset();
        songPosition--;
        if (_songs.size()<0){
            songPosition = _songs.size()-1;
        }
        playSong();
    }
    public void pauseSong(){
        stopForeground(true);
        mPlayer.pause();
    }
    public boolean isPlaying(){
        return mPlayer.isPlaying();
    }
    public int getDuration(){
        return  mPlayer.getDuration();
    }
    public int getCurrentPosition(){
        return mPlayer.getCurrentPosition();
    }
    public void letsPlay(){
        mPlayer.start();
    }
    public void seekTo(int pos){
        mPlayer.seekTo(pos);
    }
    public void setShuffle(boolean value){
        if (value){
            Shuffle = true;
        }else {
            Shuffle = false;
        }
    }
    public boolean isShuffle(){
        return Shuffle;
    }
    public void setRepeatOne(boolean value){
        if (value){
            repeatOne = true;
        }else {
            repeatOne = false;
        }
    }
    public boolean isRepeatOne(){
        return repeatOne;
    }
    public Uri getSongArt(){
        return _songs.get(songPosition).getSong_bitmap();
    }

    @Override
    public void onDestroy() {
        //stopForeground(true);
        registerReceiver(BecomingNoisyReceiver, noisy);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        mp.start();

        Intent notIntent = new Intent(this,MusicService.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder notification = new Notification.Builder(this);
        notification.setContentIntent(pIntent)
                .setSmallIcon(R.drawable.background)
                .setTicker(_songs.get(songPosition).getSongName())
                .setOngoing(true)
                .setAutoCancel(true)
                .setContentTitle("Playing")
                .setContentText(_songs.get(songPosition).getSongName());
        //Notification note = notification.build();
        //startForeground(2,note);
    }
    private BroadcastReceiver BecomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())){
                if (mPlayer != null && mPlayer.isPlaying()){
                    mPlayer.stop();
                }
            }
        }
    };



}
