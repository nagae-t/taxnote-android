package com.example.taxnoteandroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;

/**
 * Created by b0ne on 2017/03/08.
 */

public class DefaultCommonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeStyle = SharedPreferencesManager.getAppThemeStyle(this);
        setTheme(ProjectDataManager.getThemeStyle(themeStyle));
        super.onCreate(savedInstanceState);

    }
}
