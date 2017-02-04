package com.example.taxnoteandroid;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.databinding.FragmentReportBinding;
import com.example.taxnoteandroid.model.Entry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportFragment extends Fragment {

    private FragmentReportBinding binding;

    public ReportFragment() {
    }

    public static ReportFragment newInstance() {
        ReportFragment fragment = new ReportFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReportBinding.inflate(inflater, container, false);

        Context context = getContext();
//        binding.pager.setAdapter(new ReportContentFragmentPagerAdapter(getChildFragmentManager(), Mode.YEAR, Mode.YEAR.getTitles(context)));

        EntryDataManager entryDataManager = new EntryDataManager(context);
        List<Entry> entries = entryDataManager.findAll(context, null, false);

        Map<Calendar, List<Entry>> map = new LinkedHashMap<>();
        for (Entry entry : entries) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(entry.date);
            calendar.set(calendar.get(Calendar.YEAR), 0, 0, 0, 0, 0);

            if (map.containsKey(calendar)) {
                map.get(calendar).add(entry);
            } else {
                List<Entry> list = new ArrayList<>();
                list.add(entry);
                map.put(calendar, list);
            }
        }

        binding.pager.setAdapter(new ReportContentFragmentPagerAdapter2(getChildFragmentManager(), Mode.YEAR, map));

        return binding.getRoot();
    }

    public static class ReportContentConfig {
        public Mode mode;
        public List<String> titles;
        public Calendar calendar;
    }

    public enum Mode2 {
        YEAR {
            @Override
            public ReportContentConfig getReportContentConfig(Context context) {
                return null;
            }
        }, MONTH {
            @Override
            public ReportContentConfig getReportContentConfig(Context context) {
                return null;
            }
        }, DAY {
            @Override
            public ReportContentConfig getReportContentConfig(Context context) {
                return null;
            }
        };

        public abstract ReportContentConfig getReportContentConfig(Context context);
    }

    public enum Mode {
        YEAR {
            @Override
            public List<String> getTitles(Context context) {
                EntryDataManager entryDataManager = new EntryDataManager(context);
                List<Entry> entries = entryDataManager.findAll(context, null, true);
                List<String> years = new ArrayList<>();
                for (Entry entry : entries) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(entry.date);
                    String year = Integer.toString(calendar.get(Calendar.YEAR));
                    if (!years.contains(year)) {
                        years.add(year);
                    }
                }
                return years;
            }
        }, MONTH {
            @Override
            public List<String> getTitles(Context context) {
                return null;
            }
        }, DAY {
            @Override
            public List<String> getTitles(Context context) {
                return null;
            }
        };

        public abstract List<String> getTitles(Context context);
    }

    public class ReportContentFragmentPagerAdapter extends FragmentPagerAdapter {

        private final Mode mode;
        private final List<String> titles;
        private final int count;

        public ReportContentFragmentPagerAdapter(FragmentManager fm, Mode mode, List<String> titles) {
            super(fm);
            this.mode = mode;
            this.titles = titles;
            this.count = titles.size();
        }

        @Override
        public Fragment getItem(int position) {
            return ReportContentFragment.newInstance(mode);
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    public class ReportContentFragmentPagerAdapter2 extends FragmentPagerAdapter {

        private final Mode mode;
        private final Map<Calendar, List<Entry>> map;
        private final int count;
        private final Calendar[] calendars;

        public ReportContentFragmentPagerAdapter2(FragmentManager fm, Mode mode, Map<Calendar, List<Entry>> map) {
            super(fm);
            this.mode = mode;
            this.map = map;
            this.count = map.size();
            calendars = map.keySet().toArray(new Calendar[map.keySet().size()]);
        }

        @Override
        public Fragment getItem(int position) {
            return ReportContentFragment.newInstance(map.get(calendars[position]));
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return Integer.toString(calendars[position].get(Calendar.YEAR));
        }
    }
}
