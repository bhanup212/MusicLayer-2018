package latest.player.music.musiclayer.Database;

import android.graphics.Bitmap;

/**
 * Created by Bhanupro on 4/16/2018.
 */

public class Song {
    public static final String TABLE_NAME = "SONGS.DB";

    public static final String SONG_ID= "_ID";
    public static final String SONG_TITLE= "_TITLE";
    public static final String SONG_ARTIST= "_ARTIST";
    public static final String SONG_URI = "_URI";
    public static final String SONG_ART = "_ART";

    private String title;
    private String artist;
    private String uri;
    private Bitmap art;

    public static final String CREATE_SONG_TABLE =
            "CREATE TABLE "+ TABLE_NAME +"("
                    +SONG_ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"
                    +SONG_TITLE+" TEXT,"
                    +SONG_ARTIST+" TEXT,"
                    +SONG_URI+" TEXT,"
                    +SONG_ART+" BLOB);";

    public Song() {
    }

    public Song(String title, String artist, String uri, Bitmap art) {
        this.title = title;
        this.artist = artist;
        this.uri = uri;
        this.art = art;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Bitmap getArt() {
        return art;
    }

    public void setArt(Bitmap art) {
        this.art = art;
    }
}
