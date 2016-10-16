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

        listView.setAdapter(new ListAdapter(getContext()));

        return v;
    }

    // @@ https://material.google.com/components/lists.html#
    class ListAdapter extends ArrayAdapter {

        private LayoutInflater layoutInflater;

        public ListAdapter(Context context) {
            super(context, 0);
            layoutInflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // getViewでListViewの1つのセルを作る
            // inflateでViewにする
            View v = layoutInflater.inflate(R.layout.row_list_item, null);

            TextView textView = (TextView) v.findViewById(R.id.text);
            textView.setText("test");

            return v;
        }

        @Override
        public int getCount() {
            return 10;
        }
    }
}
