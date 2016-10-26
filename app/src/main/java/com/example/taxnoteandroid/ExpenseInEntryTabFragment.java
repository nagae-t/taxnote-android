package com.example.taxnoteandroid;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
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

        View v = inflater.inflate(R.layout.fragment_entry_tab_expense, container, false);

        ListView listView = (ListView) v.findViewById(R.id.reason_list_view);

        List<String> strings = new ArrayList<>();
        strings.add("test1");
        strings.add("test2");
        strings.add("test3");
        strings.add("test4");
        strings.add("test5");
        strings.add("test3");
        strings.add("test4");
        strings.add("test5");

        listView.setAdapter(new ListAdapter(getContext(), strings));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                startActivity(SummaryActivity.createIntent(getContext()));
            }
        });

        return v;
    }

    //--------------------------------------------------------------//
    //    -- Display Part --
    //--------------------------------------------------------------//




    //--------------------------------------------------------------//
    //    -- List Adapter --
    //--------------------------------------------------------------//

    // @@ https://material.google.com/components/lists.html#
    class ListAdapter extends ArrayAdapter<String> {

        private LayoutInflater layoutInflater;

        public ListAdapter(Context context, List<String> texts) {
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
            String string = getItem(position);

            TextView textView = (TextView) view.findViewById(R.id.text);
            textView.setText(string);

            return view;
        }
    }
}
