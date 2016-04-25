package com.miroslavmaric.movietime.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

/**
 * Created by Miroslav Maric on 2/9/2016.
 */
public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    /*
        Before every test, start with clean state
    */
    public void setUp() {
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
    }

    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(MovieContract.MovieEntry.TABLE_NAME);
        tableNameHashSet.add(MovieContract.VideoEntry.TABLE_NAME);

        // Clear the database
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);

        SQLiteDatabase db = new MovieDbHelper(this.mContext)
                .getWritableDatabase();
        //Check if we have opened the db for writing
        assertEquals(true, db.isOpen());

        // Have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("Error: The database has not been created correctly", c.moveToFirst());

        // Verify that the tables have been created correctly
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );


        // if this fails, it means that the database doesn't contain both the movie entry
        // and video entry tables
        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Check if tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.MovieEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: Unable to query the database for movie table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> movieColumnHashSet = new HashSet<String>();
        movieColumnHashSet.add(MovieContract.MovieEntry._ID);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_TITLE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_OVERVIEW);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_POPULARITY);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_VOTE_COUNT);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_POSTER_PATH);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_ADULT);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_VIDEO);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            movieColumnHashSet.remove(columnName);
        } while (c.moveToNext());

        // if this fails, it means that the database doesn't contain all of the required movie
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required movie entry columns",
                movieColumnHashSet.isEmpty());


        // Check video table columns
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.VideoEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: Unable to query the database for video table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> videoColumnHashSet = new HashSet<String>();
        videoColumnHashSet.add(MovieContract.VideoEntry._ID);
        videoColumnHashSet.add(MovieContract.VideoEntry.COLUMN_MOVIE_KEY);
        videoColumnHashSet.add(MovieContract.VideoEntry.COLUMN_ISO);
        videoColumnHashSet.add(MovieContract.VideoEntry.COLUMN_KEY);
        videoColumnHashSet.add(MovieContract.VideoEntry.COLUMN_NAME);
        videoColumnHashSet.add(MovieContract.VideoEntry.COLUMN_SITE);
        videoColumnHashSet.add(MovieContract.VideoEntry.COLUMN_SIZE);
        videoColumnHashSet.add(MovieContract.VideoEntry.COLUMN_TYPE);

        columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            videoColumnHashSet.remove(columnName);
        } while (c.moveToNext());

        // if this fails, it means that the database doesn't contain all of the required movie
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required video entry columns",
                videoColumnHashSet.isEmpty());

        db.close();

    }

    public void testMovieTable() {
        insertMovie();
    }

    public long insertMovie() {
        // 1. Get a reference to the writable database.
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // 2. Create values for the table
        ContentValues testValues = TestUtilities.createImaginaryMovieValues();

        // 3. Insert ContentValues into database and get a row ID back
        long movieRowId;
        movieRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(movieRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // 4. Query the database and receive a Cursor back
        // A cursor is a primary interface to the query results.
        Cursor cursor = db.query(
                MovieContract.MovieEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue("Error: No Records returned from movie query", cursor.moveToFirst());

        // 5. Validate data in resulting Cursor with the original ContentValues
        TestUtilities.validateCurrentRecord("Error: Movie Query Validation Failed",
                cursor, testValues);

        // Finally, close the cursor and database
        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse("Error: More than one record returned from movie query",
                cursor.moveToNext());

        // 6. Close Cursor and Database
        cursor.close();
        db.close();
        return movieRowId;
    }

    public void testVideoTable() {

        long movieRowId = insertMovie();

        // Make sure that the ID is valid
        assertFalse("Error: Location Not Inserted Correctly", movieRowId == -1L);

        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues videoValues = TestUtilities.createVideoValues(movieRowId);

        long videoRow = db.insert(MovieContract.VideoEntry.TABLE_NAME, null, videoValues);
        assertTrue(videoRow != -1);

        Cursor videoCursor = db.query(
                MovieContract.VideoEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        assertTrue("Error: No Records returned from video query", videoCursor.moveToFirst());

        TestUtilities.validateCurrentRecord("testInsertReadDb videoEntry failed to validate",
                videoCursor, videoValues);

        assertFalse("Error: More than one record returned from video query",
                videoCursor.moveToNext());

        videoCursor.close();
        dbHelper.close();
    }
}
