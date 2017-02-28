package com.example.taxnoteandroid;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.example.taxnoteandroid.model.Entry;

import java.util.List;

/**
 * Created by b0ne on 2017/02/28.
 */

public class GraphHistoryRecyclerAdapter extends RecyclerView.Adapter<BindingHolder>
        implements View.OnClickListener, View.OnLongClickListener {

    private Context mContext;
    private RecyclerView mRecyclerView;
    private List<Entry> mDataList;

    public OnItemClickListener mOnItemClickListener;
    public OnLongItemClickListener mOnItemLongClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position, Entry item);
    }

    public interface OnLongItemClickListener {
        boolean onItemLongClick(View view, int position, Entry item);
    }

    public GraphHistoryRecyclerAdapter(Context context) {
        super();

        this.mContext = context;
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView = null;
    }

    public void setItems(List<Entry> entries) {
        mDataList = entries;
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        if (mDataList == null) return 0;
        return mDataList.size();
    }

    public void setOnItemClickListener(final OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(final OnLongItemClickListener listener) {
        mOnItemLongClickListener = listener;
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public boolean onLongClick(View view) {
        return false;
    }
}
