package net.cdmsoftware.popularmovies.models;

import android.database.Cursor;

import net.cdmsoftware.popularmovies.data.MovieContract.MovieEntry;

public class Movie {
    private long mID;
    private String mTitle;
    private String mReleaseDate;
    private String mPosterImage;
    private Double mVote;

    private Movie(long id, String title, String posterImage, String releaseDate, Double vote) {
        this.mID = id;
        this.mTitle = title;
        this.mReleaseDate = releaseDate;
        this.mPosterImage = posterImage;
        this.mVote = vote;
    }

    public long getID() {
        return mID;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getPosterImage() {
        return mPosterImage;
    }

    public static Movie fromCursor(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_ID));
        String originalTitle = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_ORIGINAL_TITLE));
        String posterImage = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER_PATH));
        String releaseDate = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE));
        double voteAverage = cursor.getDouble(cursor.getColumnIndex(MovieEntry.COLUMN_VOTE_AVERAGE));

        return new Movie(id, originalTitle, posterImage, releaseDate, voteAverage);
    }
}
