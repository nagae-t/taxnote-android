package com.example.taxnoteandroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.databinding.RowHistoryCellBinding;
import com.example.taxnoteandroid.databinding.RowHistorySectionHeaderBinding;
import com.example.taxnoteandroid.model.Entry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class HistoryTabFragment extends Fragment {
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
        View v = inflater.inflate(R.layout.fragment_history_tab, container, false);

        EntryDataManager entryDataManager = new EntryDataManager(getContext());
        Log.d("test", entryDataManager.findAll().toString());

        List<Entry> entries = entryDataManager.findAll();

        List<Item> items = new ArrayList<>();
        Map<String, List<Entry>> map2 = new LinkedHashMap<>();
        for (Entry entry : entries) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(entry.date);

            int y = calendar.get(Calendar.YEAR);
            int m = calendar.get(Calendar.MONTH);
            int d = calendar.get(Calendar.DAY_OF_MONTH);

            StringBuilder sb = new StringBuilder().append(y).append(m).append(d);
            String date = sb.toString();

            if (!map2.containsKey(date)) {
                List<Entry> entryList = new ArrayList<>();
                entryList.add(entry);
                map2.put(date, entryList);
            } else {
                List<Entry> entryList = map2.get(date);
                entryList.add(entry);
                map2.put(date, entryList);
            }
        }

        for (Map.Entry<String, List<Entry>> e : map2.entrySet()) {
            Item headerItem = new Item();
            Header header = new Header();
            headerItem.header = header;
            header.date = e.getKey();

            items.add(headerItem);

            for (Entry entry : e.getValue()) {
                header.sum += entry.price;
                Item cellItem = new Item();
                Cell cell = new Cell();
                cellItem.cell = cell;
                cell.entry = entry;
                items.add(cellItem);
            }
        }

        Log.d("test", map2.toString());
        Log.d("test", items.toString());

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.history);
        HistoryAdapter historyAdapter = new HistoryAdapter(items);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(historyAdapter);

        return v;
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
        long sum;

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

        public HistoryAdapter(List<Item> items) {
            this.items = items;
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
            switch (holder.getItemViewType()) {
                case VIEW_ITEM_HEADER: {
                    RowHistorySectionHeaderBinding binding = (RowHistorySectionHeaderBinding) holder.binding;
                    Item item = items.get(position);
                    binding.name.setText(item.header.date);
                    binding.price.setText(item.header.sum + "");
                }
                break;
                case VIEW_ITEM_CELL: {
                    RowHistoryCellBinding binding = (RowHistoryCellBinding) holder.binding;
                    Item item = items.get(position);
                    binding.price.setText(item.cell.entry.price + "");
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