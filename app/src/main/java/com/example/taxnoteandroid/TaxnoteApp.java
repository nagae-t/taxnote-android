package com.example.taxnoteandroid;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.github.gfx.android.orma.AccessThreadConstraint;
import com.helpshift.All;
import com.helpshift.Core;
import com.helpshift.InstallConfig;
import com.helpshift.exceptions.InstallException;
import com.helpshift.support.Support;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Eiichi on 2017/01/17.
 */

public class TaxnoteApp extends Application {
    private static OrmaDatabase ormaDatabase = null;
    private AppStatus mAppStatus = AppStatus.FOREGROUND;

    public static OrmaDatabase getOrmaDatabase() {
        return ormaDatabase;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        int themeStyle = SharedPreferencesManager.getAppThemeStyle(this);
        switch (themeStyle) {
            case 0:
                base.setTheme(R.style.AppTheme);
                break;
            case 1:
                base.setTheme(R.style.AppThemeSecond);
                break;
            case 2:
                base.setTheme(R.style.AppThemeThird);
                break;
        }
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Fabric
        Fabric.with(this, new Crashlytics());

        // Orma database
        TaxnoteApp.ormaDatabase = OrmaDatabase.builder(getApplicationContext())
                .trace(BuildConfig.DEBUG)
                .writeOnMainThread(AccessThreadConstraint.NONE)
                .readOnMainThread(AccessThreadConstraint.NONE)
                .build();

        // Helpshift
        Core.init(All.getInstance());
        InstallConfig installConfig = new InstallConfig.Builder()
                .setEnableInAppNotification(true)
                .build();
        try {
            Core.install(this,
                    "14f761394d47454be7d6db4956f8e4ae",
                    "texttospeech.helpshift.com",
                    "texttospeech_platform_20170117101706929-9d883d52d724719",
                    installConfig);
        } catch (InstallException e) {
            android.util.Log.e("Helpshift", "install call : ", e);
        }
        android.util.Log.d("Helpshift", Support.libraryVersion + " - is the version for gradle");

    }

    public enum AppStatus {
        BACKGROUND,                // app is background
        RETURNED_TO_FOREGROUND,    // app returned to foreground(or first launch)
        FOREGROUND;                // app is foreground
    }

    public class MyActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

        // running activity count
        private int running = 0;

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