package com.nightsparrow.movietime;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class MoviesFragment extends Fragment {

    private final String LOG_TAG =  MoviesFragment.class.getSimpleName();

    public ArrayAdapter<String> mMoviesAdapter;

    public MoviesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Allow this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.moviesfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateMovies();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateMovies() {
        FetchMoviesTask weatherTask = new FetchMoviesTask();
        weatherTask.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String[] data = {
                "Tommorow - 60",
                "Wasteland - 210",
                "Nimmerland - 120",
                "Drake LAke - 140"
        };

        ArrayList<String> dataList = new ArrayList<>(Arrays.asList(data));

        mMoviesAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_movie,
                R.id.list_item_movie_textview,
                dataList
        );

        ListView listview = (ListView) rootView.findViewById(R.id.listview_movie);
        listview.setAdapter(mMoviesAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String movie = mMoviesAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, movie);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    public class FetchMoviesTask extends AsyncTask<Void, Void, String[]> {
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(Void... params) {
            // Clear these two outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;

            // Get the sort by option from the preferences
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sort = prefs.getString(getString(R.string.pref_sort_key),
                    getString(R.string.pref_sort_popularity));

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

                Log.v(LOG_TAG, " " + url );

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
}
