package com.example.taxnoteandroid;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.FragmentReportBinding;
import com.example.taxnoteandroid.model.Entry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportFragment extends Fragment {

    private Context mContext;
    private FragmentReportBinding binding;
    private ReportContentFragmentPagerAdapter2 mPagerAdapter;
    private int mCurrentPagerPosition = -1;

    // レポート期間タイプ別の定義
    public static final int PERIOD_TYPE_YEAR = 1;
    public static final int PERIOD_TYPE_MONTH = 2;
    public static final int PERIOD_TYPE_DAY = 3;

    public ReportFragment() {
    }

    public static ReportFragment newInstance() {
        ReportFragment fragment = new ReportFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReportBinding.inflate(inflater, container, false);
        binding.strip.setTabIndicatorColorResource(R.color.accent);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity().getApplicationContext();

        int periodType = SharedPreferencesManager.getProfitLossReportPeriodType(mContext);

        // ボタン押したあとReportGroupingの実装を切り替える
        switchReportPeriod(periodType);

        binding.pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPagerPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private Map<Calendar, List<Entry>> createReportDate(ReportGrouping reportGrouping) {
        EntryDataManager entryDataManager = new EntryDataManager(mContext);
        List<Entry> entries = entryDataManager.findAll(mContext, null, true);
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

    @Override
    public void onResume() {
        super.onResume();
    }

    public void reloadData() {
        int periodType = SharedPreferencesManager.getProfitLossReportPeriodType(mContext);
        switchReportPeriod(periodType);
    }

    /**
     * 期間別のタイプで表示を切り替える
     *
     * @param periodType
     */
    public void switchReportPeriod(int periodType) {

        // ボタン押したあとReportGroupingの実装を切り替える
        ReportGrouping reportGrouping = new ReportYearGrouping();
        switch (periodType) {
            case PERIOD_TYPE_MONTH:
                reportGrouping = new ReportMonthGrouping();
                break;
            case PERIOD_TYPE_DAY:
                reportGrouping = new ReportDayGrouping();
                break;
        }
        // 期間タイプをデフォルト値として保存
        SharedPreferencesManager.saveProfitLossReportPeriodType(mContext, periodType);

        Map<Calendar, List<Entry>> map = createReportDate(reportGrouping);
        mPagerAdapter = new ReportContentFragmentPagerAdapter2(getChildFragmentManager(), reportGrouping, map);
        binding.pager.setAdapter(mPagerAdapter);
        if (mCurrentPagerPosition < 0) {
            binding.pager.setCurrentItem(mPagerAdapter.getCount() - 1);
        } else {
            binding.pager.setCurrentItem(mCurrentPagerPosition);
        }
    }

    public interface ReportGrouping {
        Calendar getGroupingCalendar(Entry entry);

        String createTitle(Calendar calendar);
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
        public String createTitle(Calendar c) {
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
        public String createTitle(Calendar c) {
            return Integer.toString(c.get(Calendar.YEAR))
                    + "/" + Integer.toString(c.get(Calendar.MONTH) + 1);
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
        public String createTitle(Calendar c) {
            return Integer.toString(c.get(Calendar.YEAR))
                    + "/" + Integer.toString(c.get(Calendar.MONTH) + 1)
                    + "/" + Integer.toString(c.get(Calendar.DATE));
        }
    }

    public class ReportContentFragmentPagerAdapter2 extends FragmentStatePagerAdapter {

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
            Calendar targetCalender = calendars[position];
            return ReportContentFragment.newInstance(map.get(targetCalender), targetCalender);
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return reportGrouping.createTitle(calendars[position]);
        }

        @Override
        public int getItemPosition(Object object){
            return POSITION_NONE;
        }

    }

}
