package com.example.taxnoteandroid.entryTab;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.taxnoteandroid.CategorySelectActivity;
import com.example.taxnoteandroid.R;
import com.example.taxnoteandroid.dataManager.AccountDataManager;
import com.example.taxnoteandroid.dataManager.ReasonDataManager;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Reason;

import java.util.List;


public class ExpenseInEntryTabFragment extends Fragment {

    private static final String EXTRA_IS_EXPENSE = "isExpense";

    public boolean isExpense = true;
    public  long    date;
    private Account account;

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

        if (!DateUtils.isToday(date)) {

            //QQQ ここで、dateを 2016年12月21日 水 とかの表示に変換したい、英語の場合も
            dateString = "";
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

        AccountDataManager accountDataManager   = new AccountDataManager(getContext());
        account                         = accountDataManager.findCurrentSelectedAccount(getContext(), isExpense);

        ((TextView) getView().findViewById(R.id.account_text_view)).setText(account.name);
    }


    //--------------------------------------------------------------//
    //    -- Reason List --
    //--------------------------------------------------------------//

    private void setReasonList(View view) {

        ListView listView = (ListView) view.findViewById(R.id.reason_list_view);

        ReasonDataManager reasonDataManager = new ReasonDataManager(getContext());
        final List<Reason> reasons = reasonDataManager.findAllWithIsExpense(isExpense, getContext());

        listView.setAdapter(new ListAdapter(getContext(), reasons));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Reason reason = (Reason) adapterView.getItemAtPosition(position);
                startActivity(SummaryActivity.createIntent(getContext(), isExpense, date, account, reason));
            }
        });
    }

    // https://material.google.com/components/lists.html#
    class ListAdapter extends ArrayAdapter<Reason> {

        private LayoutInflater layoutInflater;

        public ListAdapter(Context context, List<Reason> texts) {
            super(context, 0, texts);
            layoutInflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // getViewでListViewの1つのセルを作る
            // inflateでViewにする
            View view = layoutInflater.inflate(R.layout.row_list_with_details_item, null);

            // getItemでcallのViewにbindしたいデータ型を取得できる
            Reason reason = getItem(position);

            TextView textView = (TextView) view.findViewById(R.id.title);
            textView.setText(reason.name);
            TextView details = (TextView) view.findViewById(R.id.details);

            if (TextUtils.isEmpty(reason.details)) {
                details.setVisibility(View.GONE);
            } else {
                details.setText(reason.details);
                details.setVisibility(View.VISIBLE);
            }
            return view;
        }
    }
}
