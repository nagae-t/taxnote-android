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
import com.example.taxnoteandroid.databinding.ActivityEntryCommonBinding;
import com.example.taxnoteandroid.model.Entry;

/**
 * Created by b0ne on 2017/02/25.
 */

public class HistoryListDataActivity extends AppCompatActivity {

    private ActivityEntryCommonBinding binding;
    private CommonEntryRecyclerAdapter mEntryAdapter;
    private EntryDataManager mEntryManager;

    private static final String KEY_DATETIME_NAME = "datetime_name";
    private static final String KEY_REASON_NAME = "reason_name";

    public static void start(Context context, String datetimeName, String reasonName) {
        Intent intent = new Intent(context, HistoryListDataActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_DATETIME_NAME, datetimeName);
        intent.putExtra(KEY_REASON_NAME, reasonName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_entry_common);
        binding.entries.setLayoutManager(new LinearLayoutManager(this));
        binding.entries.addItemDecoration(new DividerDecoration(this));

        mEntryManager = new EntryDataManager(this);
        mEntryAdapter = new CommonEntryRecyclerAdapter(this);

        Intent receiptIntent = getIntent();
        String datetimeName = receiptIntent.getStringExtra(KEY_DATETIME_NAME);
        String reasonName = receiptIntent.getStringExtra(KEY_REASON_NAME);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(datetimeName + " " + reasonName);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mEntryAdapter.setOnItemClickListener(new CommonEntryRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Entry entry) {
                EntryEditActivity.start(getApplicationContext(), entry);
            }
        });
        binding.entries.setAdapter(mEntryAdapter);
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
