package com.example.taxnoteandroid.Library;

import java.text.DecimalFormat;

/**
 * Created by umemotonon on 2016/12/24.
 */

public class ValueConverter {

    public static String formatPrice(long price) {

        //@@ ここで 10,000円を 100.00と表示するようにしたい
        DecimalFormat formatForPriceStyle   = new DecimalFormat("#,###.##");
//        String priceString                  = formatForPriceStyle.format(price / 100D);

        Double doublePrice = price / 100D;

//        String priceString                  = formatForPriceStyle.format(price);

        String priceString                  = formatForPriceStyle.format(doublePrice);

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
