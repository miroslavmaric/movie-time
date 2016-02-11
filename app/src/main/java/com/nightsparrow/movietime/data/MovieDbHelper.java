package com.nightsparrow.movietime.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import  com.nightsparrow.movietime.data.MovieContract.MovieEntry;
/**
 * Created by Miroslav Maric on 2/11/2016.
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    // If one changes the database schema, one must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "movie.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final  String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY, " +
                MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_ORIGINAL_LANGUAGE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_POPULARITY + " REAL NOT NULL, " +
                MovieEntry.COLUMN_VOTE_COUNT + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                MovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_ADULT + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_VIDEO + " INTEGER NOT NULL" +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
