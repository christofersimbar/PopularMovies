package net.cdmsoftware.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class MovieContract {
    public static final String CONTENT_AUTHORITY = "net.cdmsoftware.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MOVIE = "movie";


    public static final class MovieEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static final String TABLE_NAME = "movie";

        // column names mapping
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";
        public static final String COLUMN_LIST_TYPE = "list_type";

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMovieUriByListType(String listType){
            final String PATH_LIST = "list";
            return CONTENT_URI.buildUpon().appendPath(PATH_LIST).appendPath(listType).build();
        }
    }
}
