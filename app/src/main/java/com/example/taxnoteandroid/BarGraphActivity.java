package com.example.taxnoteandroid;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import com.example.taxnoteandroid.Library.DayAxisValueFormatter;
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

    private int mXNum = 0;

    private static final String KEY_IS_EXPENSE = "is_expense";
    private static final String KEY_TARGET_CALENDAR = "target_calendar";
    private static final String KEY_PERIOD_TYPE = "period_type";
    private static final String KEY_REASON_UUID = "reason_uuid";

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

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mProjectDm = new ProjectDataManager(this);
        mEntryDm = new EntryDataManager(this);
        mReasonDm = new ReasonDataManager(this);

        Calendar targetCalendar = (Calendar) getIntent().getSerializableExtra(KEY_TARGET_CALENDAR);
        mIsExpense = getIntent().getBooleanExtra(KEY_IS_EXPENSE, true);
        int periodType = getIntent().getIntExtra(KEY_PERIOD_TYPE, 2);
        if (periodType > EntryDataManager.PERIOD_TYPE_MONTH)
            periodType = EntryDataManager.PERIOD_TYPE_MONTH;

        String titleDateFormat = (periodType == EntryDataManager.PERIOD_TYPE_MONTH)
                ? getString(R.string.date_string_format_to_year_month)
                : getString(R.string.date_string_format_to_year);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                titleDateFormat, Locale.getDefault());
        String isExpenseString = (mIsExpense) ? getString(R.string.Expense)
                : getString(R.string.Income);
        String titleDateString = simpleDateFormat.format(targetCalendar.getTime());
        String titleName = titleDateString + " " + isExpenseString;

        String reasonUuid = getIntent().getStringExtra(KEY_REASON_UUID);
        if (reasonUuid != null) {
            mReason = mReasonDm.findByUuid(reasonUuid);
            mIsExpense = mReason.isExpense;
            titleName = titleDateString + " " + mReason.name;
        }
        setTitle(titleName);

        mChart = binding.chart1;
        mChart.setOnChartValueSelectedListener(this);

        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);
        mChart.getDescription().setEnabled(false);
        mChart.setNoDataText(getString(R.string.history_data_empty));
        mChart.setNoDataTextColor(ContextCompat.getColor(this, R.color.accent));

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        mChart.setDrawGridBackground(false);


        long[] startEndDate = EntryLimitManager.getStartAndEndDate(this, periodType, targetCalendar);
        new EntryDataTask(mIsExpense).execute(startEndDate);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bar_graph, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem expenseMenu = menu.findItem(R.id.action_is_expense);
        if (!mIsExpense) {
            expenseMenu.setTitle(R.string.Income);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_is_expense:
                // switch graph
                break;
            case R.id.divide_by_year:
                break;
            case R.id.divide_by_month:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    protected RectF mOnValueSelectedRectF = new RectF();

    @Override
    public void onValueSelected(com.github.mikephil.charting.data.Entry e, Highlight h) {
        if (e == null)
            return;

        RectF bounds = mOnValueSelectedRectF;
        mChart.getBarBounds((BarEntry) e, bounds);
        MPPointF position = mChart.getPosition(e, YAxis.AxisDependency.LEFT);

        Log.i("bounds", bounds.toString());
        Log.i("position", position.toString());

        Log.i("x-index",
                "low: " + mChart.getLowestVisibleX() + ", high: "
                        + mChart.getHighestVisibleX());

        MPPointF.recycleInstance(position);
    }

    @Override
    public void onNothingSelected() {
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

    /*
    private class XYMarkerView extends MarkerView {

        private TextView tvContent;
        private IAxisValueFormatter xAxisValueFormatter;

        private DecimalFormat format;

        public XYMarkerView(Context context, IAxisValueFormatter xAxisValueFormatter) {
            super(context, R.layout.custom_marker_view);

            this.xAxisValueFormatter = xAxisValueFormatter;
            tvContent = (TextView) findViewById(R.id.tvContent);
            format = new DecimalFormat("###.0");
        }

        // callbacks everytime the MarkerView is redrawn, can be used to update the
        // content (user-interface)
        @Override
        public void refreshContent(Entry e, Highlight highlight) {

            tvContent.setText("x: " + xAxisValueFormatter.getFormattedValue(e.getX(), null) + ", y: " + format.format(e.getY()));

            super.refreshContent(e, highlight);
        }

        @Override
        public MPPointF getOffset() {
            return new MPPointF(-(getWidth() / 2), -getHeight());
        }
    }*/

    private class EntryDataTask extends AsyncTask<long[], Integer, ArrayList<BarEntry>> {
        private boolean isExpense;

        public EntryDataTask(boolean isExpense) {
            this.isExpense = isExpense;
        }

        @Override
        protected ArrayList<BarEntry> doInBackground(long[]... longs) {
            long[] startEndDate = longs[0];

            // debug
            Calendar startCal = Calendar.getInstance();
            startCal.clear();
            startCal.setTimeInMillis(startEndDate[0]);
            Calendar endCal = Calendar.getInstance();
            endCal.clear();
            endCal.setTimeInMillis(startEndDate[1]);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                    getString(R.string.date_string_format_to_year_month_day),
                    Locale.getDefault());
            String startCalStr = simpleDateFormat.format(startCal.getTime());
            String endCalStr = simpleDateFormat.format(endCal.getTime());
            // 差分の日数を計算
            final long DAY_MILLISECONDS = 1000 * 60 * 60 * 24;
            long diff =  endCal.getTimeInMillis() - startCal.getTimeInMillis();
            mXNum = (int)(diff / DAY_MILLISECONDS);
            Log.v("TEST", "startCal : " + startCalStr + ", endCal : " + endCalStr
                    + ", dayDiff : " + mXNum);

            ArrayList<BarEntry> entryData = new ArrayList<>();
            List<Entry> entries = mEntryDm.findAll(startEndDate, isExpense, true);

            Map<String, Long> entryMap = new LinkedHashMap<>();
            for (Entry entry : entries) {
                Calendar calendar = Calendar.getInstance();
                calendar.clear();
                calendar.setTimeInMillis(entry.date);

                calendar.set(calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DATE), 0, 0, 0);
                String dateStr = calendar.get(Calendar.YEAR) + "_"
                        + calendar.get(Calendar.MONTH) + "_"
                        + calendar.get(Calendar.DATE);

                if (entryMap.containsKey(dateStr)) {
                    Long _price = entryMap.get(dateStr)+entry.price;
                    entryMap.put(dateStr, _price);
                } else {
                    entryMap.put(dateStr, entry.price);
                }
            }

//            int startDate = startCal.get(Calendar.DATE);
            for (int i=0; i<mXNum; i++) {
                Calendar _cal = (Calendar) startCal.clone();
                _cal.set(_cal.get(Calendar.YEAR), _cal.get(Calendar.MONTH),
                        _cal.get(Calendar.DATE), 0, 0, 0);
                if (i > 0) _cal.add(Calendar.DAY_OF_MONTH, i);
//                String _calStr = simpleDateFormat.format(_cal.getTime());
                String dateStr = _cal.get(Calendar.YEAR) + "_"
                        + _cal.get(Calendar.MONTH) + "_"
                        + _cal.get(Calendar.DATE);
                Long _price = entryMap.get(dateStr);
                long price = (_price == null) ? 0 : _price;

                BarEntry barEntry = new BarEntry(i, (float)price);
                entryData.add(barEntry);
            }

            return entryData;
        }

        @Override
        protected void onPostExecute(ArrayList<BarEntry> result) {
            if (result == null || result.size() == 0) return;


            // if more than 60 entries are displayed in the chart, no values will be
            // drawn
            mChart.setMaxVisibleValueCount(result.size()+1);

            // X軸の設定
            IAxisValueFormatter xAxisFormatter = DayAxisValueFormatter.newInstance(
                    getApplicationContext(), mChart, 1);

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
            TypedValue barColorTv = new TypedValue();
            getTheme().resolveAttribute(R.attr.colorPrimary, barColorTv, true);
            set1.setDrawIcons(false);
            set1.setColor(ContextCompat.getColor(
                    getApplicationContext(), barColorTv.resourceId));
            List<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            BarData data = new BarData(dataSets);
            mChart.setData(data);
            mChart.getLegend().setEnabled(false);
        }
    }
}
