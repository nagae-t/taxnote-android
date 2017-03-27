package com.example.taxnoteandroid;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taxnoteandroid.Library.AsyncOkHttpClient;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.ValueConverter;
import com.example.taxnoteandroid.Library.taxnote.TNApi;
import com.example.taxnoteandroid.Library.taxnote.TNApiModel;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.FragmentHistoryTabBinding;
import com.example.taxnoteandroid.model.Entry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Response;

/**
 * 仕訳帳の画面
 */
public class HistoryTabFragment extends Fragment {

    private FragmentHistoryTabBinding binding;
    private CommonEntryRecyclerAdapter mEntryAdapter;
    private Context mContext;
    private TNApiModel mApiModel;

    public HistoryTabFragment() {
        // Required empty public constructor
    }

    public static HistoryTabFragment newInstance() {
        HistoryTabFragment fragment = new HistoryTabFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context = getContext();
        binding = FragmentHistoryTabBinding.inflate(inflater,container, false);
        binding.history.setLayoutManager(new LinearLayoutManager(context));
        binding.history.addItemDecoration(new DividerDecoration(context));

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity().getApplicationContext();

        mEntryAdapter = new CommonEntryRecyclerAdapter(mContext);

        mApiModel = new TNApiModel(mContext);
        if (!mApiModel.isLoggingIn()) binding.refreshLayout.setEnabled(false);

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

    @Override
    public void onResume() {
        super.onResume();
        loadHistoryData();

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (this.isVisible()) {
            if (isVisibleToUser) {
                loadHistoryData();
                DialogManager.showDataExportSuggestMessage(getActivity(), getFragmentManager());
            }
        }
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
                loadHistoryData();
            }
        });
    }


    //--------------------------------------------------------------//
    //    -- History View --
    //--------------------------------------------------------------//

    private void loadHistoryData() {
        if (mEntryAdapter != null) {
            mEntryAdapter.clearAll();
        }
        mEntryAdapter = new CommonEntryRecyclerAdapter(mContext);

        EntryDataManager entryDataManager   = new EntryDataManager(getContext());
        List<Entry> entries                 = entryDataManager.findAll(null, false);

        if (entries == null || entries.isEmpty()) {

            binding.empty.setText(getResources().getString(R.string.history_data_empty));
            binding.empty.setVisibility(View.VISIBLE);
        } else {
            binding.empty.setVisibility(View.GONE);
        }

        List<Entry> entryData = new ArrayList<>();
        Map<String, List<Entry>> map2 = new LinkedHashMap<>();

        // 入力日ごとにグルーピング
        for (Entry entry : entries) {

            // Format date to string
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources().getString(R.string.date_string_format_to_year_month_day_weekday));
            String dateString = simpleDateFormat.format(entry.date);

            if (!map2.containsKey(dateString)) {
                List<Entry> entryList = new ArrayList<>();
                entryList.add(entry);
                map2.put(dateString, entryList);
            } else {
                List<Entry> entryList = map2.get(dateString);
                entryList.add(entry);
                map2.put(dateString, entryList);
            }
        }

        // RecyclerViewに渡すためにMapをListに変換する
        for (Map.Entry<String, List<Entry>> e : map2.entrySet()) {

            Entry headerItem = new Entry();
            headerItem.dateString = e.getKey();
            headerItem.viewType = CommonEntryRecyclerAdapter.VIEW_ITEM_HEADER;
            entryData.add(headerItem);

            long totalPrice = 0;

            for (Entry _entry : e.getValue()) {

                // Calculate total price
                if (_entry.isExpense) {
                    totalPrice -= _entry.price;
                } else {
                    totalPrice += _entry.price;
                }

                _entry.viewType = CommonEntryRecyclerAdapter.VIEW_ITEM_CELL;
                entryData.add(_entry);
            }

            // Format the totalPrice
            headerItem.sumString = ValueConverter.formatPrice(getActivity(), totalPrice);
        }

        mEntryAdapter.addAll(entryData);
        mEntryAdapter.setOnItemClickListener(new CommonEntryRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Entry entry) {
                SharedPreferencesManager.saveTapHereHistoryEditDone(getActivity());
                EntryEditActivity.start(mContext, entry);
            }
        });
        binding.history.setAdapter(mEntryAdapter);
    }

}