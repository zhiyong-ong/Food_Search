package orbital.com.menusnap.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import orbital.com.menusnap.R;

public class SettingFragment extends PreferenceFragment {
    private SharedPreferences preferences;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_preference);
    }

    @Override
    public void onResume() {
        onCreate(null);
    }
}