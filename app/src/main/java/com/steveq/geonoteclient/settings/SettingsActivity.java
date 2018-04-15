package com.steveq.geonoteclient.settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.FrameLayout;

import com.steveq.geonoteclient.R;

public class SettingsActivity extends Activity{
    private static final String TAG = SettingsFragment.class.getSimpleName();

    FrameLayout fragmentContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "CREATE SETTINGS");
        setContentView(R.layout.settings_activity_layout);

        fragmentContainer = (FrameLayout) findViewById(R.id.fragmentContainer);

        SettingsFragment sf = new SettingsFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, sf)
                .commit();


        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(sf);
        sf.registerParent(this);
    }
}
