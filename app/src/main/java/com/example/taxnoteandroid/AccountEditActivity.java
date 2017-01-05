package com.example.taxnoteandroid;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.taxnoteandroid.dataManager.AccountDataManager;
import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ListviewFooterBinding;
import com.example.taxnoteandroid.databinding.RowAccountCellBinding;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Project;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;

import java.util.List;
import java.util.UUID;

public class AccountEditActivity extends AppCompatActivity {

    private static final String EXTRA_ISEXPENSE = "EXTRA_ISEXPENSE";
    public boolean isExpense;
    private AccountEditActivity.ListAdapter accountListAdapter;


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
        Intent i = new Intent(context, AccountEditActivity.class);
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

        RecyclerViewDragDropManager dragMgr = new RecyclerViewDragDropManager();

        dragMgr.setInitiateOnTouch(true);
        dragMgr.setInitiateOnMove(false);
        dragMgr.setInitiateOnLongPress(true);

        accountListAdapter = new AccountEditActivity.ListAdapter(this);
        accountListAdapter.addAll(accounts);
        accountListAdapter.setOnItemClickRecyclerAdapterListener(new OnItemClickRecyclerAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {

                Account account = accountListAdapter.getItem(position);
                String uuid = SharedPreferencesManager.getUuidForCurrentProject(AccountEditActivity.this);

                ProjectDataManager projectDataManager = new ProjectDataManager(AccountEditActivity.this);
                Project project = projectDataManager.findByUuid(uuid);

                if (isExpense) {
                    project.accountUuidForExpense = account.uuid;
                    projectDataManager.updateAccountUuidForExpense(project);
                } else {
                    project.accountUuidForIncome = account.uuid;
                    projectDataManager.updateAccountUuidForIncome(project);
                }

                finish();
            }
        });

        recyclerView.addItemDecoration(new DividerDecoration(this));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    class ListAdapter extends FooterRecyclerArrayAdapter<Account> {

        private final AccountDataManager accountDataManager;

        private OnItemClickRecyclerAdapterListener onItemClickRecyclerAdapterListener;

        public ListAdapter(Context context) {
            setHasStableIds(true);
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
        }

        @Override
        protected void onBindFooterItemViewHolder(BindingHolder<ViewDataBinding> holder, int position) {
            ListviewFooterBinding binding = (ListviewFooterBinding) holder.binding;
            binding.text.setText(getResources().getString(R.string.list_view_add_reason));
            binding.text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Context context = AccountEditActivity.this;
                    final View textInputView = LayoutInflater.from(context).inflate(R.layout.dialog_text_input, null);
                    new AlertDialog.Builder(context)
                            .setView(textInputView)
                            .setTitle(getResources().getString(R.string.list_view_add_reason))
                            .setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    EditText editText = (EditText) textInputView.findViewById(R.id.edit);
                                    String newName = editText.getText().toString();

                                    ProjectDataManager projectDataManager = new ProjectDataManager(context);
                                    Project project = projectDataManager.findCurrentProjectWithContext(context);

                                    AccountDataManager accountDataManager = new AccountDataManager(AccountEditActivity.this);
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

                                    // @@ 保存チェック
                                    long id = accountDataManager.save(account);
                                    account.id = id;

                                    add(account);

                                    dialogInterface.dismiss();
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

//    private void setAccountList() {
//
//        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.account_list);
//
//        // Get list
//        AccountDataManager accountDataManager = new AccountDataManager(this);
//        List<Account> accounts = accountDataManager.findAllWithIsExpense(isExpense, this);
//
//        RecyclerViewDragDropManager dragMgr = new RecyclerViewDragDropManager();
//
//        dragMgr.setInitiateOnTouch(true);
//        dragMgr.setInitiateOnMove(false);
//        dragMgr.setInitiateOnLongPress(true);
//
//        accountListAdapter = new ListAdapter(this);
//        accountListAdapter.addAll(accounts);
//        accountListAdapter.setOnItemClickRecyclerAdapterListener(new OnItemClickRecyclerAdapterListener() {
//            @Override
//            public void onItemClick(View view, int position) {
//
//                Account account = accountListAdapter.getItem(position);
//                String uuid = SharedPreferencesManager.getUuidForCurrentProject(AccountEditActivity.this);
//
//                ProjectDataManager projectDataManager = new ProjectDataManager(AccountEditActivity.this);
//                Project project = projectDataManager.findByUuid(uuid);
//
//                if (isExpense) {
//                    project.accountUuidForExpense = account.uuid;
//                    projectDataManager.updateAccountUuidForExpense(project);
//                } else {
//                    project.accountUuidForIncome = account.uuid;
//                    projectDataManager.updateAccountUuidForIncome(project);
//                }
//
//                finish();
//            }
//        });
//
//        recyclerView.addItemDecoration(new DividerDecoration(this));
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(dragMgr.createWrappedAdapter(accountListAdapter));
//
//        dragMgr.attachRecyclerView(recyclerView);
//    }
//
////    class HistoryAdapter extends RecyclerView.Adapter<BindingHolder> {
//
//    class ListAdapter extends RecyclerView.Adapter<BindingHolder> {
//
//        private final AccountDataManager accountDataManager;
//
//        private OnItemClickRecyclerAdapterListener onItemClickRecyclerAdapterListener;
//
//        public ListAdapter(Context context) {
//            setHasStableIds(true);
//            accountDataManager = new AccountDataManager(context);
//        }
//
//        @Override
//        protected BindingHolder<ViewDataBinding> onCreateItemViewHolder(ViewGroup parent, int viewType) {
//            return new BindingHolder<>(parent.getContext(), parent, R.layout.row_account_cell);
//        }
//
//        @Override
//        protected void onBindItemViewHolder(BindingHolder<ViewDataBinding> holder, final int position) {
//            RowAccountCellBinding binding = (RowAccountCellBinding) holder.binding;
//
//            final Account account = getItem(position);
//
//            final View rootView = binding.getRoot();
//            if (onItemClickRecyclerAdapterListener != null) {
//                rootView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        onItemClickRecyclerAdapterListener.onItemClick(rootView, position);
//                    }
//                });
//            }
//
//            binding.title.setText(account.name);
//        }
//
//        @Override
//        protected void onBindFooterItemViewHolder(BindingHolder<ViewDataBinding> holder, int position) {
//            ListviewFooterBinding binding = (ListviewFooterBinding) holder.binding;
//            binding.text.setText(getResources().getString(R.string.list_view_add_reason));
//            binding.text.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    final Context context = AccountSelectActivity.this;
//                    final View textInputView = LayoutInflater.from(context).inflate(R.layout.dialog_text_input, null);
//                    new AlertDialog.Builder(context)
//                            .setView(textInputView)
//                            .setTitle(getResources().getString(R.string.list_view_add_reason))
//                            .setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//
//                                    EditText editText = (EditText) textInputView.findViewById(R.id.edit);
//                                    String newName = editText.getText().toString();
//
//                                    ProjectDataManager projectDataManager = new ProjectDataManager(context);
//                                    Project project = projectDataManager.findCurrentProjectWithContext(context);
//
//                                    AccountDataManager accountDataManager = new AccountDataManager(AccountSelectActivity.this);
//                                    List<Account> accountList = accountDataManager.findAllWithIsExpense(isExpense, context);
//
//                                    Account account = new Account();
//                                    account.name = newName;
//                                    account.uuid = UUID.randomUUID().toString();
//
//                                    if (accountList.isEmpty()) {
//                                        account.order = 0;
//                                    } else {
//                                        account.order = accountList.get(accountList.size() - 1).order + 1;
//                                    }
//                                    account.isExpense = isExpense;
//                                    account.project = project;
//
//                                    // @@ 保存チェック
//                                    long id = accountDataManager.save(account);
//                                    account.id = id;
//
//                                    add(account);
//
//                                    dialogInterface.dismiss();
//                                }
//                            })
//                            .setNegativeButton(getResources().getString(R.string.cancel), null)
//                            .show();
//                }
//            });
//        }
//
//        @Override
//        public int getFooterLayoutId() {
//            return R.layout.listview_footer;
//        }
//
//        public void setOnItemClickRecyclerAdapterListener(OnItemClickRecyclerAdapterListener onItemClickRecyclerAdapterListener) {
//            this.onItemClickRecyclerAdapterListener = onItemClickRecyclerAdapterListener;
//        }
    }

}