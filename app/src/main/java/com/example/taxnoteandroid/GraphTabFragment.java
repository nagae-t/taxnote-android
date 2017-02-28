package com.example.taxnoteandroid;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.EntryDataManager.ReportGrouping;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.FragmentGraphTabBinding;
import com.example.taxnoteandroid.model.Entry;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

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

    private PieChart mChart;
    protected String[] mParties = new String[] {
            "Party A", "Party B", "Party C", "Party D", "Party E", "Party F", "Party G", "Party H",
            "Party I", "Party J", "Party K", "Party L", "Party M", "Party N", "Party O", "Party P",
            "Party Q", "Party R", "Party S", "Party T", "Party U", "Party V", "Party W", "Party X",
            "Party Y", "Party Z"
    };

    public static GraphTabFragment newInstance() {
        GraphTabFragment fragment = new GraphTabFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGraphTabBinding.inflate(inflater, container, false);
//        mChart = binding.chart1;

        binding.strip.setTabIndicatorColorResource(R.color.accent);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity().getApplicationContext();

        /*
        mChart.setUsePercentValues(true);
        mChart.getDescription().setEnabled(false);
        mChart.setExtraOffsets(5, 10, 5, 5);

        mChart.setDragDecelerationFrictionCoef(0.95f);

        mChart.setCenterText(generateCenterSpannableText());

        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(Color.WHITE);

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(58f);
        mChart.setTransparentCircleRadius(61f);

        mChart.setDrawCenterText(true);

        mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(true);

        // mChart.setUnit(" €");
        // mChart.setDrawUnitsInChart(true);

        // add a selection listener
        mChart.setOnChartValueSelectedListener(this);

        setData(4, 100);

        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        // mChart.spin(2000, 0, 360);

        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        // entry label styling
        mChart.setEntryLabelColor(Color.WHITE);
        mChart.setEntryLabelTextSize(12f);

        */


        int periodType = SharedPreferencesManager.getGraphReportPeriodType(mContext);
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
        int periodType = SharedPreferencesManager.getGraphReportPeriodType(mContext);
        switchDataView(periodType, true);
    }

    public void switchDataView(boolean isExpense) {
        int periodType = SharedPreferencesManager.getGraphReportPeriodType(mContext);
        switchDataView(periodType, isExpense);
    }

    public void switchDataView(int periodType) {
        boolean isExpense = SharedPreferencesManager.getGraphReportIsExpenseType(mContext);
        switchDataView(periodType, isExpense);
    }

    public void switchDataView(int periodType, boolean isExpense) {
        ReportGrouping reportGrouping = new ReportGrouping(periodType);
        SharedPreferencesManager.saveGraphReportPeriodType(mContext, periodType);
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

    private void setData(int count, float range) {

        float mult = range;

        ArrayList<PieEntry> entries = new ArrayList<>();

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (int i = 0; i < count ; i++) {
            entries.add(new PieEntry((float) ((Math.random() * mult) + mult / 5),
                    mParties[i % mParties.length],
                    ContextCompat.getDrawable(mContext, android.R.drawable.star_on)));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Election Results");

//        dataSet.setDrawIcons(false);
        dataSet.setDrawValues(false);

        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);
//        dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
    }

    private SpannableString generateCenterSpannableText() {

        SpannableString s = new SpannableString("MPAndroidChart\ndeveloped by Philipp Jahoda");
        s.setSpan(new RelativeSizeSpan(1.7f), 0, 14, 0);
        s.setSpan(new StyleSpan(Typeface.NORMAL), 14, s.length() - 15, 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), 14, s.length() - 15, 0);
        s.setSpan(new RelativeSizeSpan(.8f), 14, s.length() - 15, 0);
        s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - 14, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - 14, s.length(), 0);
        return s;
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
