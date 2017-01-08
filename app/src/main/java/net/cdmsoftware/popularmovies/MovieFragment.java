package net.cdmsoftware.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import net.cdmsoftware.popularmovies.adapters.GalleryAdapter;
import net.cdmsoftware.popularmovies.data.MovieContract.MovieEntry;
import net.cdmsoftware.popularmovies.sync.MovieSyncAdapter;

import static android.widget.GridView.AUTO_FIT;

public class MovieFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private RecyclerView mRecyclerView;
    private GalleryAdapter mGalleryAdapter;
    private int mPosition = RecyclerView.NO_POSITION;
    private static final String SELECTED_KEY = "selected_position";


    // loader declaration
    private static final int MOVIE_LOADER = 0;

    // projection columns
    private static final String[] MOVIE_COLUMNS = {
            MovieEntry._ID,
            MovieEntry.COLUMN_MOVIE_ID,
            MovieEntry.COLUMN_ORIGINAL_TITLE,
            MovieEntry.COLUMN_POSTER_PATH,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_VOTE_AVERAGE,
    };

    public MovieFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mGalleryAdapter = new GalleryAdapter(getActivity(), null, 2);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycle_view);
        mRecyclerView.setAdapter(mGalleryAdapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mRecyclerView.setHasFixedSize(true);

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != RecyclerView.NO_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String preferredListType = Utilities.getPreferredListType(getActivity());
        Uri uri = MovieEntry.buildMovieUriByListType(preferredListType);

        return new CursorLoader(getActivity(),
                uri,
                MOVIE_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mGalleryAdapter.swapCursor(data);
        if (mPosition != RecyclerView.NO_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mRecyclerView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mGalleryAdapter.swapCursor(null);
    }

    public void onListTypeChanged() {
        // user changed list type preference, update immediately
        MovieSyncAdapter.syncImmediately(getActivity());
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
    }
}
