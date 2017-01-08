package net.cdmsoftware.popularmovies.adapters;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.cdmsoftware.popularmovies.R;
import net.cdmsoftware.popularmovies.models.Trailer;

import java.util.List;

import static android.R.attr.id;

public class TrailerAdapter
        extends RecyclerView.Adapter<TrailerAdapter.ViewHolder> {

    private Context mContext;
    private List<Trailer> mTrailers;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public String mKey;
        public String mLink;
        public final View mView;
        public final TextView mTrailerTitle;
        public final ImageView mTrailerImage;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTrailerImage = (ImageView) view.findViewById(R.id.trailer_image);
            mTrailerTitle = (TextView) view.findViewById(R.id.trailer_title);
        }
    }

    public TrailerAdapter(Context context, List<Trailer> items) {
        this.mContext = context;
        this.mTrailers = items;
    }

    public void loadTrailers(List<Trailer> items) {
        mTrailers = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trailer_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mKey = mTrailers.get(position).getKey();
        holder.mLink = mTrailers.get(position).getLink();
        holder.mTrailerTitle.setText(mTrailers.get(position).getTitle());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + holder.mKey))
                        .putExtra("force_fullscreen",true);
                Intent webIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(holder.mLink));
                Context context = v.getContext();
                try {
                    context.startActivity(appIntent);
                } catch (ActivityNotFoundException ex) {
                    context.startActivity(webIntent);
                }
            }
        });

        // set image
        Picasso.with(mContext)
                .load(mTrailers.get(position).getImagePath())
                .placeholder(R.drawable.no_image_wide)
                .error(R.drawable.no_image_wide)
                .into(holder.mTrailerImage);
    }

    @Override
    public int getItemCount() {
        return (mTrailers!=null)?mTrailers.size():0;
    }
}
