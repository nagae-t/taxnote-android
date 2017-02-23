package com.example.taxnoteandroid;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.example.taxnoteandroid.databinding.ActivitySearchEntryBinding;


/**
 * Created by b0ne on 2017/02/23.
 */

public class SearchEntryActivity extends AppCompatActivity {

    private ActivitySearchEntryBinding binding;

    private SearchView mSearchView;

    public static void start(Context context) {
        Intent intent = new Intent(context, SearchEntryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_entry);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private SearchView.OnQueryTextListener onQueryText = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
//            queryTextSubmit();

            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
//            switchSearchToolView(newText);

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
}
