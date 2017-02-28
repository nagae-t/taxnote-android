package com.example.taxnoteandroid;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.example.taxnoteandroid.Library.ValueConverter;
import com.example.taxnoteandroid.databinding.PieGraphRowBinding;
import com.example.taxnoteandroid.databinding.RowSimpleCellBinding;
import com.example.taxnoteandroid.model.Entry;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
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
    protected String[] mParties = new String[] {
            "Party A", "Party B", "Party C", "Party D", "Party E", "Party F", "Party G", "Party H",
            "Party I", "Party J", "Party K", "Party L", "Party M", "Party N", "Party O", "Party P",
            "Party Q", "Party R", "Party S", "Party T", "Party U", "Party V", "Party W", "Party X",
            "Party Y", "Party Z"
    };

    public static final int VIEW_ITEM_GRAPH = 1;
    public static final int VIEW_ITEM_CELL = 2;

    public OnItemClickListener mOnItemClickListener;
    public OnLongItemClickListener mOnItemLongClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position, Entry item);
    }

    public interface OnLongItemClickListener {
        boolean onItemLongClick(View view, int position, Entry item);
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
                mChart.setExtraOffsets(5, 10, 5, 5);

                mChart.setDragDecelerationFrictionCoef(0.95f);


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
//                mChart.setOnChartValueSelectedListener(this);

                setGraphData();

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

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
//        for (int i = 0; i < count ; i++) {
//            entries.add(new PieEntry((float) ((Math.random() * mult) + mult / 5),
//                    mParties[i % mParties.length],
//                    ContextCompat.getDrawable(mContext, android.R.drawable.star_on)));
//        }

        for (int i=1; i<mDataList.size(); i++) {
            Entry _entry = mDataList.get(i);
            PieEntry pEntry = new PieEntry((float)_entry.price, _entry.titleName);
            pieEntries.add(pEntry);
        }

        PieDataSet dataSet = new PieDataSet(pieEntries, "Election Results");

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

    @Override
    public void onClick(View view) {

    }

    @Override
    public boolean onLongClick(View view) {
        return false;
    }
}
