package latest.player.music.musiclayer.MediaStore;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import latest.player.music.musiclayer.utils.SongInfo;

/**
 * Created by Bhanupro on 4/16/2018.
 */

public class SongManager {
    private ArrayList<SongInfo> songsList = new ArrayList<>();

    public ArrayList<SongInfo> getMp3Songs(Context ctx) {
        Uri allSongsUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        Cursor cursor =  ctx.getContentResolver().query(allSongsUri, null, null, null, selection);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String uri = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    Long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                    Long _id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

                    Uri artWorkUri = Uri.parse("content://media/external/audio/albumart");
                    Uri albumAppended = ContentUris.withAppendedId(artWorkUri,id);

                    /*Bitmap bmp = null;
                    try {
                        bmp = MediaStore.Images.Media.getBitmap(ctx.getContentResolver(),albumAppended);
                        bmp = Bitmap.createScaledBitmap(bmp,30,30,true);
                    } catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    /*SongInfo song = new SongInfo(
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));*/
                    if (title != null){
                        SongInfo song = new SongInfo(title,artist,uri,albumAppended,_id,convertDuration(Long.valueOf(duration)));
                        songsList.add(song);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return songsList;
    }
    public String convertDuration(long duration) {
        String out = null;
        long hours=0;
        try {
            hours = (duration / 3600000);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return out;
        }
        long remaining_minutes = (duration - (hours * 3600000)) / 60000;
        String minutes = String.valueOf(remaining_minutes);
        if (minutes.equals(0)) {
            minutes = "00";
        }
        long remaining_seconds = (duration - (hours * 3600000) - (remaining_minutes * 60000));
        String seconds = String.valueOf(remaining_seconds);
        if (seconds.length() < 2) {
            seconds = "00";
        } else {
            seconds = seconds.substring(0, 2);
        }

        if (hours > 0) {
            out = hours + ":" + minutes + ":" + seconds;
        } else {
            out = minutes + ":" + seconds;
        }

        return out;

    }
}
