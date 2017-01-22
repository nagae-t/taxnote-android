package com.example.taxnoteandroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taxnoteandroid.databinding.FragmentReportBinding;

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
        binding.pager.setAdapter(new ReportContentFragmentPagerAdapter(getChildFragmentManager()));

        return binding.getRoot();
    }

    public class ReportContentFragmentPagerAdapter extends FragmentPagerAdapter {

        public ReportContentFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ReportContentFragment.newInstance();
        }

        @Override
        public int getCount() {
            return 10;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return position + "";
        }
    }
}
