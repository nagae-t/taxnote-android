package com.example.taxnoteandroid;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.FragmentGraphContentBinding;
import com.example.taxnoteandroid.model.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by b0ne on 2017/02/28.
 */

public class GraphContentFragment extends Fragment implements OnChartValueSelectedListener  {

    private Context mContext;
    private FragmentGraphContentBinding binding;
    private GraphHistoryRecyclerAdapter mRecyclerAdapter;
    private EntryDataManager mEntryManager;
    private int mPeriodType;
    private long[] mStartEndDate;
    private boolean mIsExpense;

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

        Calendar targetCalendar  = (Calendar) getArguments().getSerializable(KEY_TARGET_CALENDAR);
        mIsExpense = getArguments().getBoolean(KEY_IS_EXPENSE, false);
        mStartEndDate = getStartAndEndDate(targetCalendar);
        mPeriodType = SharedPreferencesManager.getGraphReportPeriodType(mContext);

        mEntryManager = new EntryDataManager(mContext);

        new EntryDataTask(mIsExpense).execute(mStartEndDate);
    }

    private long[] getStartAndEndDate(Calendar c) {
        Calendar startDate = (Calendar)c.clone();
        Calendar endDate = (Calendar)c.clone();
        endDate.set(c.get(Calendar.YEAR)+1, 0, 1);

        switch (mPeriodType) {
            case EntryDataManager.PERIOD_TYPE_MONTH:
                endDate.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, 1);
                break;
            case EntryDataManager.PERIOD_TYPE_DAY:
                endDate.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), 0, 0, 0);
                endDate.add(Calendar.DATE, 1);
                break;
        }

        long[] result = {startDate.getTimeInMillis(), endDate.getTimeInMillis()};
        return result;
    }

    @Override
    public void onValueSelected(com.github.mikephil.charting.data.Entry e, Highlight h) {
        if (e == null)
            return;
        Log.v("TEST",
                "Value: " + e.getY() + ", index: " + h.getX()
                        + ", DataSet index: " + h.getDataSetIndex());
    }

    @Override
    public void onNothingSelected() {

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
            List<Map.Entry<Long, Entry>> entrySortList = sortLinkedHashMap(entryMap);

            entryData.add(entrySum);
            for (Map.Entry<Long, Entry> entry : entrySortList) {
                entryData.add(entry.getValue());
            }

            return entryData;
        }

        private List<Map.Entry<Long, Entry>> sortLinkedHashMap(Map<Long, Entry> sourceMap) {
            List<Map.Entry<Long, Entry>> dataList =
                    new ArrayList<>(sourceMap.entrySet());
            Collections.sort(dataList, new Comparator<Map.Entry<Long, Entry>>() {

                @Override
                public int compare(Map.Entry<Long, Entry> entry1,
                                   Map.Entry<Long, Entry> entry2) {
                    long entry1sum = entry1.getValue().price;
                    long entry2sum = entry2.getValue().price;
                    if (entry1sum < entry2sum) {
                        return 1;
                    } else if (entry1sum == entry2sum) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            });
            return dataList;
        }

        @Override
        protected void onPostExecute(List<Entry> result) {
            if (result == null || result.size() == 0) return;

            for (Entry entry : result) {
                Log.v("TEST", entry.titleName + " : " + entry.price);
            }
            mRecyclerAdapter = new GraphHistoryRecyclerAdapter(mContext, result);
            binding.recyclerContent.setAdapter(mRecyclerAdapter);
        }
    }
}
