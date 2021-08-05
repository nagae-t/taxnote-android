package com.example.taxnoteandroid;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.example.taxnoteandroid.Library.taxnote.TNApiModel;
import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.github.gfx.android.orma.AccessThreadConstraint;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Eiichi on 2017/01/17.
 */

public class TaxnoteApp extends MultiDexApplication {
    private static TaxnoteApp singleton;
    private static OrmaDatabase ormaDatabase = null;
    private AppStatus mAppStatus = AppStatus.FOREGROUND;

    // アプリ内共通の変数
    public Calendar SELECTED_TARGET_CAL = null;
    public List<Calendar> ALL_PERIOD_CALS = null;

    // 仕訳帳を選択して編集する際に扱う変数
    public boolean IS_HISTORY_LIST_EDITING = false;

    public static TaxnoteApp getInstance() {
        return singleton;
    }

    public static OrmaDatabase getOrmaDatabase() {
        return ormaDatabase;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        int themeStyle = SharedPreferencesManager.getAppThemeStyle(this);
        base.setTheme(ProjectDataManager.getThemeStyle(themeStyle));
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;

        // Orma database
        TaxnoteApp.ormaDatabase = OrmaDatabase.builder(getApplicationContext())
                .trace(BuildConfig.DEBUG)
                .writeOnMainThread(AccessThreadConstraint.NONE)
                .readOnMainThread(AccessThreadConstraint.NONE)
                .build();

        registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks());
    }

    public enum AppStatus {
        BACKGROUND,                // app is background
        RETURNED_TO_FOREGROUND,    // app returned to foreground(or first launch)
        FOREGROUND                // app is foreground
    }

    // readonly
    public AppStatus getAppStatus() {
        return mAppStatus;
    }

    public class MyActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

        // running activity count
        private int running = 0;
        private TNApiModel apiModel;

        public MyActivityLifecycleCallbacks() {
            apiModel = new TNApiModel(getApplicationContext());
            apiModel.setIsSyncing(false);
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
            if (++running == 1) {
                // running activity is 1,
                // app must be returned from background just now (or first launch)
                mAppStatus = AppStatus.RETURNED_TO_FOREGROUND;
            } else if (running > 1) {
                // 2 or more running activities,
                // should be foreground already.
                mAppStatus = AppStatus.FOREGROUND;
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {
            if (mAppStatus == null || apiModel == null) return;
            if (mAppStatus == AppStatus.RETURNED_TO_FOREGROUND) {
                // 起動時、アプリ再表示のときにデータの同期を行う
                apiModel.syncData(activity, null);
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
            if (--running == 0) {
                // no active activity
                // app goes to background
                mAppStatus = AppStatus.BACKGROUND;
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }
    }
}