package com.nightsparrow.movietime;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Miroslav Maric on 2/11/2016.
 */
public class FetchMoviesTask extends AsyncTask<Void, Void, String[]> {
    private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

    private ArrayAdapter<String> mMoviesAdapter;
    private final Context mContext;

    public FetchMoviesTask(Context context, ArrayAdapter<String> moviesAdapter) {
        mContext = context;
        mMoviesAdapter = moviesAdapter;
    }

    @Override
    protected String[] doInBackground(Void... params) {
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
            Log.v(LOG_TAG, "Data: " + moviesJsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "IO Error: " + e);
            // If the code didn't successfully get the movie data, there's no point in
            // attempting to parse it.
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error: " + e);
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

        try {
            return getMoviesDataFromJson(moviesJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String[] result) {
        if (result != null) {
            mMoviesAdapter.clear();
            for (String movieStr : result) {
                mMoviesAdapter.add(movieStr);
            }
        }
    }

    private String[] getMoviesDataFromJson(String moviesJsonString) throws JSONException {

        // Names of the JSON objects that need to be extracted.
        final String TMDB_RESULTS = "results";
        final String TMDB_ORIGINAL_TITLE = "original_title";
        final String TMDB_TITLE = "title";  // localization ...
        final String TMDB_OVERVIEW = "overview";
        final String TMDB_RELEASE_DATE = "release_date";    // yyyy-mm-dd
        final String TMDB_POPULARITY = "popularity";
        final String TMDB_VOTE_COUNT = "vote_count";
        final String TMDB_VOTE_AVERAGE = "vote_average";

        // TODO: Will be needed after UI upgrade
        final String TMDB_POSTER_PATH = "poster_path";

        JSONObject moviesJson = new JSONObject(moviesJsonString);
        JSONArray moviesArray = moviesJson.getJSONArray(TMDB_RESULTS);


        String[] resultStrs = new String[moviesArray.length()];
        for(int i = 0; i < moviesArray.length(); i++) {

            // Get the JSON object representing the movie
            JSONObject movie = moviesArray.getJSONObject(i);

            String title = movie.getString(TMDB_TITLE);
            String releaseDate = movie.getString(TMDB_RELEASE_DATE);
            String popularity = movie.getString(TMDB_POPULARITY);
            String vote_average = movie.getString(TMDB_VOTE_AVERAGE);

            // TODO: Refactor this to return additional attributes whn the
            // data provider is implemented
            resultStrs[i] = title + " - " + releaseDate + " - " + vote_average;
        }

        for (String s : resultStrs) {
            Log.v(LOG_TAG, "Movie entry: " + s);
        }

        return resultStrs;
    }
}