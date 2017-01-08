package net.cdmsoftware.popularmovies;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import net.cdmsoftware.popularmovies.data.MovieContract;

import java.util.Map;
import java.util.Set;

public class TestUtilities extends AndroidTestCase {
    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    /*
        Students: Use this to create some default weather values for your database tests.
     */
    static ContentValues createMovieValues() {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, "original title");
        weatherValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, "poster path");
        weatherValues.put(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH, "backdrop path");
        weatherValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, "overview");
        weatherValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, 1419033600L);
        weatherValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, 4.3);

        return weatherValues;
    }
}
