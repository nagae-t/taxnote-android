package com.example.taxnoteandroid;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by b0ne on 2017/02/27.
 */

public class GraphTabFragment extends Fragment {

    private Context mContext;

    public static GraphTabFragment newInstance() {
        GraphTabFragment fragment = new GraphTabFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity().getApplicationContext();

    }
}
