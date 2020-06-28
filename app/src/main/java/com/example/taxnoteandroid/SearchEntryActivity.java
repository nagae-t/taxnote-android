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
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.taxnoteandroid.Library.BroadcastUtil;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.taxnote.TNApiModel;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.databinding.ActivityEntryCommonBinding;
import com.example.taxnoteandroid.model.Entry;

import java.util.List;


/**
 * Created by b0ne on 2017/02/23.
 */

public class SearchEntryActivity extends DefaultCommonActivity {

    private ActivityEntryCommonBinding binding;
    private EntryDataManager mEntryManager;
    private SearchView mSearchView;
    private CommonEntryRecyclerAdapter mEntryAdapter;
    private String mSearchWord;
    private boolean mIsOnSearchSubmit = false;
    private boolean mIsCommon;
    private boolean mIsExpense;
    private long mStartTime = 0;
    private long mEndTime = 0;
    private String mReasonName;

    private TNApiModel mApiModel;

    private static final String KEY_IS_COMMON = "is_common";
    private static final String KEY_REASON_NAME = "reason_name";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_END_TIME = "end_time";
    private static final String KEY_IS_EXPENSE = "is_expense";


    public static void start(Context context) {
        Intent intent = new Intent(context, SearchEntryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_IS_COMMON, true);
        context.startActivity(intent);
    }

    public static void start(Context context, long[] startEndDate) {
        Intent intent = new Intent(context, SearchEntryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_IS_COMMON, true);
        if (startEndDate.length == 2) {
            intent.putExtra(KEY_START_TIME, startEndDate[0]);
            intent.putExtra(KEY_END_TIME, startEndDate[1]);
        }
        context.startActivity(intent);
    }

    public static void start(Context context, String reasonName) {
        Intent intent = new Intent(context, SearchEntryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_IS_COMMON, true);
        intent.putExtra(KEY_REASON_NAME, reasonName);
        context.startActivity(intent);
    }

    public static void start(Context context, long startTime, long endTime) {
        Intent intent = new Intent(context, SearchEntryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_START_TIME, startTime);
        intent.putExtra(KEY_END_TIME, endTime);
        intent.putExtra(KEY_IS_COMMON, true);
        context.startActivity(intent);
    }

    public static void start(Context context, long startTime, long endTime, String reasonName) {
        Intent intent = new Intent(context, SearchEntryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_START_TIME, startTime);
        intent.putExtra(KEY_END_TIME, endTime);
        intent.putExtra(KEY_REASON_NAME, reasonName);
        intent.putExtra(KEY_IS_COMMON, true);
        context.startActivity(intent);
    }

    public static void startWithIsExpense(Context context, long[] startEndDate,
                                          String reasonName, boolean isExpense) {
        Intent intent = new Intent(context, SearchEntryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_IS_EXPENSE, isExpense);
        intent.putExtra(KEY_REASON_NAME, reasonName);
        intent.putExtra(KEY_IS_COMMON, false);
        if (startEndDate.length == 2) {
            intent.putExtra(KEY_START_TIME, startEndDate[0]);
            intent.putExtra(KEY_END_TIME, startEndDate[1]);
        }
        context.startActivity(intent);
    }

    public static void startWithIsExpense(Context context, long startTime, long endTime,
                                          String reasonName, boolean isExpense) {
        Intent intent = new Intent(context, SearchEntryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_START_TIME, startTime);
        intent.putExtra(KEY_END_TIME, endTime);
        intent.putExtra(KEY_IS_EXPENSE, isExpense);
        intent.putExtra(KEY_REASON_NAME, reasonName);
        intent.putExtra(KEY_IS_COMMON, false);
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
        mApiModel = new TNApiModel(this);

        Intent intent = getIntent();
        mIsCommon = intent.getBooleanExtra(KEY_IS_COMMON, false);
        mIsExpense = intent.getBooleanExtra(KEY_IS_EXPENSE, false);
        mStartTime = intent.getLongExtra(KEY_START_TIME, 0);
        mEndTime = intent.getLongExtra(KEY_END_TIME, 0);
        mReasonName = intent.getStringExtra(KEY_REASON_NAME);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mEntryAdapter.setOnItemClickListener(new CommonEntryRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Entry entry) {
                EntryEditActivity.start(getApplicationContext(), entry);
            }
        });
        binding.entries.setAdapter(mEntryAdapter);

        binding.refreshLayout.setEnabled(false);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mSearchWord != null)
            execSearchTask(mSearchWord);
    }

    private SearchView.OnQueryTextListener onQueryText = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            mIsOnSearchSubmit = true;
            closeKeyboard(mSearchView);
            if (query.length() > 0) {
                mSearchWord = query;
                execSearchTask(query);
            } else {
                mEntryAdapter.clearAllToNotifyData();
            }

            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if (newText.length() > 0) {
                mSearchWord = newText;
                execSearchTask(newText);
            } else {
                mEntryAdapter.clearAllToNotifyData();
            }

            return false;
        }
    };

    private void execSearchTask(String word) {
        binding.loading.setVisibility(View.VISIBLE);
        binding.refreshLayout.setVisibility(View.GONE);
        new EntrySearchTask().execute(word);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_entry, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView)searchItem.getActionView();
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT);
        mSearchView.setLayoutParams(params);

        mSearchView.setIconifiedByDefault(true);
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setOnQueryTextListener(onQueryText);
        mSearchView.setFocusable(true);
        mSearchView.setIconified(false);
        mSearchView.requestFocusFromTouch();

        searchItem.expandActionView();

        return super.onCreateOptionsMenu(menu);
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
                break;
            case R.id.action_delete:
//                showAllDeleteDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void closeKeyboard(View view) {
        if (view == null) return;
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void showAllDeleteDialog() {
        final Context context = getApplicationContext();
        String searchTitle = getString(R.string.search)+":"+mSearchWord;

        String deleteBtnTitle = getString(R.string.delete_current_something, searchTitle);
        // Confirm dialog
        new AlertDialog.Builder(this)
                .setTitle(null)
                .setMessage(getString(R.string.delete_this_screen_data_confirm_message))
                .setPositiveButton(deleteBtnTitle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        List<Entry> dataList = mEntryAdapter.getItems();
                        for (Entry entry : dataList) {
                            if (entry.dateString == null) {
                                mEntryManager.updateSetDeleted(entry.uuid, mApiModel);
                            }
                        }
                        mEntryAdapter.clearAll();
                        mEntryAdapter.notifyDataSetChanged();

                        mApiModel.saveAllNeedSaveSyncDeletedData(null);

                        DialogManager.showToast(context, context.getString(R.string.delete_done));
                        BroadcastUtil.sendReloadReport(SearchEntryActivity.this);
                        finish();

                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    /**
     * 検索処理のタスク
     */
    private class EntrySearchTask extends AsyncTask<String, Integer, List<Entry>> {

        @Override
        protected List<Entry> doInBackground(String... strings) {
            String word = strings[0];
            List<Entry> result;
            long[] startEndDate = null;
            if (mStartTime != 0 && mEndTime != 0)
                startEndDate = new long[]{mStartTime, mEndTime};

            if (mIsCommon) {
                result = mEntryManager.searchBy(word, mReasonName, startEndDate, false);
            } else {
                result = mEntryManager.searchBy(word, mReasonName, startEndDate, mIsExpense, false);
            }
            for (Entry entry : result) {
                entry.viewType = CommonEntryRecyclerAdapter.VIEW_ITEM_CELL;
            }

            return result;
        }

        @Override
        protected void onPostExecute(List<Entry> result) {
            binding.loading.setVisibility(View.GONE);
            if (result == null || result.size() == 0) {

                mEntryAdapter.clearAllToNotifyData();

                //QQ キーボードの検索の虫眼鏡ボタンをタップした時だけメッセージをだす
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
}
