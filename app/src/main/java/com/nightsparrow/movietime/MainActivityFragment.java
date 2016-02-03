package com.nightsparrow.movietime;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public ArrayAdapter<String> mMovieAdapter;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);

        String[] data = {
                "Tommorow - 60",
                "Wasteland - 210",
                "Nimmerland - 120",
                "Drake LAke - 140"
        };

        ArrayList<String> dataList = new ArrayList<>(Arrays.asList(data));

        mMovieAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_movie,
                R.id.list_item_movie_textview,
                dataList
        );

        ListView listview = (ListView) root.findViewById(R.id.listview_movie);
        listview.setAdapter(mMovieAdapter);

        return root;
    }
}
