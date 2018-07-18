package com.example.taxnoteandroid.Library;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by b0ne on 2018/07/18.
 */

public class AppPermission {

    public static final int REQUEST_CODE_REQUEST_PERMISSION = 100;

    public static void permissionsCheck(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
//        KVStorage storage = AppStorage.getStorage(activity);
//        String checkedKey = AppStorage.KEY_IS_MARSHMALLOW_PERMISSIONS_CHECKED;
//        boolean isFirstChecked = storage.getBoolean(checkedKey);
//        if (isFirstChecked) return;

        checkAndRequestPermissions(activity, new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
        }, REQUEST_CODE_REQUEST_PERMISSION);
//        storage.saveBoolean(checkedKey, true);
    }

    public static void checkAndRequestPermissions(Activity activity, String[] permissions, int requestCode) {
        boolean allGranted = true;
        for (String permission: permissions) {
            if (!hasSelfPermission(activity.getApplicationContext(), permission)) {
                allGranted = false;
            }
        }

        if (!allGranted) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }
    }

    public static void requestPermission(Activity activity, String permission) {
        ActivityCompat.requestPermissions(activity,
                new String[]{permission}, REQUEST_CODE_REQUEST_PERMISSION);
    }
    public static void requestPermissions(Activity activity, String[] permissions) {
        ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE_REQUEST_PERMISSION);
    }

    public static boolean hasSelfPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
}
