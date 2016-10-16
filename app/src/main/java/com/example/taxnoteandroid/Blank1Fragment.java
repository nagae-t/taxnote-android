package com.example.taxnoteandroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class Blank1Fragment extends Fragment {
    public Blank1Fragment() {
        // Required empty public constructor
    }

    public static Blank1Fragment newInstance() {
        Blank1Fragment fragment = new Blank1Fragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_blank1, container, false);
    }
}
