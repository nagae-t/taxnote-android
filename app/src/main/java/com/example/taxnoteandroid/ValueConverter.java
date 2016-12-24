package com.example.taxnoteandroid;

import java.text.DecimalFormat;

/**
 * Created by umemotonon on 2016/12/24.
 */

//QQ ここクラスメソッドで作りたいんやけど。。どうでしょ？
public class ValueConverter {

    public String formatPrice(long price) {

        DecimalFormat formatForPriceStyle   = new DecimalFormat("#,###.##");
        String priceString                  = formatForPriceStyle.format(price);

        return priceString;
    }

    public String formatPriceWithSymbol(long price, boolean isExpense) {

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
