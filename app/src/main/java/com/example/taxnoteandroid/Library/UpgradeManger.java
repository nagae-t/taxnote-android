package com.example.taxnoteandroid.Library;

import android.content.Context;

import com.example.taxnoteandroid.Library.zeny.ZNUtils;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;

/**
 * Created by umemotonon on 2017/01/29.
 */

public class UpgradeManger {

    private static final String GOOGLE_PLAY_LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAm+14FzQyLcAO7X2zwFDWXwHDuzN8RA60R71JouG5TO6la3xh0A7uWIQ4Y2k1kvqa/fHRAOble7TxIDsy11GsLjD/2sI+e4p4pE5vDKeY3ARBadcQI7iDc/VVnkzCSrZeoGTYinm+99diGn71cGIlF+7ISnh98Kss1zguKLlY+tCkaDDCe+moghLYTvqVuJg27ShVfxxPpWr4gwMusdSMcbJLR6S4ajeWbEtacGAdEJnzQfuAH6RMnt/ggZa4CFRVbNnJA6Eft/CCQL7GFBwBYnkMfG+Jdr+66BcTHbtPP8cE5WdmjGzDje+iy5HGYyIfqiDTdBs178zgWKUS8TM9QwIDAQAB";
    private static final String GOOGLE_PLAY_ZENY_LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgGp6uJvklRusgPPk+VxDaZQ+zCzD/nowZqnz1CbVp8ADHsqsyXAxmYIT2bXnhVI/ZTcnsSp7RI2D+BrW/jMV7BThskGTGe56emywa2PI5dz3IGOr79gFlXvxLl4AI1yxktF8j8igN8RBSA5vA9tqOVhvecr0nhUEmzl0PBlGaXKsPgawvlKto9tZled6u3xvh80nsfwBJdAzt4CWYjH2NFuEA9aBbO+wDpkkQO/PO9ssFAIznUFZWe0vc3R6JOh7vXoQWp2AYUTtT1Dk1wAe11bC3s5w+D5UhWePoJQ99+wANZSEXqsX4ysSC7RVWVm13lmZgnNxMUS72yT35UCfTQIDAQAB";
    public static final String SKU_TAXNOTE_PLUS_ID = "taxnote.plus.sub";
    public static final String SKU_TAXNOTE_PLUS_ID1 = "taxnote.plus.sub1";
    public static final String SKU_TAXNOTE_PLUS_ID2 = "taxnote.plus.sub2";
    public static final String SKU_TAXNOTE_CLOUD_ID = "taxnote.cloud.sub";
    public static final String SKU_ZENY_PREMIUM_ID = "zeny.premium.sub";

    public static String getGooglePlayLicenseKey() {
        return (ZNUtils.isZeny()) ? GOOGLE_PLAY_ZENY_LICENSE_KEY
                : GOOGLE_PLAY_LICENSE_KEY;
    }

    public static boolean taxnotePlusIsActive(Context context) {
        long expireTime = SharedPreferencesManager.getTaxnotePlusExpiryTime(context);
        long now = System.currentTimeMillis();

        return expireTime > now;
    }

    public static boolean taxnoteCloudIsActive(Context context) {
        if (ZNUtils.isZeny()) return zenyPremiumIsActive(context);

        long expireTime = SharedPreferencesManager.getTaxnoteCloudExpiryTime(context);
        long now = System.currentTimeMillis();

        return expireTime > now;
    }

    public static boolean zenyPremiumIsActive(Context context) {
        long expireTime = SharedPreferencesManager.getZenyPremiumExpiryTime(context);
        long now = System.currentTimeMillis();

        return expireTime > now;
    }
}
