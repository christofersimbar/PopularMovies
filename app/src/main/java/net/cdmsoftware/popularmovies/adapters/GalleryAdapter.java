package net.cdmsoftware.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import net.cdmsoftware.popularmovies.R;
import net.cdmsoftware.popularmovies.data.MovieContract.MovieEntry;
import net.cdmsoftware.popularmovies.models.Movie;

public class GalleryAdapter extends CursorRecyclerViewAdapter<GalleryAdapter.ViewHolder> {
    private final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w185";
    private Context mContext;
    private int mSpanCount;
    private int VIEW_TYPE_EMPTY = 1;
    private int VIEW_TYPE_ITEM = 2;

    public interface Callback {
        /**
         * GalleryAdapter Callback for when an item has been selected.
         */
        void onItemSelected(Uri uri);
    }

    public GalleryAdapter(Context context, Cursor cursor, int spanCount) {
        super(context, cursor);
        this.mContext = context;
        this.mSpanCount = spanCount;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public ImageView mPosterImage;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mPosterImage = (ImageView) view.findViewById(R.id.posterImage);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_item, parent, false);
        itemView.getLayoutParams().width = parent.getMeasuredWidth() / mSpanCount;
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor) {
        final Movie movieItem = Movie.fromCursor(cursor);
        Picasso.with(mContext)
                .load(IMAGE_BASE_URL + movieItem.getPosterImage())
                .placeholder(R.drawable.no_image)
                .error(R.drawable.no_image)
                .into(viewHolder.mPosterImage);

        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Callback) v.getContext())
                        .onItemSelected(MovieEntry.buildMovieUri(movieItem.getID()));
            }
        });
    }
}