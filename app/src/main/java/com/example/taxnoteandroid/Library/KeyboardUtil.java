package com.example.taxnoteandroid.Library;

/**
 * Created by Eiichi on 2017/01/18.
 */

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class KeyboardUtil{

  public static void hideKeyboard(final Activity activity, final View view)
  {
    if(view==null){
      return;
    }

    try{
      InputMethodManager manager = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
      manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
      view.clearFocus();
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  public static void showKeyboard(final Activity activity, final View view)  {

    if (activity == null) {
      return;
    }

    view.requestFocus();

    final Runnable runnable = new Runnable(){
      @Override
      public void run(){

        try{
          InputMethodManager manager = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
          manager.showSoftInput(view.findFocus(), InputMethodManager.SHOW_FORCED);
        }
        catch(Exception e){
          e.printStackTrace();
        }
      }
    };

    new Handler().post(runnable);
  }
}
