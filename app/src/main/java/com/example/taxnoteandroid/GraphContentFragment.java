package com.example.taxnoteandroid;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taxnoteandroid.Library.EntryLimitManager;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.FragmentGraphContentBinding;
import com.example.taxnoteandroid.model.Entry;
import com.github.mikephil.charting.charts.PieChart;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by b0ne on 2017/02/28.
 */

public class GraphContentFragment extends Fragment {

    private Context mContext;
    private FragmentGraphContentBinding binding;
    private GraphHistoryRecyclerAdapter mRecyclerAdapter;
    private EntryDataManager mEntryManager;
    private int mPeriodType;

    private static final String KEY_TARGET_CALENDAR = "TARGET_CALENDAR";
    private static final String KEY_IS_EXPENSE= "IS_EXPENSE";

    public static GraphContentFragment newInstance(Calendar targetCalendar, boolean isExpense) {
        GraphContentFragment fragment = new GraphContentFragment();
        Bundle args = new Bundle();
        args.putSerializable(KEY_TARGET_CALENDAR, targetCalendar);
        args.putBoolean(KEY_IS_EXPENSE, isExpense);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGraphContentBinding.inflate(inflater, container, false);
        binding.recyclerContent.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerContent.addItemDecoration(new DividerDecoration(getContext()));
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity().getApplicationContext();

        mPeriodType = SharedPreferencesManager.getProfitLossReportPeriodType(mContext);
        mEntryManager = new EntryDataManager(mContext);
        Calendar targetCalendar  = (Calendar) getArguments().getSerializable(KEY_TARGET_CALENDAR);
        boolean isExpense = getArguments().getBoolean(KEY_IS_EXPENSE, true);

        long[] startEndDate = EntryLimitManager.getStartAndEndDate(mPeriodType, targetCalendar);
        new EntryDataTask(isExpense).execute(startEndDate);
    }


    private class EntryDataTask extends AsyncTask<long[], Integer, List<Entry>> {
        private boolean isExpense;

        public EntryDataTask(boolean isExpense) {
            this.isExpense = isExpense;
        }

        @Override
        protected List<Entry> doInBackground(long[]... longs) {
            long[] startEndDate = longs[0];
            List<Entry> entryData = new ArrayList<>();
            List<Entry> entries = mEntryManager.findAll(startEndDate, isExpense, false);

            Entry entrySum = new Entry();
            entrySum.viewType = GraphHistoryRecyclerAdapter.VIEW_ITEM_CELL;
            entrySum.isExpense = isExpense;
            entrySum.titleName = (isExpense) ?
                    mContext.getString(R.string.Expense) :
                    mContext.getString(R.string.Income);
            for (Entry entry : entries) {
                entrySum.price += entry.price;
            }

            Map<Long, Entry> entryMap = new LinkedHashMap<>();

            for (Entry entry : entries) {
                Long id = entry.reason.id;
                if (entryMap.containsKey(id)) {
                    Entry _entry2 = entryMap.get(id);
                    _entry2.price += entry.price;
                } else {
                    Entry _entry1 = new Entry();
                    _entry1.viewType = GraphHistoryRecyclerAdapter.VIEW_ITEM_CELL;
                    _entry1.titleName = entry.reason.name;
                    _entry1.price += entry.price;
                    _entry1.isExpense = isExpense;
                    entryMap.put(id, _entry1);
                }
            }
            List<Map.Entry<Long, Entry>> entrySortList = EntryLimitManager.sortLinkedHashMap(entryMap);

            entryData.add(entrySum);
            for (Map.Entry<Long, Entry> entry : entrySortList) {
                entryData.add(entry.getValue());
            }

            return entryData;
        }


        @Override
        protected void onPostExecute(List<Entry> result) {
            if (result == null || result.size() == 0) return;

            mRecyclerAdapter = new GraphHistoryRecyclerAdapter(mContext, result);
            mRecyclerAdapter.setOnGraphClickListener(new GraphHistoryRecyclerAdapter.OnGraphClickListener() {
                @Override
                public void onClick(View view, PieChart chart) {
                    view.getContext().sendBroadcast(new Intent(MainActivity.BROADCAST_SWITCH_GRAPH_EXPENSE));
                }
            });
            binding.recyclerContent.setAdapter(mRecyclerAdapter);
        }
    }
}
