package com.example.taxnoteandroid.Library.taxnote;

import android.content.Context;

import com.example.taxnoteandroid.R;
import com.example.taxnoteandroid.dataManager.EntryDataManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TNUtils {

    public static String getCalendarStringFromPeriodType(Context context,
                                                         Calendar cal, int periodType) {
        String dateFormatStr;
        switch (periodType) {
            case EntryDataManager.PERIOD_TYPE_MONTH:
                dateFormatStr = context.getString(R.string.date_string_format_to_year_month);
                break;
            case EntryDataManager.PERIOD_TYPE_DAY:
                dateFormatStr = context.getString(R.string.date_string_format_to_year_month_day);
                break;
            default: // year;
                dateFormatStr = context.getString(R.string.date_string_format_to_year);

        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                dateFormatStr, Locale.getDefault());
        String calStr = simpleDateFormat.format(cal.getTime());
        return calStr;
    }
}
