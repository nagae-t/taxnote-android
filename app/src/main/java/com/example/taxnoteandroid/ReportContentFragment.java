package com.example.taxnoteandroid;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taxnoteandroid.databinding.FragmentReportContentBinding;
import com.example.taxnoteandroid.databinding.RowReportSectionBinding;
import com.example.taxnoteandroid.databinding.RowReportSumBinding;
import com.example.taxnoteandroid.model.Entry;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class ReportContentFragment extends Fragment {

    private static final String EXTRA_MODE_ = "EXTRA_";
    private FragmentReportContentBinding binding;

    public ReportContentFragment() {
    }

    public static ReportContentFragment newInstance(List<Entry> entries) {
        ReportContentFragment fragment = new ReportContentFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_MODE_, Parcels.wrap(entries));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReportContentBinding.inflate(inflater, container, false);

        List<Entry> entries = Parcels.unwrap(getArguments().getParcelable(EXTRA_MODE_));
        Log.d("entries", entries.toString());

        List<ReportContentAdapter.Item> items = new ArrayList<>();

        ReportContentAdapter.Item sectionA = ReportContentAdapter.Item.newInstanceSectionA();
        ReportContentAdapter.Item sectionB = ReportContentAdapter.Item.newInstanceSectionB();

        ReportContentAdapter.Item sumA = ReportContentAdapter.Item.newInstanceSumA();
        ReportContentAdapter.Item sumB = ReportContentAdapter.Item.newInstanceSumB();

        long count = 0;

        for (Entry entry : entries) {
            if (entry.isExpense) {
                // +
                sumB.sum += entry.price;
                count += entry.price;
            } else {
                // -
                sumA.sum += entry.price;
                count -= entry.price;
            }
        }

        // @@ 科目の合計はこっちでやる

        binding.price.setText(Long.toString(count));

        items.add(sectionA);
        items.add(sumA);

        items.add(sectionB);
        items.add(sumB);

        binding.reportList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.reportList.setAdapter(new ReportContentAdapter(items));


        return binding.getRoot();
    }

    static class ReportContentAdapter extends RecyclerView.Adapter<BindingHolder> {

        public static class Item {
            private static final int VIEW_ITEM_SECTION_A = 1;
            private static final int VIEW_ITEM_SECTION_B = 2;
            private static final int VIEW_ITEM_SUM_A_ = 3;
            private static final int VIEW_ITEM_SUM_B_ = 4;
            private static final int VIEW_ITEM_REPORT = 5;

            private final int viewItemId;
            private Entry entry;
            private long sum;

            private Item(int viewItemId) {
                this.viewItemId = viewItemId;
            }

            public static Item newInstanceSectionA() {
                return new Item(VIEW_ITEM_SECTION_A);
            }

            public static Item newInstanceSectionB() {
                return new Item(VIEW_ITEM_SECTION_B);
            }

            public static Item newInstanceSumA() {
                return new Item(VIEW_ITEM_SUM_A_);
            }

            public static Item newInstanceSumB() {
                return new Item(VIEW_ITEM_SUM_B_);
            }
        }

        private List<Item> items;

        public ReportContentAdapter(List<Item> items) {
            this.items = items;
        }

        @Override
        public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case Item.VIEW_ITEM_SECTION_A:
                    return new BindingHolder(parent.getContext(), parent, R.layout.row_report_section);
                case Item.VIEW_ITEM_SECTION_B:
                    return new BindingHolder(parent.getContext(), parent, R.layout.row_report_section);
                case Item.VIEW_ITEM_SUM_A_:
                case Item.VIEW_ITEM_SUM_B_:
                    return new BindingHolder(parent.getContext(), parent, R.layout.row_report_sum);
                case Item.VIEW_ITEM_REPORT:
                    return null;
            }
            return null;
        }

        @Override
        public void onBindViewHolder(BindingHolder holder, final int position) {
            Item item = items.get(position);
            switch (holder.getItemViewType()) {
                case Item.VIEW_ITEM_SECTION_A: {
                    RowReportSectionBinding binding = (RowReportSectionBinding) holder.binding;
                    break;
                }
                case Item.VIEW_ITEM_SECTION_B: {
                    RowReportSectionBinding binding = (RowReportSectionBinding) holder.binding;
                    break;
                }
                case Item.VIEW_ITEM_SUM_A_: {
                    RowReportSumBinding binding = (RowReportSumBinding) holder.binding;
                    binding.sum.setText(Long.toString(item.sum));
                    break;
                }
                case Item.VIEW_ITEM_SUM_B_: {
                    RowReportSumBinding binding = (RowReportSumBinding) holder.binding;
                    binding.sum.setText(Long.toString(item.sum));
                    break;
                }
                case Item.VIEW_ITEM_REPORT:
                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).viewItemId;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }
}
