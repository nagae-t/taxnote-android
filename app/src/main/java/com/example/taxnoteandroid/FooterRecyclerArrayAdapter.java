package com.example.taxnoteandroid;

import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.view.ViewGroup;

public abstract class FooterRecyclerArrayAdapter<T> extends RecyclerArrayAdapter<T, BindingHolder<ViewDataBinding>> {

    public static final int ITEM_VIEW_TYPE_LOADING = Integer.MAX_VALUE - 1;

    protected abstract BindingHolder<ViewDataBinding> onCreateItemViewHolder(ViewGroup parent, int viewType);

    protected abstract void onBindFooterItemViewHolder(BindingHolder<ViewDataBinding> holder, int position);
    protected abstract void onBindItemViewHolder(BindingHolder<ViewDataBinding> holder, int position);

    @LayoutRes
    public abstract int getFooterLayoutId();

    @Override
    protected int getFooterPositionOffset() {
        return 1;
    }

    @Override
    public BindingHolder<ViewDataBinding> onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_VIEW_TYPE_LOADING:
                return new BindingHolder<>(parent.getContext(), parent, getFooterLayoutId());
        }
        return onCreateItemViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(BindingHolder<ViewDataBinding> holder, int position) {
        if (holder.getItemViewType() == ITEM_VIEW_TYPE_LOADING) {
            onBindFooterItemViewHolder(holder, position);
        } else {
            onBindItemViewHolder(holder, position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return ITEM_VIEW_TYPE_LOADING;
        }
        return super.getItemViewType(position);
    }
}