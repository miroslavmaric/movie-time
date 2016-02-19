package com.nightsparrow.movietime;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nightsparrow.movietime.data.MovieContract;
import com.nightsparrow.movietime.data.MovieContract.MovieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by Miroslav Maric on 2/11/2016.
 */
public class FetchMoviesTask extends AsyncTask<Void, Void, Void> {
    private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

    private final Context mContext;

    public FetchMoviesTask(Context context) {
        mContext = context;
    }

    /**
     * Helper method to handle insertion of a new movie in the movie database.
     *
     * @return the row ID of the added movie.
     */
    long addMovie(long movieId, String title, String originalTitle,
                  String originalLanguage, String overview, String releaseDate,
                  double popularity, long voteCount, double vote_average,
                  String posterPath, boolean adult, boolean video) {
        long movieRowId;

        // First, check if the movie with this city name exists in the db
        Cursor movieCursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                new String[]{MovieContract.MovieEntry._ID},
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{String.valueOf(movieId)},
                null);

        if (movieCursor.moveToFirst()) {
            int movieIdIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry._ID);
            movieRowId = movieCursor.getLong(movieIdIndex);
        } else {
            // First create a ContentValues object to hold the data to be added.
            ContentValues movieValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movieId);
            movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, title);
            movieValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, originalTitle);
            movieValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE, originalLanguage);
            movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, overview);
            movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
            movieValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY, popularity);
            movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_COUNT, voteCount);
            movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, vote_average);
            movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, posterPath);
            movieValues.put(MovieContract.MovieEntry.COLUMN_ADULT, adult ? 1 : 0);
            movieValues.put(MovieContract.MovieEntry.COLUMN_VIDEO, video ? 1 : 0);

            // Finally, insert movie data into the database.
            Uri insertedUri = mContext.getContentResolver().insert(
                    MovieContract.MovieEntry.CONTENT_URI,
                    movieValues
            );

            // The resulting URI contains the ID for the row.  Extract the movieRowId from the Uri.
            movieRowId = ContentUris.parseId(insertedUri);
        }

        movieCursor.close();

        return movieRowId;
    }

    @Override
    protected Void doInBackground(Void... params) {
        // Clear these two outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String moviesJsonStr = null;

        // Get the sort by option from the preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String sort = prefs.getString(mContext.getString(R.string.pref_sort_key),
                mContext.getString(R.string.pref_sort_popularity));

        String page = "1";

        try {
            final String MOVIES_BASE_URL = "https://api.themoviedb.org/3/discover/movie";
            final String SORT_BY_PARAM = "sort_by";
            final String API_PARAM = "api_key";
            final String PAGE_PARAM = "page";

            Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_BY_PARAM, sort)
                    .appendQueryParameter(PAGE_PARAM, page)
                    .appendQueryParameter(API_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            Log.v(LOG_TAG, " " + url);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();

            if (inputStream == null) {
                // Nothing to do.
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }

            moviesJsonStr = buffer.toString();
            getMoviesDataFromJson(moviesJsonStr);

            Log.v(LOG_TAG, "Data: " + moviesJsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "IO Error: " + e);
            // If the code didn't successfully get the movie data, there's no point in
            // attempting to parse it.
            return null;
        } catch (JSONException  e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (null != urlConnection) {
                urlConnection.disconnect();
            }
            if (null != reader) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream " + e);
                }
            }
        }

        return null;
    }

    private void getMoviesDataFromJson(String moviesJsonString) throws JSONException {

        // Names of the JSON objects that need to be extracted.
        final String TMDB_RESULTS = "results";

        final String TMDB_ID = "id";
        final String TMDB_TITLE = "title";  // localization ...
        final String TMDB_ORIGINAL_TITLE = "original_title";
        final String TMBD_ORIGINAL_LANGUAGE = "original_language";
        final String TMDB_OVERVIEW = "overview";
        final String TMDB_RELEASE_DATE = "release_date";    // yyyy-mm-dd
        final String TMDB_POPULARITY = "popularity";
        final String TMDB_VOTE_COUNT = "vote_count";
        final String TMDB_VOTE_AVERAGE = "vote_average";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMBS_ADULT = "adult";
        final String TMBD_VIDEO = "video";

        try {
            JSONObject moviesJson = new JSONObject(moviesJsonString);
            JSONArray moviesArray = moviesJson.getJSONArray(TMDB_RESULTS);

            // Insert the new movie information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(moviesArray.length());

            for (int i = 0; i < moviesArray.length(); i++) {

                // Get the JSON object representing the movie
                JSONObject movie = moviesArray.getJSONObject(i);

                long movieId = movie.getLong(TMDB_ID);
                String title = movie.getString(TMDB_TITLE);
                String originalTitle = movie.getString(TMDB_ORIGINAL_TITLE);
                String originalLanguage = movie.getString(TMBD_ORIGINAL_LANGUAGE);
                String overview = movie.getString(TMDB_OVERVIEW);
                String releaseDate = movie.getString(TMDB_RELEASE_DATE);
                double popularity = movie.getDouble(TMDB_POPULARITY);
                long voteCount = movie.getLong(TMDB_VOTE_COUNT);
                double voteAverage = movie.getDouble(TMDB_VOTE_AVERAGE);
                String posterPath = movie.getString(TMDB_POSTER_PATH).substring(1); // skip '/'
                boolean adult = movie.getBoolean(TMBS_ADULT);
                boolean video = movie.getBoolean(TMBD_VIDEO);


                ContentValues movieValues = new ContentValues();

                movieValues.put(MovieEntry.COLUMN_MOVIE_ID, movieId);
                movieValues.put(MovieEntry.COLUMN_TITLE, title);
                movieValues.put(MovieEntry.COLUMN_ORIGINAL_TITLE, originalTitle);
                movieValues.put(MovieEntry.COLUMN_ORIGINAL_LANGUAGE, originalLanguage);
                movieValues.put(MovieEntry.COLUMN_OVERVIEW, overview);
                movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
                movieValues.put(MovieEntry.COLUMN_POPULARITY, popularity);
                movieValues.put(MovieEntry.COLUMN_VOTE_COUNT, voteCount);
                movieValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, voteAverage);
                movieValues.put(MovieEntry.COLUMN_POSTER_PATH, posterPath);
                movieValues.put(MovieEntry.COLUMN_ADULT, adult ? 1 : 0);
                movieValues.put(MovieEntry.COLUMN_VIDEO, video ? 1 : 0);

                cVVector.add(movieValues);
            }

            int inserted = 0;
            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = mContext.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, cvArray);
            }

            Log.d(LOG_TAG, "FetchMoviesTask Complete. " + inserted + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

    }
}