package com.nightsparrow.movietime.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

/**
 * Created by Miroslav Maric on 2/9/2016.
 */
public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(MovieContract.MovieEntry.TABLE_NAME);

        // Clear the database
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);

        SQLiteDatabase db = new MovieDbHelper(this.mContext)
                .getWritableDatabase();
        //Check if we have opened the db for writing
        assertEquals(true, db.isOpen());

        // Have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("Error: The database has not been created correctly", c.moveToFirst());

        // Verify that the table has been created correctly
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that the database doesn't contain movie entry table
        assertTrue("Error: The database was created without the movie entry table",
                        tableNameHashSet.isEmpty());

        // Check if tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.MovieEntry.TABLE_NAME + ")",
                null);
        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(MovieContract.MovieEntry._ID);
        locationColumnHashSet.add(MovieContract.MovieEntry.COLUMN_TITLE);
        locationColumnHashSet.add(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE);
        locationColumnHashSet.add(MovieContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE);
        locationColumnHashSet.add(MovieContract.MovieEntry.COLUMN_OVERVIEW);
        locationColumnHashSet.add(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
        locationColumnHashSet.add(MovieContract.MovieEntry.COLUMN_POPULARITY);
        locationColumnHashSet.add(MovieContract.MovieEntry.COLUMN_VOTE_COUNT);
        locationColumnHashSet.add(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE);
        locationColumnHashSet.add(MovieContract.MovieEntry.COLUMN_POSTER_PATH);
        locationColumnHashSet.add(MovieContract.MovieEntry.COLUMN_ADULT);
        locationColumnHashSet.add(MovieContract.MovieEntry.COLUMN_VIDEO);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while (c.moveToNext());

        // if this fails, it means that the database doesn't contain all of the required movie
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required movie entry columns",
                locationColumnHashSet.isEmpty());
        db.close();

    }

    public void setUp() {

    }

    void deleteDatabase() {

    }

    public long insertMovie() {
        return -1;
    }


    static ContentValues createImaginaryMovieValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
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

    static long insertImaginaryMovieValues(Context context) {
        // insert our test records into the database
        MovieDbHelper dbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestDb.createImaginaryMovieValues();

        long locationRowId;
        locationRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert Imaginary Movie Values", locationRowId != -1);

        return locationRowId;
    }

}
