package xyz.dradge.radalla.tabs.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import xyz.dradge.radalla.MainActivity;

/**
 * The settings view.
 */
public class SettingsViewFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    /**
     * Sets up all the PreferenceViews and PreferenceChangeListeners.
     * @param savedInstanceState not used.
     * @param rootKey not used.
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getPreferenceManager().getContext();
        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(context);
        SharedPreferences sharedPreferences = preferenceScreen.getSharedPreferences();

        MainActivity mainActivity = (MainActivity) getActivity();
        String[] stations = mainActivity.getStationNames().toArray(new String[0]);

        PreferenceCategory generalCategory = new PreferenceCategory(context);
        generalCategory.setKey("generalCategory");
        generalCategory.setTitle("General");
        preferenceScreen.addPreference(generalCategory);

        PreferenceCategory stationCategory = new PreferenceCategory(context);
        stationCategory.setKey("stationCategory");
        stationCategory.setTitle("Station view");
        preferenceScreen.addPreference(stationCategory);

        PreferenceCategory routeCategory = new PreferenceCategory(context);
        routeCategory.setKey("routeCategory");
        routeCategory.setTitle("Route view");
        preferenceScreen.addPreference(routeCategory);

        ListPreference defaultView = new ListPreference(context);
        defaultView.setKey("defaultView");
        defaultView.setTitle("Default view");
        defaultView.setDefaultValue("0");
        defaultView.setEntries(new String[] {"Station", "Route"});
        defaultView.setEntryValues(new String[] {"0", "1"});
        defaultView.setSummary(defaultView.getEntries()[Integer.parseInt(sharedPreferences.getString("defaultView", "0"))]);
        defaultView.setOnPreferenceChangeListener((p, v) -> {
            p.setSummary(defaultView.getEntries()[Integer.parseInt(v.toString())]);
            return true;
        });
        generalCategory.addPreference(defaultView);

        ListPreference stationDefaultStation = new ListPreference(context);
        stationDefaultStation.setKey("stationDefaultStation");
        stationDefaultStation.setTitle("Default station");
        stationDefaultStation.setEntries(stations);
        stationDefaultStation.setEntryValues(stations);
        stationDefaultStation.setSummary(sharedPreferences.getString(stationDefaultStation.getKey(), "Not set"));
        stationDefaultStation.setOnPreferenceChangeListener(this);
        stationCategory.addPreference(stationDefaultStation);

        ListPreference routeDefaultOrigin = new ListPreference(context);
        routeDefaultOrigin.setKey("routeDefaultOrigin");
        routeDefaultOrigin.setTitle("Default origin station");
        routeDefaultOrigin.setEntries(stations);
        routeDefaultOrigin.setEntryValues(stations);
        routeDefaultOrigin.setSummary(sharedPreferences.getString(routeDefaultOrigin.getKey(), "Not set"));
        routeDefaultOrigin.setOnPreferenceChangeListener(this);
        routeCategory.addPreference(routeDefaultOrigin);

        ListPreference routeDefaultDestination = new ListPreference(context);
        routeDefaultDestination.setKey("routeDefaultDestination");
        routeDefaultDestination.setTitle("Default destination");
        routeDefaultDestination.setEntries(stations);
        routeDefaultDestination.setEntryValues(stations);
        routeDefaultDestination.setSummary(sharedPreferences.getString(routeDefaultDestination.getKey(), "Not set"));
        routeDefaultDestination.setOnPreferenceChangeListener(this);
        routeCategory.addPreference(routeDefaultDestination);

        setPreferenceScreen(preferenceScreen);
    }

    /**
     * Updates the summary text on PreferenceChange.
     * @param preference the preference that was updated.
     * @param newValue the new value of given preference.
     * @return true.
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        preference.setSummary(newValue.toString());
        return true;
    }
}
