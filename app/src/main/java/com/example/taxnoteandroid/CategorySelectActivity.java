package com.example.taxnoteandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.taxnoteandroid.dataManager.AccountDataManager;
import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Project;

import java.util.List;

public class CategorySelectActivity extends AppCompatActivity {

    private static final String EXTRA_ISEXPENSE = "EXTRA_ISEXPENSE";

    public static Intent createIntent(Context context, boolean isExpense) {
        Intent i = new Intent(context, CategorySelectActivity.class);
        i.putExtra(EXTRA_ISEXPENSE, isExpense);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_select);

        final boolean isExpense = getIntent().getBooleanExtra(EXTRA_ISEXPENSE, false);

        ListView listView = (ListView) findViewById(R.id.list);

        AccountDataManager accountDataManager = new AccountDataManager(this);
        List<Account> accounts = accountDataManager.findAllByIsExpense(isExpense);

        CategorySelectAdapter categorySelectAdapter = new CategorySelectAdapter(this, accounts);
        listView.setAdapter(categorySelectAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Account account = (Account) adapterView.getItemAtPosition(position);
                String uuid = SharedPreferencesManager.getUuidForCurrentProject(CategorySelectActivity.this);

                ProjectDataManager projectDataManager = new ProjectDataManager(CategorySelectActivity.this);
                Project project = projectDataManager.findByUuid(uuid);

                if (isExpense) {
                    project.accountUuidForExpense = account.uuid;
                } else {
                    project.accountUuidForIncome = account.uuid;
                }

                //@@ これで projectが更新されるの？
                projectDataManager.updateUuid(project, uuid);
                finish();
            }
        });
    }

    class CategorySelectAdapter extends ArrayAdapter<Account> {

        private LayoutInflater layoutInflater;

        public CategorySelectAdapter(Context context, List<Account> accounts) {
            super(context, 0, accounts);
            layoutInflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = layoutInflater.inflate(R.layout.row_category_select, null);

            Account account = getItem(position);

            TextView textView = (TextView) view.findViewById(R.id.category);

            textView.setText(account.name);

            return view;
        }
    }
}