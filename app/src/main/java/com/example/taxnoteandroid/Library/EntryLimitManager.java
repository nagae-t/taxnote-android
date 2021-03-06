package com.example.taxnoteandroid.Library;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

import com.example.taxnoteandroid.R;
import com.example.taxnoteandroid.UpgradeActivity;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.model.Entry;
import com.example.taxnoteandroid.model.Project;

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

    private static final long limitNumberOfEntryPerMonth = 15;
    private static final long limitEntryAddSubProject = 10;


    public static boolean limitNewEntryForFreeUsersWithDate(Context context, long date) {
        if (UpgradeManger.taxnotePlusIsActive(context)) {
            return false;
        }

        // 初回は31件まで無料で入力可能に
        if (allowAdditionalEntryForNewUsers(context)) {
            return false;
        }

        // Get start and end date from the current date
        long[] startAndEndDate = getStartAndEndDate(date);

        EntryDataManager entryDataManager = new EntryDataManager(context);
        List<Entry> entries = entryDataManager.searchBy(null, null, startAndEndDate, false);

        // Check the count of entries within the month of the current date
        if (entries.size() < limitNumberOfEntryPerMonth) {
            return false;
        }

        return true;
    }

    private static boolean allowAdditionalEntryForNewUsers(Context context) {

        EntryDataManager entryDataManager = new EntryDataManager(context);
        List<Entry> entries = entryDataManager.findAll();

        // Show free entry extend message
        if (entries.size() > 15 && entries.size() < 32) {

            // Show the dialog only one time
            if (SharedPreferencesManager.isFreeEntryExtendViewDone(context)) {
                return true;
            }

            showFreeEntryExtendView(context);
            SharedPreferencesManager.saveFreeEntryExtendViewDone(context);
            return false;
        }

        if (entries.size() < 32) {
            return true;
        }

        return false;
    }

    private static void showFreeEntryExtendView(final Context context) {

        new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.free_limit_extend_title))
                .setMessage(context.getResources().getString(R.string.free_limit_extend_message))
                .setPositiveButton(context.getResources().getString(R.string.free_limit_extend_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showFreeEntryExtendDoneView(context);
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

    private static void showFreeEntryExtendDoneView(final Context context) {

        new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.free_limit_extend_done_title))
                .setMessage(context.getResources().getString(R.string.free_limit_extend_done_message))
                .setPositiveButton(context.getResources().getString(R.string.benefits_of_upgrade), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // Show upgrade activity
                        Intent intent = new Intent(context, UpgradeActivity.class);
                        context.startActivity(intent);
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(context.getResources().getString(R.string.cancel), null)
                .show();
    }

    public static boolean limitNewEntryAddSubProject(Context context) {

        ProjectDataManager projectDm = new ProjectDataManager(context);
        EntryDataManager entryDm = new EntryDataManager(context);
        Project project = projectDm.findCurrent();

        if (project.isMaster) return false;

        if (entryDm.countAll() < limitEntryAddSubProject) return false;

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

    public static long[] getStartAndEndDate(Context context, int periodType, Calendar c) {
        Calendar startCalendar = (Calendar) c.clone();
        Calendar endCalendar = (Calendar) c.clone();

        int lastDayOfMonthIndex = 26;
        int closingDateIndex = SharedPreferencesManager.getMonthlyClosingDateIndex(context);
        int startMonthIndex = SharedPreferencesManager.getStartMonthOfYearIndex(context);

        int startDate = 1;
        int endDate = 1;
        if (closingDateIndex < lastDayOfMonthIndex) {
            startDate = closingDateIndex + 3;
            endDate = closingDateIndex + 3;
        }
        int startMonth = c.get(Calendar.MONTH);
        int startYear = c.get(Calendar.YEAR);
        int endMonth = c.get(Calendar.MONTH);
        int endYear = c.get(Calendar.YEAR);

        // 締め日が15日以降なら次月分に
        if (startDate >= 15) {
            startMonth -= 1;
        }
        // 締め日が14日までなら前月分、または締め日が月末
        if (startDate < 15 || closingDateIndex == lastDayOfMonthIndex) {
            endMonth += 1;
        }

        // 開始月がマイナスなら去年12月にする
        if (startMonth < 0) {
            startMonth = 11;
            startYear -= 1;
        }

        if (startDate == 0) {
            Calendar _c = Calendar.getInstance();
            _c.set(startYear, startMonth, 1);
            startDate = _c.getActualMaximum(Calendar.DAY_OF_MONTH);
        }

        switch (periodType) {
            case EntryDataManager.PERIOD_TYPE_YEAR:
                int yearEndYear = c.get(Calendar.YEAR);
                startCalendar.set(yearEndYear, startMonthIndex, startDate);
                endCalendar.set(yearEndYear + 1, startMonthIndex, endDate);
                break;
            case EntryDataManager.PERIOD_TYPE_MONTH:
                startCalendar.set(startYear, startMonth, startDate);
                endCalendar.set(endYear, endMonth, endDate);
                break;
            case EntryDataManager.PERIOD_TYPE_DAY:
                startCalendar.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), 0, 0, 0);
                endCalendar.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), 0, 0, 0);
                endCalendar.add(Calendar.DATE, 1);
                break;
        }

        long[] result = {startCalendar.getTimeInMillis(), endCalendar.getTimeInMillis()};

        // for debug
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
//                context.getResources().getString(R.string.date_string_format_to_year_month_day));
//        String startCalStr = simpleDateFormat.format(startCalendar.getTime());
//        String endCalStr = simpleDateFormat.format(endCalendar.getTime());
        return result;
    }

    // priceによるソート
    public static List<Map.Entry<Long, Entry>> sortLinkedHashMap(Map<Long, Entry> sourceMap, final boolean byReason) {
        List<Map.Entry<Long, Entry>> dataList =
                new ArrayList<>(sourceMap.entrySet());
        Collections.sort(dataList, new Comparator<Map.Entry<Long, Entry>>() {

            @Override
            public int compare(Map.Entry<Long, Entry> entry1,
                               Map.Entry<Long, Entry> entry2) {
                long entry1Order = entry1.getValue().price;
                long entry2Order = entry2.getValue().price;
                if (byReason) {
                    entry1Order = entry2.getValue().reasonOrder;
                    entry2Order = entry1.getValue().reasonOrder;
                }

                if (entry1Order < entry2Order) {
                    return 1;
                } else if (entry1Order == entry2Order) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });
        return dataList;
    }

    public static List<Map.Entry<String, Entry>> sortMemoLinkedHashMap(Map<String, Entry> sourceMap) {
        List<Map.Entry<String, Entry>> dataList =
                new ArrayList<>(sourceMap.entrySet());
        Collections.sort(dataList, new Comparator<Map.Entry<String, Entry>>() {

            @Override
            public int compare(Map.Entry<String, Entry> entry1,
                               Map.Entry<String, Entry> entry2) {
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
