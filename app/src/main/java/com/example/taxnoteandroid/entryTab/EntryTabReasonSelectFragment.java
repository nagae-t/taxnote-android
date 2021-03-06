package com.example.taxnoteandroid.entryTab;

import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.taxnoteandroid.AccountSelectActivity;
import com.example.taxnoteandroid.DatePickerDialogFragment;
import com.example.taxnoteandroid.DividerDecoration;
import com.example.taxnoteandroid.Library.AsyncOkHttpClient;
import com.example.taxnoteandroid.Library.BroadcastUtil;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.KeyboardUtil;
import com.example.taxnoteandroid.Library.taxnote.TNApi;
import com.example.taxnoteandroid.Library.taxnote.TNApiModel;
import com.example.taxnoteandroid.R;
import com.example.taxnoteandroid.dataManager.AccountDataManager;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.ReasonDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.FragmentEntryTabReasonSelectBinding;
import com.example.taxnoteandroid.databinding.ListviewFooterBinding;
import com.example.taxnoteandroid.databinding.RowListWithDetailsItemBinding;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Entry;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import okhttp3.Response;

public class EntryTabReasonSelectFragment extends Fragment {

    private static final String EXTRA_IS_EXPENSE = "isExpense";

    private static final int TYPE_REASON = 0;
    private static final int TYPE_FOOTER = 1;

    public boolean isExpense = true;
    public long date;
    private Account account;
    private MyRecyclerViewAdapter adapter;
    private FragmentEntryTabReasonSelectBinding binding;
    private ReasonDataManager reasonDataManager;
    private EntryDataManager entryManager;
    private List<Reason> reasonList;

    private Context mContext;
    private TNApiModel mApiModel;

    public EntryTabReasonSelectFragment() {
        // Required empty public constructor
    }

    public static EntryTabReasonSelectFragment newInstance(boolean isExpense) {

        EntryTabReasonSelectFragment fragment = new EntryTabReasonSelectFragment();

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
        binding = FragmentEntryTabReasonSelectBinding.inflate(inflater, container, false);
        binding.setIsExpense(isExpense);

        binding.reasonList.addItemDecoration(new DividerDecoration(getContext()));
        binding.reasonList.setLayoutManager(new LinearLayoutManager(getContext()));

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = getActivity().getApplicationContext();
        mApiModel = new TNApiModel(mContext);
        if (!mApiModel.isLoggingIn()) binding.refreshLayout.setEnabled(false);

        entryManager = new EntryDataManager(mContext);
        reasonDataManager = new ReasonDataManager(mContext);
        setDateView();
        setAccountView();
        setReasonList();

        binding.refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!TNApi.isNetworkConnected(mContext)) {
                    binding.refreshLayout.setRefreshing(false);
                    DialogManager.showOKOnlyAlert(getActivity(),
                            null, getString(R.string.network_not_connection));
                    return;
                }
                mApiModel = new TNApiModel(mContext);
                if (!mApiModel.isLoggingIn() || mApiModel.isSyncing()) {
                    binding.refreshLayout.setRefreshing(false);
                    return;
                }

                refreshSyncData();
            }
        });
    }

    private void refreshSyncData() {
        mApiModel.syncData(getActivity(), new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e("Error", "refreshSyncData onFailure");
                binding.refreshLayout.setRefreshing(false);
                String errorMsg = "";
                if (response != null) {
                    errorMsg = response.message();
                }
                DialogManager.showOKOnlyAlert(getActivity(),
                        "Error", errorMsg);

            }

            @Override
            public void onSuccess(Response response, String content) {
                binding.refreshLayout.setRefreshing(false);
                setReasonList();
                loadCurrentAccount();
            }
        });
    }

    public void afterLogin() {
        mApiModel = new TNApiModel(mContext);
        if (mApiModel.isLoggingIn()) {
            binding.refreshLayout.setEnabled(true);
        } else {
            binding.refreshLayout.setEnabled(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        loadCurrentDateWithToast(false);
        loadCurrentAccount();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (this.isVisible()) {
            if (isVisibleToUser) {

                // Sync selected date between expense and income tabs
                date = SharedPreferencesManager.getCurrentSelectedDate(getActivity());
                loadCurrentDateWithToast(false);
            }
        }
    }


    //--------------------------------------------------------------//
    //    -- Reason List --
    //--------------------------------------------------------------//

    private void setReasonList() {

        // Adapter
        adapter = new MyRecyclerViewAdapter();
        binding.reasonList.setAdapter(adapter);

        // Attach recyclerView to ItemTouchHelper so that you can drag and drop the items in order to change the order.
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback();
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(binding.reasonList);
    }


    //--------------------------------------------------------------//
    //    -- Date Part --
    //--------------------------------------------------------------//

    private void setDateView() {

        binding.dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerDialogFragment fragment = DatePickerDialogFragment.newInstance(date, null);
                fragment.setOnDateSetListener(new DatePickerDialogFragment.OnDateSetListener() {
                    @Override
                    public void onDateSet(Calendar calendar) {
                        date = calendar.getTimeInMillis();
                        loadCurrentDateWithToast(true);

                        // Save selected date
                        SharedPreferencesManager.saveCurrentSelectedDate(getActivity(), date);
                    }
                });
                fragment.show(getFragmentManager(), DatePickerDialogFragment.class.getName());
            }
        });

        binding.datePreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(date);
                c.add(Calendar.DATE, -1);
                date = c.getTimeInMillis();
                loadCurrentDateWithToast(false);

                // Save selected date
                SharedPreferencesManager.saveCurrentSelectedDate(getActivity(), date);
            }
        });

        binding.dateNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(date);
                c.add(Calendar.DATE, 1);
                date = c.getTimeInMillis();
                loadCurrentDateWithToast(false);

                // Save selected date
                SharedPreferencesManager.saveCurrentSelectedDate(getActivity(), date);
            }
        });
    }

    private void loadCurrentDateWithToast(boolean showToast) {

        String dateString = getResources().getString(R.string.date_string_today);

        // Show the date if it is not today
        if (!DateUtils.isToday(date)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources().getString(R.string.date_string_format_to_year_month_day_weekday));
            dateString = simpleDateFormat.format(date);
        }

        if (showToast) {
            DialogManager.showToast(getActivity(), dateString);
        }

        ((TextView) getView().findViewById(R.id.date_text_view)).setText(dateString);
    }

    private void setAccountView() {
        binding.accountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(AccountSelectActivity.createIntent(getContext(), isExpense));
            }
        });
    }


    //--------------------------------------------------------------//
    //    -- Account Part --
    //--------------------------------------------------------------//

    private void loadCurrentAccount() {
        AccountDataManager accountDataManager = new AccountDataManager(getContext());
        account = accountDataManager.findCurrentSelectedAccount(isExpense);
        if (account != null && account.name != null)
            binding.accountTextView.setText(account.name);
    }

    public void reloadData() {
        entryManager = new EntryDataManager(mContext);
        reasonDataManager = new ReasonDataManager(mContext);
        if (adapter != null) {
            adapter.onReasonDataManagerChanged();
        }
    }

    //--------------------------------------------------------------//
    //    -- Rename --
    //--------------------------------------------------------------//

    private void showRenameReasonDialog(final Reason reason, final int position) {
        DialogManager.showRenameCateDialog(getParentFragment().getActivity(), reason, null,
                new DialogManager.CategoryCombineListener() {
                    @Override
                    public void onCombine(Reason fromReason, Reason toReason, int countTarget) {
                        if (countTarget > 0) {
                            entryManager.updateCombine(fromReason, toReason);
                        }
                        reasonDataManager.updateSetDeleted(fromReason.uuid, mApiModel);

                        BroadcastUtil.sendReloadReport(getActivity());

                        String title = mContext.getString(R.string.done);
                        String msg = mContext.getString(R.string.combined_categories);
                        DialogManager.showCustomAlertDialog(mContext,
                                getFragmentManager(), title, msg);

                    }

                    @Override
                    public void onCombine(Account fromAccount, Account toAccount, int countTarget) {
                    }

                });
    }

    //--------------------------------------------------------------//
    //    -- Delete --
    //--------------------------------------------------------------//

    private void deleteReason(final Reason reason) {

        // Check if Entry data has this reason already
        final EntryDataManager entryDataManager = new EntryDataManager(getContext());
        Entry entry = entryDataManager.hasReasonInEntryData(reason);

        if (entry != null) {
            int countReason = entryDataManager.countByReason(reason);

            DialogManager.confirmEntryDeleteForReson((AppCompatActivity) getActivity(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            entryDataManager.deleteByReason(reason, mApiModel);
                            reasonDataManager.updateSetDeleted(reason.uuid, mApiModel);

                            BroadcastUtil.sendReloadReport(getActivity());

                            adapter.onReasonDataManagerChanged();
                            String message = getString(R.string.bulk_del_complete, reason.name);
                            DialogManager.showToast(getActivity(), message);

                            dialogInterface.dismiss();
                        }
                    },
                    countReason, reason, null);

            return;
        }

        // Confirm dialog
        new AlertDialog.Builder(getContext())
                .setTitle(reason.name)
                .setMessage(getResources().getString(R.string.delete_confirm_message))
                .setPositiveButton(getResources().getString(R.string.Delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();
                        reasonDataManager.updateSetDeleted(reason.uuid, mApiModel);

                        adapter.onReasonDataManagerChanged();

                        String message = reason.name + getResources().getString(R.string.delete_done_after_title);
                        DialogManager.showToast(getActivity(), message);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), null)
                .show();

    }

    //--------------------------------------------------------------//
    //    -- Reason List View Holder --
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

            int size = reasonList.size();

            if (position_from < 0 || size <= position_from ||
                    position_to < 0 || size <= position_to) {
                return false;
            }

            if (position_from == position_to) {
                return false;
            }

            Collections.swap(reasonList, position_from, position_to);
            adapter.notifyItemMoved(position_from, position_to);
            for (int i = 0; i < size; i++) {
                Reason reason = reasonList.get(i);
                reasonDataManager.updateOrder(reason.id, i); // Update database

                mApiModel.updateReason(reason.uuid, null);
            }

            return true;
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder holder, int action) {
            if (action == ItemTouchHelper.ACTION_STATE_IDLE) {
                BroadcastUtil.sendReloadReport(getActivity());
            }
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

    //--------------------------------------------------------------//
    //    -- Reason List Adapter --
    //--------------------------------------------------------------//

    private class MyRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public MyRecyclerViewAdapter() {
            onReasonDataManagerChanged();
        }

        @Override
        public int getItemViewType(int position) {
            if (position < reasonList.size()) {
                return TYPE_REASON;
            } else {
                return TYPE_FOOTER;
            }
        }

        private Reason getItem(int position) {
            if (reasonList != null && position < reasonList.size()) {
                return reasonList.get(position);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            MyViewHolder h = (MyViewHolder) holder;
            h.setReason(position, getItem(position));
        }

        @Override
        public int getItemCount() {
            if (reasonList != null) {
                return reasonList.size() + 1; // Add 1 for footer.
            }
            return 1;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int type) {
            try {
                View view = null;

                if (type == TYPE_REASON) {
                    view = LayoutInflater.from(getContext()).inflate(R.layout.row_list_with_details_item, null);
                } else if (type == TYPE_FOOTER) {
                    view = LayoutInflater.from(getContext()).inflate(R.layout.listview_footer, null);
                }

                view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
                MyViewHolder holder = new MyViewHolder(view);

                return holder;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        public void onReasonDataManagerChanged() {
            reasonList = reasonDataManager.findAllWithIsExpense(isExpense);
            this.notifyDataSetChanged();
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener {

        public int position;
        public Reason reason;
        public View button_right;

        public MyViewHolder(View view) {
            super(view);
        }

        public void setReason(final int position, final Reason reason) {
            try {
                this.position = position;
                this.reason = reason;

                Object obj = DataBindingUtil.bind(this.itemView);

                if (obj instanceof RowListWithDetailsItemBinding) {
                    RowListWithDetailsItemBinding binding = (RowListWithDetailsItemBinding) obj;
                    binding.title.setText(reason.name);
                    binding.details.setText(reason.details);
                    String reasonDesc = reason.details;
                    if (reason.details != null) {
                        reasonDesc = reason.details.replace(" ", "");
                        reasonDesc = reasonDesc.replace("???", "");
                    }
                    binding.details.setVisibility(TextUtils.isEmpty(reasonDesc) ? View.GONE : View.VISIBLE);
                    this.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onClick_Reason();
                        }
                    });

                    final PopupMenu popup = new PopupMenu(getContext(), binding.menuRight);
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
                            onClick_Footer();
                        }
                    });
                    ListviewFooterBinding binding = (ListviewFooterBinding) obj;
                    binding.text.setText(getResources().getString(R.string.list_view_add_reason));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onClick_Reason() {
            startActivity(SummaryActivity.createIntent(getContext(), isExpense, date, account, reason));
        }

        public void onClick_Footer() {
            final Context context = getContext();
            final View textInputView = LayoutInflater.from(context).inflate(R.layout.dialog_text_input, null);
            final EditText editText = textInputView.findViewById(R.id.edit);
            final EditText editDescText = textInputView.findViewById(R.id.edit_desc);
            editDescText.setVisibility(View.VISIBLE);

            new AlertDialog.Builder(context)
                    .setView(textInputView)
                    .setTitle(getResources().getString(R.string.list_view_add_reason))
                    .setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            try {
                                String newName = editText.getText().toString();
                                String newDesc = editDescText.getText().toString();

                                KeyboardUtil.hideKeyboard(getActivity(), editText);
                                KeyboardUtil.hideKeyboard(getActivity(), editDescText);

                                // Check empty
                                if (newName.isEmpty()) {
                                    return;
                                }

                                ProjectDataManager projectDataManager = new ProjectDataManager(context);
                                Project project = projectDataManager.findCurrent();

                                List<Reason> reasonList = reasonDataManager.findAllWithIsExpense(isExpense);

                                Reason reason = new Reason();
                                reason.name = newName;
                                reason.details = newDesc;
                                reason.uuid = UUID.randomUUID().toString();

                                if (reasonList.isEmpty()) {
                                    reason.order = 0;
                                } else {
                                    reason.order = reasonList.get(reasonList.size() - 1).order + 1;
                                }
                                reason.isExpense = isExpense;
                                reason.project = project;

                                long id = reasonDataManager.save(reason);

                                // Success
                                if (id != -1) {

                                    reason.id = id;
                                    adapter.onReasonDataManagerChanged(); // 2017/01/23 Bug fixed. Refresh GUI after database is changed.
                                    dialogInterface.dismiss();
                                    DialogManager.showToast(context, newName);
                                }

                                mApiModel.saveReason(reason.uuid, null);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    })

                    .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            EditText editText = (EditText) textInputView.findViewById(R.id.edit);
                            KeyboardUtil.hideKeyboard(getActivity(), editText); // 2017/01/24 E.Nozaki Hide software keyboard.
                        }
                    })
                    .show();

            KeyboardUtil.showKeyboard(getActivity(), editText); // 2017/01/23 E.Nozaki Show software keyboard for editText.
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {

                case R.id.reorder:
                    showHowToReorderMessage();
                    break;

                case R.id.rename:
                    showRenameReasonDialog(reason, position);
                    break;

                case R.id.delete:
                    deleteReason(reason);
                    break;
            }
            return true;
        }
    }


    //--------------------------------------------------------------//
    //    -- Help --
    //--------------------------------------------------------------//

    private void showHowToReorderMessage() {

        new AlertDialog.Builder(getActivity())
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