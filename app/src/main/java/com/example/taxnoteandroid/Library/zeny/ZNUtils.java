package com.example.taxnoteandroid.Library.zeny;

import com.example.taxnoteandroid.BuildConfig;

/**
 * Created by b0ne on 2017/04/21.
 */

public class ZNUtils {
    public static final String PACKAGE_NAME = "com.nonapp.zeny";

    public static boolean isZeny(String packageName) {
        return packageName.startsWith(PACKAGE_NAME);
    }

    public static boolean isZeny() {
        return BuildConfig.FLAVOR.startsWith("zeny");
    }
}
