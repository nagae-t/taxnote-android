package com.example.taxnoteandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

/**
 * Created by b0ne on 2017/04/11.
 */

public class InputRecurringEditActivity extends DefaultCommonActivity {

    private static final String KEY_IS_EXPENSE = "is_expense";

    public static void start(Context context, boolean isExpense) {
        Intent intent = new Intent(context, InputRecurringEditActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_IS_EXPENSE, isExpense);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        boolean isExpense = getIntent().getBooleanExtra(KEY_IS_EXPENSE, false);
        setTitle(R.string.Income);
        if (isExpense) {
            setTitle(R.string.Expense);
        }


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

}
