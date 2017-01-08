package net.cdmsoftware.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import net.cdmsoftware.popularmovies.adapters.ReviewAdapter;
import net.cdmsoftware.popularmovies.adapters.TrailerAdapter;
import net.cdmsoftware.popularmovies.data.MovieContract.MovieEntry;
import net.cdmsoftware.popularmovies.models.Review;
import net.cdmsoftware.popularmovies.models.Trailer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static net.cdmsoftware.popularmovies.R.id.averageVote;
import static net.cdmsoftware.popularmovies.R.id.backdropImage;
import static net.cdmsoftware.popularmovies.R.id.originalTitle;
import static net.cdmsoftware.popularmovies.R.id.overview;
import static net.cdmsoftware.popularmovies.R.id.releaseDate;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private final String LIST_TYPE_FAVORITE = "favorite";
    private final String API_BASE_URL = "http://api.themoviedb.org/3/movie";
    private final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w500";
    private final String PATH_TRAILERS = "videos";
    private final String PATH_REVIEWS = "reviews";
    private final String API_KEY = "api_key";

    static final String DETAIL_URI = "URI";

    private String mMovieShare;

    private static final String FORECAST_SHARE_HASHTAG = " #PopularMoviesApp";
    private ShareActionProvider mShareActionProvider;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            MovieEntry.COLUMN_MOVIE_ID,
            MovieEntry.COLUMN_ORIGINAL_TITLE,
            MovieEntry.COLUMN_POSTER_PATH,
            MovieEntry.COLUMN_BACKDROP_PATH,
            MovieEntry.COLUMN_OVERVIEW,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_VOTE_AVERAGE
    };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    static final int COL_MOVIE_ID = 0;
    static final int COL_ORIGINAL_TITLE = 1;
    static final int COL_POSTER_PATH = 2;
    static final int COL_BACKDROP_PATH = 3;
    static final int COL_OVERVIEW = 4;
    static final int COL_RELEASE_DATE = 5;
    static final int COL_VOTE_AVERAGE = 6;

    TextView mOriginalTitle;
    ImageView mBackdropImage;
    TextView mReleaseDate;
    TextView mAverageVote;
    TextView mOverview;
    RecyclerView mTrailersRecyclerView;
    RecyclerView mReviewsRecyclerView;
    Toolbar mToolbar;
    CollapsingToolbarLayout mCollapsingToolbar;
    FloatingActionButton mFab;
    View mTrailerLayout;
    View mReviewLayout;

    private TrailerAdapter mTrailerAdapter;
    private ReviewAdapter mReviewAdapter;
    private Cursor mCurrentCursor;

    private Uri mUri;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mFab = (FloatingActionButton) rootView.findViewById(R.id.fab_favorite);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFavorite();
            }
        });

        mCollapsingToolbar = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        if (mToolbar!=null && ((AppCompatActivity)getActivity()).getSupportActionBar()==null){
            ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
            ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mOriginalTitle = (TextView) rootView.findViewById(originalTitle);
        mBackdropImage = (ImageView) rootView.findViewById(backdropImage);
        mReleaseDate = (TextView) rootView.findViewById(releaseDate);
        mAverageVote = (TextView) rootView.findViewById(averageVote);
        mOverview = (TextView) rootView.findViewById(overview);
        mTrailersRecyclerView = (RecyclerView) rootView.findViewById(R.id.trailers_recycle_view);
        mReviewsRecyclerView = (RecyclerView) rootView.findViewById(R.id.reviews_recycle_view);

        // these layouts are used to hide contents while executing AsyncTask
        mTrailerLayout = rootView.findViewById(R.id.trailer_layout);
        mReviewLayout = rootView.findViewById(R.id.review_layout);

        mTrailerAdapter = new TrailerAdapter(getActivity(), new ArrayList<Trailer>());
        mTrailersRecyclerView.setAdapter(mTrailerAdapter);
        mTrailersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        mTrailersRecyclerView.setHasFixedSize(true);

        mReviewAdapter = new ReviewAdapter(new ArrayList<Review>());
        mReviewsRecyclerView.setAdapter(mReviewAdapter);
        mReviewsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mReviewsRecyclerView.setHasFixedSize(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detail, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to ShareActionProvider
        if (mMovieShare != null) {
            mShareActionProvider.setShareIntent(createShareMovieIntent());
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null?");
        }
    }

    private Intent createShareMovieIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                mMovieShare + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mUri==null) {
            mUri = MovieEntry.buildMovieUriByListType(Utilities.getPreferredListType(getActivity()));
        }
        return new CursorLoader(
                getActivity(),
                mUri,
                DETAIL_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            mCurrentCursor = data;

            String originalTitle = data.getString(COL_ORIGINAL_TITLE);
            // if toolbar exists then we are in single pane
            // set title on toolbar if single pane, otherwise set title on textview
            if (mCollapsingToolbar != null) {
                mCollapsingToolbar.setTitle(originalTitle);
            } else {
                mOriginalTitle.setText(originalTitle);
            }

            String backdropPath = data.getString(COL_BACKDROP_PATH);
            Picasso.with(getActivity())
                    .load(IMAGE_BASE_URL + backdropPath)
                    .placeholder(R.drawable.no_image_wide)
                    .error(R.drawable.no_image_wide)
                    .into(mBackdropImage);

            String overview = data.getString(COL_OVERVIEW);
            mOverview.setText(overview);

            String releaseDate = data.getString(COL_RELEASE_DATE);
            mReleaseDate.setText(Utilities.formatDate(releaseDate));

            String averageVote = data.getString(COL_VOTE_AVERAGE);
            mAverageVote.setText(getString(R.string.label_rating, averageVote));

            if (isFavorite(data.getLong(COL_MOVIE_ID))) {
                mFab.setImageResource(R.drawable.ic_favorite_white_24dp);
            } else {
                mFab.setImageResource(R.drawable.ic_favorite_border_white_24dp);
            }

            new TrailersAsyncTask(data.getLong(COL_MOVIE_ID)).execute();
            new ReviewsAsyncTask(data.getLong(COL_MOVIE_ID)).execute();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private class TrailersAsyncTask extends AsyncTask<String, Void, List<Trailer>> {
        private long mMovieId;

        TrailersAsyncTask(long movieId) {
            this.mMovieId = movieId;
        }

        @Override
        protected List<Trailer> doInBackground(String... keywords) {
            // the results array
            final String TMDB_TRAILER_RESULTS = "results";

            // results array children that need to be extracted
            final String TMDB_TRAILER_NAME = "name";
            final String TMDB_TRAILER_KEY = "key";
            List<Trailer> trailers = new ArrayList<>();

            // Create URL object
            Uri builtUri = Uri.parse(API_BASE_URL)
                    .buildUpon()
                    .appendPath(String.valueOf(mMovieId))
                    .appendPath(PATH_TRAILERS)
                    .appendQueryParameter(API_KEY, BuildConfig.THEMOVIEDB_API_KEY)
                    .build();
            URL url = Utilities.createUrl(builtUri.toString());
            Log.i(LOG_TAG, builtUri.toString());

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            if (url != null) {
                jsonResponse = Utilities.makeHttpRequest(url);
            }

            // abort process if there is an error when getting response
            if (jsonResponse == null) return null;

            try {
                JSONObject baseJsonResponse = new JSONObject(jsonResponse);
                JSONArray itemsArray = baseJsonResponse.getJSONArray(TMDB_TRAILER_RESULTS);
                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject item = itemsArray.getJSONObject(i);

                    String name = item.getString(TMDB_TRAILER_NAME);
                    String key = item.getString(TMDB_TRAILER_KEY);

                    trailers.add(new Trailer(name, key));
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return trailers;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(List<Trailer> trailers) {
            mTrailerAdapter.loadTrailers(trailers);

            // update share intent content
            // get first trailer
            if (mTrailerAdapter.getItemCount() > 0) {
                mMovieShare = mCurrentCursor.getString(COL_ORIGINAL_TITLE) + " " + trailers.get(0).getLink();
                mTrailerLayout.setVisibility(View.VISIBLE);

                // If onCreateOptionsMenu has already happened, we need to update the share intent now.
                if (mShareActionProvider != null) {
                    mShareActionProvider.setShareIntent(createShareMovieIntent());
                }
            } else {
                mTrailerLayout.setVisibility(View.INVISIBLE);
            }
        }
    }

    private class ReviewsAsyncTask extends AsyncTask<String, Void, List<Review>> {
        private long mMovieId;

        ReviewsAsyncTask(long movieId) {
            this.mMovieId = movieId;
        }

        @Override
        protected List<Review> doInBackground(String... keywords) {
            // the results array
            final String TMDB_REVIEW_RESULTS = "results";

            // results array children that need to be extracted
            final String TMDB_REVIEW_AUTHOR = "author";
            final String TMDB_REVIEW_CONTENT = "content";
            List<Review> reviews = new ArrayList<>();

            // Create URL object
            Uri builtUri = Uri.parse(API_BASE_URL)
                    .buildUpon()
                    .appendPath(String.valueOf(mMovieId))
                    .appendPath(PATH_REVIEWS)
                    .appendQueryParameter(API_KEY, BuildConfig.THEMOVIEDB_API_KEY)
                    .build();
            URL url = Utilities.createUrl(builtUri.toString());
            Log.i(LOG_TAG, builtUri.toString());

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            if (url != null) {
                jsonResponse = Utilities.makeHttpRequest(url);
            }

            // abort process if there is an error when getting response
            if (jsonResponse == null) return null;

            try {
                JSONObject baseJsonResponse = new JSONObject(jsonResponse);
                JSONArray itemsArray = baseJsonResponse.getJSONArray(TMDB_REVIEW_RESULTS);
                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject item = itemsArray.getJSONObject(i);

                    String author = item.getString(TMDB_REVIEW_AUTHOR);
                    String content = item.getString(TMDB_REVIEW_CONTENT);

                    reviews.add(new Review(author, content));
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return reviews;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(List<Review> reviews) {
            mReviewAdapter.loadReviews(reviews);
            if (mReviewAdapter.getItemCount() > 0) {
                mReviewLayout.setVisibility(View.VISIBLE);
            } else {
                mReviewLayout.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void toggleFavorite() {
        if (isFavorite(mCurrentCursor.getLong(COL_MOVIE_ID))) {
            getActivity().getContentResolver().delete(
                    MovieEntry.buildMovieUri(mCurrentCursor.getLong(COL_MOVIE_ID)),
                    MovieEntry.COLUMN_MOVIE_ID + "=? AND " + MovieEntry.COLUMN_LIST_TYPE + "='favorite'",
                    new String[]{String.valueOf(mCurrentCursor.getLong(COL_MOVIE_ID))});

            mFab.setImageResource(R.drawable.ic_favorite_border_white_24dp);
            Toast.makeText(getActivity(), "Movie removed from favorites!", Toast.LENGTH_SHORT).show();
        } else {
            ContentValues contentValues = new ContentValues();

            contentValues.put(MovieEntry.COLUMN_MOVIE_ID, mCurrentCursor.getLong(COL_MOVIE_ID));
            contentValues.put(MovieEntry.COLUMN_ORIGINAL_TITLE, mCurrentCursor.getString(COL_ORIGINAL_TITLE));
            contentValues.put(MovieEntry.COLUMN_POSTER_PATH, mCurrentCursor.getString(COL_POSTER_PATH));
            contentValues.put(MovieEntry.COLUMN_BACKDROP_PATH, mCurrentCursor.getString(COL_BACKDROP_PATH));
            contentValues.put(MovieEntry.COLUMN_OVERVIEW, mCurrentCursor.getString(COL_OVERVIEW));
            contentValues.put(MovieEntry.COLUMN_RELEASE_DATE, mCurrentCursor.getString(COL_RELEASE_DATE));
            contentValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, mCurrentCursor.getString(COL_VOTE_AVERAGE));
            contentValues.put(MovieEntry.COLUMN_LIST_TYPE, LIST_TYPE_FAVORITE);

            getActivity().getContentResolver().insert(MovieEntry.CONTENT_URI, contentValues);
            mFab.setImageResource(R.drawable.ic_favorite_white_24dp);
            Toast.makeText(getActivity(), "Movie added to favorites!", Toast.LENGTH_SHORT).show();
        }
        getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
    }

    private boolean isFavorite(long movieId) {
        Cursor cursor = getActivity().getContentResolver().query(MovieEntry.CONTENT_URI,
                DETAIL_COLUMNS,
                MovieEntry.COLUMN_MOVIE_ID + "=? AND " + MovieEntry.COLUMN_LIST_TYPE + "='" + LIST_TYPE_FAVORITE + "'",
                new String[]{String.valueOf(movieId)},
                null);
        if (cursor != null) {
            try {
                return cursor.getCount() > 0;
            } finally {
                cursor.close();
            }
        } else {
            return false;
        }
    }
}
