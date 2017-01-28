package com.example.taxnoteandroid;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.databinding.FragmentReportContentBinding;

public class ReportContentFragment extends Fragment {

    private static final String EXTRA_MODE = "EXTRA_MODE";
    private FragmentReportContentBinding binding;

    public ReportContentFragment() {
    }

    public static ReportContentFragment newInstance(ReportFragment.Mode mode) {
        ReportContentFragment fragment = new ReportContentFragment();
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_MODE, mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReportContentBinding.inflate(inflater, container, false);

        ReportFragment.Mode mode = (ReportFragment.Mode) getArguments().getSerializable(EXTRA_MODE);

        //
        if (mode == ReportFragment.Mode.YEAR) {

        }

        Context context = getContext();
        EntryDataManager entryDataManager = new EntryDataManager(context);
        entryDataManager.findAll(context, null, false);

        return binding.getRoot();
    }

//    class ReportContentAdapter extends RecyclerView.Adapter<BindingHolder> {
//
//        class Item {
//            private static final int VIEW_ITEM_HEADER = 1;
//            private static final int VIEW_ITEM_HEADER_ = 2;
//            private static final int VIEW_ITEM_REPORT = 3;
//
//            private Entry entry;
//
//        }
//
//        private static final int VIEW_ITEM_HEADER = 1;
//        private static final int VIEW_ITEM_CELL = 2;
//
//        private List<Item> items;
//        private OnItemClickRecyclerAdapterListener onItemClickRecyclerAdapterListener;
//
//        public ReportContentAdapter(List<Item> items) {
//            this.items = items;
//        }
//
//        public void setOnItemClickRecyclerAdapterListener(OnItemClickRecyclerAdapterListener onItemClickRecyclerAdapterListener) {
//            this.onItemClickRecyclerAdapterListener = onItemClickRecyclerAdapterListener;
//        }
//
//        @Override
//        public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            switch (viewType) {
//                case VIEW_ITEM_HEADER:
//                    return new BindingHolder(parent.getContext(), parent, R.layout.row_history_section_header);
//                case VIEW_ITEM_CELL:
//                    return new BindingHolder(parent.getContext(), parent, R.layout.row_history_cell);
//            }
//            return null;
//        }
//
//        @Override
//        public void onBindViewHolder(BindingHolder holder, final int position) {
//
//            switch (holder.getItemViewType()) {
//
//                case VIEW_ITEM_HEADER: {
//                    RowHistorySectionHeaderBinding binding = (RowHistorySectionHeaderBinding) holder.binding;
//                    Item item = items.get(position);
//                    binding.name.setText(item.header.date);
//                    binding.price.setText(item.header.sum);
//                }
//                break;
//
//                case VIEW_ITEM_CELL: {
//
//                    RowHistoryCellBinding binding = (RowHistoryCellBinding) holder.binding;
//                    Item item = items.get(position);
//
//                    binding.getRoot().setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if (onItemClickRecyclerAdapterListener != null) {
//                                onItemClickRecyclerAdapterListener.onItemClick(v, position);
//                            }
//                        }
//                    });
//
//                    if (item.cell.entry.isExpense) {
//                        binding.name.setText(item.cell.entry.reason.name + " / " + item.cell.entry.account.name);
//                    } else {
//                        binding.name.setText(item.cell.entry.account.name + " / " + item.cell.entry.reason.name);
//                    }
//
//
//                    if (TextUtils.isEmpty(item.cell.entry.memo)) {
//                        binding.memo.setVisibility(View.GONE);
//                    } else {
//                        binding.memo.setVisibility(View.VISIBLE);
//                        binding.memo.setText(item.cell.entry.memo);
//                    }
//
//                    // Create price string
//                    String priceString                  = ValueConverter.formatPriceWithSymbol(item.cell.entry.price, item.cell.entry.isExpense);
//                    binding.price.setText(priceString);
//
//                    // Set price color
//                    if (item.cell.entry.isExpense) {
//                        binding.price.setTextColor(ContextCompat.getColor(getContext(), R.color.expense));
//                    } else  {
//                        binding.price.setTextColor(ContextCompat.getColor(getContext(), R.color.primary));
//                    }
//                }
//                break;
//            }
//        }
//
//        @Override
//        public int getItemViewType(int position) {
//            Item item = items.get(position);
//            if (item.header != null) {
//                return VIEW_ITEM_HEADER;
//            }
//            return VIEW_ITEM_CELL;
//        }
//
//        @Override
//        public int getItemCount() {
//            return items.size();
//        }
//    }
}
