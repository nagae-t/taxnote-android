package com.example.taxnoteandroid;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.github.gfx.android.orma.AccessThreadConstraint;
import com.helpshift.All;
import com.helpshift.Core;
import com.helpshift.InstallConfig;
import com.helpshift.exceptions.InstallException;
import com.helpshift.support.Support;

import org.solovyev.android.checkout.Billing;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Eiichi on 2017/01/17.
 */

public class TaxnoteApp extends Application {
    private static OrmaDatabase ormaDatabase = null;


    public static OrmaDatabase getOrmaDatabase() {
        return ormaDatabase;
    }

    private static TaxnoteApp sInstance;

    private final Billing mBilling = new Billing(this, new Billing.DefaultConfiguration() {
        @Override

        //QQ これなに？
        public String getPublicKey() {
            return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiqf39c7TtSqe9FV2Xz/Xa2S6dexgD2k5qK1ZnC7uCctI2J+Y8GW1oG2S5wN/zdxB5nlkP/a94GiAZqmxhLknVFqRMq32f4zuT2M8mGxFmCMpqQbvYgI2hDXY0xS7c0EITHNPykTRAqS1tgjuHRDWrNjfae7FuvIEJMe4h41tbYAAdKh8Uv+sv3cVmmTXn2j+Ep42XhE1moLug26orCS7IfKAJjAiRK5lzCaCF3mNqPcjogxjG425P44oVT8Ewnx4+N9qbfkzQueCqkw4mD4UdBABCefjZ6t+N2+ZEwGreV/nu5P7kXOsDZp9SGlNB99rL21Xnpzc+QDQvUkBXlNTWQIDAQAB";
        }
    });

    public TaxnoteApp() {
        sInstance = this;
    }

    public static TaxnoteApp get() {
        return sInstance;
    }

    public Billing getBilling() {
        return mBilling;
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
}