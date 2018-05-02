package latest.player.music.musiclayer.utils;

import java.io.Serializable;

public class AlbumInfo implements Serializable {
    private long albumID;
    private String albumName;
    private String albumArtist;
    private String albumPath;

    public AlbumInfo(long albumID, String albumName, String albumArtist) {
        this.albumID = albumID;
        this.albumName = albumName;
        this.albumArtist = albumArtist;
        //this.albumPath = albumPath;
    }

    public long getAlbumID() {
        return albumID;
    }

    public void setAlbumID(long albumID) {
        this.albumID = albumID;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getAlbumArtist() {
        return albumArtist;
    }

    public void setAlbumArtist(String albumArtist) {
        this.albumArtist = albumArtist;
    }

    public String getAlbumPath() {
        return albumPath;
    }

    public void setAlbumPath(String albumPath) {
        this.albumPath = albumPath;
    }
}
