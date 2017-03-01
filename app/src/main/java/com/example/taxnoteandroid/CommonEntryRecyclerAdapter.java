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
import com.example.taxnoteandroid.databinding.RowHistorySectionHeaderBinding;
import com.example.taxnoteandroid.databinding.RowSimpleCellBinding;
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

    private static final int VIEW_ITEM_HEADER = 1;
    private static final int VIEW_ITEM_CELL = 2;
    private static final int VIEW_ITEM_REPORT_CELL = 3;
    private static final int VIEW_ITEM_REPORT_TOTAL = 4;

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

    public List<Entry> getItems() {
        return mDataList;
    }

    public void add(Entry entry) {
        mDataList.add(entry);
    }

    public void addAll(List<Entry> entries) {
        mDataList.addAll(entries);
    }

    public void clearAll() {
        mDataList = new ArrayList<>();
    }

    public void clearAllToNotifyData() {
        mDataList = new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_ITEM_HEADER:
                return new BindingHolder(parent.getContext(), parent, R.layout.row_history_section_header);
            case VIEW_ITEM_CELL:
                return new BindingHolder(parent.getContext(), parent, R.layout.row_history_cell);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, final int position) {
        int viewType = holder.getItemViewType();
        final Entry entry = mDataList.get(position);
        String priceString;
        int priceColor = (entry.isExpense) ? ContextCompat.getColor(mContext, R.color.expense)
                : ContextCompat.getColor(mContext, R.color.primary);

        switch (viewType) {
            case VIEW_ITEM_HEADER:
                RowHistorySectionHeaderBinding headerBindding = (RowHistorySectionHeaderBinding) holder.binding;
                headerBindding.name.setText(entry.dateString);
                headerBindding.price.setText(entry.sumString);
                break;
            case VIEW_ITEM_CELL:

                RowHistoryCellBinding cellBinding = (RowHistoryCellBinding) holder.binding;

                // price
                priceString = ValueConverter.formatPriceWithSymbol(mContext ,entry.price, entry.isExpense);
                cellBinding.price.setText(priceString);
                cellBinding.price.setTextColor(priceColor);

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
                cellBinding.name.setText(nameText);

                // Memo
                TextView memoTv = cellBinding.memo;
                if (TextUtils.isEmpty(entry.memo)) {
                    memoTv.setVisibility(View.GONE);
                } else {
                    memoTv.setVisibility(View.VISIBLE);
                    memoTv.setText(entry.memo);
                }

                cellBinding.getRoot().setOnClickListener(
                        getCellOnClickListener(position, entry));
                break;
            case VIEW_ITEM_REPORT_CELL:
            case VIEW_ITEM_REPORT_TOTAL:

                RowSimpleCellBinding rowSimpleBinding = (RowSimpleCellBinding) holder.binding;

                // sum price
                priceString = ValueConverter.formatPrice(mContext, entry.price);
                rowSimpleBinding.price.setText(priceString);
                rowSimpleBinding.price.setTextColor(priceColor);

                String nameString = entry.reasonName;
                if (viewType == VIEW_ITEM_REPORT_TOTAL) {
                    nameString = mContext.getString(R.string.total);
                }
                rowSimpleBinding.labelName.setText(nameString);

                rowSimpleBinding.getRoot().setOnClickListener(
                        getCellOnClickListener(position, entry));
                break;
        }
    }

    private View.OnClickListener getCellOnClickListener(final int position, final Entry entry) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, position, entry);
                }
            }
        };
    }

    @Override
    public int getItemViewType(int position) {
        Entry item = mDataList.get(position);
        if (item.dateString != null) {
            return VIEW_ITEM_HEADER;
        }
        return VIEW_ITEM_CELL;
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
        if (item.dateString != null || item.sumString != null) return;
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
        if (item.dateString != null || item.sumString != null) return false;
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
