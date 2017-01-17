package com.example.taxnoteandroid;

import android.app.Application;

import com.example.taxnoteandroid.model.OrmaDatabase;
import com.github.gfx.android.orma.AccessThreadConstraint;
import com.helpshift.All;
import com.helpshift.Core;
import com.helpshift.InstallConfig;
import com.helpshift.exceptions.InstallException;
import com.helpshift.support.Support;

/**
 * Created by Eiichi on 2017/01/17.
 */

public class TaxNoteApplication extends Application
{
  private static OrmaDatabase ormaDatabase = null;

  @Override
  public void onCreate()
  {
    super.onCreate();

    // Orma database
    TaxNoteApplication.ormaDatabase = OrmaDatabase.builder(getApplicationContext())
      .trace(BuildConfig.DEBUG)
      .writeOnMainThread(AccessThreadConstraint.NONE)
      .readOnMainThread(AccessThreadConstraint.NONE)
      .build();


    // You initialize the library by calling Core.install(APPLICATION, API_KEY, DOMAIN,
    // APP_ID) in your application's onCreate()

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

  public static OrmaDatabase getOrmaDatabase()
  {
    return ormaDatabase;
  }
}