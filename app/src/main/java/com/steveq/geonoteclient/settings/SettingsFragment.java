package com.steveq.geonoteclient.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.steveq.geonoteclient.App;
import com.steveq.geonoteclient.R;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    private static final String TAG = SettingsFragment.class.getSimpleName();
    private Activity parent;
    private Context attachContext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_general);
        initSummary();
    }

    public void registerParent(Activity activity) {
        this.parent = activity;
    }

    public void initSummary() {
        String val = String.valueOf(PreferenceManager.getDefaultSharedPreferences(parent).getInt(getString(R.string.settings_radius_key), 100));
        Preference preference = findPreference(getString(R.string.settings_radius_key));

        preference.setSummary(val);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "FRAGMENT ATTACHED :: " + context);
        attachContext = context;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if("radius".equals(s)){
            Preference preference = findPreference(App.getContext().getResources().getString(R.string.settings_radius_key));

            if(preference != null)
                preference.setSummary(String.valueOf(sharedPreferences.getInt(App.getContext().getResources().getString(R.string.settings_radius_key), 100)));

        }
    }
}
