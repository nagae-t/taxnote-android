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
    private static final String TAXNOTE_PLUS_PURCHASE_TIME      = "TAXNOTE_PLUS_PURCHASE_TIME";
    private static final String HELP_FIRST_LAUNCH_KEY           = "HELP_FIRST_LAUNCH_KEY";
    private static final String HELP_SELECT_SUMMARY_KEY         = "HELP_SELECT_SUMMARY_KEY";
    private static final String HELP_SELECT_REGISTER_KEY        = "HELP_SELECT_REGISTER_KEY";
    private static final String HELP_FIRST_REGISTER_DONE_KEY    = "HELP_FIRST_REGISTER_DONE_KEY";
    private static final String HELP_HISTORY_TAB_KEY            = "HELP_HISTORY_TAB_KEY";
    private static final String DATE_CURRENT_SELECTED           = "DATE_CURRENT_SELECTED";
    private static final String TAP_HERE_HISTORY_DONE_KEY       = "TAP_HERE_HISTORY_DONE_KEY";
    private static final String ASK_ANYTHING_DONE_KEY           = "ASK_ANYTHING_DONE_KEY";


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
    //    -- Current Date --
    //--------------------------------------------------------------//

    public static void saveCurrentSelectedDate(Context context, long selectedDate) {
        getSharedPreferences(context).edit().putLong(DATE_CURRENT_SELECTED, selectedDate).apply();
    }

    public static long getCurrentSelectedDate(Context context) {
        return getSharedPreferences(context).getLong(DATE_CURRENT_SELECTED, System.currentTimeMillis());
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

    public static void saveTaxnotePlusPurchaseTime(Context context, long purchaseTime) {
        getSharedPreferences(context).edit().putLong(TAXNOTE_PLUS_PURCHASE_TIME, purchaseTime).apply();
    }

    public static long getTaxnotePlusPurchaseTime(Context context) {
        return getSharedPreferences(context).getLong(TAXNOTE_PLUS_PURCHASE_TIME, 0);
    }


    //--------------------------------------------------------------//
    //    -- Help Message --
    //--------------------------------------------------------------//

    public static void saveFirstLaunchMessageDone(Context context) {
        getSharedPreferences(context).edit().putBoolean(HELP_FIRST_LAUNCH_KEY, true).apply();
    }

    public static boolean isFirstLaunchMessageDone(Context context) {
        return getSharedPreferences(context).getBoolean(HELP_FIRST_LAUNCH_KEY, false);
    }

    public static void saveSelectSummaryMessageDone(Context context) {
        getSharedPreferences(context).edit().putBoolean(HELP_SELECT_SUMMARY_KEY, true).apply();
    }

    public static boolean isSelectSummaryMessageDone(Context context) {
        return getSharedPreferences(context).getBoolean(HELP_SELECT_SUMMARY_KEY, false);
    }

    public static void saveSelectRegisterMessageDone(Context context) {
        getSharedPreferences(context).edit().putBoolean(HELP_SELECT_REGISTER_KEY, true).apply();
    }

    public static boolean isSelectRegisterMessageDone(Context context) {
        return getSharedPreferences(context).getBoolean(HELP_SELECT_REGISTER_KEY, false);
    }

    public static void saveFirstRegisterDone(Context context) {
        getSharedPreferences(context).edit().putBoolean(HELP_FIRST_REGISTER_DONE_KEY, true).apply();
    }

    public static boolean isFirstRegisterDone(Context context) {
        return getSharedPreferences(context).getBoolean(HELP_FIRST_REGISTER_DONE_KEY, false);
    }

    public static void saveHistoryTabHelpDone(Context context) {
        getSharedPreferences(context).edit().putBoolean(HELP_HISTORY_TAB_KEY, true).apply();
    }

    public static boolean isHistoryTabHelpDone(Context context) {
        return getSharedPreferences(context).getBoolean(HELP_HISTORY_TAB_KEY, false);
    }

    public static void saveTapHereHistoryEditDone(Context context) {
        getSharedPreferences(context).edit().putBoolean(TAP_HERE_HISTORY_DONE_KEY, true).apply();
    }

    public static boolean isTapHereHistoryEditDone(Context context) {
        return getSharedPreferences(context).getBoolean(TAP_HERE_HISTORY_DONE_KEY, false);
    }

    public static void saveAskAnythingMessageDone(Context context) {
        getSharedPreferences(context).edit().putBoolean(ASK_ANYTHING_DONE_KEY, true).apply();
    }

    public static boolean isAskAnythingMessageDone(Context context) {
        return getSharedPreferences(context).getBoolean(ASK_ANYTHING_DONE_KEY, false);
    }
}
