package latest.player.music.musiclayer.utils;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Created by Bhanupro on 4/13/2018.
 */

public class SongInfo {
    private String songName;
    private String artistName;
    private String songUrl;
    private String songAlbum;
    private Uri song_bitmap;
    private long song_id;
    private String duration;

    public SongInfo() {
    }

    public SongInfo(String songName, String artistName, String songUrl,Uri song_bitmap,long id,String duration) {
        this.songName = songName;
        this.artistName = artistName;
        this.songUrl = songUrl;
        this.song_bitmap = song_bitmap;
        this.song_id = id;
        this.duration = duration;
        //this.songAlbum = songAlbum;
    }

    public SongInfo(String songName, String artistName, String songUrl) {
        this.songName = songName;
        this.artistName = artistName;
        this.songUrl = songUrl;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }

    public String getSongAlbum() {
        return songAlbum;
    }

    public void setSongAlbum(String songAlbum) {
        this.songAlbum = songAlbum;
    }

    public Uri getSong_bitmap() {
        return song_bitmap;
    }

    public void setSong_bitmap(Uri song_bitmap) {
        this.song_bitmap = song_bitmap;
    }

    public long getSong_id() {
        return song_id;
    }

    public void setSong_id(long song_id) {
        this.song_id = song_id;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
