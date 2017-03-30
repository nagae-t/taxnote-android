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
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.example.taxnoteandroid.Library.BroadcastUtil;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.TNAppNotification;
import com.example.taxnoteandroid.Library.taxnote.TNApiModel;
import com.example.taxnoteandroid.dataManager.DefaultDataInstaller;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityMainBinding;
import com.example.taxnoteandroid.entryTab.EntryTabFragment;
import com.helpshift.support.Support;
import com.kobakei.ratethisapp.RateThisApp;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.lang.reflect.Field;

import static com.example.taxnoteandroid.R.string.report;
import static com.example.taxnoteandroid.TaxnoteConsts.MIXPANEL_TOKEN;

public class MainActivity extends DefaultCommonActivity {

    private ActivityMainBinding binding;
    private TabPagerAdapter mTabPagerAdapter;
    private int mBottomNaviSelected = 0;
    private boolean mGraphMenuIsExpense = true;

    public static final String BROADCAST_AFTER_LOGIN
            = "broadcast_main_after_login";
    public static final String BROADCAST_REPORT_RELOAD
            = "broadcast_main_report_reload";
    public static final String BROADCAST_RESTART_APP
            = "broadcast_main_restart_app";
    public static final String BROADCAST_SWITCH_GRAPH_EXPENSE
            = "broadcast_main_switch_graph_expense";

    private final BroadcastReceiver mAfterLoginReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isLoggingIn = intent.getBooleanExtra(BroadcastUtil.KEY_IS_LOGGING_IN, false);
            afterLogin(isLoggingIn);
        }
    };

    /**
     * Broadcast for get new home timeline
     */
    private final BroadcastReceiver mReportReloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reportReload();
        }
    };
    private final BroadcastReceiver mSwitchGraphExpenseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean oldVal = SharedPreferencesManager.getGraphReportIsExpenseType(getApplicationContext());
            mGraphMenuIsExpense = !oldVal;
            reportSwitchView(mGraphMenuIsExpense);
        }
    };

    /**
     * Broadcast to restart app
     */
    private final BroadcastReceiver mRestartAppReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
            System.exit(0);
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

        registerReceiver(mAfterLoginReceiver, new IntentFilter(BROADCAST_AFTER_LOGIN));
        registerReceiver(mReportReloadReceiver, new IntentFilter(BROADCAST_REPORT_RELOAD));
        registerReceiver(mRestartAppReceiver, new IntentFilter(BROADCAST_RESTART_APP));
        registerReceiver(mSwitchGraphExpenseReceiver, new IntentFilter(BROADCAST_SWITCH_GRAPH_EXPENSE));

        DefaultDataInstaller.installDefaultUserAndCategories(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mGraphMenuIsExpense = SharedPreferencesManager.getGraphReportIsExpenseType(this);
        setBottomNavigation();

        TNAppNotification.cancel(this, TNAppNotification.DAILY_ALERT_INPUT_FORGET_ID);

        // 起動時にデータの同期を行う
        TNApiModel apiModel = new TNApiModel(this);
        apiModel.setIsSyncing(false);
        apiModel.syncData(false, null);
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
        MenuItem profitLossSettingsMenu = menu.findItem(R.id.action_profit_loss_settings);
        MenuItem profitLossExportMenu = menu.findItem(R.id.action_profit_loss_export);
        MenuItem isExpenseMenu = menu.findItem(R.id.action_report_is_expense);

        helpMenu.setVisible(false);
        exportMenu.setVisible(false);
        searchMenu.setVisible(false);
        periodDivMenu.setVisible(false);
        profitLossSettingsMenu.setVisible(false);
        profitLossExportMenu.setVisible(false);
        isExpenseMenu.setVisible(false);

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
                profitLossSettingsMenu.setVisible(true);
                profitLossExportMenu.setVisible(true);
                break;
            case 3: // グラフ
                periodDivMenu.setVisible(true);
                isExpenseMenu.setVisible(true);
                String menuTitle = (mGraphMenuIsExpense)
                        ? getString(R.string.Income) : getString(R.string.Expense);
                isExpenseMenu.setTitle(menuTitle);
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
                reportSwitchPeriod(EntryDataManager.PERIOD_TYPE_YEAR);
                break;
            case R.id.divide_by_month:
                reportSwitchPeriod(EntryDataManager.PERIOD_TYPE_MONTH);
                break;
            case R.id.divide_by_day:
                reportSwitchPeriod(EntryDataManager.PERIOD_TYPE_DAY);
                break;
            case R.id.action_report_is_expense:
                if (mGraphMenuIsExpense) {
                    reportSwitchView(false);
                } else {
                    reportSwitchView(true);
                }
                break;
            case R.id.action_profit_loss_settings:
                ProfitLossSettingsActivity.start(this);
                break;
            case R.id.action_profit_loss_export:
                long[] startEndDate = getReportStartEndDate();

                if (startEndDate == null || startEndDate[0] <= 0) {
                    DialogManager.showToast(this, getString(R.string.no_data_to_export_message));
                    break;
                }

                ProfitLossExportActivity.start(this, startEndDate);
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

                int itemId = item.getItemId();
                mBottomNaviSelected = itemId;
                switch (itemId) {
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
                        reportReload();
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

    private void afterLogin(boolean isLoggingIn) {
        if (mTabPagerAdapter == null) return;
        CustomViewPager pager = binding.pager;

        EntryTabFragment entryTabFragment = (EntryTabFragment) mTabPagerAdapter
                .instantiateItem(pager, 0);
        if (entryTabFragment != null)
            entryTabFragment.afterLogin();

        HistoryTabFragment historyTabFragment = (HistoryTabFragment) mTabPagerAdapter
                .instantiateItem(pager, 1);
        if (historyTabFragment != null)
            historyTabFragment.afterLogin();

        ReportFragment reportFragment = (ReportFragment) mTabPagerAdapter
                .instantiateItem(pager, 2);
        if (reportFragment != null)
            reportFragment.reloadData();

        GraphTabFragment graphFragment = (GraphTabFragment) mTabPagerAdapter
                .instantiateItem(pager, 3);
        if (graphFragment != null)
            graphFragment.reloadData();
    }

    private void reportSwitchPeriod(int type) {
        if (mTabPagerAdapter == null) return;

        CustomViewPager pager = binding.pager;
        if (mBottomNaviSelected == R.id.tab3) {
            ReportFragment reportFragment = (ReportFragment) mTabPagerAdapter
                    .instantiateItem(pager, pager.getCurrentItem());
            if (reportFragment == null) return;

            reportFragment.switchReportPeriod(type);
        } else if (mBottomNaviSelected == R.id.tab4) {
            GraphTabFragment graphFragment = (GraphTabFragment) mTabPagerAdapter
                    .instantiateItem(pager, pager.getCurrentItem());
            if (graphFragment == null) return;

            graphFragment.switchDataView(type);
        }
    }

    private void reportSwitchView(boolean isExpense) {
        if (mTabPagerAdapter == null) return;

        CustomViewPager pager = binding.pager;
        if (mBottomNaviSelected == R.id.tab4) {
            GraphTabFragment graphFragment = (GraphTabFragment) mTabPagerAdapter
                    .instantiateItem(pager, pager.getCurrentItem());
            if (graphFragment == null) return;

            graphFragment.switchDataView(isExpense);
            mGraphMenuIsExpense = SharedPreferencesManager.getGraphReportIsExpenseType(this);
            invalidateOptionsMenu();
        }
    }

    private void reportReload() {
        if (mTabPagerAdapter == null) return;

        CustomViewPager pager = binding.pager;
        if (mBottomNaviSelected == R.id.tab3) {
            ReportFragment reportFragment =
                    (ReportFragment) mTabPagerAdapter.instantiateItem(pager, 2);
            if (reportFragment == null) return;

            reportFragment.reloadData();
        } else if (mBottomNaviSelected == R.id.tab4) {
            GraphTabFragment graphFragment = (GraphTabFragment) mTabPagerAdapter
                    .instantiateItem(pager, pager.getCurrentItem());
            if (graphFragment == null) return;

            graphFragment.reloadData();
        }
    }

    private long[] getReportStartEndDate() {
        if (mBottomNaviSelected == R.id.tab3) {
            ReportFragment reportFragment =
                    (ReportFragment) mTabPagerAdapter.instantiateItem(binding.pager, 2);
            if (reportFragment == null) return new long[2];

            return reportFragment.getStartEndDate();
        }
        return new long[2];
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
        RateThisApp.Config config = new RateThisApp.Config(3, 10);
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
    protected void onNewIntent(Intent intent) {
        TNAppNotification.cancel(this, TNAppNotification.DAILY_ALERT_INPUT_FORGET_ID);
        super.onNewIntent(intent);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mAfterLoginReceiver);
        unregisterReceiver(mReportReloadReceiver);
        unregisterReceiver(mRestartAppReceiver);
        unregisterReceiver(mSwitchGraphExpenseReceiver);
        super.onDestroy();
    }
}
