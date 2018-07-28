package com.example.taxnoteandroid.Library;

import android.app.Activity;
import android.content.Intent;

import com.example.taxnoteandroid.BarGraphActivity;
import com.example.taxnoteandroid.HistoryListDataActivity;
import com.example.taxnoteandroid.InputRecurringListActivity;
import com.example.taxnoteandroid.MainActivity;

/**
 * Created by b0ne on 2017/03/27.
 */

public class BroadcastUtil {
    public static final String KEY_IS_LOGGING_IN = "is_logging_in";

    public static final String KEY_DATA_PERIOD_SCROLLED_POSITION = "data_period_position";
    public static final String KEY_DATA_PERIOD_SCROLLED_TARGET = "data_period_target";

    public static void sendRestartApp(Activity activity) {
        activity.sendBroadcast(
                new Intent(MainActivity.BROADCAST_RESTART_APP));
    }

    public static void sendAfterLogin(Activity activity, boolean isLoggingIn) {
        Intent intent = new Intent(MainActivity.BROADCAST_AFTER_LOGIN);
        intent.putExtra(KEY_IS_LOGGING_IN, isLoggingIn);
        activity.sendBroadcast(intent);
    }

    public static void sendReloadReport(Activity activity) {
        activity.sendBroadcast(new Intent(MainActivity.BROADCAST_REPORT_RELOAD));
        activity.sendBroadcast(new Intent(HistoryListDataActivity.BROADCAST_DATA_RELOAD));
        activity.sendBroadcast(new Intent(BarGraphActivity.BROADCAST_RELOAD_DATA));
    }

    public static void sendSwitchGraphExpense(Activity activity) {
        activity.sendBroadcast(new Intent(MainActivity.BROADCAST_SWITCH_GRAPH_EXPENSE));
    }

    public static void sendReloadRecurringList(Activity activity) {
        activity.sendBroadcast(new Intent(InputRecurringListActivity.BROADCAST_RELOAD_DATA));
    }

    public static void sendAdviewToggle(Activity activity) {
        activity.sendBroadcast(new Intent(MainActivity.BROADCAST_ADVIEW_TOGGLE));
    }

    public static void sendOnDataPeriodScrolled(Activity activity, int target, int position) {
        Intent intent = new Intent(MainActivity.BROADCAST_ADVIEW_TOGGLE);
        intent.putExtra(KEY_DATA_PERIOD_SCROLLED_TARGET, target);
        intent.putExtra(KEY_DATA_PERIOD_SCROLLED_POSITION, position);
        activity.sendBroadcast(intent);
    }
}
