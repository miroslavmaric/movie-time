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

    private static final Uri TEST_VIDEO_DIR = MovieContract.VideoEntry.CONTENT_URI;
    private static final Uri TEST_VIDEO_VITH_MOVIE_ID_DIR = MovieContract.VideoEntry.buildVideoWithMovieId(TEST_MOVIE_ID);

    public void testUriMatcher() {
        UriMatcher testMatcher = MovieProvider.buildUriMatcher();

        assertEquals("Error: The MOVIE URI was matched incorrectly.",
                testMatcher.match(TEST_MOVIE_DIR), MovieProvider.MOVIE);

        assertEquals("Error: The MOVIE WITH MOVIE ID URI was matched incorrectly.",
                testMatcher.match(TEST_MOVIE_WITH_MOVIE_ID_DIR), MovieProvider.MOVIE_WITH_MOVIE_ID);

        assertEquals("Error: The VIDEO URI was matched incorrectly.",
                testMatcher.match(TEST_VIDEO_DIR), MovieProvider.VIDEO);

        assertEquals("Error: The VIDEO WITH MOVIE ID URI was matched incorrectly.",
                testMatcher.match(TEST_VIDEO_VITH_MOVIE_ID_DIR), MovieProvider.VIDEO_WITH_MOVIE_ID);
    }
}
