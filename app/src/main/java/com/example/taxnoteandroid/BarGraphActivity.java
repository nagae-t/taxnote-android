package com.example.taxnoteandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.taxnoteandroid.Library.DayAxisValueFormatter;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.EntryLimitManager;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.ReasonDataManager;
import com.example.taxnoteandroid.databinding.ActivityBarGraphBinding;
import com.example.taxnoteandroid.model.Entry;
import com.example.taxnoteandroid.model.Reason;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by b0ne on 2017/04/13.
 */

public class BarGraphActivity extends DefaultCommonActivity implements OnChartValueSelectedListener {

    private ActivityBarGraphBinding binding;
    protected BarChart mChart;

    private ProjectDataManager mProjectDm;
    private EntryDataManager mEntryDm;
    private ReasonDataManager mReasonDm;
    private Reason mReason = null;
    private boolean mIsExpense;
    private int mPeriodType;
    private Calendar mTargetCalendar;
    private List<Calendar> mCalendars;

    private int mSelectedGraphXIndex;

    private static final String KEY_IS_EXPENSE = "is_expense";
    private static final String KEY_TARGET_CALENDAR = "target_calendar";
    private static final String KEY_PERIOD_TYPE = "period_type";
    private static final String KEY_REASON_UUID = "reason_uuid";

    public static final String BROADCAST_RELOAD_DATA
            = "broadcast_bar_graph_reload_data";

    private final BroadcastReceiver mReloadDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadDataToView();
        }
    };

    public static void start(Context context, boolean isExpense, Calendar targetCalender, int periodType) {
        Intent intent = new Intent(context, BarGraphActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_IS_EXPENSE, isExpense);
        intent.putExtra(KEY_TARGET_CALENDAR, targetCalender);
        intent.putExtra(KEY_PERIOD_TYPE, periodType);
        context.startActivity(intent);
    }

    public static void startForReason(Context context, String reasonUuid,
                                      Calendar targetCalender, int periodType) {
        Intent intent = new Intent(context, BarGraphActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_REASON_UUID, reasonUuid);
        intent.putExtra(KEY_TARGET_CALENDAR, targetCalender);
        intent.putExtra(KEY_PERIOD_TYPE, periodType);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bar_graph);

        registerReceiver(mReloadDataReceiver, new IntentFilter(BROADCAST_RELOAD_DATA));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mProjectDm = new ProjectDataManager(this);
        mEntryDm = new EntryDataManager(this);
        mReasonDm = new ReasonDataManager(this);

        mTargetCalendar = (Calendar) getIntent().getSerializableExtra(KEY_TARGET_CALENDAR);
        mIsExpense = getIntent().getBooleanExtra(KEY_IS_EXPENSE, true);
        mPeriodType = getIntent().getIntExtra(KEY_PERIOD_TYPE, 2);
        if (mPeriodType > EntryDataManager.PERIOD_TYPE_MONTH)
            mPeriodType = EntryDataManager.PERIOD_TYPE_MONTH;

        loadDataToView();
    }

    private void setTitleName() {
        String titleDateFormat = (mPeriodType == EntryDataManager.PERIOD_TYPE_MONTH)
                ? getString(R.string.date_string_format_to_year_month)
                : getString(R.string.date_string_format_to_year);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                titleDateFormat, Locale.getDefault());
        String isExpenseString = (mIsExpense) ? getString(R.string.Expense)
                : getString(R.string.Income);
        String titleDateString = simpleDateFormat.format(mTargetCalendar.getTime());
        String titleName = titleDateString + " " + isExpenseString;

        String reasonUuid = getIntent().getStringExtra(KEY_REASON_UUID);
        if (reasonUuid != null) {
            mReason = mReasonDm.findByUuid(reasonUuid);
            mIsExpense = mReason.isExpense;
            titleName = titleDateString + " " + mReason.name;
        }
        setTitle(titleName);
    }

    private void loadDataToView() {
        setTitleName();
        binding.chart1.setVisibility(View.GONE);
        long[] startEndDate = EntryLimitManager.getStartAndEndDate(this, mPeriodType, mTargetCalendar);
        new EntryDataTask().execute(startEndDate);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bar_graph, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem expenseMenu = menu.findItem(R.id.action_is_expense);
        int titleRes = (mIsExpense) ? R.string.Income : R.string.Expense;
        expenseMenu.setTitle(titleRes);

        if (mReason != null)
            expenseMenu.setVisible(false);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_is_expense:
                mIsExpense = !mIsExpense;
                invalidateOptionsMenu();
                loadDataToView();
                break;
            case R.id.divide_by_year:
                if (mPeriodType == EntryDataManager.PERIOD_TYPE_YEAR)
                    return super.onOptionsItemSelected(item);
                mPeriodType = EntryDataManager.PERIOD_TYPE_YEAR;
                mTargetCalendar.set(Calendar.MONTH, 0);
                loadDataToView();
                break;
            case R.id.divide_by_month:
                if (mPeriodType == EntryDataManager.PERIOD_TYPE_MONTH)
                    return super.onOptionsItemSelected(item);
                mPeriodType = EntryDataManager.PERIOD_TYPE_MONTH;
                mTargetCalendar.set(Calendar.MONTH, mSelectedGraphXIndex);
                loadDataToView();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onValueSelected(com.github.mikephil.charting.data.Entry e, Highlight h) {
        if (e == null)
            return;

        RectF bounds = new RectF();
        mChart.getBarBounds((BarEntry) e, bounds);
        MPPointF position = mChart.getPosition(e, YAxis.AxisDependency.LEFT);
        MPPointF.recycleInstance(position);

        mSelectedGraphXIndex = (int)e.getX();
        if ((int)e.getY() > 0)
            showBarInfoDialog((int)e.getX(), (long)e.getY());
    }

    @Override
    public void onNothingSelected() {
    }

    private void showBarInfoDialog(int x, long price) {
        Calendar _cal = mCalendars.get(x);
        DialogManager.showBarInfoDialog(this,
                mPeriodType, _cal, mReason, mIsExpense, price);
    }

    private class MyAxisValueFormatter implements IAxisValueFormatter {

        private DecimalFormat mFormat;
        private boolean isDecimal;

        private MyAxisValueFormatter() {
            isDecimal = mProjectDm.getDecimalStatus();
            mFormat = (isDecimal) ?
                    new DecimalFormat("#,##0.00") :
                    new DecimalFormat("#,###");
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            if (isDecimal) {
                Double doubleVal = value / 100D;
                return mFormat.format(doubleVal);
            } else {
                return mFormat.format(value);
            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReloadDataReceiver);
        super.onDestroy();
    }

    private class EntryDataTask extends AsyncTask<long[], Integer, ArrayList<BarEntry>> {

        @Override
        protected ArrayList<BarEntry> doInBackground(long[]... longs) {
            long[] startEndDate = longs[0];
            boolean isPeriodYear = (mPeriodType == EntryDataManager.PERIOD_TYPE_YEAR);

            // debug
            Calendar startCal = Calendar.getInstance();
            startCal.clear();
            startCal.setTimeInMillis(startEndDate[0]);
            startCal.set(Calendar.MILLISECOND, 0);
            Calendar endCal = Calendar.getInstance();
            endCal.clear();
            endCal.setTimeInMillis(startEndDate[1]);
            endCal.set(Calendar.MILLISECOND, 0);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                    getString(R.string.date_string_format_to_year_month_day),
                    Locale.getDefault());
//            String startCalStr = simpleDateFormat.format(startCal.getTime());
//            String endCalStr = simpleDateFormat.format(endCal.getTime());
            // 差分の日数を計算
            final long DAY_MILLISECONDS = 1000 * 60 * 60 * 24;
            long diff =  endCal.getTimeInMillis() - startCal.getTimeInMillis();
            int xNum = (isPeriodYear) ? 12 : (int)(diff / DAY_MILLISECONDS);

            ArrayList<BarEntry> barEntries = new ArrayList<>();
            mCalendars = new ArrayList<>();
            List<Entry> entries;
            if (mReason == null) {
                entries = mEntryDm.findAll(startEndDate, mIsExpense, true);
            } else {
                List<Entry> _entries = mEntryDm.findAll(startEndDate, mIsExpense, false);
                entries = new ArrayList<>();

                // Filter data by reasonName
                for (Entry _entry : _entries) {
                    if (_entry.reason.name.equals(mReason.name)) {
                        entries.add(_entry);
                    }
                }
            }

//            Log.v("TEST", "startCal : " + startCalStr + ", endCal : " + endCalStr
//                    + ", dayDiff : " + xNum + ", entrySize: " + entries.size());

            Map<String, Long> entryMap = new LinkedHashMap<>();
            for (Entry entry : entries) {
                Calendar calendar = Calendar.getInstance();
                calendar.clear();
                calendar.setTimeInMillis(entry.date);

                calendar.set(calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DATE), 0, 0, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                String periodStr = (isPeriodYear)
                        ? calendar.get(Calendar.YEAR) + "_" + calendar.get(Calendar.MONTH)
                        : calendar.get(Calendar.YEAR) + "_"
                            + calendar.get(Calendar.MONTH) + "_"
                            + calendar.get(Calendar.DATE);

                if (entryMap.containsKey(periodStr)) {
                    Long _price = entryMap.get(periodStr)+entry.price;
                    entryMap.put(periodStr, _price);
                } else {
                    entryMap.put(periodStr, entry.price);
                }
            }

            for (int i = 0; i < xNum; i++) {
                Calendar _cal = (Calendar) startCal.clone();

                _cal.set(_cal.get(Calendar.YEAR), _cal.get(Calendar.MONTH),
                        _cal.get(Calendar.DATE), 0, 0, 0);
                if (i > 0) {
                    if (isPeriodYear) {
                        _cal.add(Calendar.MONTH, i);
                    } else {
                        _cal.add(Calendar.DAY_OF_MONTH, i);
                    }
                }

                String periodStr = (isPeriodYear)
                        ? _cal.get(Calendar.YEAR) + "_" + _cal.get(Calendar.MONTH)
                        : _cal.get(Calendar.YEAR) + "_"
                        + _cal.get(Calendar.MONTH) + "_"
                        + _cal.get(Calendar.DATE);
                Long _price = entryMap.get(periodStr);
                long price = (_price == null) ? 0 : _price;

                BarEntry barEntry = new BarEntry(i, (float)price);
                barEntries.add(barEntry);
                mCalendars.add(_cal);
            }

            return barEntries;
        }

        @Override
        protected void onPostExecute(ArrayList<BarEntry> result) {
            if (result == null) return;

            mChart = binding.chart1;
            mChart.setOnChartValueSelectedListener(BarGraphActivity.this);

            mChart.setDrawBarShadow(false);
            mChart.getDescription().setEnabled(false);
            mChart.getLegend().setEnabled(false);

            // scaling can now only be done on x- and y-axis separately
            mChart.setPinchZoom(false);
            mChart.setDrawGridBackground(false);
            mChart.setNoDataText(getString(R.string.history_data_empty));
            mChart.setNoDataTextColor(ContextCompat.getColor(
                    getApplicationContext(), R.color.accent));

            // no data
            if (result.size() == 0) return;

            // if more than 60 entries are displayed in the chart, no values will be
            // drawn
            mChart.setMaxVisibleValueCount(result.size()+1);


            // X軸の設定
            IAxisValueFormatter xAxisFormatter = DayAxisValueFormatter.newInstance(
                    getApplicationContext(), mChart, mPeriodType);

            XAxis xAxis = mChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setGranularity(1f); // only intervals of 1 day
            xAxis.setLabelCount(7);
            xAxis.setValueFormatter(xAxisFormatter);

            IAxisValueFormatter custom = new MyAxisValueFormatter();

            // Y軸の設定
            YAxis leftAxis = mChart.getAxisLeft();
            leftAxis.setLabelCount(8, false);
            leftAxis.setValueFormatter(custom);
            leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
            leftAxis.setSpaceTop(8f);
            leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

            mChart.getAxisRight().setEnabled(false);

            // データをグラフに反映
            BarDataSet set1 = new BarDataSet(result, null);
            set1.setDrawValues(false);
            TypedValue barColorTv = new TypedValue();
            getTheme().resolveAttribute(R.attr.colorPrimary, barColorTv, true);
            set1.setDrawIcons(false);
            set1.setColor(ContextCompat.getColor(
                    getApplicationContext(), barColorTv.resourceId));
            List<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            BarData data = new BarData(dataSets);
            mChart.setData(data);
            mChart.setVisibility(View.VISIBLE);
        }
    }
}
