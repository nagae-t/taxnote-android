package com.example.taxnoteandroid.entryTab;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.taxnoteandroid.R;

import java.util.ArrayList;
import java.util.List;


public class IncomeInEntryTabFragment extends Fragment {

    public IncomeInEntryTabFragment() {
        // Required empty public constructor
    }

    public static IncomeInEntryTabFragment newInstance() {
        IncomeInEntryTabFragment fragment = new IncomeInEntryTabFragment();
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
        listView.setAdapter(new ListAdapter(getContext(), strings));

        return v;
    }

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
            View v = layoutInflater.inflate(R.layout.row_list_item, null);

            // getItemでcallのViewにbindしたいデータ型を取得できる
            String s = getItem(position);

            TextView textView = (TextView) v.findViewById(R.id.text);
            textView.setText(s);

            return v;
        }
    }
}
