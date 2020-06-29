package com.example.taxnoteandroid;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.example.taxnoteandroid.Library.AsyncOkHttpClient;
import com.example.taxnoteandroid.Library.BroadcastUtil;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.ValueConverter;
import com.example.taxnoteandroid.Library.taxnote.TNApi;
import com.example.taxnoteandroid.Library.taxnote.TNApiModel;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.FragmentHistoryTabBinding;
import com.example.taxnoteandroid.model.Entry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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

    private ProjectDataManager mProjectManager;
    private EntryDataManager mEntryManager;
    private Entry mSelectedEntry;
    private int mSelectedPosition = -1;

    private SearchView searchView;
    private String mQuery = "";

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
        binding = FragmentHistoryTabBinding.inflate(inflater, container, false);
        binding.history.setLayoutManager(new LinearLayoutManager(context));
        binding.history.addItemDecoration(new DividerDecoration(context));

        setHasOptionsMenu(true);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity().getApplicationContext();

        mProjectManager = new ProjectDataManager(mContext);
        mEntryManager = new EntryDataManager(mContext);

        mApiModel = new TNApiModel(mContext);
        if (!mApiModel.isCloudActive() || !mApiModel.isLoggingIn())
            binding.refreshLayout.setEnabled(false);

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
        loadHistoryData();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) menuItem.getActionView();
        searchView.setIconifiedByDefault(true);
        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(onQueryText);

        searchView.setMaxWidth(getView().getWidth() - (int) (56f * getResources().getDisplayMetrics().density));
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (this.isVisible()) {
            if (isVisibleToUser) {
                DialogManager.showDataExportSuggestMessage(getActivity(), getFragmentManager());
            } else {
                clearSearchEntry();
            }
        }
    }

    public void afterLogin() {
        mApiModel = new TNApiModel(mContext);
        if (mApiModel.isCloudActive() && mApiModel.isLoggingIn()) {
            binding.refreshLayout.setEnabled(true);
        } else {
            binding.refreshLayout.setEnabled(false);
        }
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
            }
        });
    }

    public void showDeleteConfirmDialog() {
        String projectName = mProjectManager.getCurrentName();
        String targetName = mQuery.isEmpty() ? projectName : projectName + " " + mQuery;
        String deleteBtnTitle = getString(R.string.delete_current_something, targetName);
        // Confirm dialog
        new AlertDialog.Builder(getActivity())
                .setTitle(null)
                .setMessage(getString(R.string.delete_this_screen_data_confirm_message))
                .setPositiveButton(deleteBtnTitle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteEntries();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void deleteEntries() {
        final TNSimpleDialogFragment loading = DialogManager.getLoading((AppCompatActivity) getActivity());
        loading.show(getFragmentManager(), null);
        final Handler uiHandler = new Handler(Looper.getMainLooper());
        new Thread(new Runnable() {
            @Override
            public void run() {

                List<Entry> dataList = mEntryManager.searchBy(mQuery, null, null, false);
                for (Entry entry : dataList) {
                    mEntryManager.updateSetDeleted(entry.uuid);
                }


                // delete finished
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        loading.dismissAllowingStateLoss();
                        mApiModel.saveAllNeedSaveSyncDeletedData(null);

                        clearSearchEntry();
                        DialogManager.showToast(mContext, mContext.getString(R.string.delete_done));
                        BroadcastUtil.sendReloadReport(getActivity());

                    }
                });
            }
        }).start();
    }

    //--------------------------------------------------------------//
    //    -- History View --
    //--------------------------------------------------------------//

    public void loadHistoryData() {
        mProjectManager = new ProjectDataManager(mContext);
        mEntryManager = new EntryDataManager(mContext);
        if (mEntryAdapter != null) {
            if (TaxnoteApp.getInstance().IS_HISTORY_LIST_EDITING) {
                TaxnoteApp.getInstance().IS_HISTORY_LIST_EDITING = false;
                if (mSelectedEntry != null) {
                    Entry changedEntry = mEntryManager.findById(mSelectedEntry.id);
                    if (changedEntry == null || changedEntry.deleted) {
                        mEntryAdapter.removeItem(mSelectedPosition);
                    } else {
                        changedEntry.viewType = mSelectedEntry.viewType;
                        mEntryAdapter.setItem(mSelectedPosition, changedEntry);
                    }
                    mSelectedEntry = null;
                    return;
                }
                mEntryAdapter.notifyDataSetChanged();
                return;

            }
            mEntryAdapter.clearAllToNotifyData();
        } else {
            mEntryAdapter = new CommonEntryRecyclerAdapter(mContext);
        }

//        binding.empty.setText(mContext.getString(R.string.loading));

        binding.loading.setVisibility(View.VISIBLE);
        binding.refreshLayout.setVisibility(View.GONE);
        new EntryDataTask().execute(0);
    }

    private class EntryDataTask extends AsyncTask<Integer, Integer, List<Entry>> {

        @Override
        protected List<Entry> doInBackground(Integer... integers) {
            List<Entry> entries = mEntryManager.searchBy(null, null, null, false);

            if (entries == null || entries.isEmpty() || entries.size() == 0) {
                return new ArrayList<>();
            }

            return setupSectionHeader(entries, false);
        }

        @Override
        protected void onPostExecute(List<Entry> result) {
            binding.loading.setVisibility(View.GONE);
            if (result.size() == 0) {
                binding.refreshLayout.setVisibility(View.GONE);
                binding.empty.setText(getResources().getString(R.string.history_data_empty));
                binding.empty.setVisibility(View.VISIBLE);
            } else {
                binding.empty.setVisibility(View.GONE);
                binding.refreshLayout.setVisibility(View.VISIBLE);
            }

            mEntryAdapter.addAll(result);
            mEntryAdapter.setOnItemClickListener(new CommonEntryRecyclerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position, Entry entry) {
                    mSelectedEntry = entry;
                    mSelectedPosition = position;
                    TaxnoteApp.getInstance().IS_HISTORY_LIST_EDITING = true;
                    SharedPreferencesManager.saveTapHereHistoryEditDone(getActivity());
                    EntryEditActivity.start(mContext, entry);
                    mEntryAdapter.notifyDataSetChanged();
                }
            });
            binding.history.setAdapter(mEntryAdapter);
        }
    }

    private List<Entry> setupSectionHeader(List<Entry> entries, Boolean hasTotalPrice) {
        List<Entry> entryData = new ArrayList<>();
        Map<String, List<Entry>> map2 = new LinkedHashMap<>();

        // 入力日ごとにグルーピング
        for (Entry entry : entries) {

            // Format date to string
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                    mContext.getResources().getString(R.string.date_string_format_to_year_month_day_weekday),
                    Locale.getDefault());
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

        long totalPrice = 0;
        Entry totalPriceHeaderItem = new Entry();
        totalPriceHeaderItem.dateString = getString(R.string.total);
        totalPriceHeaderItem.viewType = CommonEntryRecyclerAdapter.VIEW_ITEM_HEADER;
        if (hasTotalPrice) {
            entryData.add(totalPriceHeaderItem);
        }

        // RecyclerViewに渡すためにMapをListに変換する
        for (Map.Entry<String, List<Entry>> e : map2.entrySet()) {

            Entry headerItem = new Entry();
            headerItem.dateString = e.getKey();
            headerItem.viewType = CommonEntryRecyclerAdapter.VIEW_ITEM_HEADER;
            entryData.add(headerItem);

            long dailyTotalPrice = 0;

            for (Entry _entry : e.getValue()) {

                // Calculate total price
                if (_entry.isExpense) {
                    dailyTotalPrice -= _entry.price;
                } else {
                    dailyTotalPrice += _entry.price;
                }

                _entry.viewType = CommonEntryRecyclerAdapter.VIEW_ITEM_CELL;
                entryData.add(_entry);
            }

            // Format the totalPrice
            headerItem.sumString = ValueConverter.formatPrice(mContext, dailyTotalPrice);

            totalPrice += dailyTotalPrice;
        }

        totalPriceHeaderItem.sumString = ValueConverter.formatPrice(mContext, totalPrice);
        return entryData;
    }

    private boolean mIsOnSearchSubmit = false;

    private SearchView.OnQueryTextListener onQueryText = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            mIsOnSearchSubmit = true;
            closeKeyboard(searchView);
            if (query.length() > 0) {
                execSearchTask(query);
            } else {
                loadHistoryData();
            }

            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            mQuery = newText;
            if (newText.length() > 0) {
                execSearchTask(newText);
            } else {
                loadHistoryData();
            }

            return false;
        }
    };

    private void execSearchTask(String word) {
        binding.loading.setVisibility(View.VISIBLE);
        binding.refreshLayout.setVisibility(View.GONE);
        new EntrySearchTask().execute(word);
    }

    /**
     * 検索処理のタスク
     */
    private class EntrySearchTask extends AsyncTask<String, Integer, List<Entry>> {

        @Override
        protected List<Entry> doInBackground(String... strings) {
            String word = strings[0];
            List<Entry> result;
            result = mEntryManager.searchBy(word, null, null, false);
            for (Entry entry : result) {
                entry.viewType = CommonEntryRecyclerAdapter.VIEW_ITEM_CELL;
            }

            return setupSectionHeader(result, true);
        }

        @Override
        protected void onPostExecute(List<Entry> result) {
            binding.loading.setVisibility(View.GONE);
            if (result == null || result.size() == 0) {

                mEntryAdapter.clearAllToNotifyData();

                //QQ キーボードの検索の虫眼鏡ボタンをタップした時だけメッセージをだす
                if (mIsOnSearchSubmit) {
                    DialogManager.showToast(getContext(),
                            getString(R.string.no_match_by_search_message));
                    mIsOnSearchSubmit = false;
                }
                binding.refreshLayout.setVisibility(View.VISIBLE);
                return;
            }

            mEntryAdapter.setItems(result);
            mEntryAdapter.notifyDataSetChanged();
            binding.refreshLayout.setVisibility(View.VISIBLE);
        }
    }

    void clearSearchEntry() {
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.setIconified(true);
        }
    }

    private void closeKeyboard(View view) {
        if (view == null) return;
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}