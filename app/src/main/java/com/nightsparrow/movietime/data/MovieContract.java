package com.nightsparrow.movietime.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Miroslav Maric on 2/11/2016.
 *
 * Defines table and column names for the movie database.
 */
public class MovieContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.nightsparrow.movietime";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content:/com.nightsparrow.movietime/movie/ is a valid path for
    // looking at movie data. content://com.nightsparrow.movietim/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    public static final String PATH_MOVIE= "movie";

    /*
       Inner class that defines the contents of the movie table
     */
    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;


        public static final String TABLE_NAME = "movie";

        // ID of the movie, from themovedb
        public static final String COLUMN_MOVIE_ID = "id";

        // Translated title of the movie
        public static final String COLUMN_TITLE = "title";

        //  Title of the movie in the original language
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";

        // Original language of the movie (en, fr, ...)
        public static final String COLUMN_ORIGINAL_LANGUAGE = "original_language";

        // Overview of the plot
        public static final String COLUMN_OVERVIEW = "overview";

        // Release date of the movie
        public static final String COLUMN_RELEASE_DATE = "release_date";

        // Popularity, measured by the number of recent views
        // Details of the measurement are unknown
        public static final String COLUMN_POPULARITY = "popularity";

        // Number of votes
        public static final String COLUMN_VOTE_COUNT = "vote_count";

        // The average of all votes for this movie
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";

        // A path to the movie poster
        public static final String COLUMN_POSTER_PATH = "poster_path";

        // Indicates if this is an adult title
        public static final String COLUMN_ADULT = "adult";

        // Indicates if this movie has associated trailer
        public static final String COLUMN_VIDEO = "video";


        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMovieWithMovieId(long movieId) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(movieId)).build();
        }

        public static long getMovieIdFromUri(Uri uri) {
            long movieId = Long.parseLong(uri.getPathSegments().get(1));
            return movieId;
        }
    }

}
