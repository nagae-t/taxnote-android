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

import com.example.taxnoteandroid.dataManager.RecurringDataManager;
import com.example.taxnoteandroid.databinding.ActivityEntryCommonBinding;
import com.example.taxnoteandroid.model.Recurring;

import java.util.List;

/**
 * Created by b0ne on 2017/04/11.
 */

public class InputRecurringListActivity extends DefaultCommonActivity {

    private ActivityEntryCommonBinding binding;
    private RecurringDataManager mRecurringDm;
    private CommonEntryRecyclerAdapter mRecyclerAdapter;
    private List<Recurring> mDataList;

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
        mRecyclerAdapter = new CommonEntryRecyclerAdapter(this);

    }

    private class RecurringDataTask extends AsyncTask<Integer, Integer, List<Recurring>> {

        @Override
        protected List<Recurring> doInBackground(Integer... integers) {
            return null;
        }
    }

}
