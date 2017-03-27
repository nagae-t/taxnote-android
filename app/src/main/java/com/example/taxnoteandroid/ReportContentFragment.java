package com.example.taxnoteandroid;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taxnoteandroid.Library.AsyncOkHttpClient;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.EntryLimitManager;
import com.example.taxnoteandroid.Library.ValueConverter;
import com.example.taxnoteandroid.Library.taxnote.TNApi;
import com.example.taxnoteandroid.Library.taxnote.TNApiModel;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.FragmentReportContentBinding;
import com.example.taxnoteandroid.model.Entry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Response;

public class ReportContentFragment extends Fragment {

    private static final String KEY_TARGET_CALENDAR = "KEY_TARGET_CALENDAR";

    private FragmentReportContentBinding binding;
    private Context mContext;
    private Boolean isShowBalanceCarryForward = false;

    private CommonEntryRecyclerAdapter mRecyclerAdapter;
    private EntryDataManager mEntryManager;
    private Calendar mTargetCalendar;
    private int mPeriodType;

    private TNApiModel mApiModel;

    public ReportContentFragment() {
    }

    public static ReportContentFragment newInstance(Calendar targetCalendar) {
        ReportContentFragment fragment = new ReportContentFragment();
        Bundle args = new Bundle();
        args.putSerializable(KEY_TARGET_CALENDAR, targetCalendar);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReportContentBinding.inflate(inflater, container, false);
        binding.reportList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.reportList.addItemDecoration(new DividerDecoration(getContext()));
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity().getApplicationContext();
        mApiModel = new TNApiModel(mContext);
        if (!mApiModel.isLoggingIn()) binding.refreshLayout.setEnabled(false);

        mEntryManager = new EntryDataManager(mContext);
        mPeriodType = SharedPreferencesManager.getProfitLossReportPeriodType(mContext);
        mTargetCalendar  = (Calendar) getArguments().getSerializable(KEY_TARGET_CALENDAR);

        binding.topBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HistoryListDataActivity.startForBalance(mContext, mTargetCalendar);
            }
        });

        binding.refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!TNApi.isNetworkConnected(mContext) || !mApiModel.isLoggingIn()
                        || mApiModel.isSyncing()) {
                    binding.refreshLayout.setRefreshing(false);
                    return;
                }

                refreshSyncData();
            }
        });
    }

    public long[] getStartEndDate() {
        long[] startEndDate = EntryLimitManager.getStartAndEndDate(mContext, mPeriodType, mTargetCalendar);
        return startEndDate;
    }

    @Override
    public void onResume() {
        super.onResume();

        loadReportData();
    }

    private void refreshSyncData() {
        mApiModel.syncData(true, new AsyncOkHttpClient.Callback() {
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
                Log.v("TEST", "refreshSyncData onSuccess");
                binding.refreshLayout.setRefreshing(false);
                loadReportData();
            }
        });
    }

    private void loadReportData() {
        isShowBalanceCarryForward = SharedPreferencesManager.getBalanceCarryForward(mContext);
        String balanceText = mContext.getString(R.string.Balance);
        if (isShowBalanceCarryForward)
            balanceText = mContext.getString(R.string.Balance)
                    + mContext.getString(R.string.balance_carry_forward_view);
        binding.balanceTv.setText(balanceText);

        long[] startEndDate = EntryLimitManager.getStartAndEndDate(mContext, mPeriodType, mTargetCalendar);
        new ReportDataTask().execute(startEndDate);
    }

    private void setTopBalanceValue(long price) {
        // 残高の表示
        String priceString = ValueConverter.formatPrice(mContext, price);
        int priceColor = (price < 0) ? ContextCompat.getColor(mContext, R.color.expense)
                : ContextCompat.getColor(mContext, R.color.primary);
        priceString = (price > 0) ? "+"+priceString : priceString;
        binding.price.setText(priceString);
        binding.price.setTextColor(priceColor);
    }

    private class ReportDataTask extends AsyncTask<long[], Integer, List<Entry>> {

        @Override
        protected List<Entry> doInBackground(long[]... longs) {
            long[] startEndDate = longs[0];
            List<Entry> resultEntries = new ArrayList<>();
            List<Entry> entries = mEntryManager.findAll(startEndDate, false);

            Entry incomeSection = new Entry();
            incomeSection.viewType = CommonEntryRecyclerAdapter.VIEW_ITEM_HEADER;
            incomeSection.titleName = mContext.getString(R.string.Income);
            Entry expenseSection = new Entry();
            expenseSection.viewType = CommonEntryRecyclerAdapter.VIEW_ITEM_HEADER;
            expenseSection.titleName = mContext.getString(R.string.Expense);

            Entry incomeSum = new Entry();
            incomeSum.viewType = CommonEntryRecyclerAdapter.VIEW_ITEM_REPORT_TOTAL;
            Entry expenseSum = new Entry();
            expenseSum.viewType = CommonEntryRecyclerAdapter.VIEW_ITEM_REPORT_TOTAL;
            expenseSum.isExpense = true;

            // 支出と収入のそれぞれの合計を計算する
            long balancePrice = 0;
            for (Entry entry : entries) {
                if (entry.isExpense) {
                    // -
                    expenseSum.price += entry.price;
                    balancePrice -= entry.price;
                } else {
                    // +
                    incomeSum.price += entry.price;
                    balancePrice += entry.price;
                }
            }
            Entry topBalance = new Entry();
            if (isShowBalanceCarryForward) {
                topBalance.price = mEntryManager.findSumBalance(startEndDate[1]);
            } else {
                topBalance.price = balancePrice;
            }
            // このデータはAdapterで表示しないのでのちに削除
            resultEntries.add(topBalance);

            // 支出と収入データを分ける
            List<Entry> incomeList = new ArrayList<>();
            List<Entry> expenseList = new ArrayList<>();
            for (Entry entry : entries) {
                if (entry.reason.isExpense) {
                    expenseList.add(entry);
                } else {
                    incomeList.add(entry);
                }
            }

            Map<Long, Entry> incomeMap = new LinkedHashMap<>();
            Map<Long, Entry> expenseMap = new LinkedHashMap<>();

            for (Entry entry : incomeList) {
                Long id = entry.reason.id;
                if (incomeMap.containsKey(id)) {
                    Entry _entry2 = incomeMap.get(id);
                    _entry2.price += entry.price;
                } else {
                    Entry _entry1 = new Entry();
                    _entry1.viewType = CommonEntryRecyclerAdapter.VIEW_ITEM_REPORT_CELL;
                    _entry1.reasonName = entry.reason.name;
                    _entry1.price += entry.price;
                    incomeMap.put(id, _entry1);
                }
            }

            for (Entry entry : expenseList) {
                Long id = entry.reason.id;
                if (expenseMap.containsKey(id)) {
                    Entry _entry2 = expenseMap.get(id);
                    _entry2.price += entry.price;
                } else {
                    Entry _entry1 = new Entry();
                    _entry1.viewType = CommonEntryRecyclerAdapter.VIEW_ITEM_REPORT_CELL;
                    _entry1.reasonName = entry.reason.name;
                    _entry1.price += entry.price;
                    _entry1.isExpense = true;
                    expenseMap.put(id, _entry1);
                }
            }

            // 順番ソート
            List<Map.Entry<Long, Entry>> incomeSortList = EntryLimitManager.sortLinkedHashMap(incomeMap);
            List<Map.Entry<Long, Entry>> expenseSortList = EntryLimitManager.sortLinkedHashMap(expenseMap);

            // 表示データはここから
            resultEntries.add(incomeSection);
            resultEntries.add(incomeSum);
            for (Map.Entry<Long, Entry> entry : incomeSortList) {
                resultEntries.add(entry.getValue());
            }

            resultEntries.add(expenseSection);
            resultEntries.add(expenseSum);
            for (Map.Entry<Long, Entry> entry : expenseSortList) {
                resultEntries.add(entry.getValue());
            }

            return resultEntries;
        }

        @Override
        protected void onPostExecute(List<Entry> result) {
            if (result == null || result.size() == 0) return;

            Entry topBalance = result.get(0);
            setTopBalanceValue(topBalance.price);
            result.remove(0);

            mRecyclerAdapter = new CommonEntryRecyclerAdapter(mContext, result);
            mRecyclerAdapter.setOnItemClickListener(new CommonEntryRecyclerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position, Entry entry) {
                    String reasonName = null;
                    if (entry.viewType == CommonEntryRecyclerAdapter.VIEW_ITEM_REPORT_CELL)
                        reasonName = entry.reasonName;
                    HistoryListDataActivity.start(mContext, mTargetCalendar, reasonName, entry.isExpense);
                }
            });
            binding.reportList.setAdapter(mRecyclerAdapter);
        }
    }
}
