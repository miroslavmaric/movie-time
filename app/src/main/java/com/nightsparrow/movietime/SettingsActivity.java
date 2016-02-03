package com.nightsparrow.movietime;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/**
 * Created by x on 2/3/2016.
 */
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
        }



        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
                return false;
        }
}
