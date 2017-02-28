package com.example.taxnoteandroid;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taxnoteandroid.databinding.FragmentGraphContentBinding;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.Calendar;

/**
 * Created by b0ne on 2017/02/28.
 */

public class GraphContentFragment extends Fragment  implements OnChartValueSelectedListener  {

    private Context mContext;
    private FragmentGraphContentBinding binding;
    private GraphHistoryRecyclerAdapter mRecyclerAdapter;
    private Calendar mTargetCalendar;

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

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;
        Log.v("TEST",
                "Value: " + e.getY() + ", index: " + h.getX()
                        + ", DataSet index: " + h.getDataSetIndex());
    }

    @Override
    public void onNothingSelected() {

    }
}
