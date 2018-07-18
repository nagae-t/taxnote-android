package com.example.taxnoteandroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;

/**
 * Created by b0ne on 2017/03/08.
 */

public class DefaultCommonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeStyle = SharedPreferencesManager.getAppThemeStyle(this);
        Log.v("TEST", "DefaultCommonActivity themeStyle: " + themeStyle);
        /*
        switch (themeStyle) {
            case 0:
                setTheme(R.style.AppTheme);
                break;
            case 1:
                setTheme(R.style.AppThemeSecond);
                break;
            case 2:
                setTheme(R.style.AppThemeThird);
                break;
        }*/
        setTheme(ProjectDataManager.getThemeStyle(themeStyle));
        super.onCreate(savedInstanceState);

    }
}
