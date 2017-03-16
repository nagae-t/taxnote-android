package com.example.taxnoteandroid.Library;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;

/**
 * Created by b0ne on 2017/03/16.
 */

public class DailyAlertInputForgetService extends Service {

    private static Context mContext;
    protected final IBinder binder = new Binder() {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags ) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };

    public static void start(Context context) {
        Intent intent = new Intent(context, DailyAlertInputForgetService.class);
        context.startService(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("TEST", "AlertService onCreate");

        mContext = getApplicationContext();
        boolean isDailyAlertEnable = SharedPreferencesManager.getDailyAlertInputForgetEnable(mContext);
        if (!isDailyAlertEnable) {
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("TEST", "AlertService onStartCommand show notification");
        TNAppNotification appNotification = TNAppNotification.newInstanceForAlertInputForget(mContext);
        appNotification.show();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.v("TEST", "AlertService onDestroy");
        super.onDestroy();
    }
}
