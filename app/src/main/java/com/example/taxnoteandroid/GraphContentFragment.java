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
import java.util.List;

import static com.helpshift.util.HelpshiftContext.getApplicationContext;

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
            Context context = getApplicationContext();
            long[] startEndDate = longs[0];
            List<Entry> entryData = new ArrayList<>();
            List<Entry> entries;
            entries = mEntryManager.findAll(context, startEndDate, false);

            return entryData;
        }
    }
}
