package com.example.taxnoteandroid.entryTab;

import android.content.Context;
import android.content.DialogInterface;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.taxnoteandroid.AccountSelectActivity;
import com.example.taxnoteandroid.BindingHolder;
import com.example.taxnoteandroid.DatePickerDialogFragment;
import com.example.taxnoteandroid.DividerDecoration;
import com.example.taxnoteandroid.FooterRecyclerArrayAdapter;
import com.example.taxnoteandroid.OnItemClickRecyclerAdapterListener;
import com.example.taxnoteandroid.R;
import com.example.taxnoteandroid.dataManager.AccountDataManager;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.ReasonDataManager;
import com.example.taxnoteandroid.databinding.FragmentEntryTabExpenseBinding;
import com.example.taxnoteandroid.databinding.ListviewFooterBinding;
import com.example.taxnoteandroid.databinding.RowListWithDetailsItemBinding;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Entry;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;


public class ExpenseInEntryTabFragment extends Fragment {

    private static final String EXTRA_IS_EXPENSE = "isExpense";

    public boolean isExpense = true;
    public long date;
    private Account account;
    private ListAdapter reasonListAdapter;
    private FragmentEntryTabExpenseBinding binding;


    public ExpenseInEntryTabFragment() {
        // Required empty public constructor
    }

    public static ExpenseInEntryTabFragment newInstance(boolean isExpense) {

        ExpenseInEntryTabFragment fragment = new ExpenseInEntryTabFragment();

        // Set value on bundle
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_IS_EXPENSE, isExpense);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set default values
        isExpense = getArguments().getBoolean(EXTRA_IS_EXPENSE);
        date = System.currentTimeMillis();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding = FragmentEntryTabExpenseBinding.inflate(inflater, container, false);

        binding.setIsExpense(isExpense);

        View view = binding.getRoot();

        setDateView();
        setAccountView(view);
        setReasonList(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        loadCurrentDate();
        loadCurrentAccount();
    }


    //--------------------------------------------------------------//
    //    -- Date Part --
    //--------------------------------------------------------------//

    private void setDateView() {

        binding.dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerDialogFragment fragment = DatePickerDialogFragment.newInstance(date, getResources().getString(R.string.entry_tab_fragment_date));
                fragment.setOnDateSetListener(new DatePickerDialogFragment.OnDateSetListener() {
                    @Override
                    public void onDateSet(Calendar calendar) {
                        date = calendar.getTimeInMillis();
                        loadCurrentDate();
                    }
                });
                fragment.show(getFragmentManager(), DatePickerDialogFragment.class.getName());
            }
        });
    }

    private void loadCurrentDate() {

        String dateString = getResources().getString(R.string.date_string_today);

        // Show the date if it is not today
        if (!DateUtils.isToday(date)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources().getString(R.string.date_string_format_to_year));
            dateString = simpleDateFormat.format(date);
        }

        ((TextView) getView().findViewById(R.id.date_text_view)).setText(dateString);
    }


    //--------------------------------------------------------------//
    //    -- Account Part --
    //--------------------------------------------------------------//

    private void setAccountView(View view) {

        view.findViewById(R.id.account_text_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(AccountSelectActivity.createIntent(getContext(), isExpense));
            }
        });
    }

    private void loadCurrentAccount() {

        AccountDataManager accountDataManager = new AccountDataManager(getContext());
        account = accountDataManager.findCurrentSelectedAccount(getContext(), isExpense);

        ((TextView) getView().findViewById(R.id.account_text_view)).setText(account.name);
    }


    //--------------------------------------------------------------//
    //    -- Reason List --
    //--------------------------------------------------------------//

    private void setReasonList(View view) {

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.reason_list);

        final ReasonDataManager reasonDataManager = new ReasonDataManager(getContext());
        final List<Reason> reasons = reasonDataManager.findAllWithIsExpense(isExpense, getContext());

        RecyclerViewDragDropManager dragMgr = new RecyclerViewDragDropManager();

        dragMgr.setInitiateOnTouch(true);
        dragMgr.setInitiateOnMove(false);
        dragMgr.setInitiateOnLongPress(true);

        reasonListAdapter = new ListAdapter(getContext());
        reasonListAdapter.addAll(reasons);
        reasonListAdapter.setOnItemClickRecyclerAdapterListener(new OnItemClickRecyclerAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                Reason reason = reasonListAdapter.getItem(position);
                startActivity(SummaryActivity.createIntent(getContext(), isExpense, date, account, reason));
            }
        });
        recyclerView.addItemDecoration(new DividerDecoration(getContext()));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(reasonListAdapter);

//        recyclerView.setAdapter(dragMgr.createWrappedAdapter(reasonListAdapter));
//        dragMgr.attachRecyclerView(recyclerView);
    }

    class ListAdapter extends FooterRecyclerArrayAdapter<Reason> /**implements DraggableItemAdapter<BindingHolder<ViewDataBinding>>**/ {

        private final ReasonDataManager reasonDataManager;
        private OnItemClickRecyclerAdapterListener onItemClickRecyclerAdapterListener;

        public ListAdapter(Context context) {
//            setHasStableIds(true);
            reasonDataManager = new ReasonDataManager(context);
        }

        @Override
        protected BindingHolder<ViewDataBinding> onCreateItemViewHolder(ViewGroup parent, int viewType) {
            return new BindingHolder<>(parent.getContext(), parent, R.layout.row_list_with_details_item);
        }

        @Override
        protected void onBindItemViewHolder(BindingHolder<ViewDataBinding> holder, final int position) {

            RowListWithDetailsItemBinding binding = (RowListWithDetailsItemBinding) holder.binding;

            final Reason reason = getItem(position);

            final View rootView = binding.getRoot();
            if (onItemClickRecyclerAdapterListener != null) {
                rootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onItemClickRecyclerAdapterListener.onItemClick(rootView, position);
                    }
                });
            }

            binding.title.setText(reason.name);

            final PopupMenu popup = new PopupMenu(rootView.getContext(), binding.menuRight);

            popup.getMenuInflater().inflate(R.menu.menu_category_right, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {

                        case R.id.rename:
                            renameReason(reason, position);
                            break;

                        case R.id.delete:
                            deleteReason(reason);
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

            if (TextUtils.isEmpty(reason.details)) {
                binding.details.setVisibility(View.GONE);
            } else {
                binding.details.setVisibility(View.VISIBLE);
            }

            binding.details.setText(reason.details);
        }

        private void deleteReason(final Reason reason) {

            // Check if Entry data has this reason already
            EntryDataManager entryDataManager   = new EntryDataManager(getContext());
            Entry entry                         = entryDataManager.hasReasonInEntryData(reason);

            if (entry != null) {

                // Show error message
                new AlertDialog.Builder(getContext())
                        .setTitle(getResources().getString(R.string.Error))
                        .setMessage(getResources().getString(R.string.using_this_account_in_entry_already))
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            // Confirm dialog
            new AlertDialog.Builder(getContext())
                    .setTitle(reason.name)
                    .setMessage(getResources().getString(R.string.delete_confirm_message))
                    .setPositiveButton(getResources().getString(R.string.Delete), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            long deleted = reasonDataManager.delete(reason.id);
                            if (deleted != 0) {
                                remove(reason);
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
                    final Context context = getContext();
                    final View textInputView = LayoutInflater.from(context).inflate(R.layout.dialog_text_input, null);
                    new AlertDialog.Builder(context)
                            .setView(textInputView)
                            .setTitle(getResources().getString(R.string.list_view_add_reason))
                            .setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    EditText editText = (EditText) textInputView.findViewById(R.id.edit);
                                    String reasonName = editText.getText().toString();

                                    ProjectDataManager projectDataManager = new ProjectDataManager(context);
                                    Project project = projectDataManager.findCurrentProjectWithContext(context);

                                    ReasonDataManager reasonDataManager = new ReasonDataManager(getContext());
                                    List<Reason> reasonList = reasonDataManager.findAllWithIsExpense(isExpense, context);

                                    Reason reason = new Reason();
                                    reason.name = reasonName;
                                    reason.uuid = UUID.randomUUID().toString();

                                    if (reasonList.isEmpty()) {
                                        reason.order = 0;
                                    } else {
                                        reason.order = reasonList.get(reasonList.size() - 1).order + 1;
                                    }
                                    reason.isExpense = isExpense;
                                    reason.project = project;
                                    reason.details = "";

                                    // @@ 保存チェック
                                    long id = reasonDataManager.save(reason);
                                    reason.id = id;

                                    add(reason);

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
//            Reason movedItem = getItem(fromPosition);
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

    private void renameReason(final Reason reason, final int position) {

        final Context context = getContext();
        final View textInputView = LayoutInflater.from(context).inflate(R.layout.dialog_text_input, null);

        final EditText editText = (EditText) textInputView.findViewById(R.id.edit);
        editText.setText(reason.name);

        new AlertDialog.Builder(context)
                .setView(textInputView)
                .setTitle(getResources().getString(R.string.list_view_rename))
                .setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String reasonName = editText.getText().toString();

                        ReasonDataManager reasonDataManager = new ReasonDataManager(getContext());
                        reasonDataManager.updateName(reason.id, reasonName);

                        Reason oldReason = reasonListAdapter.getItem(position);
                        if (oldReason != null) {
                            oldReason.name = reasonName;
                            reasonListAdapter.notifyDataSetChanged();
                        }

                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), null)
                .show();
    }


    //--------------------------------------------------------------//
    //    -- Delete --
    //--------------------------------------------------------------//

}
