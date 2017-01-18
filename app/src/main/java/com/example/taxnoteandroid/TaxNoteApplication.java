package com.example.taxnoteandroid;

import android.app.Application;

import com.example.taxnoteandroid.model.OrmaDatabase;
import com.github.gfx.android.orma.AccessThreadConstraint;

/**
 * Created by Eiichi on 2017/01/17.
 */

public class TaxnoteApplication extends Application
{
  private static OrmaDatabase ormaDatabase = null;

  @Override
  public void onCreate()
  {
    super.onCreate();

    TaxnoteApplication.ormaDatabase = OrmaDatabase.builder(getApplicationContext())
      .trace(BuildConfig.DEBUG)
      .writeOnMainThread(AccessThreadConstraint.NONE)
      .readOnMainThread(AccessThreadConstraint.NONE)
      .build();
  }

  public static OrmaDatabase getOrmaDatabase()
  {
    return ormaDatabase;
  }
}