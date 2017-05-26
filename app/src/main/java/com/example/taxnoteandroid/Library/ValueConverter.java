package com.example.taxnoteandroid.Library;

import android.content.Context;

import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;

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
        boolean decimalStatus = projectDataManager.getDecimalStatus();
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

    public static long cloudExpiryString2long(String expiryString) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'Etc/GMT'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            cal.setTime(sdf.parse(expiryString));
        } catch (ParseException e) {
            return 0;
        }
        return cal.getTimeInMillis();
    }

    public static String replaceFullMark(String text) {
        text = text.replace("＠", "@");
        text = text.replace("￥", "¥");
        return text;
    }

    public static String parseCategoryName(Context context, String name) {
        boolean isExportSubject = SharedPreferencesManager.getExportSujectEnable(context);
        if (!isExportSubject) return name;

        name = replaceFullMark(name);
        int index1 = name.indexOf("@");
        int index2 = name.indexOf("¥");
        if (index1 > -1) {
            name = name.substring(0, index1);
        } else if (index2 > -1) {
            name = name.substring(0, index2);
        }

        return name;
    }

    public static String parseSubCategoryName(Context context, String name) {
        String defaultName = "";
        boolean isExportSubject = SharedPreferencesManager.getExportSujectEnable(context);
        if (!isExportSubject) return defaultName;

        name = replaceFullMark(name);
        int index1 = name.indexOf("@");
        int lastIndex = name.length();
        if (index1 > -1) {
            name = name.substring(index1+1, lastIndex);
            if (name.length() == 0) return defaultName;

            int index2 = name.indexOf("¥");
            if (index2 > -1) {
                String _name = name.substring(0, index2);
                if (_name.length() > 0)
                    name = _name;
            }
            return name;
        }

        return defaultName;
    }

    public static String parseTaxPartName(Context context, String name) {
        String defaultName = "";
        boolean isExportSubject = SharedPreferencesManager.getExportSujectEnable(context);
        if (!isExportSubject) return defaultName;

        name = replaceFullMark(name);
        int index1 = name.indexOf("¥");
        int lastIndex = name.length();
        if (index1 > -1) {
            name = name.substring(index1+1, lastIndex);
            if (name.length() == 0) return defaultName;

            int index2 = name.indexOf("@");
            if (index2 > -1) {
                String _name = name.substring(0, index2);
                if (_name.length() > 0)
                    name = _name;
            }
            return name;
        }

        return defaultName;
    }

}
