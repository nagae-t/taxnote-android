package com.example.taxnoteandroid;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;

import com.example.taxnoteandroid.databinding.ActivitySearchEntryBinding;


/**
 * Created by b0ne on 2017/02/23.
 */

public class SearchEntryActivity extends AppCompatActivity {

    private ActivitySearchEntryBinding binding;

    public static void start(Context context) {
        Intent intent = new Intent(context, SearchEntryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_entry);
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

        return super.onCreateOptionsMenu(menu);
    }
}
