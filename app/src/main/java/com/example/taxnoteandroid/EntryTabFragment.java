package com.example.taxnoteandroid;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taxnoteandroid.entryTab.ExpenseInEntryTabFragment;
import com.example.taxnoteandroid.entryTab.IncomeInEntryTabFragment;


public class EntryTabFragment extends Fragment {

    public EntryTabFragment() {
        // Required empty public constructor
    }

    public static EntryTabFragment newInstance() {
        EntryTabFragment fragment = new EntryTabFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_entry_tab, container, false);

        TabLayout tabLayout = (TabLayout) v.findViewById(R.id.tab);
        final ViewPager viewPager = (ViewPager) v.findViewById(R.id.pager);
        viewPager.setAdapter(new TabPagerAdapter(getChildFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

        return v;
    }

    class TabPagerAdapter extends FragmentPagerAdapter {

        public TabPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return ExpenseInEntryTabFragment.newInstance();
                case 1:
                    return IncomeInEntryTabFragment.newInstance();
            }
            return EntryTabFragment.newInstance();
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    // @@
                    return "支出";
                case 1:
                    return "収入";
            }
            return super.getPageTitle(position);
        }
    }
}
