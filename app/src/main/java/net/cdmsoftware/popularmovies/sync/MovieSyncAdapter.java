package net.cdmsoftware.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import net.cdmsoftware.popularmovies.BuildConfig;
import net.cdmsoftware.popularmovies.R;
import net.cdmsoftware.popularmovies.Utilities;
import net.cdmsoftware.popularmovies.data.MovieContract.MovieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = MovieSyncAdapter.class.getSimpleName();

    // Interval at which to sync with the weather, in seconds.
    private static final int SYNC_INTERVAL = 60 * 60;
    private static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;


    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        final String LIST_TYPE_FAVORITE = "favorite";
        Log.d(LOG_TAG, "onPerformSync Called.");

        // list type: popular, top rate or favorite
        String listType = Utilities.getPreferredListType(this.getContext());

        // do not perform sync if list type is Favorite because it is a local list
        if (listType.equals(LIST_TYPE_FAVORITE)) return;

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String jsonStr;

        try {
            final String API_BASE_URL =
                    "http://api.themoviedb.org/3/movie";
            final String API_KEY = "api_key";

            Uri builtUri = Uri.parse(API_BASE_URL)
                    .buildUpon()
                    .appendPath(listType)
                    .appendQueryParameter(API_KEY, BuildConfig.THEMOVIEDB_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());
            Log.v(LOG_TAG, builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // add newline to make debugging easier
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            jsonStr = buffer.toString();

            // delete old data so we don't build up an endless history
            getContext().getContentResolver().delete(MovieEntry.buildMovieUriByListType(listType),
                    MovieEntry.COLUMN_LIST_TYPE + " = ?",
                    new String[]{listType});

            getDataFromJson(jsonStr, listType);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    private static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private void getDataFromJson(String jsonStr, String listType)
            throws JSONException {

        // the results array
        final String TMDB_RESULTS = "results";

        // results array children that need to be extracted
        final String TMDB_ID = "id";
        final String TMDB_ORIGINAL_TITLE = "original_title";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_BACKDROP_PATH = "backdrop_path";
        final String TMDB_OVERVIEW = "overview";
        final String TMDB_RELEASE_DATE = "release_date";
        final String TMDB_VOTE_AVERAGE = "vote_average";

        try {
            JSONObject baseJsonResponse = new JSONObject(jsonStr);
            JSONArray itemsArray = baseJsonResponse.getJSONArray(TMDB_RESULTS);

            // Insert the new movie information into the database
            Vector<ContentValues> cVVector = new Vector<>(itemsArray.length());

            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject item = itemsArray.getJSONObject(i);

                long id = item.getLong(TMDB_ID);
                String originalTitle = item.getString(TMDB_ORIGINAL_TITLE);
                String posterImage = item.getString(TMDB_POSTER_PATH);
                String backdropImage = item.getString(TMDB_BACKDROP_PATH);
                String overview = item.getString(TMDB_OVERVIEW);
                String releaseDate = item.getString(TMDB_RELEASE_DATE);
                Double voteAverage = item.getDouble(TMDB_VOTE_AVERAGE);

                ContentValues contentValues = new ContentValues();

                contentValues.put(MovieEntry.COLUMN_MOVIE_ID, id);
                contentValues.put(MovieEntry.COLUMN_ORIGINAL_TITLE, originalTitle);
                contentValues.put(MovieEntry.COLUMN_POSTER_PATH, posterImage);
                contentValues.put(MovieEntry.COLUMN_BACKDROP_PATH, backdropImage);
                contentValues.put(MovieEntry.COLUMN_OVERVIEW, overview);
                contentValues.put(MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
                contentValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, voteAverage);
                contentValues.put(MovieEntry.COLUMN_LIST_TYPE, listType);

                cVVector.add(contentValues);
            }

            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, cvArray);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    private static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        MovieSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}