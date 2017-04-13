package com.example.taxnoteandroid;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.taxnoteandroid.Library.DayAxisValueFormatter;
import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.databinding.ActivityBarGraphBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by b0ne on 2017/04/13.
 */

public class BarGraphActivity extends DefaultCommonActivity implements OnChartValueSelectedListener {

    private ActivityBarGraphBinding binding;
    protected BarChart mChart;

    private ProjectDataManager mProjectDm;

    private static final String KEY_IS_EXPENSE = "is_expense";
    private static final String KEY_TARGET_CALENDAR = "target_calendar";
    private static final String KEY_PERIOD_TYPE = "period_type";

    public static final int PERIOD_TYPE_MONTH = 1;
    public static final int PERIOD_TYPE_YEAR = 2;

    public static void start(Context context, boolean isExpense, Calendar targetCalender, int periodType) {
        Intent intent = new Intent(context, BarGraphActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_IS_EXPENSE, isExpense);
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

        mChart = binding.chart1;
        mChart.setOnChartValueSelectedListener(this);

        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);
        mChart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        mChart.setDrawGridBackground(false);
        // mChart.setDrawYLabels(false);

        // X軸の設定
        IAxisValueFormatter xAxisFormatter = DayAxisValueFormatter.newInstance(this, mChart, 1);

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

        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);
        // l.setExtra(ColorTemplate.VORDIPLOM_COLORS, new String[] { "abc",
        // "def", "ghj", "ikl", "mno" });
        // l.setCustom(ColorTemplate.VORDIPLOM_COLORS, new String[] { "abc",
        // "def", "ghj", "ikl", "mno" });

        XYMarkerView mv = new XYMarkerView(this, xAxisFormatter);
        mv.setChartView(mChart); // For bounds control
//        mChart.setMarker(mv); // Set the marker to the chart

        setData(12, 50);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bar_graph, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     *
     * @param count
     * @param range
     */
    private void setData(int count, float range) {

        float start = 1f;

        ArrayList<BarEntry> yVals1 = new ArrayList<>();

        for (int i = (int) start; i < start + count + 1; i++) {
            float mult = (range + 1);
            float val = (float) (Math.random() * mult);

//            if (Math.random() * 100 < 25) {
//                yVals1.add(new BarEntry(i, val, ContextCompat.getDrawable(this, R.drawable.ic_delete_forever_black_24dp)));
//            } else {
//                yVals1.add(new BarEntry(i, val));
//            }
            yVals1.add(new BarEntry(i, val));
        }

//        BarDataSet set1;
        /**/
        BarDataSet set1 = new BarDataSet(yVals1, null);
        TypedValue barColorTv = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, barColorTv, true);
        set1.setDrawIcons(false);
        set1.setColor(ContextCompat.getColor(this, barColorTv.resourceId));
        List<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        BarData data = new BarData(dataSets);
        mChart.setData(data);
        mChart.getLegend().setEnabled(false); /**/

        /*
        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) mChart.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVals1, "The year 2017");

            set1.setDrawIcons(false);

            set1.setColors(ColorTemplate.MATERIAL_COLORS);

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            data.setBarWidth(0.9f);

            mChart.setData(data);
        } */
    }

    protected RectF mOnValueSelectedRectF = new RectF();

    @Override
    public void onValueSelected(Entry e, Highlight h) {
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
    }
}
