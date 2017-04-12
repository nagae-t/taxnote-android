package com.example.taxnoteandroid;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.taxnoteandroid.dataManager.RecurringDataManager;
import com.example.taxnoteandroid.databinding.ActivityEntryCommonBinding;
import com.example.taxnoteandroid.model.Recurring;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by b0ne on 2017/04/11.
 */

public class InputRecurringListActivity extends DefaultCommonActivity {

    private ActivityEntryCommonBinding binding;
    private RecurringDataManager mRecurringDm;
    private RecurringRecyclerAdapter mRecyclerAdapter;

    public static void start(Context context) {
        Intent intent = new Intent(context, InputRecurringListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_entry_common);
        binding.entries.setLayoutManager(new LinearLayoutManager(this));
        binding.entries.addItemDecoration(new DividerDecoration(this));
        binding.refreshLayout.setEnabled(false);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mRecurringDm = new RecurringDataManager(this);
        loadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_input_recurring, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_add_expense:
                InputRecurringEditActivity.start(this, true);
                break;
            case R.id.action_add_income:
                InputRecurringEditActivity.start(this, false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadData() {
        if (mRecyclerAdapter != null) {
            mRecyclerAdapter.clearAllToNotifyData();
        }
        mRecyclerAdapter = new RecurringRecyclerAdapter(this);
        new RecurringDataTask().execute(0);
    }

    private class RecurringDataTask extends AsyncTask<Integer, Integer, List<Recurring>> {

        @Override
        protected List<Recurring> doInBackground(Integer... integers) {
            List<Recurring> recurringData = new ArrayList<>();
            List<Recurring> recurrings = mRecurringDm.findCurrentAll();
            if (recurrings == null || recurrings.size() == 0) return recurringData;

            String[] recDateList = mRecurringDm.getDesignatedDateList();
            Map<String, List<Recurring>> map = new LinkedHashMap<>();

            // 繰り返し日付でグルーピング
            for (Recurring _rec : recurrings) {
                String dateString = recDateList[Integer.valueOf(_rec.dateIndex+"")];
                List<Recurring> recList = new ArrayList<>();
                if (!map.containsKey(dateString)) {
                    recList.add(_rec);
                } else {
                    recList = map.get(dateString);
                    recList.add(_rec);
                }
                map.put(dateString, recList);
            }

            for (Map.Entry<String, List<Recurring>> e : map.entrySet()) {
                Recurring headerItem = new Recurring();
                headerItem.titleName = e.getKey();
                headerItem.viewType = RecurringRecyclerAdapter.VIEW_ITEM_HEADER;
                recurringData.add(headerItem);

                for (Recurring _rec : e.getValue()) {
                    _rec.viewType = RecurringRecyclerAdapter.VIEW_ITEM_CELL;
                    recurringData.add(_rec);
                }
            }

            return recurringData;
        }

        @Override
        protected void onPostExecute(List<Recurring> result) {
            if (result == null || result.size() == 0) return;

            mRecyclerAdapter.setItems(result);
            mRecyclerAdapter.setOnItemClickListener(new RecurringRecyclerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position, Recurring item) {

                }
            });
            binding.entries.setAdapter(mRecyclerAdapter);
        }
    }

}
