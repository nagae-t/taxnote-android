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
import com.example.taxnoteandroid.databinding.RowReportCategoryBinding;
import com.example.taxnoteandroid.databinding.RowReportSectionBinding;
import com.example.taxnoteandroid.databinding.RowReportSumBinding;
import com.example.taxnoteandroid.model.Entry;
import com.example.taxnoteandroid.model.Reason;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        ReportContentAdapter.Item incomeSection = ReportContentAdapter.Item.newInstanceSectionIncome();
        ReportContentAdapter.Item ExpenseSection = ReportContentAdapter.Item.newInstanceSectionExpense();

        ReportContentAdapter.Item incomeSum = ReportContentAdapter.Item.newInstanceSumIncome();
        ReportContentAdapter.Item ExpenseSum = ReportContentAdapter.Item.newInstanceSumExpense();

        long count = 0;

        for (Entry entry : entries) {
            if (entry.isExpense) {
                // +
                ExpenseSum.sum += entry.price;
                count += entry.price;
            } else {
                // -
                incomeSum.sum += entry.price;
                count -= entry.price;
            }
        }

        // @@ 科目の合計はこっちでやる

        List<Entry> incomeList = new ArrayList<>();
        List<Entry> expenseList = new ArrayList<>();

        for (Entry entry : entries) {
            if (entry.reason.isExpense) {
                expenseList.add(entry);
            } else {
                incomeList.add(entry);
            }
        }

        Map<Long, ReportContentAdapter.Item> incomeMap = new LinkedHashMap<>();
        Map<Long, ReportContentAdapter.Item> expenseMap = new LinkedHashMap<>();

        for (Entry entry : incomeList) {
            Long id = entry.reason.id;
            if (incomeMap.containsKey(id)) {
                ReportContentAdapter.Item i = incomeMap.get(id);
                i.sum += entry.price;
            } else {
                ReportContentAdapter.Item item = ReportContentAdapter.Item.newInstanceCategory(entry.reason);
                item.sum += entry.price;
                incomeMap.put(id, item);
            }
        }

        for (Entry entry : expenseList) {
            Long id = entry.reason.id;
            if (expenseMap.containsKey(id)) {
                ReportContentAdapter.Item i = expenseMap.get(id);
                i.sum += entry.price;
            } else {
                ReportContentAdapter.Item item = ReportContentAdapter.Item.newInstanceCategory(entry.reason);
                item.sum += entry.price;
                expenseMap.put(id, item);
            }
        }

        binding.price.setText(Long.toString(count));

        items.add(incomeSection);
        items.add(incomeSum);
        for (Map.Entry<Long, ReportContentAdapter.Item> entry : incomeMap.entrySet()) {
            items.add(entry.getValue());
        }

        items.add(ExpenseSection);
        items.add(ExpenseSum);
        for (Map.Entry<Long, ReportContentAdapter.Item> entry : expenseMap.entrySet()) {
            items.add(entry.getValue());
        }

        binding.reportList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.reportList.setAdapter(new ReportContentAdapter(items));


        return binding.getRoot();
    }

    static class ReportContentAdapter extends RecyclerView.Adapter<BindingHolder> {

        public static class Item {
            private static final int VIEW_ITEM_SECTION_INCOME = 1;
            private static final int VIEW_ITEM_SECTION_EXPENSE = 2;
            private static final int VIEW_ITEM_SUM_INCOME = 3;
            private static final int VIEW_ITEM_SUM_EXPENSE = 4;
            private static final int VIEW_ITEM_CATEGORY = 5;

            private final int viewItemId;
            private long sum;
            private String reasonName;

            private Item(int viewItemId) {
                this.viewItemId = viewItemId;
            }

            public static Item newInstanceSectionIncome() {
                return new Item(VIEW_ITEM_SECTION_INCOME);
            }

            public static Item newInstanceSectionExpense() {
                return new Item(VIEW_ITEM_SECTION_EXPENSE);
            }

            public static Item newInstanceSumIncome() {
                return new Item(VIEW_ITEM_SUM_INCOME);
            }

            public static Item newInstanceSumExpense() {
                return new Item(VIEW_ITEM_SUM_EXPENSE);
            }

            public static Item newInstanceCategory(Reason reason) {
                Item i = new Item(VIEW_ITEM_CATEGORY);
                i.reasonName = reason.name;
                return i;
            }
        }

        private List<Item> items;

        public ReportContentAdapter(List<Item> items) {
            this.items = items;
        }

        @Override
        public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case Item.VIEW_ITEM_SECTION_INCOME:
                    return new BindingHolder(parent.getContext(), parent, R.layout.row_report_section);
                case Item.VIEW_ITEM_SECTION_EXPENSE:
                    return new BindingHolder(parent.getContext(), parent, R.layout.row_report_section);
                case Item.VIEW_ITEM_SUM_INCOME:
                case Item.VIEW_ITEM_SUM_EXPENSE:
                    return new BindingHolder(parent.getContext(), parent, R.layout.row_report_sum);
                case Item.VIEW_ITEM_CATEGORY:
                    return new BindingHolder(parent.getContext(), parent, R.layout.row_report_category);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(BindingHolder holder, final int position) {
            Item item = items.get(position);
            switch (holder.getItemViewType()) {
                case Item.VIEW_ITEM_SECTION_INCOME: {
                    RowReportSectionBinding binding = (RowReportSectionBinding) holder.binding;
                    binding.title.setText(R.string.Income);
                    break;
                }
                case Item.VIEW_ITEM_SECTION_EXPENSE: {
                    RowReportSectionBinding binding = (RowReportSectionBinding) holder.binding;
                    binding.title.setText(R.string.Expense);
                    break;
                }
                case Item.VIEW_ITEM_SUM_INCOME: {
                    RowReportSumBinding binding = (RowReportSumBinding) holder.binding;
                    binding.sum.setText(Long.toString(item.sum));
                    break;
                }
                case Item.VIEW_ITEM_SUM_EXPENSE: {
                    RowReportSumBinding binding = (RowReportSumBinding) holder.binding;
                    binding.sum.setText(Long.toString(item.sum));
                    break;
                }
                case Item.VIEW_ITEM_CATEGORY: {
                    RowReportCategoryBinding binding = (RowReportCategoryBinding) holder.binding;
                    binding.name.setText(item.reasonName);
                    binding.sum.setText(Long.toString(item.sum));
                    break;
                }
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
