package com.miroslavmaric.movietime.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.miroslavmaric.movietime.BuildConfig;
import com.miroslavmaric.movietime.data.MovieContract;

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
 * Created by Miroslav Maric on 2/20/2016.
 */
public class MovieTimeService extends IntentService {

    // TODO: Consider using async adapter for more efficient use
    // of battery and smoother user experience

    private final String LOG_TAG = MovieTimeService.class.getSimpleName();
    public static final String SORT_QUERY_EXTRA = "sqe";

    public MovieTimeService() {
        super("MovieTimeService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Clear these two outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String moviesJsonStr = null;

        String sortQuery = intent.getStringExtra(SORT_QUERY_EXTRA);
        String page = "1";

        try {
            final String MOVIES_BASE_URL = "https://api.themoviedb.org/3/discover/movie";
            final String SORT_BY_PARAM = "sort_by";
            final String API_PARAM = "api_key";
            final String PAGE_PARAM = "page";

            Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_BY_PARAM, sortQuery)
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
                return;
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
                return;
            }

            moviesJsonStr = buffer.toString();
            getMoviesDataFromJson(moviesJsonStr);

            //Log.v(LOG_TAG, "Data: " + moviesJsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "IO Error: " + e);
            // If the code didn't successfully get the movie data, there's no point in
            // attempting to parse it.
            return;
        } catch (JSONException  e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } catch (Exception  e) {
            Log.e(LOG_TAG, "ERROR " + e.getMessage(), e);
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

        return;
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
            Vector<ContentValues> movieCvVector = new Vector<ContentValues>(moviesArray.length());

            // Content values for movie videos
            Vector<ContentValues> videoCvVector = new Vector<ContentValues>();

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

                movieValues.put(MovieContract.MovieEntry._ID, movieId);
                movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, title);
                movieValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, originalTitle);
                movieValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE, originalLanguage);
                movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, overview);
                movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
                movieValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY, popularity);
                movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_COUNT, voteCount);
                movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, voteAverage);
                movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, posterPath);
                movieValues.put(MovieContract.MovieEntry.COLUMN_ADULT, adult ? 1 : 0);
                movieValues.put(MovieContract.MovieEntry.COLUMN_VIDEO, video ? 1 : 0);

                movieCvVector.add(movieValues);

                // Fetch videos associated with this movie
                String videosJsonStr = fetchVideosForMovie(movieId);

                // Content values for movie videos
                ContentValues cv = getVideoDataFromJson(videosJsonStr);
                videoCvVector.add(cv);
            }

            int inserted = 0;
            // add to database
            if (movieCvVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[movieCvVector.size()];
                movieCvVector.toArray(cvArray);
                inserted = this.getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, cvArray);

                if (videoCvVector.size() > 0) {
                    cvArray = new ContentValues[videoCvVector.size()];
                    videoCvVector.toArray(cvArray);
                    this.getContentResolver().bulkInsert(MovieContract.VideoEntry.CONTENT_URI, cvArray);
                }

                // TODO: Delete excess movies and videos
                // keep only a constant number of most popular or rated
                // (depends on current pref) movies in database
                // OR add a pref and let user decide on the number (100, 200 or 300)
                // need to keep database relatively small
                // PS: also delete videos associated with these movies

                // Get the movies which are not among N most popular ones
                String where = MovieContract.MovieEntry._ID + " not in " +
                        " ( select " + MovieContract.MovieEntry._ID + " from " +
                        MovieContract.MovieEntry.TABLE_NAME + " order by " +
                        MovieContract.MovieEntry.COLUMN_POPULARITY + " desc limit 20);";

                int rowsDeleted = this.getContentResolver().delete(
                        MovieContract.MovieEntry.CONTENT_URI,
                        where,
                        null);


                Log.v(LOG_TAG, "Number of deleted rows: " + rowsDeleted);
            }

            Log.d(LOG_TAG, "Fetch Movies Complete. " + inserted + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private String fetchVideosForMovie(long movieId) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String videosJsonStr = null;

        try {
            final String MOVIES_BASE_URL = " http://api.themoviedb.org/3/movie/" + movieId + "/videos";
            final String API_PARAM = "api_key";

            Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                    .appendQueryParameter(API_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();

            URL videoUrl = new URL(builtUri.toString());

            //Log.v(LOG_TAG, " " + videoUrl);

            urlConnection = (HttpURLConnection) videoUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();

            if (inputStream == null) {
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                // Convert to more human friendly format
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }

            videosJsonStr = buffer.toString();

            // Log.v(LOG_TAG, "Data: " + videosJsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "IO Error: " + e);
            return null;
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

        return videosJsonStr;
    }

    private ContentValues getVideoDataFromJson(String moviesJsonString) throws JSONException {
        ContentValues videoValues = new ContentValues();

        // Names of the JSON objects that need to be extracted.
        final String TMDB_RESULTS = "results";
        final String TMDB_MOVIE_KEY = "id";

        final String TMDB_ID = "id";
        final String TMDB_ISO = "iso_639_1";
        final String TMDB_KEY = "key";
        final String TMBD_NAME = "name";
        final String TMDB_SITE = "site";
        final String TMDB_SIZE = "size";
        final String TMDB_TYPE = "type";

        try {
            JSONObject moviesJson = new JSONObject(moviesJsonString);
            long movieKey = moviesJson.getLong(TMDB_MOVIE_KEY);
            JSONArray moviesArray = moviesJson.getJSONArray(TMDB_RESULTS);

            for (int i = 0; i < moviesArray.length(); i++) {

                // Get the JSON object representing the movie
                JSONObject video = moviesArray.getJSONObject(i);

                String videoId = video.getString(TMDB_ID);
                String iso = video.getString(TMDB_ISO);
                String key = video.getString(TMDB_KEY);
                String name = video.getString(TMBD_NAME);
                String site = video.getString(TMDB_SITE);
                String size = video.getString(TMDB_SIZE);
                String type = video.getString(TMDB_TYPE);

                videoValues.put(MovieContract.VideoEntry._ID, videoId);
                videoValues.put(MovieContract.VideoEntry.COLUMN_MOVIE_KEY, movieKey);
                videoValues.put(MovieContract.VideoEntry.COLUMN_ISO, iso);
                videoValues.put(MovieContract.VideoEntry.COLUMN_KEY, key);
                videoValues.put(MovieContract.VideoEntry.COLUMN_NAME, name);
                videoValues.put(MovieContract.VideoEntry.COLUMN_SITE, site);
                videoValues.put(MovieContract.VideoEntry.COLUMN_SIZE, size);
                videoValues.put(MovieContract.VideoEntry.COLUMN_TYPE, type);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return videoValues;
    }
}
