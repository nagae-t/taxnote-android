package com.example.taxnoteandroid.Library;

import android.content.Context;

import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.model.Entry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by umemotonon on 2016/12/24.
 */

public class EntryLimitManager {

    private static final long limitNumberOfEntryPerMonth    = 15;


    public static boolean limitNewEntryForFreeUsersWithDate(Context context, long date) {

        if (UpgradeManger.taxnotePlusIsActive(context)) {
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

    public static long[] getStartAndEndDate(int periodType, Calendar c) {
        Calendar startDate = (Calendar)c.clone();
        Calendar endDate = (Calendar)c.clone();

        switch (periodType) {
            case EntryDataManager.PERIOD_TYPE_YEAR:
                endDate.set(c.get(Calendar.YEAR)+1, 0, 1);
                break;
            case EntryDataManager.PERIOD_TYPE_MONTH:
                endDate.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, 1);
                break;
            case EntryDataManager.PERIOD_TYPE_DAY:
                endDate.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), 0, 0, 0);
                endDate.add(Calendar.DATE, 1);
                break;
        }

        long[] result = {startDate.getTimeInMillis(), endDate.getTimeInMillis()};
        return result;
    }

    public static List<Map.Entry<Long, Entry>> sortLinkedHashMap(Map<Long, Entry> sourceMap) {
        List<Map.Entry<Long, Entry>> dataList =
                new ArrayList<>(sourceMap.entrySet());
        Collections.sort(dataList, new Comparator<Map.Entry<Long, Entry>>() {

            @Override
            public int compare(Map.Entry<Long, Entry> entry1,
                               Map.Entry<Long, Entry> entry2) {
                long entry1sum = entry1.getValue().price;
                long entry2sum = entry2.getValue().price;
                if (entry1sum < entry2sum) {
                    return 1;
                } else if (entry1sum == entry2sum) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });
        return dataList;
    }

}
