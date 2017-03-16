package com.example.taxnoteandroid.Library;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.example.taxnoteandroid.AlertInputForgetSettingsActivity.DAILY_ALERT_SERVICE_ID;

/**
 * Created by b0ne on 2017/03/16.
 */

public class OnBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)
                || action.equals(Intent.ACTION_LOCKED_BOOT_COMPLETED)) {

            DailyScheduler dailyScheduler = new DailyScheduler(context);
            dailyScheduler.setBySavedDailyAlertInputForget(DAILY_ALERT_SERVICE_ID);
        }
    }
}
