package com.nightsparrow.movietime;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.nightsparrow.movietime.data.MovieContract;

/**
 * Created by Miroslav Maric on 2/15/2016.
 */
public class TestFetchMoviesTask extends AndroidTestCase {
    static final long ADD_MOVIE_ID = 293660;
    static final String ADD_TITLE = "Deadpool";
    static final String ADD_ORIGINAL_TITLE = "Deadpool";
    static final String ADD_ORIGINAL_LANGUAGE = "en";
    static final String ADD_OVERVIEW = "Based upon Marvel Comics’ most unconventional anti-hero, ...";
    static final String ADD_RELEASE_DATE = "2016-02-09";
    static final double ADD_POPULARITY = 102.052029;
    static final long ADD_VOTE_COUNT = 617;
    static final double ADD_VOTE_AVERAGE = 7.14;
    static final String ADD_POSTER_PATH = "nbIrDhOtUpdD9HKDBRy02a8VhpV.jpg";
    static final boolean ADD_ADULT = false;
    static final boolean ADD_VIDEO = false;

    /*
        This test will only run on API level 11 and higher because of a requirement in the
        content provider.
     */
    @TargetApi(11)
    public void testAddMovie() {
        // start from a clean state
        getContext().getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{String.valueOf(ADD_MOVIE_ID)});

        FetchMoviesTask fmt = new FetchMoviesTask(getContext(), null);
        long movieRowId = fmt.addMovie(
                ADD_MOVIE_ID,
                ADD_TITLE,
                ADD_ORIGINAL_TITLE,
                ADD_ORIGINAL_LANGUAGE,
                ADD_OVERVIEW,
                ADD_RELEASE_DATE,
                ADD_POPULARITY,
                ADD_VOTE_COUNT,
                ADD_VOTE_AVERAGE,
                ADD_POSTER_PATH,
                ADD_ADULT,
                ADD_VIDEO
        );

        // does addLocation return a valid record ID?
        assertFalse("Error: addMovie returned an invalid ID on insert",
                movieRowId == -1);

        // test all this twice
        for ( int i = 0; i < 2; i++ ) {

            // does the ID point to our location?
            Cursor movieCursor = getContext().getContentResolver().query(
                    MovieContract.MovieEntry.CONTENT_URI,
                    new String[]{
                            MovieContract.MovieEntry._ID,
                            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
                            MovieContract.MovieEntry.COLUMN_TITLE,
                            MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
                            MovieContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE,
                            MovieContract.MovieEntry.COLUMN_OVERVIEW,
                            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
                            MovieContract.MovieEntry.COLUMN_POPULARITY,
                            MovieContract.MovieEntry.COLUMN_VOTE_COUNT,
                            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
                            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
                            MovieContract.MovieEntry.COLUMN_ADULT,
                            MovieContract.MovieEntry.COLUMN_VIDEO
                    },
                    MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{String.valueOf(ADD_MOVIE_ID)},
                    null);

            // these match the indices of the projection
            if (movieCursor.moveToFirst()) {
                assertEquals("Error: the queried value of movieRowId does not match the returned value" +
                        "from addMovie", movieCursor.getLong(0), movieRowId);
                assertEquals("Error: the queried value of movieId is incorrect",
                        movieCursor.getLong(1), ADD_MOVIE_ID);
                assertEquals("Error: the queried value of movie title is incorrect",
                        movieCursor.getString(2), ADD_TITLE);
                assertEquals("Error: the queried value of original title is incorrect",
                        movieCursor.getString(3), ADD_ORIGINAL_TITLE);
                assertEquals("Error: the queried value of original language is incorrect",
                        movieCursor.getString(4), ADD_ORIGINAL_LANGUAGE);
                assertEquals("Error: the queried value of movie overview is incorrect",
                        movieCursor.getString(5), ADD_OVERVIEW);
                assertEquals("Error: the queried value of release date is incorrect",
                        movieCursor.getString(6), ADD_RELEASE_DATE);
                assertEquals("Error: the queried value of popularity is incorrect",
                        movieCursor.getDouble(7), ADD_POPULARITY);
                assertEquals("Error: the queried value of vote count is incorrect",
                        movieCursor.getLong(8), ADD_VOTE_COUNT);
                assertEquals("Error: the queried value of vote average is incorrect",
                        movieCursor.getDouble(9), ADD_VOTE_AVERAGE);
                assertEquals("Error: the queried value of poster path is incorrect",
                        movieCursor.getString(10), ADD_POSTER_PATH);
                assertEquals("Error: the queried value of adult indicator is incorrect",
                        movieCursor.getInt(11), ADD_ADULT ? 1 : 0);
                assertEquals("Error: the queried value of video indicator is incorrect",
                        movieCursor.getInt(12), ADD_VIDEO ? 1 : 0);
            } else {
                fail("Error: the id you used to query returned an empty cursor");
            }

            // there should be no more records
            assertFalse("Error: there should be only one record returned from a movie query",
                    movieCursor.moveToNext());

            // add the movie again
            long newMovieRowId = fmt.addMovie(
                    ADD_MOVIE_ID,
                    ADD_TITLE,
                    ADD_ORIGINAL_TITLE,
                    ADD_ORIGINAL_LANGUAGE,
                    ADD_OVERVIEW,
                    ADD_RELEASE_DATE,
                    ADD_POPULARITY,
                    ADD_VOTE_COUNT,
                    ADD_VOTE_AVERAGE,
                    ADD_POSTER_PATH,
                    ADD_ADULT,
                    ADD_VIDEO
            );

            assertEquals("Error: inserting a movie again should return the same ID",
                    movieRowId, newMovieRowId);
        }
        // reset our state back to normal
        getContext().getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{String.valueOf(ADD_MOVIE_ID)});

        // clean up the test so that other tests can use the content provider
        getContext().getContentResolver().
                acquireContentProviderClient(MovieContract.MovieEntry.CONTENT_URI).
                getLocalContentProvider().shutdown();
    }
}
