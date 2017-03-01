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
import com.example.taxnoteandroid.databinding.FragmentGraphTabBinding;
import com.example.taxnoteandroid.model.Entry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by b0ne on 2017/02/27.
 */

public class GraphTabFragment extends Fragment  {

    private Context mContext;
    private FragmentGraphTabBinding binding;
    private GraphContentFragmentPagerAdapter mPagerAdapter;
    private int mCurrentPagerPosition = -1;
    private boolean mIsExpense;

    public static GraphTabFragment newInstance() {
        GraphTabFragment fragment = new GraphTabFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGraphTabBinding.inflate(inflater, container, false);
        binding.strip.setTabIndicatorColorResource(R.color.accent);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity().getApplicationContext();


        int periodType = SharedPreferencesManager.getProfitLossReportPeriodType(mContext);
        mIsExpense = SharedPreferencesManager.getGraphReportIsExpenseType(mContext);
        switchDataView(periodType, mIsExpense);

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

    @Override
    public void onResume() {
        super.onResume();
    }

    private List<Calendar> createData(ReportGrouping reportGrouping) {
        EntryDataManager entryDataManager = new EntryDataManager(mContext);
        List<Entry> entries = entryDataManager.findAll(mContext, null, true);
        List<Calendar> result = new ArrayList<>();
        for (Entry entry : entries) {
            Calendar calendar = reportGrouping.getGroupingCalendar(entry);

            if (!result.contains(calendar)) {
                result.add(calendar);
            }
        }

        return result;
    }

    public void reloadData() {
        int periodType = SharedPreferencesManager.getProfitLossReportPeriodType(mContext);
        boolean isExpense = SharedPreferencesManager.getGraphReportIsExpenseType(mContext);
        switchDataView(periodType, isExpense);
    }

    public void switchDataView(boolean isExpense) {
        int periodType = SharedPreferencesManager.getProfitLossReportPeriodType(mContext);
        switchDataView(periodType, isExpense);
    }

    public void switchDataView(int periodType) {
        boolean isExpense = SharedPreferencesManager.getGraphReportIsExpenseType(mContext);
        switchDataView(periodType, isExpense);
    }

    public void switchDataView(int periodType, boolean isExpense) {
        ReportGrouping reportGrouping = new ReportGrouping(periodType);
        SharedPreferencesManager.saveProfitLossReportPeriodType(mContext, periodType);
        SharedPreferencesManager.saveGraphReportIsExpenseType(mContext, isExpense);

        List<Calendar> calendars = createData(reportGrouping);
        mPagerAdapter = new GraphContentFragmentPagerAdapter(
                getChildFragmentManager(), reportGrouping, calendars, isExpense);
        binding.pager.setAdapter(mPagerAdapter);
        if (mCurrentPagerPosition < 0) {
            binding.pager.setCurrentItem(mPagerAdapter.getCount() - 1);
        } else {
            binding.pager.setCurrentItem(mCurrentPagerPosition);
        }
    }

    private class GraphContentFragmentPagerAdapter extends FragmentStatePagerAdapter {

        private final List<Calendar> calendars;
        private final ReportGrouping reportGrouping;
        private final boolean isExpense;

        public GraphContentFragmentPagerAdapter(FragmentManager fm,
                                                ReportGrouping reportGrouping,
                                                List<Calendar> calendars,
                                                boolean isExpense) {
            super(fm);
            this.reportGrouping = reportGrouping;
            this.calendars = calendars;
            this.isExpense = isExpense;
        }

        @Override
        public Fragment getItem(int position) {
            Calendar targetCalender = calendars.get(position);
            return GraphContentFragment.newInstance(targetCalender, isExpense);
        }

        @Override
        public int getCount() {
            if (calendars == null) return 0;
            return calendars.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return reportGrouping.getTabTitle(calendars.get(position));
        }

        @Override
        public int getItemPosition(Object object){
            return POSITION_NONE;
        }
    }

}
