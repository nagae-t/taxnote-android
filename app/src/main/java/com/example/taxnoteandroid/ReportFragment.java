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
import com.example.taxnoteandroid.dataManager.EntryDataManager.ReportGrouping;
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
    private ReportContentFragmentPagerAdapter mPagerAdapter;
    private int mCurrentPagerPosition = -1;

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
        ReportGrouping reportGrouping = new ReportGrouping(periodType);
        // 期間タイプをデフォルト値として保存
        SharedPreferencesManager.saveProfitLossReportPeriodType(mContext, periodType);

        Map<Calendar, List<Entry>> map = createReportDate(reportGrouping);
        mPagerAdapter = new ReportContentFragmentPagerAdapter(getChildFragmentManager(), reportGrouping, map);
        binding.pager.setAdapter(mPagerAdapter);
        if (mCurrentPagerPosition < 0) {
            binding.pager.setCurrentItem(mPagerAdapter.getCount() - 1);
        } else {
            binding.pager.setCurrentItem(mCurrentPagerPosition);
        }
    }

    public class ReportContentFragmentPagerAdapter extends FragmentStatePagerAdapter {

        private final ReportGrouping reportGrouping;
        private final int count;
        private final Calendar[] calendars;

        public ReportContentFragmentPagerAdapter(FragmentManager fm, ReportGrouping reportGrouping, Map<Calendar, List<Entry>> map) {
            super(fm);
            this.reportGrouping = reportGrouping;
            this.count = map.size();
            calendars = map.keySet().toArray(new Calendar[map.keySet().size()]);
        }

        @Override
        public Fragment getItem(int position) {
            Calendar targetCalender = calendars[position];
            return ReportContentFragment.newInstance(targetCalender);
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return reportGrouping.getTabTitle(calendars[position]);
        }

        @Override
        public int getItemPosition(Object object){
            return POSITION_NONE;
        }

    }

}
