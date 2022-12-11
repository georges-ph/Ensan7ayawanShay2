package ga.jundbits.ensan7ayawanshay2.UI;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import ga.jundbits.ensan7ayawanshay2.Models.UsersModel;
import ga.jundbits.ensan7ayawanshay2.R;
import ga.jundbits.ensan7ayawanshay2.Utils.HelperMethods;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        EditTextPreference namePref = findPreference("name");
        ListPreference darkThemePref = findPreference("theme");
        SwitchPreferenceCompat notificationsPref = findPreference("notifications");

        if (namePref == null || notificationsPref == null || darkThemePref == null) {
            return;
        }

        if (HelperMethods.getCurrentUserModel() == null) {
            namePref.setVisible(false);
            notificationsPref.setVisible(false);
        } else {
            namePref.setText(HelperMethods.getCurrentUserModel().getName());
            notificationsPref.setChecked(HelperMethods.getCurrentUserModel().isNotifications());
        }

        namePref.setOnPreferenceChangeListener(this);
        darkThemePref.setOnPreferenceChangeListener(this);
        notificationsPref.setOnPreferenceChangeListener(this);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        UsersModel usersModel = HelperMethods.getCurrentUserModel();

        switch (preference.getKey()) {

            case "name":
                usersModel.setName((String) newValue);
                break;

            case "notifications":
                usersModel.setNotifications((Boolean) newValue);
                break;

            case "theme":
                String theme = (String) newValue;
                if (theme.equals("light")) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else if (theme.equals("dark")) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else if (theme.equals("system")) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                }
                break;

        }

        HelperMethods.setCurrentUserModel(usersModel);

        return true;

    }

}