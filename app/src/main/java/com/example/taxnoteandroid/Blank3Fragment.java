package com.example.taxnoteandroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class Blank3Fragment extends Fragment {
    public Blank3Fragment() {
        // Required empty public constructor
    }

    public static Blank3Fragment newInstance() {
        Blank3Fragment fragment = new Blank3Fragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blank3, container, false);
    }
}
