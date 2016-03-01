package com.nightsparrow.movietime.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import java.util.Map;

/**
 * Created by Miroslav Maric on 2/11/2016.
 */
public class TestUtilities extends AndroidTestCase {

    static ContentValues createImaginaryMovieValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(MovieContract.MovieEntry._ID, 12412);
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

    static ContentValues createVideoValues(long movieRowId) {
        ContentValues testValues = new ContentValues();
        testValues.put(MovieContract.VideoEntry._ID, 12321);
        testValues.put(MovieContract.VideoEntry.COLUMN_MOVIE_KEY, movieRowId);
        testValues.put(MovieContract.VideoEntry.COLUMN_ISO, "en");
        testValues.put(MovieContract.VideoEntry.COLUMN_KEY, "12xf42");
        testValues.put(MovieContract.VideoEntry.COLUMN_NAME, "Some Trailer");
        testValues.put(MovieContract.VideoEntry.COLUMN_SITE, "Youtube");
        testValues.put(MovieContract.VideoEntry.COLUMN_SIZE, "1020p");
        testValues.put(MovieContract.VideoEntry.COLUMN_TYPE, "trailer");

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

    /*
        This only tests that the onChange function is called; it does not test that the
        correct Uri is returned. PollingCheck class has been taken from the Android
        CTS tests.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
