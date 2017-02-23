package com.example.taxnoteandroid;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.databinding.ActivitySearchEntryBinding;
import com.example.taxnoteandroid.model.Entry;

import java.util.List;


/**
 * Created by b0ne on 2017/02/23.
 */

public class SearchEntryActivity extends AppCompatActivity {

    private ActivitySearchEntryBinding binding;
    private EntryDataManager mEntryManager;
    private SearchView mSearchView;
    private CommonEntryRecyclerAdapter mEntryAdapter;

    public static void start(Context context) {
        Intent intent = new Intent(context, SearchEntryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_entry);
        binding.entries.setLayoutManager(new LinearLayoutManager(this));
        binding.entries.addItemDecoration(new DividerDecoration(this));

        mEntryManager = new EntryDataManager(this);
        mEntryAdapter = new CommonEntryRecyclerAdapter(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mEntryAdapter.setOnItemClickListener(new CommonEntryRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Entry entry) {
                EntryEditActivity.start(getApplicationContext(), entry);
            }
        });
        binding.entries.setAdapter(mEntryAdapter);

    }

    private SearchView.OnQueryTextListener onQueryText = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            closeKeyboard(mSearchView);
            if (query.length() > 0) {
                new EntrySearchTask().execute(query);
            } else {
                mEntryAdapter.clearAllToNotifyData();
            }

            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if (newText.length() > 0) {
                new EntrySearchTask().execute(newText);
            } else {
                mEntryAdapter.clearAllToNotifyData();
            }

            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_entry, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setOnQueryTextListener(onQueryText);
        mSearchView.setFocusable(true);
        mSearchView.setIconified(false);
        mSearchView.requestFocusFromTouch();

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
        }
        return super.onOptionsItemSelected(item);
    }

    private void closeKeyboard(View view) {
        if (view == null) return;
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * 検索処理のタスク
     */
    private class EntrySearchTask extends AsyncTask<String, Integer, List<Entry>> {

        @Override
        protected List<Entry> doInBackground(String... strings) {
            String word = strings[0];
            List<Entry> result = mEntryManager.searchBy(word);

            return result;
        }

        @Override
        protected void onPostExecute(List<Entry> result) {
            if (result == null || result.size() == 0) {
                mEntryAdapter.clearAllToNotifyData();
                return;
            }

            mEntryAdapter.setItems(result);
            mEntryAdapter.notifyDataSetChanged();
        }
    }
}
