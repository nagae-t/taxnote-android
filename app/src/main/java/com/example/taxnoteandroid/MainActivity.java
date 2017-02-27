package com.example.taxnoteandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.example.taxnoteandroid.dataManager.DefaultDataInstaller;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityMainBinding;
import com.example.taxnoteandroid.entryTab.EntryTabFragment;
import com.helpshift.support.Support;
import com.kobakei.ratethisapp.RateThisApp;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.lang.reflect.Field;

import static com.example.taxnoteandroid.R.string.report;
import static com.example.taxnoteandroid.TaxnoteConsts.MIXPANEL_TOKEN;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private TabPagerAdapter mTabPagerAdapter;

    public static final String BROADCAST_REPORT_RELOAD
            = "broadcast_main_report_reload";

    /**
     * Broadcast for get new home timeline
     */
    private final BroadcastReceiver mReportReloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reportReload();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        setMixpanel();
        setRateThisApp();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerReceiver(mReportReloadReceiver, new IntentFilter(BROADCAST_REPORT_RELOAD));

        DefaultDataInstaller.installDefaultUserAndCategories(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setBottomNavigation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem helpMenu = menu.findItem(R.id.help_in_entry_tab);
        MenuItem exportMenu = menu.findItem(R.id.data_export);
        MenuItem searchMenu = menu.findItem(R.id.action_search);
        MenuItem periodDivMenu = menu.findItem(R.id.action_period_div);

        helpMenu.setVisible(false);
        exportMenu.setVisible(false);
        searchMenu.setVisible(false);
        periodDivMenu.setVisible(false);

        switch (binding.pager.getCurrentItem()) {

            case 0: // 入力
                helpMenu.setVisible(true);
                break;
            case 1: // 仕訳帳
                exportMenu.setVisible(true);
                searchMenu.setVisible(true);
                break;
            case 2: // 損益表
                periodDivMenu.setVisible(true);
                break;
            case 3: // グラフ
                break;
            case 4: // 設定
                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.help_in_entry_tab:
                Support.showFAQSection(this,"22");
                break;

            case R.id.data_export:
                Intent intent = new Intent(this, DataExportActivity.class);
                startActivity(intent);
                break;

            case R.id.action_search:
                SearchEntryActivity.start(this);
                break;

            // 損益表のメニューオプションが選択されたとき
            case R.id.divide_by_year:
                reportSwitchPeriod(ReportFragment.PERIOD_TYPE_YEAR);
                break;
            case R.id.divide_by_month:
                reportSwitchPeriod(ReportFragment.PERIOD_TYPE_MONTH);
                break;
            case R.id.divide_by_day:
                reportSwitchPeriod(ReportFragment.PERIOD_TYPE_DAY);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    //--------------------------------------------------------------//
    //    -- Bottom Navigation --
    //--------------------------------------------------------------//

    private void setBottomNavigation() {

        // Set pager
        mTabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager());
        binding.pager.setAdapter(mTabPagerAdapter);
        binding.pager.beginFakeDrag();
        binding.pager.setOffscreenPageLimit(mTabPagerAdapter.getCount());

        // Set the default title
        setTitle(getString(R.string.Entry));

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.tab1:
                        binding.pager.setCurrentItem(0, false);
                        setTitle(getString(R.string.Entry));
                        break;
                    case R.id.tab2:
                        binding.pager.setCurrentItem(1, false);
                        setTitle(getString(R.string.History));
                        break;
                    case R.id.tab3:
                        binding.pager.setCurrentItem(2, false);
                        setTitle(getString(R.string.report));
                        reportReload();
                        break;
                    case R.id.tab4:
                        binding.pager.setCurrentItem(3, false);
                        setTitle(getString(R.string.Graph));
                        break;
                    case R.id.tab5:
                        binding.pager.setCurrentItem(4, false);
                        setTitle(getString(R.string.Settings));
                        break;
                }
                invalidateOptionsMenu();
                return true;
            }
        });
    }

    /**
     * Clear animation of BottomNavigationView
     * http://stackoverflow.com/questions/41649494/how-to-remove-icon-animation-for-bottom-navigation-view-in-android
     */
    public static class BottomNavigationViewHelper {

        public static void disableShiftMode(BottomNavigationView view) {
            BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
            try {
                Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
                shiftingMode.setAccessible(true);
                shiftingMode.setBoolean(menuView, false);
                shiftingMode.setAccessible(false);
                for (int i = 0; i < menuView.getChildCount(); i++) {
                    BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                    //noinspection RestrictedApi
                    item.setShiftingMode(false);
                    // set once again checked value, so view will be updated
                    //noinspection RestrictedApi
                    item.setChecked(item.getItemData().isChecked());
                }
            } catch (NoSuchFieldException e) {
                Log.e("BNVHelper", "Unable to get shift mode field", e);
            } catch (IllegalAccessException e) {
                Log.e("BNVHelper", "Unable to change value of shift mode", e);
            }
        }
    }

    private void reportSwitchPeriod(int type) {
        if (mTabPagerAdapter == null) return;

        CustomViewPager pager = binding.pager;
        ReportFragment reportFragment =
                (ReportFragment) mTabPagerAdapter.instantiateItem(pager, pager.getCurrentItem());
        if (reportFragment == null) return;

        reportFragment.switchReportPeriod(type);
    }

    private void reportReload() {
        if (mTabPagerAdapter == null) return;

        CustomViewPager pager = binding.pager;
        ReportFragment reportFragment =
                (ReportFragment) mTabPagerAdapter.instantiateItem(pager, 2);
        if (reportFragment == null) return;

        reportFragment.reloadData();
    }

    class TabPagerAdapter extends FragmentPagerAdapter {

        public TabPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return EntryTabFragment.newInstance();
                case 1:
                    return HistoryTabFragment.newInstance();
                case 2:
                    return ReportFragment.newInstance();
                case 3:
                    return GraphTabFragment.newInstance();
                case 4:
                    return SettingsTabFragment.newInstance();
            }
            return EntryTabFragment.newInstance();
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.Entry);
                case 1:
                    return getString(R.string.History);
                case 2:
                    return getString(report);
                case 3:
                    return getString(R.string.Graph);
                case 4:
                    return getString(R.string.Settings);
            }
            return super.getPageTitle(position);
        }


    }


    //--------------------------------------------------------------//
    //    -- Library --
    //--------------------------------------------------------------//

    private void setMixpanel() {

        MixpanelAPI mixpanel = MixpanelAPI.getInstance(this, MIXPANEL_TOKEN);

        //QQ 自分のデバイスだけTrackingを無視したい
//        // JSONOBjectでエラーがでますねん
//        JSONObject props = new JSONObject();
//        props.put("$ignore", "true");
//        mixpanel.registerSuperProperties(props);

        if (SharedPreferencesManager.isFirstLaunchDone(this)) {
            return;
        }

        mixpanel.track("First Launch");
        SharedPreferencesManager.saveFirstLaunchDone(this);
    }

    private void setRateThisApp() {

        RateThisApp.onStart(this);
        RateThisApp.showRateDialogIfNeeded(this);

        // Custom message
        RateThisApp.Config config = new RateThisApp.Config();
        config.setTitle(R.string.rate_app_title);
        config.setMessage(R.string.rate_app_message);
        config.setYesButtonText(R.string.rate_app_yes_button);
        config.setNoButtonText(R.string.rate_app_no_button);
        config.setCancelButtonText(R.string.rate_app_cancel_button);
        RateThisApp.init(config);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (binding.pager == null) return super.onKeyDown(keyCode, event);

        // バックキーで「入力」画面に戻るように
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            int currentItem = binding.pager.getCurrentItem();
            if (currentItem != 0) {
                BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
                bottomNavigationView.findViewById(R.id.tab1).performClick();
                return false;
            }

        }
        return super.onKeyDown(keyCode, event);

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReportReloadReceiver);
        super.onDestroy();
    }
}
