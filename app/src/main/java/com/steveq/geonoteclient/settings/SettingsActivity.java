package com.steveq.geonoteclient.settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.steveq.geonoteclient.R;

public class SettingsActivity extends Activity{
    private static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SettingsFragment sf = new SettingsFragment();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, sf)
                .commit();


        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(sf);
        sf.registerParent(this);
    }
}
