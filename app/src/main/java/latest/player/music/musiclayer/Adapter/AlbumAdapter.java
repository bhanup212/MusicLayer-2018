package latest.player.music.musiclayer.Adapter;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.zip.Inflater;

import latest.player.music.musiclayer.R;
import latest.player.music.musiclayer.utils.AlbumInfo;
import latest.player.music.musiclayer.utils.SongInfo;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder>{

    private ArrayList<AlbumInfo> mAlbums = new ArrayList<>();
    private Context mContext;

    public AlbumAdapter(ArrayList<AlbumInfo> album, Context context) {
        mAlbums = album;
        mContext = context;
    }

    @Override
    public AlbumAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_list_row,null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AlbumAdapter.ViewHolder holder, int position) {
            AlbumInfo info = mAlbums.get(position);
            holder.albumName.setText(info.getAlbumName());
            //long id = info.getAlbumID();
            Uri uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), info.getAlbumID());
            Picasso.get().load(uri).error(R.drawable.ic_audiotrack_white).fit()
                    .placeholder(R.drawable.ic_audiotrack_white).fit().into(holder.albumImg);
    }

    @Override
    public int getItemCount() {
        return mAlbums.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        protected ImageView albumImg;
        protected TextView albumName;
        public ViewHolder(View itemView) {
            super(itemView);
            albumImg = itemView.findViewById(R.id.album_list_image);
            albumName = itemView.findViewById(R.id.album_list_name);
            //albumName.setBackgroundColor(Color.TRANSPARENT);
            Typeface custom_title_font = Typeface.createFromAsset(mContext.getAssets(),"fonts/Prime Regular.otf");
            albumName.setTypeface(custom_title_font);

            //Typeface custom_artist_name = Typeface.createFromAsset(mContext.getAssets(),"fonts/Prime Light.otf");
            //songArtist.setTypeface(custom_artist_name);
        }
    }
}
