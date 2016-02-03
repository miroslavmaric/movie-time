package com.nightsparrow.movietime;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_detail, container, false);

        Intent intent = getActivity().getIntent();
        if (null != intent && intent.hasExtra(Intent.EXTRA_TEXT)) {
            String movie = intent.getStringExtra(Intent.EXTRA_TEXT);
            TextView textview = (TextView) root.findViewById(R.id.textview_detail);
            textview.setText(movie);
        }

        return root;
    }
}
