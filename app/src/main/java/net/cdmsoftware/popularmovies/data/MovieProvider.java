package net.cdmsoftware.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.cdmsoftware.popularmovies.data.MovieContract.MovieEntry;

import static android.R.attr.id;

public class MovieProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    static final int MOVIES = 100;
    static final int MOVIE_ID = 101;
    static final int MOVIE_LIST = 102;

    static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY,MovieContract.PATH_MOVIE+"/list/*", MOVIE_LIST);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY,MovieContract.PATH_MOVIE+"/#",MOVIE_ID);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY,MovieContract.PATH_MOVIE,MOVIES);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mOpenHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                cursor = database.query(MovieEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case MOVIE_ID:
                selection = MovieEntry.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(MovieEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case MOVIE_LIST:
                selection = MovieEntry.COLUMN_LIST_TYPE + "=?";
                selectionArgs = new String[] { uri.getLastPathSegment() };
                cursor = database.query(MovieEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE_LIST:
                return MovieContract.MovieEntry.CONTENT_LIST_TYPE;
            case MOVIE_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri+ " with match " + match);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIES:
                long _id = db.insert(MovieEntry.TABLE_NAME, null, contentValues);
                if ( _id > 0 )
                    returnUri = ContentUris.withAppendedId(uri, id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch (match) {
            case MOVIE_ID: {
                rowsDeleted = db.delete(MovieEntry.TABLE_NAME,selection,selectionArgs);
                break;
            }
            case MOVIE_LIST: {
                selection = MovieEntry.COLUMN_LIST_TYPE + "=?";
                selectionArgs = new String[] { uri.getLastPathSegment() };
                rowsDeleted = db.delete(MovieEntry.TABLE_NAME,selection,selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if ((rowsDeleted != 0)||(selection==null)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String s, String[] strings) {
        // don't need to implement update. If user choose to remove a movie from fave list,
        // then we can just delete the movie from db
        return 0;
    }
}
