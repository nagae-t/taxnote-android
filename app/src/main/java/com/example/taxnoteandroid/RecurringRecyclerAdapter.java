package com.example.taxnoteandroid;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.example.taxnoteandroid.model.Recurring;

import java.util.List;

/**
 * Created by b0ne on 2017/04/12.
 */

public class RecurringRecyclerAdapter extends  RecyclerView.Adapter<BindingHolder>
        implements View.OnClickListener, View.OnLongClickListener  {

    private Context mContext;
    private RecyclerView mRecyclerView;
    private List<Recurring> mDataList;

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public boolean onLongClick(View view) {
        return false;
    }

}
