package com.example.taxnoteandroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.databinding.FragmentHistoryTabBinding;
import com.example.taxnoteandroid.databinding.RowHistoryCellBinding;
import com.example.taxnoteandroid.databinding.RowHistorySectionHeaderBinding;
import com.example.taxnoteandroid.model.Entry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class HistoryTabFragment extends Fragment {

    private FragmentHistoryTabBinding binding;

    public HistoryTabFragment() {
        // Required empty public constructor
    }

    public static HistoryTabFragment newInstance() {
        HistoryTabFragment fragment = new HistoryTabFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoryTabBinding.inflate(inflater,container, false);
        binding.history.setLayoutManager(new LinearLayoutManager(getContext()));

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        loadHistoryData();
    }


    //--------------------------------------------------------------//
    //    -- History View --
    //--------------------------------------------------------------//

    private void loadHistoryData() {

        EntryDataManager entryDataManager   = new EntryDataManager(getContext());
        List<Entry> entries                 = entryDataManager.findAll(getContext());

        if (entries == null || entries.isEmpty()) {

            binding.empty.setText(getResources().getString(R.string.history_data_empty));
            binding.empty.setVisibility(View.VISIBLE);
        } else {
            binding.empty.setVisibility(View.GONE);
        }

        final List<Item> items = new ArrayList<>();
        Map<String, List<Entry>> map2 = new LinkedHashMap<>();

        // 入力日ごとにグルーピング
        for (Entry entry : entries) {

            // Format date to string
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources().getString(R.string.date_string_format_to_month));
            String dateString = simpleDateFormat.format(entry.date);

            if (!map2.containsKey(dateString)) {
                List<Entry> entryList = new ArrayList<>();
                entryList.add(entry);
                map2.put(dateString, entryList);
            } else {
                List<Entry> entryList = map2.get(dateString);
                entryList.add(entry);
                map2.put(dateString, entryList);
            }
        }

        // RecyclerViewに渡すためにMapをListに変換する
        // Itemって中にheaderかCellかで分けて入れてる
        for (Map.Entry<String, List<Entry>> e : map2.entrySet()) {
            Item headerItem = new Item();
            Header header = new Header();
            headerItem.header = header;
            header.date = e.getKey();

            items.add(headerItem);

            long totalPrice = 0;

            for (Entry entry : e.getValue()) {

                // Calculate total price
                if (entry.isExpense) {
                    totalPrice -= entry.price;
                } else {
                    totalPrice += entry.price;
                }

                Item cellItem = new Item();
                Cell cell = new Cell();
                cellItem.cell = cell;
                cell.entry = entry;
                items.add(cellItem);
            }

            // Format the totalPrice
            header.sum = ValueConverter.formatPrice(totalPrice);
        }

        HistoryAdapter historyAdapter = new HistoryAdapter(items);
        historyAdapter.setOnItemClickRecyclerAdapterListener(new OnItemClickRecyclerAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                Item item = items.get(position);
                startActivity(EntryEditActivity.createIntent(getContext(), item.cell.entry));
            }
        });
        binding.history.setAdapter(historyAdapter);
    }

        class Item {
        Header header;
        Cell cell;

        @Override
        public String toString() {
            return "Item{" +
                    "header=" + header +
                    ", cell=" + cell +
                    '}';
        }
    }

    class Header {
        String date;
        String sum;

        @Override
        public String toString() {
            return "Header{" +
                    "date='" + date + '\'' +
                    ", sum=" + sum +
                    '}';
        }
    }

    class Cell {
        Entry entry;

        @Override
        public String toString() {
            return "Cell{" +
                    "entry=" + entry +
                    '}';
        }
    }

    class HistoryAdapter extends RecyclerView.Adapter<BindingHolder> {

        private static final int VIEW_ITEM_HEADER = 1;
        private static final int VIEW_ITEM_CELL = 2;

        private List<Item> items;
        private OnItemClickRecyclerAdapterListener onItemClickRecyclerAdapterListener;

        public HistoryAdapter(List<Item> items) {
            this.items = items;
        }

        public void setOnItemClickRecyclerAdapterListener(OnItemClickRecyclerAdapterListener onItemClickRecyclerAdapterListener) {
            this.onItemClickRecyclerAdapterListener = onItemClickRecyclerAdapterListener;
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

            switch (holder.getItemViewType()) {

                case VIEW_ITEM_HEADER: {
                    RowHistorySectionHeaderBinding binding = (RowHistorySectionHeaderBinding) holder.binding;
                    Item item = items.get(position);
                    binding.name.setText(item.header.date);
                    binding.price.setText(item.header.sum);
                }
                break;

                case VIEW_ITEM_CELL: {

                    RowHistoryCellBinding binding = (RowHistoryCellBinding) holder.binding;
                    Item item = items.get(position);

                    binding.getRoot().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (onItemClickRecyclerAdapterListener != null) {
                                onItemClickRecyclerAdapterListener.onItemClick(v, position);
                            }
                        }
                    });

                    if (item.cell.entry.isExpense) {
                        binding.name.setText(item.cell.entry.reason.name + " / " + item.cell.entry.account.name);
                    } else {
                        binding.name.setText(item.cell.entry.account.name + " / " + item.cell.entry.reason.name);
                    }


                    if (TextUtils.isEmpty(item.cell.entry.memo)) {
                        binding.memo.setVisibility(View.GONE);
                    } else {
                        binding.memo.setVisibility(View.VISIBLE);
                        binding.memo.setText(item.cell.entry.memo);
                    }

                    // Create price string
                    String priceString                  = ValueConverter.formatPriceWithSymbol(item.cell.entry.price, item.cell.entry.isExpense);
                    binding.price.setText(priceString);

                    // Set price color
                    if (item.cell.entry.isExpense) {
                        binding.price.setTextColor(ContextCompat.getColor(getContext(), R.color.expense));
                    } else  {
                        binding.price.setTextColor(ContextCompat.getColor(getContext(), R.color.primary));
                    }
                }
                break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            Item item = items.get(position);
            if (item.header != null) {
                return VIEW_ITEM_HEADER;
            }
            return VIEW_ITEM_CELL;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }
}