package com.example.taxnoteandroid;

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

import java.util.ArrayList;
import java.util.List;


public class EntryFragment extends Fragment {

    public EntryFragment() {
        // Required empty public constructor
    }

    public static EntryFragment newInstance() {
        EntryFragment fragment = new EntryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_blank1, container, false);

        ListView listView = (ListView) v.findViewById(R.id.list);

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
