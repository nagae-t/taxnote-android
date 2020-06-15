package com.example.taxnoteandroid.entryTab;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.R;
import com.example.taxnoteandroid.TNSimpleDialogFragment;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.helpshift.support.Support;


public class EntryTabFragment extends Fragment {

    private ViewPager mPager;
    private TabPagerAdapter mPagerAdapter;

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
        mPager = (ViewPager) v.findViewById(R.id.pager);
        mPagerAdapter = new TabPagerAdapter(getChildFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        tabLayout.setupWithViewPager(mPager);

        // Reset selected date
        SharedPreferencesManager.saveCurrentSelectedDate(getActivity(),System.currentTimeMillis());

        DialogManager.showReleaseNoteAfterUpdate(getActivity(), getFragmentManager());

        DialogManager.showFirstLaunchMessage(getActivity(),getFragmentManager());

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        // キャンペーンで31件まで入力できる時に説明するから一旦消す
//        DialogManager.showBusinessModelMessage(getActivity(), getFragmentManager());

        DialogManager.showAskAnythingMessage(getActivity(), getFragmentManager());
        DialogManager.showHistoryTabHelpMessage(getActivity(), getFragmentManager());
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (this.isVisible()) {
            if (isVisibleToUser) {
                checkHelpshiftReplyMessage();
            }
        }
    }

    public void afterLogin() {
        EntryTabReasonSelectFragment fragment1 = (EntryTabReasonSelectFragment)mPagerAdapter.instantiateItem(mPager, 0);
        if (fragment1 != null) fragment1.afterLogin();

        EntryTabReasonSelectFragment fragment2 = (EntryTabReasonSelectFragment)mPagerAdapter.instantiateItem(mPager, 1);
        if (fragment2 != null) fragment2.afterLogin();
    }

    public void reloadData() {
        EntryTabReasonSelectFragment fragment1 = (EntryTabReasonSelectFragment)mPagerAdapter.instantiateItem(mPager, 0);
        fragment1.reloadData();

        EntryTabReasonSelectFragment fragment2 = (EntryTabReasonSelectFragment)mPagerAdapter.instantiateItem(mPager, 1);
        fragment2.reloadData();
    }

    private class TabPagerAdapter extends FragmentPagerAdapter {

        public TabPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return EntryTabReasonSelectFragment.newInstance(true);
                case 1:
                    return EntryTabReasonSelectFragment.newInstance(false);
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
                    return getString(R.string.Expense);
                case 1:
                    return getString(R.string.Income);
            }
            return super.getPageTitle(position);
        }
    }


    //--------------------------------------------------------------//
    //    -- Help --
    //--------------------------------------------------------------//

    private void checkHelpshiftReplyMessage() {

        // Check if the Hhelpshit reply message exists
        if (Support.getNotificationCount() != 0) {

            // Custom Alert
            final TNSimpleDialogFragment dialogFragment = TNSimpleDialogFragment.newInstance();
            dialogFragment.setTitle(getString(R.string.Helpshift_ReplyTitle));
            dialogFragment.setMessage(getString(R.string.Helpshift_ReplyMessage));

            dialogFragment.setCloseToFinish(true);
            dialogFragment.setPositiveBtnText("OK");

            dialogFragment.setDialogListener(new TNSimpleDialogFragment.TNSimpleDialogListener() {
                @Override
                public void onPositiveBtnClick(DialogInterface dialogInterface, int i, String tag) {
                    dialogInterface.dismiss();
                    Support.showConversation(getActivity());
                }
                @Override
                public void onNeutralBtnClick(DialogInterface dialogInterface, int i, String tag) {}
                @Override
                public void onNegativeBtnClick(DialogInterface dialogInterface, int i, String tag) {}
                @Override
                public void onDialogCancel(DialogInterface dialogInterface, String tag) {}
                @Override
                public void onDialogDismiss(DialogInterface dialogInterface, String tag) {}
            });

            dialogFragment.show(getFragmentManager(), null);
        }
    }

}
