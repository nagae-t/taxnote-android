package com.example.taxnoteandroid;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.dataManager.AccountDataManager;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ListviewFooterBinding;
import com.example.taxnoteandroid.databinding.RowAccountCellBinding;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Entry;
import com.example.taxnoteandroid.model.Project;

import java.util.List;
import java.util.UUID;

public class AccountSelectActivity extends AppCompatActivity {

    private static final String EXTRA_ISEXPENSE = "EXTRA_ISEXPENSE";
    public boolean isExpense;
    private AccountSelectActivity.ListAdapter accountListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_select);

        setIntentData();
        setTitle();
        setAccountList();
    }


    //--------------------------------------------------------------//
    //    -- Intent --
    //--------------------------------------------------------------//

    public static Intent createIntent(Context context, boolean isExpense) {
        Intent i = new Intent(context, AccountSelectActivity.class);
        i.putExtra(EXTRA_ISEXPENSE, isExpense);
        return i;
    }

    private void setIntentData() {

        Intent intent   = getIntent();
        isExpense       = intent.getBooleanExtra(EXTRA_ISEXPENSE, false);
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

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.account_list);

        // Get list
        AccountDataManager accountDataManager = new AccountDataManager(this);
        List<Account> accounts = accountDataManager.findAllWithIsExpense(isExpense, this);

        accountListAdapter = new ListAdapter(this);
        accountListAdapter.addAll(accounts);
        accountListAdapter.setOnItemClickRecyclerAdapterListener(new OnItemClickRecyclerAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {

                Account account = accountListAdapter.getItem(position);
                String uuid = SharedPreferencesManager.getUuidForCurrentProject(AccountSelectActivity.this);

                ProjectDataManager projectDataManager = new ProjectDataManager(AccountSelectActivity.this);
                Project project = projectDataManager.findByUuid(uuid);

                if (isExpense) {
                    project.accountUuidForExpense = account.uuid;
                    projectDataManager.updateAccountUuidForExpense(project);
                } else {
                    project.accountUuidForIncome = account.uuid;
                    projectDataManager.updateAccountUuidForIncome(project);
                }

                DialogManager.showToast(AccountSelectActivity.this, account.name);
                finish();
            }
        });

        recyclerView.addItemDecoration(new DividerDecoration(this));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(accountListAdapter);
    }

    class ListAdapter extends FooterRecyclerArrayAdapter<Account> {

        private final AccountDataManager accountDataManager;

        private OnItemClickRecyclerAdapterListener onItemClickRecyclerAdapterListener;

        public ListAdapter(Context context) {
            accountDataManager = new AccountDataManager(context);
        }

        @Override
        protected BindingHolder<ViewDataBinding> onCreateItemViewHolder(ViewGroup parent, int viewType) {
            return new BindingHolder<>(parent.getContext(), parent, R.layout.row_account_cell);
        }

        @Override
        protected void onBindItemViewHolder(BindingHolder<ViewDataBinding> holder, final int position) {
            RowAccountCellBinding binding = (RowAccountCellBinding) holder.binding;

            final Account account = getItem(position);

            final View rootView = binding.getRoot();
            if (onItemClickRecyclerAdapterListener != null) {
                rootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onItemClickRecyclerAdapterListener.onItemClick(rootView, position);

                    }
                });
            }

            binding.title.setText(account.name);

            final PopupMenu popup = new PopupMenu(rootView.getContext(), binding.menuRight);

            popup.getMenuInflater().inflate(R.menu.menu_category_right, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {

                        case R.id.rename:
                            renameAccount(account, position);
                            break;

                        case R.id.delete:
                            deleteAccount(account);
                            break;
                    }
                    return true;
                }
            });

            binding.menuRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popup.show();
                }
            });
        }

        private void deleteAccount(final Account account) {

            // Check if Entry data has this account already
            EntryDataManager entryDataManager   = new EntryDataManager(AccountSelectActivity.this);
            Entry entry                         = entryDataManager.hasAccountInEntryData(account);

            if (entry != null) {

                // Show error message
                new AlertDialog.Builder(AccountSelectActivity.this)
                        .setTitle(getResources().getString(R.string.Error))
                        .setMessage(getResources().getString(R.string.using_this_account_in_entry_already))
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            // Confirm dialog
            new AlertDialog.Builder(AccountSelectActivity.this)
                    .setTitle(account.name)
                    .setMessage(getResources().getString(R.string.delete_confirm_message))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            long deleted = accountDataManager.delete(account.id);

                            if (deleted != 0) {

                                remove(account);

                                String message = account.name + getResources().getString(R.string.delete_done_after_title);
                                DialogManager.showToast(AccountSelectActivity.this, message);
                            }

                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.cancel), null)
                    .show();
        }

        @Override
        protected void onBindFooterItemViewHolder(BindingHolder<ViewDataBinding> holder, int position) {
            ListviewFooterBinding binding = (ListviewFooterBinding) holder.binding;
            binding.text.setText(getResources().getString(R.string.list_view_add_reason));
            binding.text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Context context = AccountSelectActivity.this;
                    final View textInputView = LayoutInflater.from(context).inflate(R.layout.dialog_text_input, null);
                    new AlertDialog.Builder(context)
                            .setView(textInputView)
                            .setTitle(getResources().getString(R.string.list_view_add_reason))
                            .setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    EditText editText = (EditText) textInputView.findViewById(R.id.edit);
                                    String newName = editText.getText().toString();

                                    // Check empty
                                    if (newName.isEmpty()) {
                                        return;
                                    }

                                    ProjectDataManager projectDataManager = new ProjectDataManager(context);
                                    Project project = projectDataManager.findCurrentProjectWithContext(context);

                                    AccountDataManager accountDataManager = new AccountDataManager(AccountSelectActivity.this);
                                    List<Account> accountList = accountDataManager.findAllWithIsExpense(isExpense, context);

                                    Account account = new Account();
                                    account.name = newName;
                                    account.uuid = UUID.randomUUID().toString();

                                    if (accountList.isEmpty()) {
                                        account.order = 0;
                                    } else {
                                        account.order = accountList.get(accountList.size() - 1).order + 1;
                                    }
                                    account.isExpense = isExpense;
                                    account.project = project;

                                    long id = accountDataManager.save(account);

                                    // Success
                                    if (id != -1) {

                                        account.id = id;
                                        add(account);
                                        dialogInterface.dismiss();

                                        DialogManager.showToast(context,newName);
                                    }
                                }
                            })
                            .setNegativeButton(getResources().getString(R.string.cancel), null)
                            .show();
                }
            });
        }

        @Override
        public int getFooterLayoutId() {
            return R.layout.listview_footer;
        }

        public void setOnItemClickRecyclerAdapterListener(OnItemClickRecyclerAdapterListener onItemClickRecyclerAdapterListener) {
            this.onItemClickRecyclerAdapterListener = onItemClickRecyclerAdapterListener;
        }
    }


    //--------------------------------------------------------------//
    //    -- Rename --
    //--------------------------------------------------------------//

    private void renameAccount(final Account account, final int position) {

        final Context context = AccountSelectActivity.this;
        final View textInputView = LayoutInflater.from(context).inflate(R.layout.dialog_text_input, null);

        final EditText editText = (EditText) textInputView.findViewById(R.id.edit);
        editText.setText(account.name);

        new AlertDialog.Builder(context)
                .setView(textInputView)
                .setTitle(getResources().getString(R.string.list_view_rename))
                .setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String newName = editText.getText().toString();

                        AccountDataManager accountDataManager = new AccountDataManager(AccountSelectActivity.this);
                        accountDataManager.updateName(account.id, newName);

                        Account oldAccount = accountListAdapter.getItem(position);
                        if (oldAccount != null) {
                            oldAccount.name = newName;
                            accountListAdapter.notifyDataSetChanged();

                                DialogManager.showToast(AccountSelectActivity.this, newName);
                        }

                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), null)
                .show();
    }
}