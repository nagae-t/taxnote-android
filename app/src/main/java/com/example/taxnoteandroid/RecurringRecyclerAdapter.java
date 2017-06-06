package com.example.taxnoteandroid;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.taxnoteandroid.Library.ValueConverter;
import com.example.taxnoteandroid.Library.zeny.ZNUtils;
import com.example.taxnoteandroid.databinding.RowHistoryCellBinding;
import com.example.taxnoteandroid.databinding.RowHistorySectionHeaderBinding;
import com.example.taxnoteandroid.model.Recurring;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by b0ne on 2017/04/12.
 */

public class RecurringRecyclerAdapter extends  RecyclerView.Adapter<BindingHolder>
        implements View.OnClickListener, View.OnLongClickListener  {

    private Context mContext;
    private RecyclerView mRecyclerView;
    private List<Recurring> mDataList;
    private TypedValue mIncomePriceTv = new TypedValue();

    public static final int VIEW_ITEM_HEADER = 1;
    public static final int VIEW_ITEM_CELL = 2;

    public OnItemClickListener mOnItemClickListener;
    public OnLongItemClickListener mOnItemLongClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position, Recurring item);
    }

    public interface OnLongItemClickListener {
        boolean onItemLongClick(View view, int position, Recurring item);
    }

    public RecurringRecyclerAdapter(Context context) {
        super();
        this.mContext = context;
        mDataList = new ArrayList<>();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, mIncomePriceTv, true);
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

    public void setItems(List<Recurring> entries) {
        mDataList = entries;
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
    public void onBindViewHolder(BindingHolder holder, int position) {
        int viewType = holder.getItemViewType();
        final Recurring recurring = mDataList.get(position);
        String priceString;
        int priceColor = (recurring.isExpense) ? ContextCompat.getColor(mContext, R.color.expense)
                : ContextCompat.getColor(mContext, mIncomePriceTv.resourceId);
        switch (viewType) {
            case VIEW_ITEM_HEADER:
                RowHistorySectionHeaderBinding headerBinding = (RowHistorySectionHeaderBinding) holder.binding;
                headerBinding.name.setText(recurring.titleName);
                headerBinding.price.setVisibility(View.GONE);
                break;
            case VIEW_ITEM_CELL:
                RowHistoryCellBinding cellBinding = (RowHistoryCellBinding) holder.binding;

                // price
                priceString = ValueConverter.formatPrice(mContext ,recurring.price);
                cellBinding.price.setText(priceString);
                cellBinding.price.setTextColor(priceColor);

                // Name
                String nameText;
                if (!ZNUtils.isZeny()) {
                    String reasonName = ValueConverter.parseCategoryName(mContext, recurring.reason.name);
                    String accName = ValueConverter.parseCategoryName(mContext, recurring.account.name);
                    nameText = (recurring.isExpense) ? reasonName + " / " + accName
                            : accName + " / " + reasonName;
                } else {
                    nameText = recurring.reason.name;
                }

                cellBinding.name.setText(nameText);

                // Memo
                TextView memoTv = cellBinding.memo;
                if (TextUtils.isEmpty(recurring.memo)) {
                    memoTv.setVisibility(View.GONE);
                } else {
                    memoTv.setVisibility(View.VISIBLE);
                    memoTv.setText(recurring.memo);
                }

                cellBinding.getRoot().setOnClickListener(
                        getCellOnClickListener(position, recurring));
                break;
        }
    }

    private View.OnClickListener getCellOnClickListener(final int position, final Recurring recurring) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, position, recurring);
                }
            }
        };
    }

    @Override
    public int getItemCount() {
        if (mDataList == null) return 0;
        return mDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Recurring item = mDataList.get(position);
        return item.viewType;
    }

    @Override
    public void onClick(View view) {
        if (mRecyclerView == null) return;
        int position = mRecyclerView.getChildAdapterPosition(view);
        if (position < 0) return;

        Recurring item = mDataList.get(position);
        if (item.titleName != null) return;
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(view, position, item);
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (mRecyclerView == null) return false;
        int position = mRecyclerView.getChildAdapterPosition(view);
        if (position < 0) return false;

        Recurring item = mDataList.get(position);
        if (item.titleName != null) return false;
        if (mOnItemLongClickListener != null) {
            return mOnItemLongClickListener.onItemLongClick(view, position, item);
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
