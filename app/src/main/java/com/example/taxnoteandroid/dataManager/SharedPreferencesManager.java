package com.example.taxnoteandroid.dataManager;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {

    private static final String PREF_NAME                       = "pref";
    private static final String IS_DEFAULT_DATABASE_SET_KEY     = "IS_DEFAULT_DATABASE_SET_KEY";
    private static final String UUID_FOR_CURRENT_KEY            = "UUID_FOR_CURRENT_KEY";
    private static final String CHARACTER_CODE_FOR_CURRENT_KEY  = "CHARACTER_CODE_FOR_CURRENT_KEY";
    private static final String EXPORT_FORMAT_FOR_CURRENT_KEY   = "EXPORT_FORMAT_FOR_CURRENT_KEY";
    private static final String EXPORT_RANGE_TYPE_KEY           = "EXPORT_RANGE_TYPE_KEY";
    private static final String EXPORT_RANGE_BEGIN_DATE         = "EXPORT_RANGE_BEGIN_DATE";
    private static final String EXPORT_RANGE_END_DATE           = "EXPORT_RANGE_END_DATE";


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


    //--------------------------------------------------------------//
    //    -- Data Export --
    //--------------------------------------------------------------//

    public static boolean saveCurrentCharacterCode(Context context, String characterCode) {
        return getSharedPreferences(context).edit().putString(CHARACTER_CODE_FOR_CURRENT_KEY, characterCode).commit();
    }

    public static String getCurrentCharacterCode(Context context) {
        return getSharedPreferences(context).getString(CHARACTER_CODE_FOR_CURRENT_KEY, "UTF8");
    }

    public static boolean saveCurrentExportFormat(Context context, String exportFormat) {
        return getSharedPreferences(context).edit().putString(EXPORT_FORMAT_FOR_CURRENT_KEY, exportFormat).commit();
    }

    public static String getCurrentExportFormat(Context context) {
        return getSharedPreferences(context).getString(EXPORT_FORMAT_FOR_CURRENT_KEY, "csv");
    }

    public static boolean saveExportRangeType(Context context, String exportRange) {
        return getSharedPreferences(context).edit().putString(EXPORT_RANGE_TYPE_KEY, exportRange).commit();
    }

    public static String getExportRangeType(Context context) {
        return getSharedPreferences(context).getString(EXPORT_RANGE_TYPE_KEY, "all");
    }

    public static boolean saveDateRangeBeginDate(Context context, long beginDate) {
        return getSharedPreferences(context).edit().putLong(EXPORT_RANGE_BEGIN_DATE, beginDate).commit();
    }

    public static long getDateRangeBeginDate(Context context) {
        return getSharedPreferences(context).getLong(EXPORT_RANGE_BEGIN_DATE, 0);
    }

    public static boolean saveDateRangeEndDate(Context context, long beginDate) {
        return getSharedPreferences(context).edit().putLong(EXPORT_RANGE_END_DATE, beginDate).commit();
    }

    public static long getDateRangeEndDate(Context context) {
        return getSharedPreferences(context).getLong(EXPORT_RANGE_END_DATE, 0);
    }
}
