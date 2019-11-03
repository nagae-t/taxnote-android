package com.example.taxnoteandroid;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.example.taxnoteandroid.Library.ValueConverter;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.PieGraphRowBinding;
import com.example.taxnoteandroid.databinding.RowSimpleCellBinding;
import com.example.taxnoteandroid.model.Entry;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by b0ne on 2017/02/28.
 */

public class GraphHistoryRecyclerAdapter extends RecyclerView.Adapter<BindingHolder>
        implements View.OnClickListener, View.OnLongClickListener {

    private Context mContext;
    private List<Entry> mDataList;
    private PieChart mChart;
    private TypedValue mIncomePriceTv = new TypedValue();

    public static final int VIEW_ITEM_GRAPH = 1;
    public static final int VIEW_ITEM_CELL = 2;
    public static final int VIEW_CARRIED_BAL_CELL = 10;

    private static final int NOT_GRAPH_DATA_NUM = 3;

    public OnItemClickListener mOnItemClickListener;
    public OnLongItemClickListener mOnItemLongClickListener;
    public OnGraphClickListener mOnGraphClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position, Entry item);
    }

    public interface OnLongItemClickListener {
        boolean onItemLongClick(View view, int position, Entry item);
    }

    public interface OnGraphClickListener {
        void onClick(View view, PieChart chart);
    }

    public GraphHistoryRecyclerAdapter(Context context) {
        super();

        this.mContext = context;
        this.mDataList = new ArrayList<>();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, mIncomePriceTv, true);
    }

    public GraphHistoryRecyclerAdapter(Context context, List<Entry> dataList) {
        super();

        this.mContext = context;
        this.mDataList = new ArrayList<>();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, mIncomePriceTv, true);

        Entry graphEntry = new Entry();
        graphEntry.viewType = VIEW_ITEM_GRAPH;

        this.mDataList.add(graphEntry);
        this.mDataList.addAll(dataList);
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
//        mRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
//        mRecyclerView = null;
    }

    public void setItems(List<Entry> entries) {
        mDataList = entries;
    }

    public void replayGraphAnimate() {
        if (mChart == null) return;
        mChart.animateY(600, Easing.EasingOption.EaseInOutQuad);
    }

    @Override
    public int getItemViewType(int position) {
        return mDataList.get(position).viewType;
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_ITEM_GRAPH:
                return new BindingHolder(parent.getContext(), parent, R.layout.pie_graph_row);
            case VIEW_ITEM_CELL:
            case VIEW_CARRIED_BAL_CELL:
                return new BindingHolder(parent.getContext(), parent, R.layout.row_simple_cell);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, final int position) {
        int viewType = holder.getItemViewType();
        final Entry entry = mDataList.get(position);

        switch (viewType) {
            case VIEW_ITEM_GRAPH:
                PieGraphRowBinding graphBinding = (PieGraphRowBinding) holder.binding;
                mChart = graphBinding.chart1;

                mChart.setNoDataText(mContext.getString(R.string.history_data_empty));
                mChart.setNoDataTextColor(ContextCompat.getColor(mContext, R.color.accent));
                mChart.setUsePercentValues(true);
                mChart.getDescription().setEnabled(false);
                mChart.setExtraOffsets(2, 2, 2, 2);

                mChart.setDragDecelerationEnabled(false);
                mChart.setOnChartGestureListener(null);

                mChart.setDrawHoleEnabled(true);
                mChart.setHoleColor(Color.WHITE);

                mChart.setTransparentCircleColor(Color.WHITE);

                mChart.setHoleRadius(40f);
                mChart.setTransparentCircleRadius(40f);

                mChart.setDrawCenterText(true);

                mChart.setRotationAngle(270);

                // enable rotation of the chart by touch
                mChart.setRotationEnabled(true);
                mChart.setHighlightPerTapEnabled(true);
                mChart.setTouchEnabled(false);

                setGraphData();

                // Animation speed
                mChart.animateY(500, Easing.EasingOption.EaseInOutQuad);

                mChart.getLegend().setEnabled(false);
                // entry label styling
                mChart.setEntryLabelColor(Color.WHITE);
                mChart.setEntryLabelTextSize(12f);

                if (mDataList.size() == NOT_GRAPH_DATA_NUM) {
                    graphBinding.chart1.setVisibility(View.GONE);
                    graphBinding.empty.setVisibility(View.VISIBLE);
                } else {
                    graphBinding.chart1.setVisibility(View.VISIBLE);
                    graphBinding.empty.setVisibility(View.GONE);
                }

                graphBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mOnGraphClickListener != null) {
                            mOnGraphClickListener.onClick(view, mChart);
                        }
                    }
                });

                break;
            case VIEW_CARRIED_BAL_CELL:
                RowSimpleCellBinding cbCell = (RowSimpleCellBinding) holder.binding;
                String cbPrice = ValueConverter.formatPrice(mContext, entry.price);
                int cbColor = (entry.price < 0) ? ContextCompat.getColor(mContext, R.color.expense)
                        : ContextCompat.getColor(mContext, mIncomePriceTv.resourceId);
                cbCell.price.setTextColor(cbColor);
                cbCell.price.setText(cbPrice);
                cbCell.labelName.setText(entry.titleName);
                cbCell.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mOnItemClickListener != null)
                            mOnItemClickListener.onItemClick(view, position, entry);
                    }
                });
                break;
            case VIEW_ITEM_CELL:
                RowSimpleCellBinding cellBinding = (RowSimpleCellBinding) holder.binding;
                String priceString = ValueConverter.formatPrice(mContext, entry.price);
                int priceColor = (entry.isExpense) ? ContextCompat.getColor(mContext, R.color.expense)
                        : ContextCompat.getColor(mContext, mIncomePriceTv.resourceId);
                cellBinding.price.setTextColor(priceColor);
                cellBinding.price.setText(priceString);
                cellBinding.labelName.setText(entry.titleName);
                cellBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mOnItemClickListener != null)
                            mOnItemClickListener.onItemClick(view, position, entry);
                    }
                });
                break;
        }
    }

    private void setGraphData() {
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        if (mDataList.size() < NOT_GRAPH_DATA_NUM) return;

        int[] colorList = new int[mDataList.size()-2];
        int color1 = R.color.pie_chart_color1;
        int color2 = R.color.pie_chart_color2;
        int color3 = R.color.pie_chart_color3;

        // 帳簿のテーマカラーにグラフ色をあわせる
        int themeStyle = SharedPreferencesManager.getAppThemeStyle(mContext);
        if (themeStyle == 1) {
            color1 = R.color.second_pie_chart_color1;
            color2 = R.color.second_pie_chart_color2;
            color3 = R.color.second_pie_chart_color3;
        } else if (themeStyle == 2) {
            color1 = R.color.third_pie_chart_color1;
            color2 = R.color.third_pie_chart_color2;
            color3 = R.color.third_pie_chart_color3;
        }

        Long sumPrice = mDataList.get(1).price;
        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(1);
        for (int i=2; i<mDataList.size(); i++) {
            Entry _entry = mDataList.get(i);
            float entryPrice = (float) _entry.price;
            String labelName = _entry.titleName;
            // set value cut off
            float pricePercent = (entryPrice / sumPrice) * 100;
            if (pricePercent < 2.0f) {
                labelName = null;
            }

            PieEntry pEntry = new PieEntry(entryPrice, labelName, _entry);
            if (pricePercent < 2.0f) {
                pEntry.setData(null);
            }
            pieEntries.add(pEntry);

            int colorIndex = i-2;
            if (colorIndex == 0) {
                colorList[0] = color1;
            } else if (colorIndex%2 != 0) {
                colorList[colorIndex] = color2;
            } else if (colorIndex%2 == 0) {
                colorList[colorIndex] = color3;
            }
        }

        PieDataSet dataSet = new PieDataSet(pieEntries, null);

        // パーセント数値の表示（label内で済ました）
        dataSet.setDrawValues(true);
        dataSet.setValueFormatter(new MyPieValueFormatter());

        // add a lot of colors
        dataSet.setColors(colorList, mContext);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);
        mChart.setUsePercentValues(true);

        mChart.invalidate();

    }

    private class MyPieValueFormatter implements IValueFormatter {

        protected DecimalFormat mFormat;

        public MyPieValueFormatter() {
            mFormat = new DecimalFormat("###,###,##0.0");
        }
        @Override
        public String getFormattedValue(float value, com.github.mikephil.charting.data.Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            if(value < 2) return "";
            else return mFormat.format(value) + " %";
        }
    }

    @Override
    public int getItemCount() {
        if (mDataList == null) return 0;
        return mDataList.size();
    }

    public void setOnItemClickListener(final OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(final OnLongItemClickListener listener) {
        mOnItemLongClickListener = listener;
    }

    public void setOnGraphClickListener(OnGraphClickListener listener) {
        mOnGraphClickListener = listener;
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public boolean onLongClick(View view) {
        return false;
    }
}
