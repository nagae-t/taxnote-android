package com.example.taxnoteandroid.Library;

import android.content.Context;

import com.example.taxnoteandroid.dataManager.ProjectDataManager;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by umemotonon on 2016/12/24.
 */

public class ValueConverter {

    public static String formatPrice(Context context, long price) {

        ProjectDataManager projectDataManager = new ProjectDataManager(context);
        boolean decimalStatus = projectDataManager.getDecimalStatusWithContect();
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

    public static long dateString2long(String dateString) {
        if (dateString == null) return 0;

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            cal.setTime(sdf.parse(dateString));
        } catch (ParseException e) {
            return 0;
        }
        return cal.getTimeInMillis();
    }

    public static String long2dateString(long time) {
        if (time <= 0) return "";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        cal.setTimeInMillis(time);
        return sdf.format(cal.getTime());
    }
}
