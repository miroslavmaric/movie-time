package com.nightsparrow.movietime;

import android.content.Intent;
import android.content.pm.PackageManager;
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

import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";

    private Uri mMovieUri;

    private static final int DETAIL_LOADER = 0;
    private static final int VIDEO_LOADER = 1;

    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_POPULARITY,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH
    };

    private static final String[] VIDEO_COLUMNS = {
            MovieContract.VideoEntry.TABLE_NAME + "." + MovieContract.VideoEntry._ID,
            MovieContract.VideoEntry.COLUMN_NAME,
            MovieContract.VideoEntry.COLUMN_SIZE,
            MovieContract.VideoEntry.COLUMN_SITE,
            MovieContract.VideoEntry.COLUMN_KEY,
    };

    // These constants correspond to the projection defined above, and must change if the
    // projection changes
    private static final int COL_MOVIE_ID = 0;
    private static final int COL_MOVIE_TITLE = 1;
    private static final int COL_MOVIE_OVERVIEW = 2;
    private static final int COL_MOVIE_RELEASE_DATE = 3;
    private static final int COL_MOVIE_POPULARITY = 4;
    private static final int COL_VOTE_AVERAGE = 5;
    private static final int COL_POSTER_PATH = 6;

    private static final int COL_VIDEO_ID = 0;
    private static final int COL_VIDEO_NAME = 1;
    private static final int COL_VIDEO_SIZE = 2;
    private static final int COL_VIDEO_SITE = 3;
    private static final int COL_VIDEO_KEY = 4;

    private String mMoviesString;

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mMovieUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View root = inflater.inflate(R.layout.fragment_detail, container, false);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        getLoaderManager().initLoader(VIDEO_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mMovieUri) {
            switch (id) {
                case DETAIL_LOADER:
                    // Create and return a CursorLoader that will take care of
                    // creating a Cursor for the data being displayed.
                    CursorLoader detailLoader = new CursorLoader(
                            getActivity(),
                            mMovieUri,
                            MOVIE_COLUMNS,
                            null,
                            null,
                            null
                    );
                    return detailLoader;
                case VIDEO_LOADER:
                    long movieId = MovieContract.MovieEntry.getMovieIdFromUri(mMovieUri);
                    Uri videoUri = MovieContract.VideoEntry.buildVideoWithMovieId(movieId);
                    CursorLoader videoLoader = new CursorLoader(
                            getActivity(),
                            videoUri,
                            VIDEO_COLUMNS,
                            null,
                            null,
                            null
                    );
                    return videoLoader;
                default:
                    return null;

            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        int id = loader.getId();
        switch (id) {
            case DETAIL_LOADER:
                String title = data.getString(COL_MOVIE_TITLE);
                String date = data.getString(COL_MOVIE_RELEASE_DATE);
                String overview = data.getString(COL_MOVIE_OVERVIEW);
                mMoviesString = String.format("%s %s %s", title, date, overview);

                TextView tvTitle = (TextView) getView().findViewById(R.id.text_view_title);
                tvTitle.setText(title);

                TextView tvDate = (TextView) getView().findViewById(R.id.text_view_date);
                tvDate.setText(date);

                TextView tvOverview = (TextView) getView().findViewById(R.id.text_view_overview);
                tvOverview.setText(overview);

                ImageView iv = (ImageView) getView().findViewById(R.id.image_view_poster);

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
                break;

            case VIDEO_LOADER:
                //ListView listViewVideo = (ListView) getView().findViewById(R.id.list_view_video);

                do {
                    // Build the video uri
                    final String YOUTUBE_BASE = "https://www.youtube.com/watch";
                    final String YOUTUBE_KEY = "v";

                    final Uri videoUri = Uri.parse(YOUTUBE_BASE).buildUpon()
                            .appendQueryParameter(YOUTUBE_KEY, data.getString(COL_VIDEO_KEY))
                            .build();


                    final String THUMBNAIL_BASE = "http://img.youtube.com/vi";
                    final Uri imageUri = Uri.parse(THUMBNAIL_BASE).buildUpon()
                            .appendPath(data.getString(COL_VIDEO_KEY))
                            .appendPath("0.jpg")
                            .build();

                    Log.v(LOG_TAG, "Thumbnail link " + imageUri);

                    //ImageView ivv = new ImageView(getContext());
                    ImageView ivv = (ImageView) getView().findViewById(R.id.image_view_video);
                    Picasso.with(getContext()).load(imageUri).into(ivv);

                    // Add listener ...
                    ivv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent youtubeIntent = new Intent(Intent.ACTION_VIEW, videoUri);
                            PackageManager packageManager = getContext().getPackageManager();
                            List activities = packageManager.queryIntentActivities(youtubeIntent,
                                    PackageManager.MATCH_DEFAULT_ONLY);
                            if (activities.size() > 0) {
                                startActivity(youtubeIntent);
                            } else {
                                // disable the video viewing ...
                            }
                        }
                    });
                    //listViewVideo.addView(ivv);

                } while (data.moveToNext());
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

}
