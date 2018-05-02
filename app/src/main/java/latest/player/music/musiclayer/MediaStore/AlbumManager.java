package latest.player.music.musiclayer.MediaStore;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;

import latest.player.music.musiclayer.utils.AlbumInfo;

public class AlbumManager {
    private ArrayList<AlbumInfo> albums = new ArrayList<>();

    public ArrayList<AlbumInfo> getAlbumList(Context context){
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final String sortBy = MediaStore.Audio.Media.ALBUM
                + " COLLATE LOCALIZED ASC";

        String[] selection = {
                //MediaStore.Audio.Media._ID,
                MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                //MediaStore.Audio.Media.TITLE,
                //MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID,
                //MediaStore.Audio.Media.DURATION
        };

        String isMusic = MediaStore.Audio.Media.IS_MUSIC + "=1";
        Cursor cursor = context.getContentResolver().query(uri,selection,isMusic,null,sortBy);

        if (cursor.moveToFirst()){

            do {
                long album_id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                String albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                String albumArtist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST));

                boolean isExists = false;
                for (int i = 0;i<albums.size();i++){
                    if (albums.get(i).getAlbumID() == album_id){
                        isExists = true;
                    }
                }
                if (!isExists){
                    String path = null;
                    /*Cursor cursor2 = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                            new String[] {MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                            MediaStore.Audio.Albums._ID+ "=?",
                            new String[] {String.valueOf(album_id)},
                            null);

                    if (cursor2.moveToFirst()) {
                        path = cursor2.getString(cursor2.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                    }*/
                    albums.add(new AlbumInfo(album_id,albumName,albumArtist));
                }

            }while (cursor.moveToNext());
        }
        return albums;
    }

}
