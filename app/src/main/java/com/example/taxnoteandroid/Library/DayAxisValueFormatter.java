package com.example.taxnoteandroid.Library;

import android.content.Context;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.Locale;

/**
 * Created by b0ne on 2017/04/13.
 */

public class DayAxisValueFormatter implements IAxisValueFormatter {

    private Context mContext;
    private BarLineChartBase<?> chart;
    private int mValueType = 1;

    private static final int VALUE_TYPE_YEAR = 1;
    private static final int VALUE_TYPE_MONTH = 2;

    public static DayAxisValueFormatter newInstance(Context context, BarLineChartBase<?> chart, int valueType) {
        DayAxisValueFormatter valueFormatter = new DayAxisValueFormatter(context, chart, valueType);
        return valueFormatter;
    }

    public DayAxisValueFormatter(Context context, BarLineChartBase<?> chart, int valueType) {
        this.mContext = context;
        this.chart = chart;
        this.mValueType = valueType;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {

        int days = (int) value;

        int year = determineYear(days);

        int month = determineMonth(days);
//        String[] months = mContext.getResources().getStringArray(R.array.month_list);
//        String monthName = mMonths[month % mMonths.length];
        String yearName = String.valueOf(year);

//        if (chart.getVisibleXRange() > 30 * 6) {
//            return monthName + " " + yearName;
//        }

        int dayOfMonth = determineDayOfMonth(days, month + 12 * (year - 2016));

        String appendix = "th";

        switch (dayOfMonth) {
            case 1:
                appendix = "st";
                break;
            case 2:
                appendix = "nd";
                break;
            case 3:
                appendix = "rd";
                break;
            case 21:
                appendix = "st";
                break;
            case 22:
                appendix = "nd";
                break;
            case 23:
                appendix = "rd";
                break;
            case 31:
                appendix = "st";
                break;
        }

        // 日本語の場合
        if (Locale.getDefault().getLanguage().equals("ja")) {
            appendix = "日";
        }
        return dayOfMonth == 0 ? "" : dayOfMonth + appendix;
//            return dayOfMonth == 0 ? "" : dayOfMonth + appendix + " " + monthName;
    }

    private int getDaysForMonth(int month, int year) {

        // month is 0-based

        if (month == 1) {
            boolean is29Feb = false;

            if (year < 1582)
                is29Feb = (year < 1 ? year + 1 : year) % 4 == 0;
            else if (year > 1582)
                is29Feb = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);

            return is29Feb ? 29 : 28;
        }

        if (month == 3 || month == 5 || month == 8 || month == 10)
            return 30;
        else
            return 31;
    }

    private int determineMonth(int dayOfYear) {

        int month = -1;
        int days = 0;

        while (days < dayOfYear) {
            month = month + 1;

            if (month >= 12)
                month = 0;

            int year = determineYear(days);
            days += getDaysForMonth(month, year);
        }

        return Math.max(month, 0);
    }

    private int determineDayOfMonth(int days, int month) {

        int count = 0;
        int daysForMonths = 0;

        while (count < month) {

            int year = determineYear(daysForMonths);
            daysForMonths += getDaysForMonth(count % 12, year);
            count++;
        }

        return days - daysForMonths;
    }

    private int determineYear(int days) {

        if (days <= 366)
            return 2016;
        else if (days <= 730)
            return 2017;
        else if (days <= 1094)
            return 2018;
        else if (days <= 1458)
            return 2019;
        else
            return 2020;
    }

}
