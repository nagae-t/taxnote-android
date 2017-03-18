package com.example.taxnoteandroid.Library;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by b0ne on 2017/03/15.
 *
 * http://kimihiro-n.appspot.com/show/343001
 * https://gist.github.com/pistatium/7481451
 *
 */

public class DailyScheduler {
    private Context context;

    public DailyScheduler(Context context) {
        this.context = context;
    }

    /**
     * durationTime(ミリ秒)後 launchServiceを実行する
     * serviceIdはどのサービスかを区別する為のID(同じなら上書き)
     *
     * @param launchService
     * @param durationTime
     * @param serviceId
     * @param <T>
     */
    public <T> void set(Class<T> launchService, long durationTime, int serviceId) {

        Intent intent = new Intent(context, launchService);

        PendingIntent action = PendingIntent.getService(context, serviceId, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager) context
                .getSystemService(context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    durationTime, action);
        } else {
            alarm.setExact(AlarmManager.RTC_WAKEUP, durationTime, action);
        }
    }

    /**
     * 起動したい時刻(hour:minute)を指定するバージョン
     * 指定した時刻で毎日起動する
     *
     * @param launchService
     * @param hour
     * @param minute
     * @param serviceId
     * @param <T>
     */
    public <T> void setByTime(Class<T> launchService, int hour, int minute, int serviceId) {
        TimeZone tz = TimeZone.getDefault();

        //今日の目標時刻のカレンダーインスタンス作成
        Calendar calTarget = Calendar.getInstance();
        calTarget.setTimeZone(tz);
        calTarget.set(Calendar.HOUR_OF_DAY, hour);
        calTarget.set(Calendar.MINUTE, minute);
        calTarget.set(Calendar.SECOND, 0);

        //現在時刻のカレンダーインスタンス作成
        Calendar calNow = Calendar.getInstance();
        calNow.setTimeZone(tz);

        //ミリ秒取得
        long targetMs = calTarget.getTimeInMillis();
        long nowMs = calNow.getTimeInMillis();

        //今日ならそのまま指定
        if (targetMs >= nowMs) {
            set(launchService, targetMs, serviceId);
            //過ぎていたら明日の同時刻を指定
        } else {
            calTarget.add(Calendar.DAY_OF_MONTH, 1);
            targetMs = calTarget.getTimeInMillis();
            set(launchService, targetMs, serviceId);
        }

    }

    /**
     * キャンセル用
     *
     * @param launchService
     * @param serviceId
     * @param <T>
     */
    public <T> void cancel(Class<T> launchService, int serviceId) {
        Intent intent = new Intent(context, launchService);
        PendingIntent action = PendingIntent.getService(context, serviceId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(action);
    }

    public void setBySavedDailyAlertInputForget(int serviceId) {
        String savedTimeString = SharedPreferencesManager.getDailyAlertInputForgetTime(context);
        Boolean notifyEnable = SharedPreferencesManager.getDailyAlertInputForgetEnable(context);
        if (savedTimeString == null || !notifyEnable) {
            cancel(DailyAlertInputForgetService.class, serviceId);
            return;
        }

        String[] timeStrings = savedTimeString.split(":");
        int hourOfDay = Integer.valueOf(timeStrings[0]);
        int minute = Integer.valueOf(timeStrings[1]);

        setByTime(DailyAlertInputForgetService.class, hourOfDay, minute, serviceId);
    }

}
