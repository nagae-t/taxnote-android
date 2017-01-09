package com.example.taxnoteandroid.Library;

import java.text.DecimalFormat;

/**
 * Created by umemotonon on 2016/12/24.
 */

public class ValueConverter {

    public static String formatPrice(long price) {

        DecimalFormat formatForPriceStyle   = new DecimalFormat("#,###.##");
        String priceString                  = formatForPriceStyle.format(price);

        return priceString;
    }

    public static String formatPriceWithSymbol(long price, boolean isExpense) {

        DecimalFormat formatForPriceStyle   = new DecimalFormat("#,###.##");
        String priceString                  = formatForPriceStyle.format(price);

        if (isExpense) {
            priceString = "-" + priceString;
        } else {
            priceString = "+" + priceString;
        }

        return priceString;
    }
}
