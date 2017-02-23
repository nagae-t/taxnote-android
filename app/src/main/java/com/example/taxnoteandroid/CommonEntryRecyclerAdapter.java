package com.example.taxnoteandroid;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.taxnoteandroid.Library.ValueConverter;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.RowHistoryCellBinding;
import com.example.taxnoteandroid.model.Entry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by b0ne on 2017/02/23.
 */

public class CommonEntryRecyclerAdapter extends RecyclerView.Adapter<BindingHolder>
        implements View.OnClickListener, View.OnLongClickListener  {

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
    public CommonEntryRecyclerAdapter(Context context) {
        super();
        this.mContext = context;
        mDataList = new ArrayList<>();
    }

    public CommonEntryRecyclerAdapter(Context context, List<Entry> entries) {
        super();
        this.mContext = context;
        this.mDataList = entries;
    }

    public void add(Entry entry) {
        mDataList.add(entry);
    }

    public void addAll(List<Entry> entries) {
        mDataList.addAll(entries);
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BindingHolder(parent.getContext(), parent, R.layout.row_history_cell);
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, final int position) {
        RowHistoryCellBinding binding = (RowHistoryCellBinding) holder.binding;
        final Entry entry = mDataList.get(position);

        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, position, entry);
                }
            }
        });

        // Name
        String nameText;
        if (entry.isExpense) {
            nameText = entry.reason.name + " / " + entry.account.name;
        } else {
            nameText = entry.account.name + " / " + entry.reason.name;
        }

        if (!SharedPreferencesManager.isTapHereHistoryEditDone(mContext)) {
            nameText += " " + mContext.getString(R.string.tap_here);
        }
        binding.name.setText(nameText);

        // Memo
        TextView memoTv = binding.memo;
        if (TextUtils.isEmpty(entry.memo)) {
            memoTv.setVisibility(View.GONE);
        } else {
            memoTv.setVisibility(View.VISIBLE);
            memoTv.setText(entry.memo);
        }

        // Create price string
        String priceString = ValueConverter.formatPriceWithSymbol(mContext ,entry.price, entry.isExpense);
        binding.price.setText(priceString);

        // Set price color
        int priceColor = (entry.isExpense) ? ContextCompat.getColor(mContext, R.color.expense)
                : ContextCompat.getColor(mContext, R.color.primary);
        binding.price.setTextColor(priceColor);
    }

    @Override
    public int getItemCount() {
        if (mDataList == null) return 0;
        return mDataList.size();
    }

    @Override
    public void onClick(View v) {
        if (mRecyclerView == null) return;
        int position = mRecyclerView.getChildAdapterPosition(v);
        if (position < 0) return;

        Entry item = mDataList.get(position);
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v, position, item);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mRecyclerView == null) return false;
        int position = mRecyclerView.getChildAdapterPosition(v);
        if (position < 0) return false;

        Entry item = mDataList.get(position);
        if (mOnItemLongClickListener != null) {
            return mOnItemLongClickListener.onItemLongClick(v, position, item);
        }
        return false;
    }

    public void setOnItemClickListener(final OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(final OnLongItemClickListener listener) {
        mOnItemLongClickListener = listener;
    }
}
