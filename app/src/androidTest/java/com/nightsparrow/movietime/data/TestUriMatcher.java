package com.nightsparrow.movietime.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by Miroslav Maric on 2/11/2016.
 */
public class TestUriMatcher extends AndroidTestCase {

    private static final long TEST_MOVIE_ID = 222000;  // December 20th, 2014

    // content://com.nightsparrow.movietime.data/movie"
    private static final Uri TEST_MOVIE_DIR = MovieContract.MovieEntry.CONTENT_URI;
    private static final Uri TEST_MOVIE_WITH_MOVIE_ID_DIR = MovieContract.MovieEntry.buildMovieWithMovieId(TEST_MOVIE_ID);

    public void testUriMatcher() {
        UriMatcher testMatcher = MovieProvider.buildUriMatcher();

        assertEquals("Error: The WEATHER URI was matched incorrectly.",
                testMatcher.match(TEST_MOVIE_DIR), MovieProvider.MOVIE);

        assertEquals("Error: The WEATHER WITH LOCATION AND DATE URI was matched incorrectly.",
                testMatcher.match(TEST_MOVIE_WITH_MOVIE_ID_DIR), MovieProvider.MOVIE_WITH_MOVIE_ID);
    }
}
