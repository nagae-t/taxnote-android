package com.example.taxnoteandroid.entryTab;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.taxnoteandroid.CategorySelectActivity;
import com.example.taxnoteandroid.R;
import com.example.taxnoteandroid.dataManager.AccountDataManager;
import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.ReasonDataManager;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;


public class ExpenseInEntryTabFragment extends Fragment {

    private static final String EXTRA_IS_EXPENSE = "isExpense";

    public boolean isExpense = true;
    public  long    date;
    private Account account;
    private ListAdapter reasonListAdapter;


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
        isExpense   = getArguments().getBoolean(EXTRA_IS_EXPENSE);
        date        = System.currentTimeMillis();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_entry_tab_expense, container, false);

        setDateView(view);
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

    private void setDateView(View view) {

        view.findViewById(R.id.date_text_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });
    }

    private void loadCurrentDate() {

        String dateString = getResources().getString(R.string.date_string_today);

        // Show the date if it is not today
        if (!DateUtils.isToday(date)) {
            SimpleDateFormat simpleDateFormat   = new SimpleDateFormat(getResources().getString(R.string.date_string_format_to_year));
            dateString                          = simpleDateFormat.format(date);
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
                startActivity(CategorySelectActivity.createIntent(getContext(), isExpense));
            }
        });
    }

    private void loadCurrentAccount() {

        AccountDataManager accountDataManager = new AccountDataManager(getContext());
        account                               = accountDataManager.findCurrentSelectedAccount(getContext(), isExpense);

        ((TextView) getView().findViewById(R.id.account_text_view)).setText(account.name);
    }


    //--------------------------------------------------------------//
    //    -- Reason List --
    //--------------------------------------------------------------//

    private void setReasonList(View view) {

        ListView listView = (ListView) view.findViewById(R.id.reason_list_view);

        final ReasonDataManager reasonDataManager = new ReasonDataManager(getContext());
        final List<Reason> reasons = reasonDataManager.findAllWithIsExpense(isExpense, getContext());

        //@@@
        reasonListAdapter = new ListAdapter(getContext(), reasons);


        listView.setAdapter(reasonListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Reason reason = (Reason) adapterView.getItemAtPosition(position);
                startActivity(SummaryActivity.createIntent(getContext(), isExpense, date, account, reason));
            }
        });

        setAddNewButton(listView,reasonListAdapter);

    }

    // https://material.google.com/components/lists.html#
    class ListAdapter extends ArrayAdapter<Reason> {

        private LayoutInflater layoutInflater;
        private final ReasonDataManager reasonDataManager;

        public ListAdapter(Context context, List<Reason> texts) {
            super(context, 0, texts);
            layoutInflater = LayoutInflater.from(context);
            reasonDataManager = new ReasonDataManager(context);
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View view = layoutInflater.inflate(R.layout.row_list_with_details_item, null);

            final Reason reason = getItem(position);

            TextView textView = (TextView) view.findViewById(R.id.title);
            textView.setText(reason.name);
            TextView details = (TextView) view.findViewById(R.id.details);
            View menu = view.findViewById(R.id.menu_right);

            final PopupMenu popup = new PopupMenu(parent.getContext(), menu);

            //QQなんかエラーがでとる
            popup.getMenuInflater().inflate(R.menu.menu_category_right, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {

                        //@@@
                        case R.id.rename:

                            renameReason(reason, position);
                            break;

                        case R.id.delete:

                            // Confirm dialog
                            new AlertDialog.Builder(getContext())
                                    .setTitle(reason.name)
                                    .setMessage(getResources().getString(R.string.delete_confirm_message))
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            //@@
                                            //QQ すでに科目を使って入力している場合どうするか 入力したデータ消さないと、削除しないようにする？

                                            long deleted = reasonDataManager.delete(reason.id);
                                            if (deleted != 0) {
                                                remove(reason);
                                            }

                                            dialogInterface.dismiss();
                                        }
                                    })
                                    .setNegativeButton(getResources().getString(R.string.cancel), null)
                                    .show();
                            break;
                    }
                    return true;
                }
            });

            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popup.show();
                }
            });

            if (TextUtils.isEmpty(reason.details)) {
                details.setVisibility(View.GONE);
            } else {
                details.setText(reason.details);
                details.setVisibility(View.VISIBLE);
            }

            return view;
        }
    }


    //--------------------------------------------------------------//
    //    -- Rename --
    //--------------------------------------------------------------//

    private void renameReason(final Reason reason, final int position) {

        final Context context = getContext();
        final View textInputView = LayoutInflater.from(context).inflate(R.layout.dialog_text_input, null);

        final EditText editText = (EditText) textInputView.findViewById(R.id.edit);
        editText.setText(reason.name);

        //@@
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
    //    -- Add A New --
    //--------------------------------------------------------------//

    private void setAddNewButton(ListView listView, final ListAdapter adapter) {

        View v = LayoutInflater.from(getContext()).inflate(R.layout.listview_footer, null);
        ((TextView) v).setText(getResources().getString(R.string.list_view_add_reason));
        listView.addFooterView(v);

        v.setOnClickListener(new View.OnClickListener() {
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

                                ProjectDataManager projectDataManager   = new ProjectDataManager(context);
                                Project project                         = projectDataManager.findCurrentProjectWithContext(context);

                                ReasonDataManager reasonDataManager = new ReasonDataManager(getContext());
                                List<Reason> reasonList             = reasonDataManager.findAllWithIsExpense(isExpense, context);

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

                                adapter.add(reason);

                                dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.cancel), null)
                        .show();
            }
        });
    }
}
