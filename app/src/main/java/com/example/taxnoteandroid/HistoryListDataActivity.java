package com.example.taxnoteandroid;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.EntryLimitManager;
import com.example.taxnoteandroid.Library.ValueConverter;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityEntryCommonBinding;
import com.example.taxnoteandroid.model.Entry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
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
    private long[] mStartEndDate;
    private String mReasonName = null;

    private static final String KEY_TARGET_CALENDAR = "target_calendar";
    private static final String KEY_REASON_NAME = "reason_name";
    private static final String KEY_IS_BALANCE = "is_balance";
    private static final String KEY_IS_EXPENSE = "is_expense";

    /**
     * 残高のコンテンツ
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
     * 収入または支出のコンテンツ
     * @param context
     * @param targetCalendar
     * @param reasonName
     * @param isExpense
     */
    public static void start(Context context, Calendar targetCalendar, String reasonName, boolean isExpense) {
        Intent intent = new Intent(context, HistoryListDataActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_TARGET_CALENDAR, targetCalendar);
        intent.putExtra(KEY_REASON_NAME, reasonName);
        intent.putExtra(KEY_IS_EXPENSE, isExpense);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_entry_common);
        binding.entries.setLayoutManager(new LinearLayoutManager(this));
        binding.entries.addItemDecoration(new DividerDecoration(this));
        mPeriodType = SharedPreferencesManager.getProfitLossReportPeriodType(this);

        mEntryManager = new EntryDataManager(this);

        Intent receiptIntent = getIntent();
        Calendar targetCalendar  = (Calendar) receiptIntent.getSerializableExtra(KEY_TARGET_CALENDAR);
        mReasonName = receiptIntent.getStringExtra(KEY_REASON_NAME);
        mIsBalance = receiptIntent.getBooleanExtra(KEY_IS_BALANCE, false);
        mIsExpense = receiptIntent.getBooleanExtra(KEY_IS_EXPENSE, false);

        String pageTitle = getCalendarStringFromPeriodType(targetCalendar);
        String pageSubTitle = mReasonName;
        if (mIsBalance) {
            pageSubTitle = getString(R.string.History);
        }
        if (mReasonName == null) {
            pageSubTitle = (mIsExpense) ? getString(R.string.Expense) : getString(R.string.Income);
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(pageTitle);
        actionBar.setSubtitle(pageSubTitle);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mStartEndDate = EntryLimitManager.getStartAndEndDate(this, mPeriodType, targetCalendar);

        binding.refreshLayout.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEntryData(mStartEndDate, mIsBalance, mIsExpense);
    }

    private String getCalendarStringFromPeriodType(Calendar c) {
        switch (mPeriodType) {
            case EntryDataManager.PERIOD_TYPE_MONTH:
                return Integer.toString(c.get(Calendar.YEAR))
                        + "/" + Integer.toString(c.get(Calendar.MONTH) + 1);
            case EntryDataManager.PERIOD_TYPE_DAY:
                return Integer.toString(c.get(Calendar.MONTH) + 1)
                        + "/" + Integer.toString(c.get(Calendar.DATE));
        }
        return Integer.toString(c.get(Calendar.YEAR));
    }

    private void loadEntryData(long[] startAndEndDate, boolean isBalance, boolean isExpense) {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_search:
                if (mIsBalance) {
                    SearchEntryActivity.start(this, mStartEndDate[0], mStartEndDate[1]);
                } else {
                    SearchEntryActivity.startWithIsExpense(
                            this, mStartEndDate[0], mStartEndDate[1], mReasonName, mIsExpense);
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

        // Confirm dialog
        new AlertDialog.Builder(this)
                .setTitle(null)
                .setMessage(getString(R.string.delete_this_screen_data_confirm_message))
                .setPositiveButton(getString(R.string.Delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        List<Entry> dataList = mEntryAdapter.getItems();
                        for (Entry entry : dataList) {
                            if (entry.dateString == null)
                                mEntryManager.delete(entry.id);
                        }
                        mEntryAdapter.clearAll();
                        mEntryAdapter.notifyDataSetChanged();
                        DialogManager.showToast(context, context.getString(R.string.delete_done));
                        sendBroadcast(new Intent(MainActivity.BROADCAST_REPORT_RELOAD));
                        finish();

                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
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
            Context context = getApplicationContext();

            List<Entry> entries;

            if (isBalance) {
                entries = mEntryManager.findAll(startEndDate, false);
            } else {
                List<Entry> _entries = mEntryManager.findAll(startEndDate, isExpense, false);
                entries = new ArrayList<>();

                // Filter data by reasonName
                String reasonName = getIntent().getStringExtra(KEY_REASON_NAME);
                if (reasonName != null ) {
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
                headerItem.sumString = ValueConverter.formatPrice(context, totalPrice);
            }

            return entryData;
        }

        @Override
        protected void onPostExecute(List<Entry> result) {
            if (result.size() == 0) {
                binding.entries.setVisibility(View.GONE);
                binding.empty.setVisibility(View.VISIBLE);
            } else {
                binding.entries.setVisibility(View.VISIBLE);
                binding.empty.setVisibility(View.GONE);
            }

            mEntryAdapter = new CommonEntryRecyclerAdapter(getApplicationContext(), result);
            mEntryAdapter.setOnItemClickListener(new CommonEntryRecyclerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position, Entry entry) {
                    EntryEditActivity.start(getApplicationContext(), entry);
                }
            });
            binding.entries.setAdapter(mEntryAdapter);
        }
    }
}
