package com.example.taxnoteandroid;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.taxnoteandroid.Library.ValueConverter;
import com.example.taxnoteandroid.databinding.PieGraphRowBinding;
import com.example.taxnoteandroid.databinding.RowSimpleCellBinding;
import com.example.taxnoteandroid.model.Entry;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by b0ne on 2017/02/28.
 */

public class GraphHistoryRecyclerAdapter extends RecyclerView.Adapter<BindingHolder>
        implements View.OnClickListener, View.OnLongClickListener {

    private Context mContext;
    private RecyclerView mRecyclerView;
    private List<Entry> mDataList;
    private PieChart mChart;

    public static final int VIEW_ITEM_GRAPH = 1;
    public static final int VIEW_ITEM_CELL = 2;

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
    }

    public GraphHistoryRecyclerAdapter(Context context, List<Entry> dataList) {
        super();

        this.mContext = context;
        this.mDataList = new ArrayList<>();

        Entry graphEntry = new Entry();
        graphEntry.viewType = VIEW_ITEM_GRAPH;

        this.mDataList.add(graphEntry);
        this.mDataList.addAll(dataList);
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView = null;
    }

    public void setItems(List<Entry> entries) {
        mDataList = entries;
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
                return new BindingHolder(parent.getContext(), parent, R.layout.row_simple_cell);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        int viewType = holder.getItemViewType();
        final Entry entry = mDataList.get(position);

        switch (viewType) {
            case VIEW_ITEM_GRAPH:
                PieGraphRowBinding graphBinding = (PieGraphRowBinding) holder.binding;
                mChart = graphBinding.chart1;


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

                //@@ グラフ書き出しのアニメーション速度の指定
                mChart.animateY(500, Easing.EasingOption.EaseInOutQuad);

                mChart.getLegend().setEnabled(false);
                // entry label styling
                mChart.setEntryLabelColor(Color.WHITE);
                mChart.setEntryLabelTextSize(12f);

                graphBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mOnGraphClickListener != null) {
                            mOnGraphClickListener.onClick(view, mChart);
                        }
                    }
                });

                break;
            case VIEW_ITEM_CELL:
                RowSimpleCellBinding cellBinding = (RowSimpleCellBinding) holder.binding;
                String priceString = ValueConverter.formatPrice(mContext, entry.price);
                int priceColor = (entry.isExpense) ? ContextCompat.getColor(mContext, R.color.expense)
                        : ContextCompat.getColor(mContext, R.color.primary);
                cellBinding.price.setTextColor(priceColor);
                cellBinding.price.setText(priceString);
                cellBinding.name.setText(entry.titleName);
                break;
        }
    }

    private void setGraphData() {
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        if (mDataList.size() < 3) return;

        for (int i=2; i<mDataList.size(); i++) {
            Entry _entry = mDataList.get(i);
            PieEntry pEntry = new PieEntry((float)_entry.price, _entry.titleName);
            pieEntries.add(pEntry);
        }

        PieDataSet dataSet = new PieDataSet(pieEntries, null);

        dataSet.setDrawValues(true);
        dataSet.setValueFormatter(new PercentFormatter());
        dataSet.setValueLinePart1OffsetPercentage(0);


        // add a lot of colors
        dataSet.setColors(new int[]{R.color.pie_chart_color6,
                R.color.pie_chart_color5,
                R.color.pie_chart_color4,
                R.color.pie_chart_color3,
                R.color.pie_chart_color2,
                R.color.pie_chart_color1}, mContext);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();

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
