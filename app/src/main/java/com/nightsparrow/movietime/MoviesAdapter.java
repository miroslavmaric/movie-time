package com.nightsparrow.movietime;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nightsparrow.movietime.data.MovieContract;

/**
 * Created by Miroslav Maric on 2/16/2016.
 */
public class MoviesAdapter extends CursorAdapter {

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
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_movie, parent, false);

        return view;
    }

    /*
        Fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Simple (and slow!) binding.

        TextView tv = (TextView)view;
        tv.setText(convertCursorRowToUXFormat(cursor));
    }
}
