package com.example.taxnoteandroid.entryTab;

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

import com.example.taxnoteandroid.BindingHolder;
import com.example.taxnoteandroid.DividerDecoration;
import com.example.taxnoteandroid.FooterRecyclerArrayAdapter;
import com.example.taxnoteandroid.OnItemClickRecyclerAdapterListener;
import com.example.taxnoteandroid.R;
import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.SummaryDataManager;
import com.example.taxnoteandroid.databinding.ListviewFooterBinding;
import com.example.taxnoteandroid.databinding.RowSummaryCellBinding;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason;
import com.example.taxnoteandroid.model.Summary;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;

import org.parceler.Parcels;

import java.util.List;
import java.util.UUID;

public class SummaryActivity extends AppCompatActivity {

    private static final String EXTRA_IS_EXPENSE = "isExpense";
    private static final String EXTRA_DATE = "date";
    public boolean isExpense;
    public Account account;
    public Reason reason;
    public long date;
    private ListAdapter summaryListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        setIntentData();
        setTitle(reason.name);
        setNextButton();
        setSummaryList(reason);
    }


    //--------------------------------------------------------------//
    //    -- Intent --
    //--------------------------------------------------------------//

    public static Intent createIntent(Context context, boolean isExpense, long date, Account account, Reason reason) {

        Intent i = new Intent(context, SummaryActivity.class);

        i.putExtra(EXTRA_IS_EXPENSE, isExpense);
        i.putExtra(Account.class.getName(), Parcels.wrap(account));
        i.putExtra(Reason.class.getName(), Parcels.wrap(reason));
        i.putExtra(EXTRA_DATE, date);

        return i;
    }

    private void setIntentData() {

        Intent intent = getIntent();

        isExpense = intent.getBooleanExtra(EXTRA_IS_EXPENSE, false);
        date = intent.getLongExtra(EXTRA_DATE, 0);
        account = Parcels.unwrap(intent.getParcelableExtra(Account.class.getName()));
        reason = Parcels.unwrap(intent.getParcelableExtra(Reason.class.getName()));
    }


    //--------------------------------------------------------------//
    //    -- Summary List --
    //--------------------------------------------------------------//

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

    private void setSummaryList(Reason reason) {

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.summary_list);

        // Get summary list
        final SummaryDataManager summaryDataManager = new SummaryDataManager(this);
        final List<Summary> summaries = summaryDataManager.findAllWithReason(reason, this);

        RecyclerViewDragDropManager dragMgr = new RecyclerViewDragDropManager();

        dragMgr.setInitiateOnTouch(true);
        dragMgr.setInitiateOnMove(false);
        dragMgr.setInitiateOnLongPress(true);

        summaryListAdapter = new ListAdapter(this);
        summaryListAdapter.addAll(summaries);
        summaryListAdapter.setOnItemClickRecyclerAdapterListener(new OnItemClickRecyclerAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {

                Summary summary = summaryListAdapter.getItem(position);
                startInputDataActivity(summary);
            }
        });
        recyclerView.addItemDecoration(new DividerDecoration(this));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(summaryListAdapter);
//        recyclerView.setAdapter(dragMgr.createWrappedAdapter(summaryListAdapter));
//
//        dragMgr.attachRecyclerView(recyclerView);
    }

    class ListAdapter extends FooterRecyclerArrayAdapter<Summary> /**implements DraggableItemAdapter<BindingHolder<ViewDataBinding>>**/ {

        private final SummaryDataManager summaryDataManager;
        private OnItemClickRecyclerAdapterListener onItemClickRecyclerAdapterListener;

        public ListAdapter(Context context) {
//            setHasStableIds(true);
            summaryDataManager = new SummaryDataManager(context);
        }

        @Override
        protected BindingHolder<ViewDataBinding> onCreateItemViewHolder(ViewGroup parent, int viewType) {
            return new BindingHolder<>(parent.getContext(), parent, R.layout.row_summary_cell);
        }

        @Override
        protected void onBindItemViewHolder(BindingHolder<ViewDataBinding> holder, final int position) {
            RowSummaryCellBinding binding = (RowSummaryCellBinding) holder.binding;

            final Summary summary = getItem(position);

            final View rootView = binding.getRoot();
            if (onItemClickRecyclerAdapterListener != null) {
                rootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onItemClickRecyclerAdapterListener.onItemClick(rootView, position);
                    }
                });
            }

            binding.title.setText(summary.name);

            final PopupMenu popup = new PopupMenu(rootView.getContext(), binding.menuRight);

            popup.getMenuInflater().inflate(R.menu.menu_category_right, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {

                        case R.id.rename:
                            renameSummary(summary, position);
                            break;

                        case R.id.delete:
                            deleteSummary(summary);
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

        private void deleteSummary(final Summary summary) {

            // Confirm dialog
            new AlertDialog.Builder(SummaryActivity.this)
                    .setTitle(summary.name)
                    .setMessage(getResources().getString(R.string.delete_confirm_message))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            //@@ すでに科目を使って入力している場合どうするか 入力したデータ消さないと、削除しないようにする？
                            long deleted = summaryDataManager.delete(summary.id);
                            if (deleted != 0) {
                                remove(summary);
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

                                    ProjectDataManager projectDataManager = new ProjectDataManager(context);
                                    Project project = projectDataManager.findCurrentProjectWithContext(context);

                                    SummaryDataManager summaryDataManager = new SummaryDataManager(SummaryActivity.this);
                                    List<Summary> summaryList = summaryDataManager.findAllWithReason(reason, context);

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

                                    // @@ 保存チェック
                                    long id = summaryDataManager.save(summary);
                                    summary.id = id;

                                    add(summary);

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

//        @Override
//        public boolean onCheckCanStartDrag(BindingHolder<ViewDataBinding> holder, int position, int x, int y) {
//            return true;
//        }
//
//        @Override
//        public ItemDraggableRange onGetItemDraggableRange(BindingHolder<ViewDataBinding> holder, int position) {
//            return null;
//        }
//
//        @Override
//        public void onMoveItem(int fromPosition, int toPosition) {
//            Summary movedItem = getItem(fromPosition);
//            add(toPosition, movedItem);
//            notifyItemMoved(fromPosition, toPosition);
//        }
//
//        @Override
//        public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
//            return true;
//        }
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

                        SummaryDataManager summaryDataManager = new SummaryDataManager(SummaryActivity.this);
                        summaryDataManager.updateName(summary.id, newName);

                        Summary oldSummary = summaryListAdapter.getItem(position);
                        if (oldSummary != null) {
                            oldSummary.name = newName;
                            summaryListAdapter.notifyDataSetChanged();
                        }

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
            finish();
        }
    }
}
