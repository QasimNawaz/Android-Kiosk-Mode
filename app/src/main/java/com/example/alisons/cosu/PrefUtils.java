package com.example.alisons.cosu;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by alisons on 12/7/2017.
 */

public class PrefUtils {

    private static final String PREF_COSU_MODE = "pref_cosu_mode";

    public static boolean isCosuModeActive(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_COSU_MODE, false);
    }

    public static void setCosuModeActive(final boolean active, final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_COSU_MODE, active).apply();
    }
}
