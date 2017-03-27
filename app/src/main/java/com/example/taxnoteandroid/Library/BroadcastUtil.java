package com.example.taxnoteandroid.Library;

import android.app.Activity;
import android.content.Intent;

import com.example.taxnoteandroid.MainActivity;

/**
 * Created by b0ne on 2017/03/27.
 */

public class BroadcastUtil {
    public static final String KEY_IS_LOGGING_IN = "is_logging_in";

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
    }

    public static void sendSwitchGraphExpense(Activity activity) {
        activity.sendBroadcast(new Intent(MainActivity.BROADCAST_SWITCH_GRAPH_EXPENSE));
    }
}
