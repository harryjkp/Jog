package com.rashem.jogps;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.rashem.audio.asdf;

public class SettingsActivity extends PreferenceActivity  {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences.OnSharedPreferenceChangeListener prefListener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences prefs,
                                                          String key) {
                        asdf.lorp=key;
                        asdf.preang=true;

                        onooooooo();

                        //asdf.speedfactorfrommps=PreferenceManager.getDefaultSharedPreferences(this).getInt("language_preference", 0);//PreferenceManager.getDefaultSharedPreferences(this).getInt("language_preference", 0);
                    }
                };
        preferences.registerOnSharedPreferenceChangeListener(prefListener);
    }

/*    @Override
       public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        asdf.targetspeed=PreferenceManager.getDefaultSharedPreferences(this).getInt("language_preference", 0);
    }*/

    public void onooooooo(){        asdf.fakespeedfactorfrommps=PreferenceManager.getDefaultSharedPreferences(this).getString("language_preference", "");
        asdf.speedfactorfrommps=Double.parseDouble(asdf.fakespeedfactorfrommps);
        //asdf.preang=true;

        //asdf.speedfactorfrommps=PreferenceManager.getDefaultSharedPreferences(this).getInt("language_preference", 0);
        //asdf.speedfactorfrommps=PreferenceManager.getDefaultSharedPreferences(this).getInt("language_preference", 0);
        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("invert",true)) {
            asdf.invert =1;

        }else {
            asdf.invert =-1;
        }
    }


}

