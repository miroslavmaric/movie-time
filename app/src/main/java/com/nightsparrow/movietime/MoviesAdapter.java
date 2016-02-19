package com.nightsparrow.movietime;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import com.nightsparrow.movietime.data.MovieContract;
import com.squareup.picasso.Picasso;

/**
 * Created by Miroslav Maric on 2/16/2016.
 */
public class MoviesAdapter extends CursorAdapter {

    private static final String LOG_TAG = MoviesAdapter.class.getSimpleName();

    public MoviesAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    private String convertCursorRowToUXFormat(Cursor cursor) {
        // get row indices for our cursor
        int titleIdx = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE);
        int popularityIdx = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POPULARITY);
        int ratingIdx = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE);

        return cursor.getString(titleIdx) + " - " + cursor.getDouble(popularityIdx) + " - " + cursor.getDouble(ratingIdx);
    }

    /*
        Views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        //View view = LayoutInflater.from(context).inflate(R.layout.list_item_movie, parent, false);

        ImageView view = new ImageView(context);
        view.setScaleType(ImageView.ScaleType.CENTER_CROP);
        view.setLayoutParams(new GridView.LayoutParams(300, 300));

        return view;
    }

    /*
        Fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Simple (and slow!) binding.
        //TextView tv = (TextView)view;
        //tv.setText(convertCursorRowToUXFormat(cursor));

        ImageView iv = (ImageView)view;

        // Build the image url
        final String TMD_BASE = "http://image.tmdb.org/t/p/";
        final String SIZE = "w185";
        final String POSTER_PATH =
                cursor.getString(MoviesFragment.COL_POSTER_PATH);

        Uri posterUri = Uri.parse(TMD_BASE).buildUpon()
                .appendPath(SIZE)
                .appendPath(POSTER_PATH)
                .build();

        // Use Picasso to fetch the image from the web and load it into image view
        Picasso.with(context).load(posterUri).into(iv);

    }
}
