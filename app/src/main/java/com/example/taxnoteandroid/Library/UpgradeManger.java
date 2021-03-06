package com.example.taxnoteandroid.Library;

import android.content.Context;

import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;

/**
 * Created by umemotonon on 2017/01/29.
 */

public class UpgradeManger {

    private static final String GOOGLE_PLAY_LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAm+14FzQyLcAO7X2zwFDWXwHDuzN8RA60R71JouG5TO6la3xh0A7uWIQ4Y2k1kvqa/fHRAOble7TxIDsy11GsLjD/2sI+e4p4pE5vDKeY3ARBadcQI7iDc/VVnkzCSrZeoGTYinm+99diGn71cGIlF+7ISnh98Kss1zguKLlY+tCkaDDCe+moghLYTvqVuJg27ShVfxxPpWr4gwMusdSMcbJLR6S4ajeWbEtacGAdEJnzQfuAH6RMnt/ggZa4CFRVbNnJA6Eft/CCQL7GFBwBYnkMfG+Jdr+66BcTHbtPP8cE5WdmjGzDje+iy5HGYyIfqiDTdBs178zgWKUS8TM9QwIDAQAB";
    public static final String SKU_TAXNOTE_PLUS_ID = "taxnote.plus.sub";
    public static final String SKU_TAXNOTE_PLUS_ID1 = "taxnote.plus.sub1";
    public static final String SKU_TAXNOTE_PLUS_ID2 = "taxnote.plus.sub2";
    public static final String SKU_TAXNOTE_PLUS_ID3 = "taxnote.plus.sub3";
    public static final String SKU_TAXNOTE_CLOUD_ID = "taxnote.cloud.sub";

    public static String getGooglePlayLicenseKey() {
        return GOOGLE_PLAY_LICENSE_KEY;
    }

    public static boolean taxnotePlusIsActive(Context context) {
        long expireTime = SharedPreferencesManager.getTaxnotePlusExpiryTime(context);
        long now = System.currentTimeMillis();

        return expireTime > now;
    }

    public static boolean taxnoteCloudIsActive(Context context) {
        long expireTime = SharedPreferencesManager.getTaxnoteCloudExpiryTime(context);
        long now = System.currentTimeMillis();

        return expireTime > now;
    }
}
