package latest.player.music.musiclayer.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import static latest.player.music.musiclayer.Database.Song.CREATE_SONG_TABLE;
import static latest.player.music.musiclayer.Database.Song.TABLE_NAME;

/**
 * Created by Bhanupro on 4/16/2018.
 */

public class SongDbHelper extends SQLiteOpenHelper {

    SQLiteDatabase mDatabase;



    public SongDbHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_SONG_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        db.execSQL(CREATE_SONG_TABLE);
    }
    public void insertSong(String title, String artist, String uri, byte[] art){
        mDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Song.SONG_TITLE,title);
        values.put(Song.SONG_ARTIST,artist);
        values.put(Song.SONG_URI,uri);
        values.put(Song.SONG_ART, art);
    }

}
