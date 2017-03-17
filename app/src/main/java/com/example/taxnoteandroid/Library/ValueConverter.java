package com.example.taxnoteandroid.Library;

import android.content.Context;

import com.example.taxnoteandroid.dataManager.ProjectDataManager;

import java.text.DecimalFormat;

/**
 * Created by umemotonon on 2016/12/24.
 */

public class ValueConverter {

    public static String formatPrice(Context context, long price) {

        ProjectDataManager projectDataManager = new ProjectDataManager(context);
        boolean decimalStatus = projectDataManager.getDecimalStatusWithContect(context);
        String priceString;

        if (decimalStatus) {

            DecimalFormat formatForPriceStyle   = new DecimalFormat("#,##0.00");
            Double doublePrice                  = price / 100D;
            priceString                         = formatForPriceStyle.format(doublePrice);

        } else {
            DecimalFormat formatForPriceStyle   = new DecimalFormat("#,###");
            priceString                         = formatForPriceStyle.format(price);
        }

        return priceString;
    }

    public static String formatPriceWithSymbol(Context context, long price, boolean isExpense) {

        String priceString = formatPrice(context, price);

//        DecimalFormat formatForPriceStyle   = new DecimalFormat("#,###.##");
//        String priceString                  = formatForPriceStyle.format(price);

        if (isExpense) {
            priceString = "-" + priceString;
        } else {
            priceString = "+" + priceString;
        }

        return priceString;
    }
}
