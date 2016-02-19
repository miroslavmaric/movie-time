package com.nightsparrow.movietime;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.nightsparrow.movietime.data.MovieContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG =  MoviesFragment.class.getSimpleName();

    private static final int MOVIES_LOADER = 0;
    // Specify which columns are used in the UI
    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_POPULARITY,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH
    };

    // These indices are tied to MOVIE_COLUMNS. If MOVIE_COLUMNS changes, these
    // must change.
    static final int COL_ID = 0;
    static final int COL_MOVIE_ID = 1;
    static final int COL_MOVIE_TITLE = 2;
    static final int COL_MOVIE_IVERVIEW = 3;
    static final int COL_MOVIE_RELEASE_DATE = 4;
    static final int COL_MOVIE_POPULARITY = 5;
    static final int COL_VOTE_AVERAGE = 6;
    static final int COL_POSTER_PATH = 7;

    private MoviesAdapter mMoviesAdapter;

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
        FetchMoviesTask movieTask = new FetchMoviesTask(getActivity());
        movieTask.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // The CursorAdapter will take data from our cursor and populate the ListView.
        mMoviesAdapter = new MoviesAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.grid_view);
        gridView.setAdapter(mMoviesAdapter);

        // We'll call our MainActivity
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    Intent intent = new Intent(getActivity(), DetailActivity.class)
                            .setData(MovieContract.MovieEntry.buildMovieUri(cursor.getLong(COL_MOVIE_ID)));
                    startActivity(intent);
                }
            }
        });

        // what did we get
        //getContext().getContentResolver().get

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Sort order:  Get sort order from preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort = prefs.getString(getActivity().getString(R.string.pref_sort_key),
                getActivity().getString(R.string.pref_sort_popularity));

        String sortOrder;
        if(sort.equals(getActivity().getString(R.string.pref_sort_popularity))) {
            sortOrder = MovieContract.MovieEntry.COLUMN_POPULARITY + " DESC";
        } else {
            sortOrder = MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE + " DESC";
        }

        Uri movieUri = MovieContract.MovieEntry.CONTENT_URI;

        return new CursorLoader(getActivity(),
                movieUri,
                MOVIE_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        // Check cursor content
            while (cursor.moveToNext())
            {
                String title = cursor.getString(1);
                Log.v(LOG_TAG, "movie title: " + title);

            }

        mMoviesAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mMoviesAdapter.swapCursor(null);
    }
}
