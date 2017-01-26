package com.example.taxnoteandroid.Library;

import android.content.Context;

import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.model.Entry;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by umemotonon on 2016/12/24.
 */

public class EntryLimitManager {

    private static final long limitNumberOfEntryPerMonth    = 15;


    public static boolean limitNewEntryForFreeUsersWithDate(Context context, long date) {

        final boolean taxnotePlusIsActive = SharedPreferencesManager.taxnotePlusIsActive(context);

        if (taxnotePlusIsActive) {
            return false;
        }

        // Get start and end date from the current date
        long[] startAndEndDate  = getStartAndEndDate(date);

        EntryDataManager entryDataManager   = new EntryDataManager(context);
        List<Entry> entries                 = entryDataManager.findAll(context, startAndEndDate, false);

        // Check the count of entries within the month of the current date
        if (entries.size() < limitNumberOfEntryPerMonth) {
            return false;
        }

        return true;
    }

    private static long[] getStartAndEndDate(long date) {

        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

        calendar.setTimeInMillis(date);
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

}
