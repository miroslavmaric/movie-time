package com.nightsparrow.movietime.data;

import android.provider.BaseColumns;

/**
 * Created by Miroslav Maric on 2/11/2016.
 *
 * Defines table and column names for the movie database.
 */
public class MovieContract {

    /*
       Inner class that defines the contents of the movie table
     */
    public static final class MovieEntry implements BaseColumns {

        public static final String TABLE_NAME = "movie";

        // ID of the movie, from themovedb
        //public static final String COLUMN_ID = "id";

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

    }
}
