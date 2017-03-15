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

import com.example.taxnoteandroid.Library.EntryLimitManager;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.EntryDataManager.ReportGrouping;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.FragmentReportBinding;
import com.example.taxnoteandroid.model.Entry;

import java.util.Calendar;
import java.util.List;

public class ReportFragment extends Fragment {

    private Context mContext;
    private FragmentReportBinding binding;
    private ReportContentFragmentPagerAdapter mPagerAdapter;
    private EntryDataManager mEntryDataManager;
    private int mCurrentPagerPosition = -1;
    private int mClosingDateIndex = 0;

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

        mEntryDataManager = new EntryDataManager(mContext);
        mClosingDateIndex = SharedPreferencesManager.getMonthlyClosingDateIndex(mContext);
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


    @Override
    public void onResume() {
        super.onResume();
    }

    public void reloadData() {
        int periodType = SharedPreferencesManager.getProfitLossReportPeriodType(mContext);
        switchReportPeriod(periodType);
    }

    public long[] getStartEndDate() {
        ReportContentFragment fragment = (ReportContentFragment) mPagerAdapter
                .instantiateItem(binding.pager, binding.pager.getCurrentItem());
        return fragment.getStartEndDate();
    }

    /**
     * 期間別のタイプで表示を切り替える
     *
     * @param periodType
     */
    public void switchReportPeriod(int periodType) {
        mClosingDateIndex = SharedPreferencesManager.getMonthlyClosingDateIndex(mContext);

        // ボタン押したあとReportGroupingの実装を切り替える
        ReportGrouping reportGrouping = new ReportGrouping(periodType);
        // 期間タイプをデフォルト値として保存
        SharedPreferencesManager.saveProfitLossReportPeriodType(mContext, periodType);

        List<Entry> entries = mEntryDataManager.findAll(mContext, null, true);
        List<Calendar> calendars = reportGrouping.getReportCalendars(mClosingDateIndex, entries);
        mPagerAdapter = new ReportContentFragmentPagerAdapter(getChildFragmentManager(), reportGrouping, calendars);
        binding.pager.setAdapter(mPagerAdapter);
        if (mCurrentPagerPosition < 0) {
            int lastIndex = mPagerAdapter.getCount() - 1;
            // 最後のページにデータがあるかどうか
            long[] startEndDate = EntryLimitManager.getStartAndEndDate(mContext,
                    periodType, calendars.get(lastIndex));
            int countData = mEntryDataManager.count(startEndDate);
            if (countData == 0) {
                binding.pager.setCurrentItem(lastIndex - 1);
            } else {
                binding.pager.setCurrentItem(lastIndex);
            }
        } else {
            binding.pager.setCurrentItem(mCurrentPagerPosition);
        }
    }

    public class ReportContentFragmentPagerAdapter extends FragmentStatePagerAdapter {

        private final ReportGrouping reportGrouping;
        private final List<Calendar> calendars;

        public ReportContentFragmentPagerAdapter(FragmentManager fm,
                                                 ReportGrouping reportGrouping,
                                                 List<Calendar> calendars) {
            super(fm);
            this.reportGrouping = reportGrouping;
            this.calendars = calendars;
        }

        @Override
        public Fragment getItem(int position) {
            Calendar targetCalender = calendars.get(position);
            return ReportContentFragment.newInstance(targetCalender);
        }

        @Override
        public int getCount() {
            if (calendars == null) return 0;
            return calendars.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return reportGrouping.getTabTitle(mContext, mClosingDateIndex, calendars.get(position));
        }

        @Override
        public int getItemPosition(Object object){
            return POSITION_NONE;
        }

    }

}
