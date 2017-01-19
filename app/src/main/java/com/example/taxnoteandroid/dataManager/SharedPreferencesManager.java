package com.example.taxnoteandroid.dataManager;

import android.content.Context;
import android.content.SharedPreferences;

import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_CHARACTER_CODE_UTF8;
import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_FORMAT_TYPE_CSV;
import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_RANGE_TYPE_ALL;

public class SharedPreferencesManager {

    private static final String PREF_NAME                       = "pref";
    private static final String IS_DEFAULT_DATABASE_SET_KEY     = "IS_DEFAULT_DATABASE_SET_KEY";
    private static final String UUID_FOR_CURRENT_KEY            = "UUID_FOR_CURRENT_KEY";
    private static final String CHARACTER_CODE_FOR_CURRENT_KEY  = "CHARACTER_CODE_FOR_CURRENT_KEY";
    private static final String EXPORT_FORMAT_FOR_CURRENT_KEY   = "EXPORT_FORMAT_FOR_CURRENT_KEY";
    private static final String EXPORT_RANGE_TYPE_KEY           = "EXPORT_RANGE_TYPE_KEY";
    private static final String EXPORT_RANGE_BEGIN_DATE         = "EXPORT_RANGE_BEGIN_DATE";
    private static final String EXPORT_RANGE_END_DATE           = "EXPORT_RANGE_END_DATE";
    private static final String TAXNOTE_PLUS_IS_ACTIVE          = "TAXNOTE_PLUS_IS_ACTIVE";


    //--------------------------------------------------------------//
    //    -- Init --
    //--------------------------------------------------------------//

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }


    //--------------------------------------------------------------//
    //    -- Default Data Install --
    //--------------------------------------------------------------//

    public static void saveDefaultDatabaseSet(Context context) {
        getSharedPreferences(context).edit().putBoolean(IS_DEFAULT_DATABASE_SET_KEY, true).apply();
    }

    public static boolean isDefaultDataBaseSet(Context context) {
        return getSharedPreferences(context).getBoolean(IS_DEFAULT_DATABASE_SET_KEY, false);
    }


    //--------------------------------------------------------------//
    //    -- Current Project Uuid --
    //--------------------------------------------------------------//

    public static void saveUuidForCurrentProject(Context context, String uuid) {
        getSharedPreferences(context).edit().putString(UUID_FOR_CURRENT_KEY, uuid).apply();
    }

    public static String getUuidForCurrentProject(Context context) {
        return getSharedPreferences(context).getString(UUID_FOR_CURRENT_KEY, "");
    }


    //--------------------------------------------------------------//
    //    -- Data Export --
    //--------------------------------------------------------------//

    public static void saveCurrentCharacterCode(Context context, String characterCode) {
        getSharedPreferences(context).edit().putString(CHARACTER_CODE_FOR_CURRENT_KEY, characterCode).apply();
    }

    public static String getCurrentCharacterCode(Context context) {
        return getSharedPreferences(context).getString(CHARACTER_CODE_FOR_CURRENT_KEY, EXPORT_CHARACTER_CODE_UTF8);
    }

    public static void saveCurrentExportFormat(Context context, String exportFormat) {
        getSharedPreferences(context).edit().putString(EXPORT_FORMAT_FOR_CURRENT_KEY, exportFormat).apply();
    }

    public static String getCurrentExportFormat(Context context) {
        return getSharedPreferences(context).getString(EXPORT_FORMAT_FOR_CURRENT_KEY, EXPORT_FORMAT_TYPE_CSV);
    }

    public static void saveExportRangeType(Context context, String exportRange) {
        getSharedPreferences(context).edit().putString(EXPORT_RANGE_TYPE_KEY, exportRange).apply();
    }

    public static String getExportRangeType(Context context) {
        return getSharedPreferences(context).getString(EXPORT_RANGE_TYPE_KEY, EXPORT_RANGE_TYPE_ALL);
    }

    public static void saveDateRangeBeginDate(Context context, long beginDate) {
        getSharedPreferences(context).edit().putLong(EXPORT_RANGE_BEGIN_DATE, beginDate).apply();
    }

    public static long getDateRangeBeginDate(Context context) {
        return getSharedPreferences(context).getLong(EXPORT_RANGE_BEGIN_DATE, System.currentTimeMillis());
    }

    public static void saveDateRangeEndDate(Context context, long beginDate) {
        getSharedPreferences(context).edit().putLong(EXPORT_RANGE_END_DATE, beginDate).apply();
    }

    public static long getDateRangeEndDate(Context context) {
        return getSharedPreferences(context).getLong(EXPORT_RANGE_END_DATE, System.currentTimeMillis());
    }


    //--------------------------------------------------------------//
    //    -- Upgrade --
    //--------------------------------------------------------------//

    public static boolean saveTaxnotePlusStatus(Context context) {
        return getSharedPreferences(context).edit().putBoolean(TAXNOTE_PLUS_IS_ACTIVE, true).commit();
    }

    public static boolean taxnotePlusIsActive(Context context) {
        return getSharedPreferences(context).getBoolean(TAXNOTE_PLUS_IS_ACTIVE, false);
    }
}
