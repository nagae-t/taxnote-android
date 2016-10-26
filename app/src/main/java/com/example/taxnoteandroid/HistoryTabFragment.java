package com.example.taxnoteandroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class HistoryTabFragment extends Fragment {
    public HistoryTabFragment() {
        // Required empty public constructor
    }

    public static HistoryTabFragment newInstance() {
        HistoryTabFragment fragment = new HistoryTabFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history_tab, container, false);
    }
}
