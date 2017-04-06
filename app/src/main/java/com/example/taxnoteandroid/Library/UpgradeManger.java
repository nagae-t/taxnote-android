package com.example.taxnoteandroid.Library;

import android.content.Context;

import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;

import java.util.Calendar;

//import com.anjlab.android.iab.v3.TransactionDetails;

/**
 * Created by umemotonon on 2017/01/29.
 */

public class UpgradeManger {

    public static final String GOOGLE_PLAY_LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAm+14FzQyLcAO7X2zwFDWXwHDuzN8RA60R71JouG5TO6la3xh0A7uWIQ4Y2k1kvqa/fHRAOble7TxIDsy11GsLjD/2sI+e4p4pE5vDKeY3ARBadcQI7iDc/VVnkzCSrZeoGTYinm+99diGn71cGIlF+7ISnh98Kss1zguKLlY+tCkaDDCe+moghLYTvqVuJg27ShVfxxPpWr4gwMusdSMcbJLR6S4ajeWbEtacGAdEJnzQfuAH6RMnt/ggZa4CFRVbNnJA6Eft/CCQL7GFBwBYnkMfG+Jdr+66BcTHbtPP8cE5WdmjGzDje+iy5HGYyIfqiDTdBs178zgWKUS8TM9QwIDAQAB";
    public static final String SKU_TAXNOTE_PLUS_ID = "taxnote.plus.sub";
    public static final String SKU_TAXNOTE_PLUS_ID1 = "taxnote.plus.sub1";
    public static final String SKU_TAXNOTE_CLOUD_ID = "taxnote.cloud.sub";

    public static boolean taxnotePlusIsActive(Context context) {

        // Get saved purchaseTime
        long purchaseTime = SharedPreferencesManager.getTaxnotePlusPurchaseTime(context);

        // Get expireTime
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(purchaseTime);

        calendar.add(Calendar.YEAR, 1);

        long expireTime = calendar.getTimeInMillis();
        long now = System.currentTimeMillis();

        return expireTime > now;
    }

//    public static void updateTaxnotePlusSubscriptionStatus(Context context, TransactionDetails details) {
//
//        Date purchaseTime = details.purchaseInfo.purchaseData.purchaseTime;
//        SharedPreferencesManager.saveTaxnotePlusPurchaseTime(context,purchaseTime.getTime());
//    }
}
