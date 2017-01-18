package com.example.taxnoteandroid.Library;

import android.content.Context;

import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.model.Entry;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_RANGE_TYPE_ALL;
import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_RANGE_TYPE_CUSTOM;
import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_RANGE_TYPE_LAST_MONTH;
import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_RANGE_TYPE_THIS_MONTH;

/**
 * Created by umemotonon on 2016/12/24.
 */

public class DataExportManager {

    public static void export(Context context) {

        List<Entry> entries = getSelectedRangeEntries(context);


    }

    public static List<Entry> getSelectedRangeEntries(Context context) {

        List<Entry> entries;
        long[] start_end;
        EntryDataManager entryDataManager = new EntryDataManager(context);

        String exportRangeType = SharedPreferencesManager.getExportRangeType(context);

        switch(exportRangeType) {

            case EXPORT_RANGE_TYPE_ALL:
                entries     = entryDataManager.findAll(context, null);
                break;

            case EXPORT_RANGE_TYPE_THIS_MONTH:
                start_end   = getThisMonthStartAndEndDate();
                entries     = entryDataManager.findAll(context, start_end);
                break;

            case EXPORT_RANGE_TYPE_LAST_MONTH:
                start_end   = getLastMonthStartAndEndDate();
                entries     = entryDataManager.findAll(context, start_end);
                break;

            case EXPORT_RANGE_TYPE_CUSTOM:
                start_end   = getCustomStartAndEndDate(context);
                entries     = entryDataManager.findAll(context, start_end);
                break;

            default:
                entries = entryDataManager.findAll(context, null);
                break;
        }

        return entries;
    }

    private static long[] getThisMonthStartAndEndDate() {

        long now            = System.currentTimeMillis();
        Calendar calendar   = Calendar.getInstance(TimeZone.getDefault());

        calendar.setTimeInMillis(now);
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long time_start = calendar.getTimeInMillis();
        calendar.add(Calendar.MONTH, 1);
        long time_end = calendar.getTimeInMillis();

        long[] start_end = {time_start, time_end};

        return start_end;
    }

    private static long[] getLastMonthStartAndEndDate() {

        long now            = System.currentTimeMillis();
        Calendar calendar   = Calendar.getInstance(TimeZone.getDefault());

        calendar.setTimeInMillis(now);
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long time_end = calendar.getTimeInMillis();
        calendar.add(Calendar.MONTH, - 1);
        long time_start = calendar.getTimeInMillis();

        long[] start_end = {time_start, time_end};

        return start_end;
    }

    private static long[] getCustomStartAndEndDate(Context context) {

        // Get saved begin and end dates
        long beginDate      = SharedPreferencesManager.getDateRangeBeginDate(context);
        long endDate        = SharedPreferencesManager.getDateRangeEndDate(context);

        // Add one day
        Calendar calendar   = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTimeInMillis(endDate);
        calendar.add(Calendar.HOUR, 24);
        endDate = calendar.getTimeInMillis();

        long[] start_end = {beginDate, endDate};

        return start_end;
    }

//    public static String join(CharSequence delimiter, List<Entry> entries) {
//        StringBuilder sb = new StringBuilder();
//        boolean firstTime = true;
////        for (Object token: tokens) {
////            if (firstTime) {
////                firstTime = false;
////            } else {
////                sb.append(delimiter);
////            }
////            sb.append(token);
////        }
//        return sb.toString();
//    }
}
