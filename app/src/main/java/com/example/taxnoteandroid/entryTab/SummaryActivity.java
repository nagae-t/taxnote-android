package com.example.taxnoteandroid.entryTab;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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

import com.example.taxnoteandroid.DefaultCommonActivity;
import com.example.taxnoteandroid.DividerDecoration;
import com.example.taxnoteandroid.Library.BroadcastUtil;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.KeyboardUtil;
import com.example.taxnoteandroid.Library.taxnote.TNApiModel;
import com.example.taxnoteandroid.R;
import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.SummaryDataManager;
import com.example.taxnoteandroid.databinding.ListviewFooterBinding;
import com.example.taxnoteandroid.databinding.RowSummaryCellBinding;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason;
import com.example.taxnoteandroid.model.Summary;

import org.parceler.Parcels;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SummaryActivity extends DefaultCommonActivity {

    private static final int TYPE_ACCOUNT = 0;
    private static final int TYPE_FOOTER = 1;

    private static final String EXTRA_IS_EXPENSE = "isExpense";
    private static final String EXTRA_DATE = "date";

    public boolean isExpense;
    public Account account;
    public Reason reason;
    public long date;

    private TNApiModel mApiModel;

    private SummaryDataManager summaryDataManager = new SummaryDataManager(this);  // 2017/01/30 E.Nozaki
    private MyRecyclerViewAdapter adapter; // 2017/01/30 E.Nozaki
    private List<Summary> summaryList = null; // 2017/01/30 E.Nozaki

    public static Intent createIntent(Context context, boolean isExpense, long date, Account account, Reason reason) {

        Intent i = new Intent(context, SummaryActivity.class);

        i.putExtra(EXTRA_IS_EXPENSE, isExpense);
        i.putExtra(Account.class.getName(), Parcels.wrap(account));
        i.putExtra(Reason.class.getName(), Parcels.wrap(reason));
        i.putExtra(EXTRA_DATE, date);

        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        mApiModel = new TNApiModel(this);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setIntentData();
        setTitle(reason.name);
        setNextButton();
        setSummaryList();

        DialogManager.showSelectSummaryMessage(SummaryActivity.this);
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

    //--------------------------------------------------------------//
    //    -- Intent --
    //--------------------------------------------------------------//

    private void setIntentData() {

        Intent intent = getIntent();

        isExpense = intent.getBooleanExtra(EXTRA_IS_EXPENSE, false);
        date = intent.getLongExtra(EXTRA_DATE, 0);
        account = Parcels.unwrap(intent.getParcelableExtra(Account.class.getName()));
        reason = Parcels.unwrap(intent.getParcelableExtra(Reason.class.getName()));
    }

    private void setNextButton() {

        View next = findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startInputDataActivity(null);
            }
        });
    }

    //--------------------------------------------------------------//
    //    -- Summary List --
    //--------------------------------------------------------------//

    private void setSummaryList() {
        // Adapter

        adapter = new MyRecyclerViewAdapter();

        // RecyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.summary_list);
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

    private void renameSummary(final Summary summary, final int position) {

        final Context context = SummaryActivity.this;
        final View textInputView = LayoutInflater.from(context).inflate(R.layout.dialog_text_input, null);

        final EditText editText = (EditText) textInputView.findViewById(R.id.edit);
        editText.setText(summary.name);

        new AlertDialog.Builder(context)
                .setView(textInputView)
                .setTitle(getResources().getString(R.string.list_view_rename))
                .setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String newName = editText.getText().toString();

                        KeyboardUtil.hideKeyboard(SummaryActivity.this, editText); // 2017/01/24 E.Nozaki Hide software keyboard.

                        SummaryDataManager summaryDataManager = new SummaryDataManager(SummaryActivity.this);
                        summaryDataManager.updateName(summary.id, newName);

                        Summary oldSummary = adapter.getItem(position);
                        if (oldSummary != null) {
                            oldSummary.name = newName;
                            adapter.onSummaryDataManagerChanged();
                            DialogManager.showToast(SummaryActivity.this, newName);
                        }

                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText editText = (EditText) textInputView.findViewById(R.id.edit);
                        KeyboardUtil.hideKeyboard(SummaryActivity.this, editText); // 2017/01/24 E.Nozaki Hide software keyboard.
                    }
                })
                .show();

        KeyboardUtil.showKeyboard(SummaryActivity.this, textInputView); // 2017/01/30 E.Nozaki Show software keyboard.

    }

    //--------------------------------------------------------------//
    //    -- Delete --
    //--------------------------------------------------------------//

    private void deleteSummary(final Summary summary) {

        // Confirm dialog
        new AlertDialog.Builder(SummaryActivity.this)
                .setTitle(summary.name)
                .setMessage(getResources().getString(R.string.delete_confirm_message))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        summaryDataManager.updateSetDeleted(summary.uuid, mApiModel);
                        adapter.onSummaryDataManagerChanged();
                        String message = summary.name + getResources().getString(R.string.delete_done_after_title);
                        DialogManager.showToast(SummaryActivity.this, message);

                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), null)
                .show();
    }

    //--------------------------------------------------------------//
    //    -- View Transition --
    //--------------------------------------------------------------//

    private void startInputDataActivity(Summary summary) {
        startActivityForResult(InputDataActivity.createIntent(SummaryActivity.this, isExpense, date, account, reason, summary), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // startActivityForResultで呼び出したActivityの処理がどうだったのかを確認する
        // resultCodeがInputDataActivityのsetResultでセットした値
        if (requestCode == 1 && resultCode == RESULT_OK) {
            BroadcastUtil.sendReloadReport(this);
            finish();
        }
    }

    //--------------------------------------------------------------//
    // Adapter for Recycler
    //--------------------------------------------------------------//

    private class MyRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public MyRecyclerViewAdapter() {
            onSummaryDataManagerChanged();
        }

        @Override
        public int getItemViewType(int position) {
            if (position < summaryList.size()) {
                return TYPE_ACCOUNT;
            } else {
                return TYPE_FOOTER;
            }
        }

        private Summary getItem(int position) {
            if (summaryList != null && position < summaryList.size()) {
                return summaryList.get(position);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            MyViewHolder h = (MyViewHolder) holder;
            h.setSummary(position, getItem(position));
        }

        @Override
        public int getItemCount() {
            if (summaryList != null) {
                return summaryList.size() + 1; // Add 1 for footer.
            }
            return 1;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int type) {
            try {
                View view = null;

                if (type == TYPE_ACCOUNT) {
                    view = LayoutInflater.from(SummaryActivity.this).inflate(R.layout.row_summary_cell, null);
                } else if (type == TYPE_FOOTER) {
                    view = LayoutInflater.from(SummaryActivity.this).inflate(R.layout.listview_footer, null);
                }

                view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
                MyViewHolder holder = new MyViewHolder(view);

                return holder;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        public void onSummaryDataManagerChanged() {
            Log.d(this.getClass().getSimpleName() + ":435", "onAccountDataManagerChanged() が呼ばれた。");
            summaryList = summaryDataManager.findAllWithReason(reason);
            this.notifyDataSetChanged();
        }
    }

    //--------------------------------------------------------------//
    // ItemTouchHelperCallback
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

            int size = summaryList.size();

            if (position_from < 0 || size <= position_from ||
                    position_to < 0 || size <= position_to) {
                return false;
            }

            if (position_from == position_to) {
                return false;
            }

            Collections.swap(summaryList, position_from, position_to);
            adapter.notifyItemMoved(position_from, position_to);
            for (int i = 0; i < size; i++) {
                Summary summary = summaryList.get(i);
                summaryDataManager.updateOrder(summary.id, i); // Update database

                mApiModel.updateSummary(summary.uuid, null);
            }

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
        public Summary summary;

        public MyViewHolder(View view) {
            super(view);
        }

        public void setSummary(final int position, final Summary summary) {
            try {
                this.position = position;
                this.summary = summary;

                Object obj = DataBindingUtil.bind(this.itemView);

                if (obj instanceof RowSummaryCellBinding) {
                    RowSummaryCellBinding binding = (RowSummaryCellBinding) obj;
                    binding.title.setText(summary.name);
                    this.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onClick_Summary(view);
                        }
                    });

                    final PopupMenu popup = new PopupMenu(SummaryActivity.this, binding.menuRight);
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

        public void onClick_Summary(View view) {
            startInputDataActivity(summary);
        }

        public void onClick_Footer(View view) {
            final Context context = SummaryActivity.this;
            final View textInputView = LayoutInflater.from(context).inflate(R.layout.dialog_text_input, null);
            new AlertDialog.Builder(context)
                    .setView(textInputView)
                    .setTitle(getResources().getString(R.string.list_view_add_reason))
                    .setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            EditText editText = (EditText) textInputView.findViewById(R.id.edit);
                            String newName = editText.getText().toString();

                            KeyboardUtil.hideKeyboard(SummaryActivity.this, editText); // 2017/01/30 E.Nozaki Hide software keyboard.

                            // Check empty
                            if (newName.isEmpty()) {
                                return;
                            }

                            ProjectDataManager projectDataManager = new ProjectDataManager(context);
                            Project project = projectDataManager.findCurrentProjectWithContext();

                            SummaryDataManager summaryDataManager = new SummaryDataManager(SummaryActivity.this);
                            List<Summary> summaryList = summaryDataManager.findAllWithReason(reason);

                            Summary summary = new Summary();
                            summary.reason = reason;
                            summary.name = newName;
                            summary.uuid = UUID.randomUUID().toString();

                            if (summaryList.isEmpty()) {
                                summary.order = 0;
                            } else {
                                summary.order = summaryList.get(summaryList.size() - 1).order + 1;
                            }
                            summary.project = project;

                            long id = summaryDataManager.save(summary);

                            // Success
                            if (id != -1) {

                                summary.id = id;
                                adapter.onSummaryDataManagerChanged();
                                dialogInterface.dismiss();
                                DialogManager.showToast(context, newName);

                                mApiModel.saveSummary(summary.uuid, null);
                            }
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            EditText editText = (EditText) textInputView.findViewById(R.id.edit);
                            KeyboardUtil.hideKeyboard(SummaryActivity.this, editText); // 2017/01/30 E.Nozaki Hide software keyboard.
                        }
                    })
                    .show();

            KeyboardUtil.showKeyboard(SummaryActivity.this, textInputView); // 2017/01/30 E.Nozaki Show software keyboard.

        }


        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {

                case R.id.reorder:
                    showHowToReorderMessage();
                    break;

                case R.id.rename:
                    renameSummary(summary, position);
                    break;

                case R.id.delete:
                    deleteSummary(summary);
                    break;
            }

            return true;
        }
    }

    //--------------------------------------------------------------//
    //    -- Help --
    //--------------------------------------------------------------//

    private void showHowToReorderMessage() {

        new AlertDialog.Builder(SummaryActivity.this)
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
