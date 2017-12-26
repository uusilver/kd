package org.tmind.kiteui.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.tmind.kiteui.R;
import org.tmind.kiteui.utils.LogUtil;

/**
 * Created by vali on 12/20/2017.
 */

public class NormalSettingFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener,Preference.OnPreferenceClickListener{

    private final static String TAG = "NormalSettingFragment.class";
    private  CharSequence[] theme;
    private  SharedPreferences sharedPreferences;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.normal_setting);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int currentThemeValue = sharedPreferences.getInt("current_theme", 1);
        ListPreference themeList = (ListPreference)findPreference("themes");
        theme = themeList.getEntries();
        themeList.setSummary(theme[currentThemeValue]);
        themeList.setOnPreferenceClickListener(this);
        themeList.setOnPreferenceChangeListener(this);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()){
            case "themes":{
                SharedPreferences.Editor editor = sharedPreferences.edit();
                int currentThemeValue = Integer.valueOf(String.valueOf(newValue));
                editor.putInt("current_theme", currentThemeValue);
                editor.commit();
                preference.setSummary(theme[currentThemeValue]);
            }
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        LogUtil.d(TAG, "Preference Click");
        return false;
    }


}
