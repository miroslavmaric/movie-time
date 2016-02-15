package com.nightsparrow.movietime.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.Map;

/**
 * Created by Miroslav Maric on 2/11/2016.
 */
public class TestUtilities extends AndroidTestCase {

    static ContentValues createImaginaryMovieValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, 111000);
        testValues.put(MovieContract.MovieEntry.COLUMN_TITLE, "Imagine");
        testValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, "Imaginary Movie");
        testValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE, "en");
        testValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, "Some plot in a imaginary land");
        testValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, "2050-03-15");
        testValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY, 23.567);
        testValues.put(MovieContract.MovieEntry.COLUMN_VOTE_COUNT, 453);
        testValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, 7.89);
        testValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, "thepath");
        testValues.put(MovieContract.MovieEntry.COLUMN_ADULT, 0);
        testValues.put(MovieContract.MovieEntry.COLUMN_VIDEO, 0);

        return testValues;
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {

        for (Map.Entry<String, Object> entry : expectedValues.valueSet()) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static long insertImaginaryMovieValues(Context context) {
        // insert our test records into the database
        MovieDbHelper dbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createImaginaryMovieValues();

        long locationRowId;
        locationRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert Imaginary Movie Values", locationRowId != -1);

        return locationRowId;
    }
}
