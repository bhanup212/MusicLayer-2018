package latest.player.music.musiclayer.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import latest.player.music.musiclayer.R;
import latest.player.music.musiclayer.utils.SongInfo;

/**
 * Created by Bhanupro on 4/13/2018.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {
    Context mContext;
    ArrayList<SongInfo> songs = new ArrayList<>();
    private onClickListener mListener;

    public SongAdapter(Context context, ArrayList<SongInfo> song) {
        mContext = context;
        songs = song;
    }

    @Override
    public SongAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.song_row_list,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SongAdapter.ViewHolder holder, final int position) {
        final SongInfo s = songs.get(position);
        holder.songTitle.setText(s.getSongName());
        holder.songArtist.setText(s.getArtistName());
        holder.songDuration.setText(s.getDuration());

        Picasso.get().load(s.getSong_bitmap()).error(R.drawable.ic_audiotrack_white)
                .placeholder(R.drawable.ic_audiotrack_white).resize(100,100)
                .centerCrop().into(holder.songAlbum);

        holder.songRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null){
                    mListener.onItemClick(holder.songRow,v,s,position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView songTitle, songArtist,songDuration;
        ImageView songAlbum;
        LinearLayout songRow;
        public ViewHolder(View itemView) {
            super(itemView);
            songTitle = itemView.findViewById(R.id.row_song_name_txt);
            songArtist = itemView.findViewById(R.id.row_song_artist_name_txt);
            songAlbum = itemView.findViewById(R.id.row_song_image);
            songDuration = itemView.findViewById(R.id.row_song_duration_txt);
            songRow = itemView.findViewById(R.id.song_row);

            Typeface custom_title_font = Typeface.createFromAsset(mContext.getAssets(),"fonts/Prime Regular.otf");
            songTitle.setTypeface(custom_title_font);

            Typeface custom_artist_name = Typeface.createFromAsset(mContext.getAssets(),"fonts/Prime Light.otf");
            songArtist.setTypeface(custom_artist_name);
        }
    }
    public interface onClickListener{
        void onItemClick(LinearLayout tv,View v,SongInfo s,int position);
    }
    public void setOnItemClickListener(final onClickListener itemClickListener){
        this.mListener = itemClickListener;
    }
}
