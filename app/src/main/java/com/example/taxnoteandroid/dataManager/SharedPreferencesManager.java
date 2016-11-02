package com.example.taxnoteandroid.dataManager;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {

    private static final String PREF_NAME                   = "pref";
    private static final String IS_DEFAULT_DATABASE_SET_KEY = "IS_DEFAULT_DATABASE_SET_KEY";
    private static final String UUID_FOR_CURRENT_KEY        = "UUID_FOR_CURRENT_KEY";


    //--------------------------------------------------------------//
    //    -- Init --
    //--------------------------------------------------------------//

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }


    //--------------------------------------------------------------//
    //    -- Default Data Install --
    //--------------------------------------------------------------//

    public static boolean saveDefaultDatabaseSet(Context context) {
        return getSharedPreferences(context).edit().putBoolean(IS_DEFAULT_DATABASE_SET_KEY, true).commit();
    }

    public static boolean isDefaultDataBaseSet(Context context) {
        return getSharedPreferences(context).getBoolean(IS_DEFAULT_DATABASE_SET_KEY, false);
    }


    //--------------------------------------------------------------//
    //    -- Current Project Uuid --
    //--------------------------------------------------------------//

    public static boolean saveUuidForCurrentProject(Context context, String uuid) {
        return getSharedPreferences(context).edit().putString(UUID_FOR_CURRENT_KEY, uuid).commit();
    }

    public static String getUuidForCurrentProject(Context context) {
        return getSharedPreferences(context).getString(UUID_FOR_CURRENT_KEY, "");
    }
}
