package com.nightsparrow.movietime;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nightsparrow.movietime.data.MovieContract;
import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    private static final int DETAIL_LOADER = 0;

    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_POPULARITY,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH
    };

    // these constants correspond to the projection defined above, and must change if the
    // projection changes
    private static final int COL_MOVIE_ID = 0;
    private static final int COL_MOVIE_TITLE = 1;
    private static final int COL_MOVIE_IVERVIEW = 2;
    private static final int COL_MOVIE_RELEASE_DATE = 3;
    private static final int COL_MOVIE_POPULARITY = 4;
    private static final int COL_VOTE_AVERAGE = 5;
    private static final int COL_POSTER_PATH = 6;

    private String mMoviesString;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_detail, container, false);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader");
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return null;
        }


        Uri data = intent.getData();
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        CursorLoader loader = new CursorLoader(
                getActivity(),
                intent.getData(),
                MOVIE_COLUMNS,
                null,
                null,
                null
        );

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");
        if (!data.moveToFirst()) { return; }

        String title = data.getString(COL_MOVIE_TITLE);
        double popularity = data.getDouble(COL_MOVIE_POPULARITY);
        double rating = data.getDouble(COL_VOTE_AVERAGE);
        mMoviesString = String.format("%s \n %f \n %f", title, popularity, rating);

        TextView detailTextView = (TextView)getView().findViewById(R.id.text_view_detail);
        detailTextView.setText(mMoviesString);

        ImageView iv = (ImageView)getView().findViewById(R.id.image_view_detail);;

        // Build the image url
        final String TMD_BASE = "http://image.tmdb.org/t/p/";
        final String SIZE = "w185";
        final String POSTER_PATH =
                data.getString(COL_POSTER_PATH);

        Uri posterUri = Uri.parse(TMD_BASE).buildUpon()
                .appendPath(SIZE)
                .appendPath(POSTER_PATH)
                .build();

        // Use Picasso to fetch the image from the web and load it into image view
        Picasso.with(getContext()).load(posterUri).into(iv);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

}
