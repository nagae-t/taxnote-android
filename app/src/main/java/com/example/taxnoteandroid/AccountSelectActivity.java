package com.example.taxnoteandroid;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.KeyboardUtil;
import com.example.taxnoteandroid.dataManager.AccountDataManager;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ListviewFooterBinding;
import com.example.taxnoteandroid.databinding.RowAccountCellBinding;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Entry;
import com.example.taxnoteandroid.model.Project;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AccountSelectActivity extends AppCompatActivity {

    private static final String EXTRA_ISEXPENSE = "EXTRA_ISEXPENSE";

    private static final int TYPE_ACCOUNT = 0;
    private static final int TYPE_FOOTER = 1;

    public boolean isExpense;

    private AccountDataManager accountDataManager = new AccountDataManager(this); // 2017/01/30 E.Nozaki
    private MyRecyclerViewAdapter adapter; // 2017/01/30 E.Nozaki
    private List<Account> accountList = null; // 2017/01/30 E.Nozaki

    public static Intent createIntent(Context context, boolean isExpense) {
        Intent i = new Intent(context, AccountSelectActivity.class);
        i.putExtra(EXTRA_ISEXPENSE, isExpense);
        return i;
    }

    //--------------------------------------------------------------//
    //    -- Intent --
    //--------------------------------------------------------------//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_select);

        setIntentData();
        setTitle();
        setAccountList();
    }

    private void setIntentData() {

        Intent intent = getIntent();
        isExpense = intent.getBooleanExtra(EXTRA_ISEXPENSE, false);
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

        // Adapter
        adapter = new MyRecyclerViewAdapter();

        // RecyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.account_list);
        recyclerView.addItemDecoration(new DividerDecoration(this));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Attach recyclerView to ItemTouchHelper so that you can drag and drop the items in order to change the order.
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback();
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);
    }

    //--------------------------------------------------------------//
    //    -- Rename --
    //--------------------------------------------------------------//

    private void renameAccount(final Account account, final int position) {

        // Check if Entry data has this account already
        EntryDataManager entryDataManager = new EntryDataManager(AccountSelectActivity.this);
        Entry entry = entryDataManager.hasAccountInEntryData(account);

        if (entry != null) {

            // Show the rename reason help message
            new AlertDialog.Builder(this)
                    .setTitle(account.name)
                    .setMessage(getResources().getString(R.string.help_rename_category_message))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            showRenameAccountDialog(account, position);
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
        } else {
            showRenameAccountDialog(account, position);
        }
    }

    private void showRenameAccountDialog(final Account account, final int position) {

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

                        KeyboardUtil.hideKeyboard(AccountSelectActivity.this, editText); // 2017/01/30 E.Nozaki Hide software keyboard.

                        accountDataManager.updateName(account.id, newName);

                        Account oldAccount = adapter.getItem(position); // 2017/01/30 E.Nozaki accountListAdapter.getItem(position);
                        if (oldAccount != null) {
                            oldAccount.name = newName;
                            adapter.onAccountDataManagerChanged(); // 2017/01/30 E.Nozaki accountListAdapter.notifyDataSetChanged();

                            DialogManager.showToast(AccountSelectActivity.this, newName);
                        }

                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText editText = (EditText) textInputView.findViewById(R.id.edit);
                        KeyboardUtil.hideKeyboard(AccountSelectActivity.this, editText); // 2017/01/30 E.Nozaki Hide software keyboard.
                    }
                })
                .show();

        KeyboardUtil.showKeyboard(AccountSelectActivity.this, textInputView); // 2017/01/30 E.Nozaki Show software keyboard.
    }


    //--------------------------------------------------------------//
    //    -- Delete --
    //--------------------------------------------------------------//

    private void deleteAccount(final Account account) {

        // Check if Entry data has this account already
        EntryDataManager entryDataManager = new EntryDataManager(AccountSelectActivity.this);
        Entry entry = entryDataManager.hasAccountInEntryData(account);

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
                            adapter.onAccountDataManagerChanged();
                            String message = account.name + getResources().getString(R.string.delete_done_after_title);
                            DialogManager.showToast(AccountSelectActivity.this, message);
                        }

                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), null)
                .show();
    }

    //--------------------------------------------------------------//
    //    -- Adapter for Recycler --
    //--------------------------------------------------------------//

    private class MyRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public MyRecyclerViewAdapter() {
            onAccountDataManagerChanged();
        }

        @Override
        public int getItemViewType(int position) {
            if (position < accountList.size()) {
                return TYPE_ACCOUNT;
            } else {
                return TYPE_FOOTER;
            }
        }

        private Account getItem(int position) {
            if (accountList != null && position < accountList.size()) {
                return accountList.get(position);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            MyViewHolder h = (MyViewHolder) holder;
            h.setAccount(position, getItem(position));
        }

        @Override
        public int getItemCount() {
            if (accountList != null) {
                return accountList.size() + 1; // Add 1 for footer.
            }
            return 1;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int type) {
            try {
                View view = null;

                if (type == TYPE_ACCOUNT) {
                    view = LayoutInflater.from(AccountSelectActivity.this).inflate(R.layout.row_account_cell, null);
                } else if (type == TYPE_FOOTER) {
                    view = LayoutInflater.from(AccountSelectActivity.this).inflate(R.layout.listview_footer, null);
                }

                view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
                MyViewHolder holder = new MyViewHolder(view);

                return holder;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        public void onAccountDataManagerChanged() {
            Log.d(this.getClass().getSimpleName() + ":435", "onAccountDataManagerChanged() が呼ばれた。");
            accountList = accountDataManager.findAllWithIsExpense(isExpense, AccountSelectActivity.this);
            this.notifyDataSetChanged();
        }
    }

    //--------------------------------------------------------------//
    // ItemTouchHelperCallback for RecyclerView
    //--------------------------------------------------------------//

    public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int flags_drag = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT; // Catch actions for moving an item toward up, down, left and right.
            int flags_swipe = 0; // Ignore actions for swiping an item.
            return makeMovementFlags(flags_drag, flags_swipe);
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return false;
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {

            int position_from = viewHolder.getAdapterPosition();
            int position_to = target.getAdapterPosition();

            Log.d(getClass().getSimpleName(), "---------------------------------");
            Log.d(getClass().getSimpleName(), "position_from = " + position_from);
            Log.d(getClass().getSimpleName(), "position_to = " + position_to);

            int size = accountList.size();

            if (position_from < 0 || size <= position_from ||
                    position_to < 0 || size <= position_to) {
                return false;
            }

            if (position_from == position_to) {
                return false;
            }

            ArrayList<Account> list = new ArrayList<Account>();

            for (int i = 0; i < size; i++) {
                if (i == position_to) list.add(accountList.get(position_from));
                if (i != position_from) list.add(accountList.get(i));
            }

            accountList = list;

            //TODO ここ聞いてないのを修正
            for (int i = 0; i < size; i++) {
                accountDataManager.updateOrder(accountList.get(i).id, i); // Update database
            }

            adapter.notifyItemMoved(position_from, position_to);

            return true;
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder holder, int action) {

            super.onSelectedChanged(holder, action);

            try {
                if (holder != null) {
                    if (action == ItemTouchHelper.ACTION_STATE_DRAG) {
                        holder.itemView.setBackgroundColor(Color.LTGRAY);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void clearView(RecyclerView view, RecyclerView.ViewHolder holder) {

            super.clearView(view, holder);

            try {
                if (holder != null) {
                    holder.itemView.setBackgroundColor(Color.TRANSPARENT);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            // Do nothing here since swiping action is not supported by this app.
        }
    }

    //----------------------------------------------------------------------------
    // ViewHolder for RecyclerView
    //----------------------------------------------------------------------------

    public class MyViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener {

        public int position;
        public Account account;

        public MyViewHolder(View view) {
            super(view);
        }

        public void setAccount(final int position, final Account account) {
            try {
                this.position = position;
                this.account = account;

                Object obj = DataBindingUtil.bind(this.itemView);

                if (obj instanceof RowAccountCellBinding) {
                    RowAccountCellBinding binding = (RowAccountCellBinding) obj;
                    binding.title.setText(account.name);
                    this.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onClick_Account(view);
                        }
                    });

                    final PopupMenu popup = new PopupMenu(AccountSelectActivity.this, binding.menuRight);
                    popup.getMenuInflater().inflate(R.menu.menu_category_right, popup.getMenu());
                    popup.setOnMenuItemClickListener(this);
                    binding.menuRight.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            popup.show();
                        }
                    });
                } else if (obj instanceof ListviewFooterBinding) {
                    this.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onClick_Footer(view);
                        }
                    });
                    ListviewFooterBinding binding = (ListviewFooterBinding) obj;
                    binding.text.setText(getResources().getString(R.string.list_view_add_reason));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onClick_Account(View view) {
            Account account = adapter.getItem(position);
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

        public void onClick_Footer(View view) {
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

                            KeyboardUtil.hideKeyboard(AccountSelectActivity.this, editText); // 2017/01/30 E.Nozaki Hide software keyboard.

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
                                adapter.onAccountDataManagerChanged();
                                dialogInterface.dismiss();

                                DialogManager.showToast(context, newName);
                            }
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            EditText editText = (EditText) textInputView.findViewById(R.id.edit);
                            KeyboardUtil.hideKeyboard(AccountSelectActivity.this, editText); // 2017/01/30 E.Nozaki Hide software keyboard.
                        }
                    })
                    .show();

            KeyboardUtil.showKeyboard(AccountSelectActivity.this, textInputView); // 2017/01/30 E.Nozaki Show software keyboard.

        }


        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {

                case R.id.reorder:
                    showHowToReorderMessage();
                    break;

                case R.id.rename:
                    renameAccount(account, position);
                    break;

                case R.id.delete:
                    deleteAccount(account);
                    break;
            }

            return true;
        }
    }

    //--------------------------------------------------------------//
    //    -- Help --
    //--------------------------------------------------------------//

    private void showHowToReorderMessage() {

        new AlertDialog.Builder(AccountSelectActivity.this)
                .setTitle(getString(R.string.reorder_how_to_title))
                .setMessage(getString(R.string.reorder_how_to_message))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }
}
