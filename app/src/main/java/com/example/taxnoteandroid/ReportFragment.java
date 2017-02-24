package com.example.taxnoteandroid;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.databinding.FragmentReportBinding;
import com.example.taxnoteandroid.model.Entry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportFragment extends Fragment {

    private FragmentReportBinding binding;

    // レポート期間タイプ別の定義
    public static final int PERIOD_TYPE_YEAR = 1;
    public static final int PERIOD_TYPE_MONTH = 2;
    public static final int PERIOD_TYPE_DAY = 3;

    private static final String KEY_PERIOD_TYPE = "report_period_type";

    public ReportFragment() {
    }

    public static ReportFragment newInstance() {
        ReportFragment fragment = new ReportFragment();
        return fragment;
    }

    public static ReportFragment newInstance(int peroidType) {
        ReportFragment fragment = new ReportFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_PERIOD_TYPE, peroidType);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReportBinding.inflate(inflater, container, false);
        Context context = getContext();

        // @@ ボタン押したあとReportGroupingの実装を切り替える
        ReportGrouping reportGrouping = new ReportYearGrouping();
        Map<Calendar, List<Entry>> map = createReportDate(context, reportGrouping);
        binding.pager.setAdapter(new ReportContentFragmentPagerAdapter2(getChildFragmentManager(), reportGrouping, map));

        return binding.getRoot();
    }

    private Map<Calendar, List<Entry>> createReportDate(Context context, ReportGrouping reportGrouping) {
        EntryDataManager entryDataManager = new EntryDataManager(context);
        List<Entry> entries = entryDataManager.findAll(context, null, true);
        Map<Calendar, List<Entry>> map = new LinkedHashMap<>();
        for (Entry entry : entries) {
            Calendar calendar = reportGrouping.getGroupingCalendar(entry);

//            for (Calendar c : map.keySet()) {
//                Log.d("cccc", "calendar : " + calendar.toString());
//                Log.d("cccc", "c : " + c.toString());
//                Log.d("cccc", "" + calendar.equals(c));
//            }

            if (map.containsKey(calendar)) {
                map.get(calendar).add(entry);
            } else {
                List<Entry> list = new ArrayList<>();
                list.add(entry);
                map.put(calendar, list);
            }
        }
        return map;
    }

    public void switchReportPeriod(int periodType) {
        Log.v("TEST", "switchReportPeriod type: " + periodType);
        switch (periodType) {
            case PERIOD_TYPE_YEAR:
                break;
            case PERIOD_TYPE_MONTH:
                break;
            case PERIOD_TYPE_DAY:
                break;
        }
    }

    public interface ReportGrouping {
        Calendar getGroupingCalendar(Entry entry);

        String createTitle(Context context, Calendar calendar);
    }

    public class ReportYearGrouping implements ReportGrouping {

        @Override
        public Calendar getGroupingCalendar(Entry entry) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(entry.date);
            calendar.set(calendar.get(Calendar.YEAR), 0, 1, 0, 0, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar;
        }

        @Override
        public String createTitle(Context context, Calendar c) {
            return Integer.toString(c.get(Calendar.YEAR));
        }
    }

    public class ReportMonthGrouping implements ReportGrouping {

        @Override
        public Calendar getGroupingCalendar(Entry entry) {
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.setTimeInMillis(entry.date);
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1, 0, 0, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar;
        }

        @Override
        public String createTitle(Context context, Calendar c) {
            return Integer.toString(c.get(Calendar.YEAR)) + "/" + Integer.toString(c.get(Calendar.MONTH));
        }
    }

    public class ReportDayGrouping implements ReportGrouping {

        @Override
        public Calendar getGroupingCalendar(Entry entry) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(entry.date);
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), 0, 0, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar;
        }

        @Override
        public String createTitle(Context context, Calendar c) {
            return Integer.toString(c.get(Calendar.YEAR)) + "/" + Integer.toString(c.get(Calendar.MONTH)) + "/" + Integer.toString(c.get(Calendar.DATE));
        }
    }

    public class ReportContentFragmentPagerAdapter2 extends FragmentPagerAdapter {

        private final ReportGrouping reportGrouping;
        private final Map<Calendar, List<Entry>> map;
        private final int count;
        private final Calendar[] calendars;

        public ReportContentFragmentPagerAdapter2(FragmentManager fm, ReportGrouping reportGrouping, Map<Calendar, List<Entry>> map) {
            super(fm);
            this.reportGrouping = reportGrouping;
            this.map = map;
            this.count = map.size();
            calendars = map.keySet().toArray(new Calendar[map.keySet().size()]);
        }

        @Override
        public Fragment getItem(int position) {
            return ReportContentFragment.newInstance(map.get(calendars[position]));
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return reportGrouping.createTitle(null, calendars[position]);
        }
    }
}
