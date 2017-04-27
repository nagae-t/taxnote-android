package com.example.taxnoteandroid;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;

import com.example.taxnoteandroid.Library.DataExportManager;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.EntryLimitManager;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityProfitLossExportBinding;
import com.example.taxnoteandroid.model.Entry;
import com.helpshift.support.Support;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_CHARACTER_CODE_SHIFTJIS;
import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_CHARACTER_CODE_UTF8;

/**
 * Created by b0ne on 2017/03/14.
 */

public class ProfitLossExportActivity extends DefaultCommonActivity {

    private ActivityProfitLossExportBinding binding;
    private String mDefaultCharCode;
    private long[] mStartEndDate;

    private static final String KEY_TARGET_START_END_DATE = "target_start_end_date";

    public static void start(Context context, long[] startEndDate) {
        Intent intent = new Intent(context, ProfitLossExportActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_TARGET_START_END_DATE, startEndDate);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_profit_loss_export);
        mDefaultCharCode = SharedPreferencesManager.getCurrentCharacterCode(this);
        mStartEndDate = getIntent().getLongArrayExtra(KEY_TARGET_START_END_DATE);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // 文字コード
        binding.charCodeValue.setText(mDefaultCharCode);

        binding.charCodeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCharCodeMenuDialog();
            }
        });

        // Help link
        binding.help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Support.showFAQSection(ProfitLossExportActivity.this,"36");
            }
        });

        // CSV 出力
        binding.csvExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new ReportDataTask().execute(mStartEndDate);
            }
        });


        // for debug
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
//                getString(R.string.date_string_format_to_year_month_day));
//        Calendar startCal = Calendar.getInstance();
//        startCal.setTimeInMillis(mStartEndDate[0]);
//        Calendar endCal = Calendar.getInstance();
//        endCal.setTimeInMillis(mStartEndDate[1]);
//        String startCalStr = simpleDateFormat.format(startCal.getTime());
//        String endCalStr = simpleDateFormat.format(endCal.getTime());
//        Log.v("TEST", "startCal : " + startCalStr + ", endCal : " + endCalStr);
    }

    private void showCharCodeMenuDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String[] items = {EXPORT_CHARACTER_CODE_UTF8, EXPORT_CHARACTER_CODE_SHIFTJIS};
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String charCodeVal = mDefaultCharCode;
                switch (i) {
                    case 0:
                        charCodeVal = EXPORT_CHARACTER_CODE_UTF8;
                        break;
                    case 1:
                        charCodeVal = EXPORT_CHARACTER_CODE_SHIFTJIS;
                        break;
                }

                SharedPreferencesManager.saveCurrentCharacterCode(ProfitLossExportActivity.this, charCodeVal);
                binding.charCodeValue.setText(charCodeVal);
                DialogManager.showToast(ProfitLossExportActivity.this, charCodeVal);
                mDefaultCharCode = charCodeVal;
            }
        });
        AlertDialog menuDialog = builder.create();
        menuDialog.setTitle(getString(R.string.character_code_select_please));
        menuDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ReportDataTask extends AsyncTask<long[], Integer, List<Entry>> {

        @Override
        protected List<Entry> doInBackground(long[]... longs) {
            long[] startEndDate = longs[0];
            Context context = getApplicationContext();
            EntryDataManager entryManager = new EntryDataManager(context);
            boolean isShowBalanceCarryForward = SharedPreferencesManager.getBalanceCarryForward(context);
            List<Entry> resultEntries = new ArrayList<>();
            List<Entry> entries = entryManager.findAll(startEndDate, false);

            Entry incomeSection = new Entry();
            incomeSection.viewType = CommonEntryRecyclerAdapter.VIEW_ITEM_HEADER;
            incomeSection.reasonName = context.getString(R.string.Income);
            Entry expenseSection = new Entry();
            expenseSection.viewType = CommonEntryRecyclerAdapter.VIEW_ITEM_HEADER;
            expenseSection.reasonName = context.getString(R.string.Expense);

            String totalString = context.getString(R.string.total);
            Entry incomeSum = new Entry();
            incomeSum.viewType = CommonEntryRecyclerAdapter.VIEW_ITEM_REPORT_TOTAL;
            incomeSum.reasonName = totalString;
            Entry expenseSum = new Entry();
            expenseSum.viewType = CommonEntryRecyclerAdapter.VIEW_ITEM_REPORT_TOTAL;
            expenseSum.reasonName = totalString;
//            expenseSum.isExpense = true;

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

            resultEntries.add(new Entry());
            Entry topBalance = new Entry();
            topBalance.reasonName = context.getString(R.string.Balance);
            if (isShowBalanceCarryForward) {
                topBalance.price = entryManager.findSumBalance(startEndDate[1]);
                topBalance.reasonName += context.getString(R.string.balance_carry_forward_view);
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

            resultEntries.add(new Entry());
            // 表示データはここから
            resultEntries.add(incomeSection);
            resultEntries.add(incomeSum);
            for (Map.Entry<Long, Entry> entry : incomeSortList) {
                resultEntries.add(entry.getValue());
            }

            resultEntries.add(new Entry());

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

            // To export CSV file...
            Calendar endCal = Calendar.getInstance();
            endCal.setTimeInMillis(mStartEndDate[1]);
            endCal.add(Calendar.DATE, -1);
            mStartEndDate[1] = endCal.getTimeInMillis();
            DataExportManager exportManager = new DataExportManager(
                    ProfitLossExportActivity.this,
                    mDefaultCharCode, mStartEndDate, result);
            exportManager.export();

            mStartEndDate = getIntent().getLongArrayExtra(KEY_TARGET_START_END_DATE);
        }
    }
}
