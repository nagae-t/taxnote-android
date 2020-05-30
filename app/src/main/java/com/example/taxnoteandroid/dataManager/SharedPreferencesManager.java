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
    private static final String EXPORT_SUBJECT_ENABLE_KEY       = "EXPORT_SUBJECT_ENABLE_KEY";
    private static final String EXPORT_FORMAT_FOR_CURRENT_KEY   = "EXPORT_FORMAT_FOR_CURRENT_KEY";
    private static final String EXPORT_RANGE_TYPE_KEY           = "EXPORT_RANGE_TYPE_KEY";
    private static final String EXPORT_RANGE_BEGIN_DATE         = "EXPORT_RANGE_BEGIN_DATE";
    private static final String EXPORT_RANGE_END_DATE           = "EXPORT_RANGE_END_DATE";
    private static final String TAXNOTE_PLUS_EXPIRY_TIME        = "TAXNOTE_PLUS_EXPIRY_TIME";
    private static final String TAXNOTE_CLOUD_EXPIRY_TIME       = "TAXNOTE_CLOUD_EXPIRY_TIME";
    private static final String ZENY_PREMIUM_EXPIRY_TIME        = "ZENY_PREMIUM_EXPIRY_TIME";
    private static final String FREE_ENTRY_EXTEND_VIEW_KEY      = "FREE_ENTRY_EXTEND_VIEW_KEY";

    private static final String HELP_FIRST_LAUNCH_KEY           = "HELP_FIRST_LAUNCH_KEY";
    private static final String HELP_SELECT_SUMMARY_KEY         = "HELP_SELECT_SUMMARY_KEY";
    private static final String HELP_SELECT_REGISTER_KEY        = "HELP_SELECT_REGISTER_KEY";
    private static final String HELP_FIRST_REGISTER_DONE_KEY    = "HELP_FIRST_REGISTER_DONE_KEY";
    private static final String HELP_HISTORY_TAB_KEY            = "HELP_HISTORY_TAB_KEY";
    private static final String DATE_CURRENT_SELECTED           = "DATE_CURRENT_SELECTED";
    private static final String TAP_HERE_HISTORY_DONE_KEY       = "TAP_HERE_HISTORY_DONE_KEY";
    private static final String ASK_ANYTHING_DONE_KEY           = "ASK_ANYTHING_DONE_KEY";
    private static final String FIRST_LAUNCH_KEY                = "FIRST_LAUNCH_KEY";
    private static final String TRACK_ENTRY_KEY                 = "TRACK_ENTRY_KEY";
    private static final String DATA_EXPORT_SUGGEST_KEY         = "DATA_EXPORT_SUGGEST_KEY";
    private static final String PROFIT_LOSS_REPORT_PERIOD_KEY   = "PROFIT_LOSS_REPORT_PERIOD_KEY";
    private static final String GRAPH_REPORT_IS_EXPENSE_KEY     = "GRAPH_REPORT_IS_EXPENSE_KEY";
    private static final String BUSINESS_MODEL_MESSAGE_KEY      = "BUSINESS_MODEL_MESSAGE_KEY";
    private static final String CHARTS_TAP_SUGGEST_KEY          = "CHARTS_TAP_SUGGEST_KEY";

    private static final String BALANCE_CARRY_FORWARD_KEY       = "BALANCE_CARRY_FORWARD_KEY";
    private static final String COMB_ALL_ACC_KEY       = "COMB_ALL_ACC_KEY";
    private static final String FIXED_CATE_ORDER_KEY            = "FIXED_CATE_ORDER_KEY";
    private static final String MONTHLY_CLOSING_DATE_INDEX_KEY  = "MONTHLY_CLOSING_DATE_INDEX_KEY";
    private static final String START_MONTH_OF_YEAR_INDEX_KEY   = "START_MONTH_OF_YEAR_INDEX_KEY";
    private static final String PROFIT_KEY   = "START_MONTH_OF_YEAR_INDEX_KEY";

    private static final String APP_THEME_STYLE_KEY             = "APP_THEME_STYLE_KEY";
    private static final String RELEASE_NOTE_KEY                = "RELEASE_NOTE_KEY";

    private static final String DAILY_ALERT_INPUT_FORGET_ENABLE_KEY = "DAILY_ALERT_INPUT_FORGET_ENABLE_KEY";
    private static final String DAILY_ALERT_INPUT_FORGET_TIME_KEY = "DAILY_ALERT_INPUT_FORGET_TIME_KEY";


    //--------------------------------------------------------------//
    //    -- Init --
    //--------------------------------------------------------------//

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void saveBoolean(Context context, String key, boolean value) {
        getSharedPreferences(context).edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(Context context, String key) {
        return getSharedPreferences(context).getBoolean(key, false);
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

    public static void saveAppThemeStyle(Context context, int type) {
        getSharedPreferences(context).edit().putInt(APP_THEME_STYLE_KEY, type).apply();
    }

    public static int getAppThemeStyle(Context context) {
        return getSharedPreferences(context).getInt(APP_THEME_STYLE_KEY, 0);
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
    //    -- Daily Alert Input Forget --
    //--------------------------------------------------------------//

    public static void saveDailyAlertInputForgetEnable(Context context, boolean val) {
        getSharedPreferences(context).edit().putBoolean(DAILY_ALERT_INPUT_FORGET_ENABLE_KEY, val).apply();
    }

    public static boolean getDailyAlertInputForgetEnable(Context context) {
        return getSharedPreferences(context).getBoolean(DAILY_ALERT_INPUT_FORGET_ENABLE_KEY, false);
    }

    // DAILY_ALERT_INPUT_FORGET_TIME_KEY
    public static void saveDailyAlertInputForgetTime(Context context, String timeStr) {
        getSharedPreferences(context).edit().putString(DAILY_ALERT_INPUT_FORGET_TIME_KEY, timeStr).apply();
    }

    public static String getDailyAlertInputForgetTime(Context context) {
        return getSharedPreferences(context).getString(DAILY_ALERT_INPUT_FORGET_TIME_KEY, null);
    }


    //--------------------------------------------------------------//
    //    -- Data Export --
    //--------------------------------------------------------------//

    public static void saveExportSubjectEnable(Context context, boolean val) {
        getSharedPreferences(context).edit().putBoolean(EXPORT_SUBJECT_ENABLE_KEY, val).apply();
    }

    public static boolean getExportSujectEnable(Context context) {
        return getSharedPreferences(context).getBoolean(EXPORT_SUBJECT_ENABLE_KEY, false);
    }


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


    //------------------------------------------------------------//
    //    -- Profit And Loss Report --
    //--------------------------------------------------------------//

    public static void saveProfitLossReportPeriodType(Context context, int type) {
        getSharedPreferences(context).edit().putInt(PROFIT_LOSS_REPORT_PERIOD_KEY, type).apply();
    }

    public static int getProfitLossReportPeriodType(Context context) {
        return getSharedPreferences(context).getInt(
                PROFIT_LOSS_REPORT_PERIOD_KEY, EntryDataManager.PERIOD_TYPE_YEAR);
    }

    public static void saveBalanceCarryForward(Context context, boolean val) {
        getSharedPreferences(context).edit().putBoolean(BALANCE_CARRY_FORWARD_KEY, val).apply();
    }

    public static boolean getBalanceCarryForward(Context context) {
        return getSharedPreferences(context).getBoolean(BALANCE_CARRY_FORWARD_KEY, true);
    }

    public static void saveCombAllAccounts(Context context, boolean val) {
        getSharedPreferences(context).edit().putBoolean(COMB_ALL_ACC_KEY, val).apply();
    }

    public static boolean getCombAllAccounts(Context context) {
        return getSharedPreferences(context).getBoolean(COMB_ALL_ACC_KEY, false);
    }

    public static void saveFixedCateOrder(Context context, boolean val) {
        getSharedPreferences(context).edit().putBoolean(FIXED_CATE_ORDER_KEY, val).apply();
    }

    public static boolean getFixedCateOrder(Context context) {
        return getSharedPreferences(context).getBoolean(FIXED_CATE_ORDER_KEY, false);
    }

    public static void saveMonthlyClosingDateIndex(Context context, int index) {
        getSharedPreferences(context).edit().putInt(MONTHLY_CLOSING_DATE_INDEX_KEY, index).apply();
    }
    public static int  getMonthlyClosingDateIndex(Context context) {
        int closingDateIndex = getSharedPreferences(context).getInt(MONTHLY_CLOSING_DATE_INDEX_KEY, 26);
        if (closingDateIndex > 26) closingDateIndex = 26;
        return closingDateIndex;
    }

    public static void saveStartMonthOfYearIndex(Context context, int index) {
        getSharedPreferences(context).edit().putInt(START_MONTH_OF_YEAR_INDEX_KEY, index).apply();
    }
    public static int getStartMonthOfYearIndex(Context context) {
        return getSharedPreferences(context).getInt(START_MONTH_OF_YEAR_INDEX_KEY, 0);
    }

    //--------------------------------------------------------------//
    //    -- Graph Report --
    //--------------------------------------------------------------//

    public static void saveGraphReportIsExpenseType(Context context, boolean val) {
        getSharedPreferences(context).edit().putBoolean(GRAPH_REPORT_IS_EXPENSE_KEY, val).apply();
    }

    public static boolean getGraphReportIsExpenseType(Context context) {
        return getSharedPreferences(context).getBoolean(GRAPH_REPORT_IS_EXPENSE_KEY, true);
    }

    //--------------------------------------------------------------//
    //    -- Upgrade --
    //--------------------------------------------------------------//

    public static void saveTaxnotePlusExpiryTime(Context context, long expiryTime) {
        getSharedPreferences(context).edit().putLong(TAXNOTE_PLUS_EXPIRY_TIME, expiryTime).apply();
    }

    public static long getTaxnotePlusExpiryTime(Context context) {
        return getSharedPreferences(context).getLong(TAXNOTE_PLUS_EXPIRY_TIME, 0);
    }

    public static void saveTaxnoteCloudExpiryTime(Context context, long expiryTime) {
        getSharedPreferences(context).edit().putLong(TAXNOTE_CLOUD_EXPIRY_TIME, expiryTime).apply();
    }

    public static long getTaxnoteCloudExpiryTime(Context context) {
        return getSharedPreferences(context).getLong(TAXNOTE_CLOUD_EXPIRY_TIME, 0);
    }

    public static void saveZenyPremiumExpiryTime(Context context, long expiryTime) {
        getSharedPreferences(context).edit().putLong(ZENY_PREMIUM_EXPIRY_TIME, expiryTime).apply();
    }

    public static long getZenyPremiumExpiryTime(Context context) {
        return getSharedPreferences(context).getLong(ZENY_PREMIUM_EXPIRY_TIME, 0);
    }

    public static void saveFreeEntryExtendViewDone(Context context) {
        getSharedPreferences(context).edit().putBoolean(FREE_ENTRY_EXTEND_VIEW_KEY, true).apply();
    }

    public static boolean isFreeEntryExtendViewDone(Context context) {
        return getSharedPreferences(context).getBoolean(FREE_ENTRY_EXTEND_VIEW_KEY, false);
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

    public static void saveDataExportSuggestDone(Context context) {
        getSharedPreferences(context).edit().putBoolean(DATA_EXPORT_SUGGEST_KEY, true).apply();
    }

    public static boolean isDataExportSuggestDone(Context context) {
        return getSharedPreferences(context).getBoolean(DATA_EXPORT_SUGGEST_KEY, false);
    }

    public static void saveBusinessModelMessageDone(Context context) {
        getSharedPreferences(context).edit().putBoolean(BUSINESS_MODEL_MESSAGE_KEY, true).apply();
    }

    public static boolean isBusinessModelMessageDone(Context context) {
        return getSharedPreferences(context).getBoolean(BUSINESS_MODEL_MESSAGE_KEY, false);
    }

    public static void saveChartsTapMessageDone(Context context) {
        getSharedPreferences(context).edit().putBoolean(CHARTS_TAP_SUGGEST_KEY, true).apply();
    }

    public static boolean isChartsTapMessageDone(Context context) {
        return getSharedPreferences(context).getBoolean(CHARTS_TAP_SUGGEST_KEY, false);
    }


    //--------------------------------------------------------------//
    //    -- Analytics --
    //--------------------------------------------------------------//

    public static void saveFirstLaunchDone(Context context) {
        getSharedPreferences(context).edit().putBoolean(FIRST_LAUNCH_KEY, true).apply();
    }

    public static boolean isFirstLaunchDone(Context context) {
        return getSharedPreferences(context).getBoolean(FIRST_LAUNCH_KEY, false);
    }

    public static void saveTrackEntryCount(Context context, long entryCount) {
        getSharedPreferences(context).edit().putLong(TRACK_ENTRY_KEY, entryCount).apply();
    }

    public static long getTrackEntryCount(Context context) {
        return getSharedPreferences(context).getLong(TRACK_ENTRY_KEY, 0);
    }


    //--------------------------------------------------------------//
    //    -- Release Note --
    //--------------------------------------------------------------//

    public static void saveLastVersionName(Context context, String versionName) {
        getSharedPreferences(context).edit().putString(RELEASE_NOTE_KEY, versionName).apply();
    }

    public static String getLastVersionName(Context context) {
        return getSharedPreferences(context).getString(RELEASE_NOTE_KEY, "");
    }


    //--------------------------------------------------------------//
    //    -- User login value --
    //--------------------------------------------------------------//

    public static void saveUserApiLoginValue(Context context, String key, String value) {
        getSharedPreferences(context).edit().putString(key, value).apply();
    }

    public static String getUserApiLoginValue(Context context, String key) {
        return getSharedPreferences(context).getString(key, null);
    }


    //--------------------------------------------------------------//
    //    -- Model sync updated value --
    //--------------------------------------------------------------//

    public static void saveSyncUpdatedAt(Context context, String key, long value) {
        getSharedPreferences(context).edit().putLong(key, value).apply();
    }

    public static long getSyncUpdatedAt(Context context, String key) {
        return getSharedPreferences(context).getLong(key, 0);
    }

}
