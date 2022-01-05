package mobi.omegacentauri.xrecolor;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;

public class Options extends PreferenceActivity {
    static final String PREFS = "preferences";
    public static final String PREF_STAT_BAR = "statBar";
    public static final String PREF_NAV_BAR = "navBarOptions";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName(PREFS);
        prefMgr.setSharedPreferencesMode(MODE_WORLD_READABLE);

        addPreferencesFromResource(R.xml.options);

        Window w = getWindow();
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.setNavigationBarColor(Color.BLACK);

        prefMgr.setDefaultValues(this, R.xml.options, false);
        SharedPreferences prefs = prefMgr.getSharedPreferences();

        for (String key : prefs.getAll().keySet()) {
            Preference pref = findPreference(key);
            if (pref instanceof  ListPreference)
                pref.setSummary(((ListPreference)pref).getEntry());
        }

        prefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Preference pref = findPreference(key);

                if (pref instanceof ListPreference) {
                    pref.setSummary(((ListPreference)pref).getEntry());
                }
            }
        });
    }

}
