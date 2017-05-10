package com.example.taxnoteandroid;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taxnoteandroid.Library.AsyncOkHttpClient;
import com.example.taxnoteandroid.Library.BroadcastUtil;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.EntryLimitManager;
import com.example.taxnoteandroid.Library.taxnote.TNApi;
import com.example.taxnoteandroid.Library.taxnote.TNApiModel;
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

import okhttp3.Response;

/**
 * Created by b0ne on 2017/02/28.
 */

public class GraphContentFragment extends Fragment {

    private Context mContext;
    private FragmentGraphContentBinding binding;
    private GraphHistoryRecyclerAdapter mRecyclerAdapter;
    private EntryDataManager mEntryManager;
    private int mPeriodType;

    private TNApiModel mApiModel;

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
        mApiModel = new TNApiModel(mContext);
        if (!mApiModel.isCloudActive())
            binding.refreshLayout.setEnabled(false);

        mPeriodType = SharedPreferencesManager.getProfitLossReportPeriodType(mContext);
        mEntryManager = new EntryDataManager(mContext);

        binding.refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!TNApi.isNetworkConnected(mContext)) {
                    binding.refreshLayout.setRefreshing(false);
                    DialogManager.showOKOnlyAlert(getActivity(),
                            null, getString(R.string.network_not_connection));
                    return;
                }
                if (!mApiModel.isLoggingIn()
                        || !mApiModel.isCloudActive()
                        || mApiModel.isSyncing()) {
                    binding.refreshLayout.setRefreshing(false);
                    return;
                }

                refreshSyncData();
            }
        });
        loadGraphData();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void replayGraphAnimate() {
        if (mRecyclerAdapter != null)
            mRecyclerAdapter.replayGraphAnimate();
    }

    private void refreshSyncData() {
        mApiModel.syncData(getActivity(), true, new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e("Error", "refreshSyncData onFailure");
                binding.refreshLayout.setRefreshing(false);
                String errorMsg = "";
                if (response != null) {
                    errorMsg = response.message();
                }
                DialogManager.showOKOnlyAlert(getActivity(),
                        "Error", errorMsg);

            }

            @Override
            public void onSuccess(Response response, String content) {
                binding.refreshLayout.setRefreshing(false);
                loadGraphData();
            }
        });
    }

    private void loadGraphData() {
        Calendar targetCalendar  = (Calendar) getArguments().getSerializable(KEY_TARGET_CALENDAR);
        boolean isExpense = getArguments().getBoolean(KEY_IS_EXPENSE, true);

        long[] startEndDate = EntryLimitManager.getStartAndEndDate(mContext, mPeriodType, targetCalendar);
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
                    _entry1.reason = entry.reason;
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
                    BroadcastUtil.sendSwitchGraphExpense(getActivity());
                }
            });
            binding.recyclerContent.setAdapter(mRecyclerAdapter);
            mRecyclerAdapter.setOnItemClickListener(new GraphHistoryRecyclerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position, Entry item) {
                    boolean isExpense = getArguments().getBoolean(KEY_IS_EXPENSE, true);
                    Calendar targetCalendar  = (Calendar) getArguments().getSerializable(KEY_TARGET_CALENDAR);
                    if (item.reason == null) {
                        BarGraphActivity.start(mContext, isExpense, targetCalendar, mPeriodType);
                    } else {
                        if (!mApiModel.isCloudActive()) {
                            // not_cloud_bar_graph
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(R.string.taxnote_cloud_first_free)
                                    .setMessage(R.string.bar_graph_cloud_required_message)
                                    .setPositiveButton(R.string.upgrade, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            UpgradeActivity.start(getActivity());
                                        }
                                    })
                                    .setNegativeButton(R.string.cancel, null)
                                    .show();
                            return;
                        }
                        BarGraphActivity.startForReason(mContext, item.reason.uuid, targetCalendar, mPeriodType);
                    }
                }
            });
        }
    }
}
