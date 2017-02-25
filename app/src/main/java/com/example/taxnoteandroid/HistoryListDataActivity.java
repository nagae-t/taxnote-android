package com.example.taxnoteandroid;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityEntryCommonBinding;
import com.example.taxnoteandroid.model.Entry;

import org.parceler.Parcels;

import java.util.Calendar;

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
    private static final String KEY_ENTRY_DATA = "entry_data";
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


    public static void start(Context context, Calendar targetCalendar, Entry entry) {
        Intent intent = new Intent(context, HistoryListDataActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_TARGET_CALENDAR, targetCalendar);
        if (entry != null)
            intent.putExtra(KEY_ENTRY_DATA, Parcels.wrap(entry));
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

        Intent receiptIntent = getIntent();
        Calendar targetCalendar  = (Calendar) receiptIntent.getSerializableExtra(KEY_TARGET_CALENDAR);
        String reasonName = receiptIntent.getStringExtra(KEY_REASON_NAME);
        boolean isBalance = receiptIntent.getBooleanExtra(KEY_IS_BALANCE, false);
        /*
        Parcelable entryPar = receiptIntent.getParcelableExtra(KEY_ENTRY_DATA);
        if (entryPar != null) {
            Entry entry = Parcels.unwrap(entryPar);
        } else {
            reasonName = getString(R.string.History);
        }*/

        String dateString = getCalendarStringFromPeriodType(targetCalendar);
        String pageTitle = dateString + " " + reasonName;
        if (isBalance) {
            pageTitle = dateString + " " + getString(R.string.History);
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(pageTitle);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mEntryAdapter.setOnItemClickListener(new CommonEntryRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Entry entry) {
                EntryEditActivity.start(getApplicationContext(), entry);
            }
        });
        binding.entries.setAdapter(mEntryAdapter);
    }

    private String getCalendarStringFromPeriodType(Calendar c) {
        switch (mPeriodType) {
            case ReportFragment.PERIOD_TYPE_MONTH:
                return Integer.toString(c.get(Calendar.YEAR))
                        + "/" + Integer.toString(c.get(Calendar.MONTH) + 1);
            case ReportFragment.PERIOD_TYPE_DAY:
                return Integer.toString(c.get(Calendar.YEAR))
                        + "/" + Integer.toString(c.get(Calendar.MONTH) + 1)
                        + "/" + Integer.toString(c.get(Calendar.DATE));
        }
        return Integer.toString(c.get(Calendar.YEAR));
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
            case R.id.action_delete:
                // TODO: 削除の確認ダイアログを表示
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
