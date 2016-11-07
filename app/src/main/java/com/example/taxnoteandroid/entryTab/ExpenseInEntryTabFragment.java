package com.example.taxnoteandroid.entryTab;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
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

    public ExpenseInEntryTabFragment() {
        // Required empty public constructor
    }

    public static ExpenseInEntryTabFragment newInstance() {
        ExpenseInEntryTabFragment fragment = new ExpenseInEntryTabFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_entry_tab_expense, container, false);

        setAccountView(view);
        setReasonList(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        loadCurrentAccount();
    }


    //--------------------------------------------------------------//
    //    -- Account Part --
    //--------------------------------------------------------------//

    private void setAccountView(View view) {

        view.findViewById(R.id.account_text_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(CategorySelectActivity.createIntent(getContext(), true));
            }
        });
    }

    private void loadCurrentAccount() {

        AccountDataManager accountDataManager   = new AccountDataManager(getContext());
        Account account                         = accountDataManager.findCurrentSelectedAccount(getContext(), true);

        ((TextView) getView().findViewById(R.id.account_text_view)).setText(account.name);
    }


    //--------------------------------------------------------------//
    //    -- Reason List --
    //--------------------------------------------------------------//

    private void setReasonList(View view) {

        ListView listView = (ListView) view.findViewById(R.id.reason_list_view);

        ReasonDataManager reasonDataManager = new ReasonDataManager(getContext());
        List<Reason> reasons = reasonDataManager.findAll();

        listView.setAdapter(new ListAdapter(getContext(), reasons));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                startActivity(SummaryActivity.createIntent(getContext()));
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
            View view = layoutInflater.inflate(R.layout.row_list_item, null);

            // getItemでcallのViewにbindしたいデータ型を取得できる
            Reason reason = getItem(position);

            TextView textView = (TextView) view.findViewById(R.id.text);
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
