package com.example.taxnoteandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.dataManager.AccountDataManager;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.ReasonDataManager;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Reason;

import java.util.List;

public class AccountEditActivity extends DefaultCommonActivity {

    private static final String EXTRA_IS_EXPENSE    = "EXTRA_IS_EXPENSE";
    private static final String EXTRA_IS_ACCOUNT    = "EXTRA_IS_ACCOUNT";
    private static final String EXTRA_ENTRY_ID      = "EXTRA_ENTRY_ID";
    public boolean isExpense;
    public boolean isAccount;
    public long entryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_account_edit);
        setIntentData();
        setTitle();

        if (isAccount) {
            setAccountList();
        } else {
            setReasonList();
        }
    }


    //--------------------------------------------------------------//
    //    -- Intent --
    //--------------------------------------------------------------//

    public static Intent createIntent(Context context, boolean isExpense, boolean isAccount, long entryId) {

        Intent i = new Intent(context, AccountEditActivity.class);
        i.putExtra(EXTRA_IS_EXPENSE, isExpense);
        i.putExtra(EXTRA_IS_ACCOUNT, isAccount);
        i.putExtra(EXTRA_ENTRY_ID, entryId);
        return i;
    }

    private void setIntentData() {

        Intent intent   = getIntent();
        isExpense       = intent.getBooleanExtra(EXTRA_IS_EXPENSE, false);
        isAccount       = intent.getBooleanExtra(EXTRA_IS_ACCOUNT, false);
        entryId         = intent.getLongExtra(EXTRA_ENTRY_ID, 0);
    }


    //--------------------------------------------------------------//
    //    -- Display Part --
    //--------------------------------------------------------------//

    private void setTitle() {

        if (isExpense) {
            setTitle(getResources().getString(R.string.entry_tab_fragment_account));
        } else {
            setTitle(getResources().getString(R.string.entry_tab_fragment_account2));
        }
    }


    //--------------------------------------------------------------//
    //    -- Account List --
    //--------------------------------------------------------------//

    private void setAccountList() {

        final ListView listView = (ListView) findViewById(R.id.list);

        // Get account list
        AccountDataManager accountDataManager   = new AccountDataManager(this);
        List<Account> accounts                  = accountDataManager.findAllWithIsExpense(isExpense);

        CategorySelectAdapter categorySelectAdapter = new CategorySelectAdapter(this, accounts);
        listView.setAdapter(categorySelectAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Account account = (Account) (listView.getItemAtPosition(position));

                // Update account
                EntryDataManager entryDataManager   = new EntryDataManager(AccountEditActivity.this);
                long updated                        = entryDataManager.updateAccount(entryId, account);

                if (updated != 0) {

                    // Show update dialog
                    DialogManager.showToast(AccountEditActivity.this, account.name);

                    finish();
                }
            }
        });
    }

    class CategorySelectAdapter extends ArrayAdapter<Account> {

        private LayoutInflater layoutInflater;

        private CategorySelectAdapter(Context context, List<Account> accounts) {
            super(context, 0, accounts);
            layoutInflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view           = layoutInflater.inflate(R.layout.row_category_select, null);
            Account account     = getItem(position);
            TextView textView   = (TextView) view.findViewById(R.id.category);

            textView.setText(account.name);

            return view;
        }
    }


    //--------------------------------------------------------------//
    //    -- Reason List --
    //--------------------------------------------------------------//

    private void setReasonList() {

        final ListView listView = (ListView) findViewById(R.id.list);

        // Get account list
        ReasonDataManager reasonDataManager   = new ReasonDataManager(this);
        final List<Reason> reasons            = reasonDataManager.findAllWithIsExpense(isExpense, this);

        ReasonSelectAdapter reasonSelectAdapter = new ReasonSelectAdapter(this, reasons);
        listView.setAdapter(reasonSelectAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Reason reason = (Reason) (listView.getItemAtPosition(position));

                // Update account
                EntryDataManager entryDataManager   = new EntryDataManager(AccountEditActivity.this);
                long updated                        = entryDataManager.updateReason(entryId, reason);

                if (updated != 0) {

                    // Show update dialog
                    DialogManager.showToast(AccountEditActivity.this, reason.name);

                    finish();
                }
            }
        });
    }

    class ReasonSelectAdapter extends ArrayAdapter<Reason> {

        private LayoutInflater layoutInflater;

        private ReasonSelectAdapter(Context context, List<Reason> reasons) {
            super(context, 0, reasons);
            layoutInflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view           = layoutInflater.inflate(R.layout.row_category_select, null);
            Reason reason       = getItem(position);
            TextView textView   = (TextView) view.findViewById(R.id.category);

            textView.setText(reason.name);

            return view;
        }
    }
}