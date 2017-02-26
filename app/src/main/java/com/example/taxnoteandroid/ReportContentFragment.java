package com.example.taxnoteandroid;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taxnoteandroid.Library.ValueConverter;
import com.example.taxnoteandroid.databinding.FragmentReportContentBinding;
import com.example.taxnoteandroid.databinding.RowHistorySectionHeaderBinding;
import com.example.taxnoteandroid.databinding.RowSimpleCellBinding;
import com.example.taxnoteandroid.model.Entry;
import com.example.taxnoteandroid.model.Reason;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportContentFragment extends Fragment {

    private static final String EXTRA_MODE_ = "EXTRA_";
    private static final String TARGET_CALENDAR = "TARGET_CALENDAR";
    private FragmentReportContentBinding binding;
    private Context mContext;
    private Calendar mTargetCalendar;

    public ReportContentFragment() {
    }

    public static ReportContentFragment newInstance(List<Entry> entries, Calendar targetCalendar) {
        ReportContentFragment fragment = new ReportContentFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_MODE_, Parcels.wrap(entries));
        args.putSerializable(TARGET_CALENDAR, targetCalendar);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReportContentBinding.inflate(inflater, container, false);
        mContext = getContext();

        List<Entry> entries = Parcels.unwrap(getArguments().getParcelable(EXTRA_MODE_));
//        Log.d("entries", entries.toString());
        mTargetCalendar = (Calendar) getArguments().getSerializable(TARGET_CALENDAR);

        List<ReportContentAdapter.Item> items = new ArrayList<>();

        ReportContentAdapter.Item incomeSection = ReportContentAdapter.Item.newInstanceSectionIncome();
        ReportContentAdapter.Item ExpenseSection = ReportContentAdapter.Item.newInstanceSectionExpense();

        ReportContentAdapter.Item incomeSum = ReportContentAdapter.Item.newInstanceSumIncome();
        ReportContentAdapter.Item ExpenseSum = ReportContentAdapter.Item.newInstanceSumExpense();
        ExpenseSum.isExpense = true;

        long count = 0;
        for (Entry entry : entries) {
            if (entry.isExpense) {
                // -
                ExpenseSum.sum += entry.price;
                count -= entry.price;
            } else {
                // +
                incomeSum.sum += entry.price;
                count += entry.price;
            }
        }

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
                i.isExpense = true;
                i.sum += entry.price;
            } else {
                ReportContentAdapter.Item item = ReportContentAdapter.Item.newInstanceCategory(entry.reason);
                item.sum += entry.price;
                item.isExpense = true;
                expenseMap.put(id, item);
            }
        }
        // 収入・支出の順番ソート
        List<Map.Entry<Long, ReportContentAdapter.Item>> incomeSortList = sortLinkedHashMap(incomeMap);
        List<Map.Entry<Long, ReportContentAdapter.Item>> expenseSortList = sortLinkedHashMap(expenseMap);

        // 残高の表示
        String priceString = ValueConverter.formatPrice(mContext, count);
        int priceColor = (count < 0) ? ContextCompat.getColor(mContext, R.color.expense)
                : ContextCompat.getColor(mContext, R.color.primary);
        priceString = (count > 0) ? "+"+priceString : priceString;
        binding.price.setText(priceString);
        binding.price.setTextColor(priceColor);
        binding.topBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HistoryListDataActivity.startForBalance(mContext, mTargetCalendar);
            }
        });

        items.add(incomeSection);
        items.add(incomeSum);
        for (Map.Entry<Long, ReportContentAdapter.Item> entry : incomeSortList) {
            items.add(entry.getValue());
        }

        items.add(ExpenseSection);
        items.add(ExpenseSum);
        for (Map.Entry<Long, ReportContentAdapter.Item> entry : expenseSortList) {
            items.add(entry.getValue());
        }

        binding.reportList.setLayoutManager(new LinearLayoutManager(mContext));
        binding.reportList.addItemDecoration(new DividerDecoration(mContext));
        final ReportContentAdapter adapter = new ReportContentAdapter(mContext, items);
        adapter.setOnItemClickListener(new ReportContentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, ReportContentAdapter.Item item) {
                String reasonName = null;
                int viewType = adapter.getItemViewType(position);
                if (viewType == ReportContentAdapter.Item.VIEW_ITEM_CATEGORY)
                    reasonName = item.reasonName;
                HistoryListDataActivity.start(mContext, mTargetCalendar, reasonName, item.isExpense);
            }
        });
        binding.reportList.setAdapter(adapter);


        return binding.getRoot();
    }

    private List<Map.Entry<Long, ReportContentAdapter.Item>> sortLinkedHashMap(Map<Long, ReportContentAdapter.Item> sourceMap) {
        List<Map.Entry<Long, ReportContentAdapter.Item>> dataList =
                new ArrayList<>(sourceMap.entrySet());
        Collections.sort(dataList, new Comparator<Map.Entry<Long, ReportContentAdapter.Item>>() {

            @Override
            public int compare(Map.Entry<Long, ReportContentAdapter.Item> entry1,
                               Map.Entry<Long, ReportContentAdapter.Item> entry2) {
                long entry1sum = entry1.getValue().sum;
                long entry2sum = entry2.getValue().sum;
                if (entry1sum < entry2sum) {
                    return 1;
                } else if (entry1sum == entry2sum) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });
        return dataList;
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
            private boolean isExpense;

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
        private Context mContext;
        private RecyclerView mRecyclerView;

        public OnItemClickListener mOnItemClickListener;

        public interface OnItemClickListener {
            void onItemClick(View view, int position, Item item);
        }

        public ReportContentAdapter(Context context, List<Item> items) {
            this.items = items;
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

        @Override
        public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case Item.VIEW_ITEM_SECTION_INCOME:
                case Item.VIEW_ITEM_SECTION_EXPENSE:
                    return new BindingHolder(parent.getContext(), parent, R.layout.row_history_section_header);
                case Item.VIEW_ITEM_SUM_INCOME:
                case Item.VIEW_ITEM_SUM_EXPENSE:
                case Item.VIEW_ITEM_CATEGORY:
                    BindingHolder bindingHolder = new BindingHolder(
                            parent.getContext(), parent, R.layout.row_simple_cell);
                    return bindingHolder;
            }
            return null;
        }

        @Override
        public void onBindViewHolder(BindingHolder holder, final int position) {
            final Item item = items.get(position);
            int viewType = holder.getItemViewType();
            switch (viewType) {
                case Item.VIEW_ITEM_SECTION_INCOME: {
                    RowHistorySectionHeaderBinding headerIncomeBinding = (RowHistorySectionHeaderBinding) holder.binding;
                    headerIncomeBinding.name.setText(R.string.Income);
                    break;
                }
                case Item.VIEW_ITEM_SECTION_EXPENSE: {
                    RowHistorySectionHeaderBinding headerExpenseBinding = (RowHistorySectionHeaderBinding) holder.binding;
                    headerExpenseBinding.name.setText(R.string.Expense);
                    break;
                }
                case Item.VIEW_ITEM_SUM_INCOME:
                case Item.VIEW_ITEM_SUM_EXPENSE:
                case Item.VIEW_ITEM_CATEGORY:

                    RowSimpleCellBinding cellBinding = (RowSimpleCellBinding) holder.binding;
                    String priceString = ValueConverter.formatPrice(mContext,item.sum);
                    cellBinding.price.setText(priceString);

                    int priceColor = (item.isExpense) ? ContextCompat.getColor(mContext, R.color.expense)
                            : ContextCompat.getColor(mContext, R.color.primary);
                    cellBinding.price.setTextColor(priceColor);

                    String reasonName = null;
                    if (viewType == Item.VIEW_ITEM_CATEGORY) {
                        reasonName = item.reasonName;
                        cellBinding.name.setText(reasonName);
                    } else {
                        cellBinding.name.setText(mContext.getString(R.string.total));
                    }
                    cellBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mOnItemClickListener != null) {
                                mOnItemClickListener.onItemClick(v, position, item);
                            }
                        }
                    });
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

        public void setOnItemClickListener(final OnItemClickListener listener) {
            mOnItemClickListener = listener;
        }

    }
}
