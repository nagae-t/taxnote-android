package com.example.taxnoteandroid;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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

public class HistoryListDataActivity extends AppCompatActivity {

    private ActivityEntryCommonBinding binding;
    private CommonEntryRecyclerAdapter mEntryAdapter;
    private EntryDataManager mEntryManager;
    private int mPeriodType;

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
        mEntryAdapter = new CommonEntryRecyclerAdapter(this);
        mEntryAdapter.setOnItemClickListener(new CommonEntryRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Entry entry) {
                EntryEditActivity.start(getApplicationContext(), entry);
            }
        });

        Intent receiptIntent = getIntent();
        Calendar targetCalendar  = (Calendar) receiptIntent.getSerializableExtra(KEY_TARGET_CALENDAR);
        String reasonName = receiptIntent.getStringExtra(KEY_REASON_NAME);
        boolean isBalance = receiptIntent.getBooleanExtra(KEY_IS_BALANCE, false);
        boolean isExpense = receiptIntent.getBooleanExtra(KEY_IS_EXPENSE, false);

        String pageTitle = getCalendarStringFromPeriodType(targetCalendar);
        String pageSubTitle = reasonName;
        if (isBalance) {
            pageSubTitle = getString(R.string.History);
        }
        if (reasonName == null) {
            pageSubTitle = (isExpense) ? getString(R.string.Expense) : getString(R.string.Income);
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(pageTitle);
        actionBar.setSubtitle(pageSubTitle);
        actionBar.setDisplayHomeAsUpEnabled(true);

        long[] startEndDate = getStartAndEndDate(targetCalendar);
        loadEntryData(startEndDate, isBalance, isExpense);
    }

    private String getCalendarStringFromPeriodType(Calendar c) {
        switch (mPeriodType) {
            case ReportFragment.PERIOD_TYPE_MONTH:
                return Integer.toString(c.get(Calendar.YEAR))
                        + "/" + Integer.toString(c.get(Calendar.MONTH) + 1);
            case ReportFragment.PERIOD_TYPE_DAY:
                return Integer.toString(c.get(Calendar.MONTH) + 1)
                        + "/" + Integer.toString(c.get(Calendar.DATE));
        }
        return Integer.toString(c.get(Calendar.YEAR));
    }

    private long[] getStartAndEndDate(Calendar c) {
        Calendar startDate = (Calendar)c.clone();
        Calendar endDate = (Calendar)c.clone();
        endDate.set(c.get(Calendar.YEAR)+1, 0, 1);

        switch (mPeriodType) {
            case ReportFragment.PERIOD_TYPE_MONTH:
                endDate.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, 1);
                break;
            case ReportFragment.PERIOD_TYPE_DAY:
                endDate.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), 0, 0, 0);
                endDate.add(Calendar.DATE, 1);
                break;
        }

//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.date_string_format_to_year_month_day_weekday));
//        String startDateString = simpleDateFormat.format(startDate.getTime());
//        String endDateString = simpleDateFormat.format(endDate.getTime());
        long[] result = {startDate.getTimeInMillis(), endDate.getTimeInMillis()};
        return result;
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
                SearchEntryActivity.start(this);
                break;

//            //@@ サブミットするので、一時的にコメントアウト
//            case R.id.action_delete:
//                // TODO: 削除の確認ダイアログを表示
//                break;
        }
        return super.onOptionsItemSelected(item);
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
                entries = mEntryManager.findAll(context, startEndDate, false);
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
                entryData.add(headerItem);

                long totalPrice = 0;

                for (Entry _entry : e.getValue()) {

                    // Calculate total price
                    if (_entry.isExpense) {
                        totalPrice -= _entry.price;
                    } else {
                        totalPrice += _entry.price;
                    }

                    entryData.add(_entry);
                }

                // Format the totalPrice
                headerItem.sumString = ValueConverter.formatPrice(context, totalPrice);
            }

            return entryData;
        }

        @Override
        protected void onPostExecute(List<Entry> result) {
            mEntryAdapter.addAll(result);
            binding.entries.setAdapter(mEntryAdapter);
        }
    }
}