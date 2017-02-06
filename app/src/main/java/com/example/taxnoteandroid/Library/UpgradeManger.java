package com.example.taxnoteandroid.Library;

import android.content.Context;

import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;

import java.util.Calendar;

//import com.anjlab.android.iab.v3.TransactionDetails;

/**
 * Created by umemotonon on 2017/01/29.
 */

public class UpgradeManger {

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
