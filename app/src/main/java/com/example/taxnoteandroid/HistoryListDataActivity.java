package com.example.taxnoteandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.taxnoteandroid.Library.BroadcastUtil;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.EntryLimitManager;
import com.example.taxnoteandroid.Library.ValueConverter;
import com.example.taxnoteandroid.Library.taxnote.TNApiModel;
import com.example.taxnoteandroid.Library.taxnote.TNApiUser;
import com.example.taxnoteandroid.Library.taxnote.TNUtils;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityEntryCommonBinding;
import com.example.taxnoteandroid.model.Entry;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by b0ne on 2017/02/25.
 */

public class HistoryListDataActivity extends DefaultCommonActivity {

    private ActivityEntryCommonBinding binding;
    private CommonEntryRecyclerAdapter mEntryAdapter;
    private EntryDataManager mEntryManager;
    private int mPeriodType;
    private boolean mIsExpense;
    private boolean mIsBalance;
    private Calendar mTargetCalendar;
    private long[] mStartEndDate;
    private String mReasonName = null;
    private String mMemoValue = null;
    private boolean mIsViewTotal = false;
    private String mPageTitleAndSub;

    private TNApiModel mApiModel;
    private List<Entry> mResultEntries;

    private Entry mSelectedEntry;
    private int mSelectedPosition = -1;

    private SearchView searchView;
    private String mQuery = "";

    private static final String KEY_TARGET_CALENDAR = "target_calendar";
    private static final String KEY_PERIOD_TYPE = "period_type";
    private static final String KEY_REASON_NAME = "reason_name";
    private static final String KEY_IS_BALANCE = "is_balance";
    private static final String KEY_IS_EXPENSE = "is_expense";
    private static final String KEY_MEMO = "memo";
    private static final String KEY_IS_VIEW_TOTAL = "is_view_total";

    public static final String BROADCAST_DATA_RELOAD
            = "broadcast_history_list_reload";

    private final BroadcastReceiver mReloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mStartEndDate != null)
                loadEntryData(mStartEndDate, mIsBalance, mIsExpense);
        }
    };

    /**
     * ????????????????????????
     *
     * @param context
     * @param targetCalendar
     */
    public static void startForBalance(Context context, Calendar targetCalendar) {
        Intent intent = new Intent(context, HistoryListDataActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_TARGET_CALENDAR, targetCalendar);
        intent.putExtra(KEY_IS_BALANCE, true);
        context.startActivity(intent);
    }

    /**
     * @param context
     * @param targetCalendar
     * @param periodType
     */
    public static void startForBalance(Context context, Calendar targetCalendar, int periodType) {
        Intent intent = new Intent(context, HistoryListDataActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_TARGET_CALENDAR, targetCalendar);
        intent.putExtra(KEY_IS_BALANCE, true);
        intent.putExtra(KEY_PERIOD_TYPE, periodType);
        context.startActivity(intent);
    }

    /**
     * ???????????????????????????????????????
     *
     * @param context
     * @param targetCalendar
     * @param reasonName
     * @param isExpense
     */
    public static void start(Context context, Calendar targetCalendar,
                             String reasonName, String memo, boolean isExpense) {
        start(context, 0, targetCalendar, reasonName, memo, isExpense, false);
    }

    public static void start(Context context, Calendar targetCalendar,
                             String reasonName, String memo,
                             boolean isExpense, boolean isViewTotal) {
        start(context, 0, targetCalendar, reasonName, memo, isExpense, isViewTotal);
    }

    public static void start(Context context, int periodType, Calendar targetCalendar,
                             String reasonName, String memo, boolean isExpense, boolean isViewTotal) {
        Intent intent = new Intent(context, HistoryListDataActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_TARGET_CALENDAR, targetCalendar);
        intent.putExtra(KEY_PERIOD_TYPE, periodType);
        intent.putExtra(KEY_REASON_NAME, reasonName);
        intent.putExtra(KEY_MEMO, memo);
        intent.putExtra(KEY_IS_EXPENSE, isExpense);
        intent.putExtra(KEY_IS_VIEW_TOTAL, isViewTotal);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerReceiver(mReloadReceiver, new IntentFilter(BROADCAST_DATA_RELOAD));

        mApiModel = new TNApiModel(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_entry_common);
        binding.entries.setLayoutManager(new LinearLayoutManager(this));
        binding.entries.addItemDecoration(new DividerDecoration(this));
        mPeriodType = SharedPreferencesManager.getProfitLossReportPeriodType(this);
        int _periodType = getIntent().getIntExtra(KEY_PERIOD_TYPE, 0);
        mPeriodType = (_periodType == 0) ? mPeriodType : _periodType;

        mEntryManager = new EntryDataManager(this);

        Intent receiptIntent = getIntent();
        Serializable calSerial = receiptIntent.getSerializableExtra(KEY_TARGET_CALENDAR);
        if (calSerial != null) mTargetCalendar = (Calendar) calSerial;
        mReasonName = receiptIntent.getStringExtra(KEY_REASON_NAME);
        mIsBalance = receiptIntent.getBooleanExtra(KEY_IS_BALANCE, false);
        mIsExpense = receiptIntent.getBooleanExtra(KEY_IS_EXPENSE, false);
        mMemoValue = receiptIntent.getStringExtra(KEY_MEMO);
        mIsViewTotal = receiptIntent.getBooleanExtra(KEY_IS_VIEW_TOTAL, false);

        String pageTitle = (mTargetCalendar == null)
                ? getString(R.string.divide_by_all)
                : TNUtils.getCalendarStringFromPeriodType(this, mTargetCalendar, mPeriodType);
        String pageSubTitle = mReasonName;
        if (mReasonName == null) {
            pageSubTitle = (mIsExpense) ? getString(R.string.Expense) : getString(R.string.Income);
        }
        if (mIsBalance) {
            pageSubTitle = getString(R.string.History);
        }

        mPageTitleAndSub = pageTitle + " " + pageSubTitle;
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(pageTitle);
        actionBar.setSubtitle(pageSubTitle);
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (mTargetCalendar == null) {
            mStartEndDate = new long[]{};
        } else {
            mStartEndDate = EntryLimitManager.getStartAndEndDate(this, mPeriodType, mTargetCalendar);
        }

        binding.refreshLayout.setEnabled(false);
        loadEntryData(mStartEndDate, mIsBalance, mIsExpense);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void loadEntryData(long[] startAndEndDate, boolean isBalance, boolean isExpense) {

        if (mEntryAdapter != null) {
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
            mEntryAdapter.clearAllToNotifyData();
        } else {
            mEntryAdapter = new CommonEntryRecyclerAdapter(this);
        }

        binding.refreshLayout.setVisibility(View.GONE);
        binding.loading.setVisibility(View.VISIBLE);
        // Load by multi task.
        new HistoryDataTask(isBalance, isExpense)
                .execute(startAndEndDate);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_history_list_data, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) menuItem.getActionView();
        searchView.setIconifiedByDefault(true);
        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(onQueryText);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        searchView.setMaxWidth(displayMetrics.widthPixels - (int) (112f * getResources().getDisplayMetrics().density));

        MenuItem exportMenu = menu.findItem(R.id.action_export);
        MenuItem delMenu = menu.findItem(R.id.action_delete);

        String targetName = getTargetName();
        String exportTitle = getString(R.string.export_current_something, targetName);
        String delTitle = getString(R.string.delete_current_something, targetName);
        exportMenu.setTitle(exportTitle);
        delMenu.setTitle(delTitle);

        return super.onPrepareOptionsMenu(menu);
    }

    private String getTargetName() {
        return mQuery.isEmpty() ? mPageTitleAndSub : mPageTitleAndSub + " " + mQuery;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_search:
                break;
            case R.id.action_export:
                String targetName = getTargetName();
                if (mIsBalance) {
                    DataExportActivity.startForBalance(this,
                            targetName, mTargetCalendar, mPeriodType, mQuery);
                } else {
                    DataExportActivity.start(this,
                            targetName, mTargetCalendar,
                            mReasonName, mMemoValue, mIsExpense, mPeriodType, mQuery);
                }
                break;
            case R.id.action_delete:
                showAllDeleteDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAllDeleteDialog() {
        final Context context = getApplicationContext();

        String deleteBtnTitle = getString(R.string.delete_current_something, getTargetName());
        // Confirm dialog
        new AlertDialog.Builder(this)
                .setTitle(null)
                .setMessage(getString(R.string.delete_this_screen_data_confirm_message))
                .setPositiveButton(deleteBtnTitle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (!mResultEntries.isEmpty()) {
                            for (Entry entry : mResultEntries) {
                                if (entry.uuid != null) {
                                    mEntryManager.updateSetDeleted(entry.uuid);
                                }
                            }
                        }

                        if (TNApiUser.isLoggingIn(context)) {
                            mApiModel.saveAllNeedSaveSyncDeletedData(null);
                        }

                        DialogManager.showToast(context, context.getString(R.string.delete_done));
                        BroadcastUtil.sendReloadReport(HistoryListDataActivity.this);
                        finish();

                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReloadReceiver);
        super.onDestroy();
    }

    private class HistoryDataTask extends AsyncTask<long[], Integer, List<Entry>> {
        private boolean isBalance;
        private boolean isExpense;

        public HistoryDataTask(boolean isBalance, boolean isExpense) {
            this.isBalance = isBalance;
            this.isExpense = isExpense;
        }

        @Override
        protected List<Entry> doInBackground(long[]... longs) {
            long[] startEndDate = longs[0];
            if (mPeriodType == EntryDataManager.PERIOD_TYPE_ALL
                    && startEndDate.length == 0) startEndDate = null;

            List<Entry> entries;

            if (isBalance) {
                entries = mEntryManager.searchBy(null, null, startEndDate, false);
            } else {
                List<Entry> _entries = (mMemoValue == null)
                        ? mEntryManager.searchBy(null, null, startEndDate, isExpense, false)
                        : mEntryManager.searchBy(null, null, startEndDate, mMemoValue, isExpense, false);
                entries = new ArrayList<>();

                // Filter data by reasonName
                String reasonName = getIntent().getStringExtra(KEY_REASON_NAME);
                if (reasonName != null) {
                    for (Entry _entry : _entries) {
                        if (_entry.reason.name.equals(reasonName)) {
                            entries.add(_entry);
                        }
                    }
                } else {
                    entries.addAll(_entries);
                }
            }

            if (entries == null || entries.isEmpty()) {
                return entries;
            }
            mResultEntries = entries;

            return setupSectionHeader(entries, false);
        }

        @Override
        protected void onPostExecute(List<Entry> result) {
            binding.refreshLayout.setVisibility(View.VISIBLE);
            binding.loading.setVisibility(View.GONE);
            if (result.size() == 0) {
                binding.entries.setVisibility(View.GONE);
                binding.empty.setVisibility(View.VISIBLE);
                return;
            } else {
                binding.entries.setVisibility(View.VISIBLE);
                binding.empty.setVisibility(View.GONE);
            }

            Entry topEntry = result.get(0);
            final boolean isMemoData = (topEntry.viewType == CommonEntryRecyclerAdapter.VIEW_ITEM_REPORT_TOTAL);

            mEntryAdapter.addAll(result);
            mEntryAdapter.setOnItemClickListener(new CommonEntryRecyclerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position, Entry entry) {

                    if (entry.price == 0) return;
                    if (isMemoData) {
                        String memoValue = (position == 0) ? null : entry.reasonName;
                        start(view.getContext(),
                                mTargetCalendar, mReasonName, memoValue,
                                entry.isExpense, (memoValue != null));
                    } else {
                        mSelectedEntry = entry;
                        mSelectedPosition = position;
                        EntryEditActivity.start(getApplicationContext(), entry);
                    }
                }
            });
            binding.entries.setAdapter(mEntryAdapter);
        }
    }

    private List<Entry> setupSectionHeader(List<Entry> entries, Boolean hasTotalPrice) {
        Context context = getApplicationContext();

        List<Entry> entryData = new ArrayList<>();
        Map<String, List<Entry>> map2 = new LinkedHashMap<>();


        Map<String, Entry> memoMap = new LinkedHashMap<>();
        // ?????????????????????
        Entry memoSum = new Entry();
        memoSum.viewType = CommonEntryRecyclerAdapter.VIEW_ITEM_REPORT_TOTAL;
        memoSum.isExpense = mIsExpense;

        // ????????????????????????????????????
        for (Entry entry : entries) {

            // Format date to string
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources().getString(R.string.date_string_format_to_year_month_day_weekday), Locale.getDefault());
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

            // ????????????????????????
            String _memo = entry.memo;
            if (memoMap.containsKey(_memo)) {
                Entry _memoEntry = memoMap.get(_memo);
                _memoEntry.price += entry.price;
            } else {
                Entry _memoEntry = new Entry();
                _memoEntry.isExpense = mIsExpense;
                _memoEntry.viewType = CommonEntryRecyclerAdapter.VIEW_ITEM_REPORT_CELL;
                _memoEntry.reasonName = _memo;
                _memoEntry.price += entry.price;
                _memoEntry.uuid = entry.uuid;
                memoMap.put(_memo, _memoEntry);
            }
            memoSum.price += entry.price;

        }
        int memoMapSize = memoMap.size();
        // ???????????????????????????????????????
        if (mIsViewTotal && memoMapSize > 1 && mReasonName != null && mMemoValue == null) {
            entryData.add(memoSum);

            Entry memoSection = new Entry();
            memoSection.viewType = CommonEntryRecyclerAdapter.VIEW_ITEM_HEADER;
            memoSection.titleName = getString(R.string.Details);
            entryData.add(memoSection);

            // ?????????????????????
            List<Map.Entry<String, Entry>> memoSortList = EntryLimitManager.sortMemoLinkedHashMap(memoMap);
            for (Map.Entry<String, Entry> entry : memoSortList) {
                Entry memoEntry = entry.getValue();
                entryData.add(memoEntry);
            }
            return entryData;

        }

        long totalPrice = 0;
        Entry totalPriceHeaderItem = new Entry();
        totalPriceHeaderItem.dateString = getString(R.string.total);
        totalPriceHeaderItem.viewType = CommonEntryRecyclerAdapter.VIEW_ITEM_HEADER;
        if (hasTotalPrice) {
            entryData.add(totalPriceHeaderItem);
        }

        // RecyclerView??????????????????Map???List???????????????
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
            headerItem.sumString = ValueConverter.formatPrice(context, dailyTotalPrice);

            totalPrice += dailyTotalPrice;
        }
        totalPriceHeaderItem.sumString = ValueConverter.formatPrice(context, totalPrice);
        return entryData;
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setQuery("", false);
            searchView.setIconified(true);
        } else {
            super.onBackPressed();
        }
    }

    private boolean mIsOnSearchSubmit = false;

    private SearchView.OnQueryTextListener onQueryText = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            closeKeyboard(searchView);
            if (query.length() > 0) {
                mIsOnSearchSubmit = true;
                execSearchTask(query);
            } else {
                loadEntryData(mStartEndDate, mIsBalance, mIsExpense);
            }

            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            mIsOnSearchSubmit = false;
            mQuery = newText;
            if (newText.length() > 0) {
                execSearchTask(newText);
            } else {
                loadEntryData(mStartEndDate, mIsBalance, mIsExpense);
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
     * ????????????????????????
     */
    private class EntrySearchTask extends AsyncTask<String, Integer, List<Entry>> {

        @Override
        protected List<Entry> doInBackground(String... strings) {
            String word = strings[0];
            List<Entry> result;

            long[] startEndDate = null;
            if (mStartEndDate.length == 2 && mStartEndDate[0] != 0 && mStartEndDate[1] != 0) {
                startEndDate = mStartEndDate;
            }

            if (mIsBalance) {
                result = mEntryManager.searchBy(word, null, startEndDate, false);
            } else {
                result = mEntryManager.searchBy(word, mReasonName, startEndDate, mIsExpense, false);
            }
            for (Entry entry : result) {
                entry.viewType = CommonEntryRecyclerAdapter.VIEW_ITEM_CELL;
            }
            mResultEntries = result;

            return setupSectionHeader(result, true);
        }

        @Override
        protected void onPostExecute(List<Entry> result) {
            binding.loading.setVisibility(View.GONE);
            if (result == null || result.size() == 0) {

                mEntryAdapter.clearAllToNotifyData();

                //QQ ????????????????????????????????????????????????????????????????????????????????????????????????
                if (mIsOnSearchSubmit) {
                    DialogManager.showToast(getApplicationContext(),
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

    private void closeKeyboard(View view) {
        if (view == null) return;
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
