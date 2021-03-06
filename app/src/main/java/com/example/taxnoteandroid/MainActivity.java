package com.example.taxnoteandroid;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.PermissionChecker;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.taxnoteandroid.Library.BroadcastUtil;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.TNAppNotification;
import com.example.taxnoteandroid.Library.TNGoogleApiClient;
import com.example.taxnoteandroid.Library.UpgradeManger;
import com.example.taxnoteandroid.Library.billing.IabHelper;
import com.example.taxnoteandroid.Library.billing.IabResult;
import com.example.taxnoteandroid.Library.billing.Inventory;
import com.example.taxnoteandroid.Library.billing.Purchase;
import com.example.taxnoteandroid.Library.taxnote.TNApiUser;
import com.example.taxnoteandroid.dataManager.DefaultDataInstaller;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityMainBinding;
import com.example.taxnoteandroid.entryTab.EntryTabFragment;
import com.example.taxnoteandroid.misc.CustomTabsUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.kobakei.ratethisapp.RateThisApp;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.lang.reflect.Field;
import java.util.Calendar;

import static com.example.taxnoteandroid.R.string.report;
import static com.example.taxnoteandroid.TaxnoteConsts.MIXPANEL_TOKEN;

public class MainActivity extends DefaultCommonActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    private ActivityMainBinding binding;
    private TabPagerAdapter mTabPagerAdapter;
    private int mBottomNaviSelected = 0;
    private boolean mGraphMenuIsExpense = true;
    private ProjectDataManager mProjectManager;
    private SearchView searchView;

    private IabHelper mBillingHelper;
    private TNGoogleApiClient tnGoogleApi;

    private AdRequest mAdRequest = null;

    private AppUpdateManager appUpdateManager;
    private static final int REQUEST_CODE_FLEXIBLE_UPDATE = 111;

    public static final String BROADCAST_AFTER_LOGIN
            = "broadcast_main_after_login";
    public static final String BROADCAST_REPORT_RELOAD
            = "broadcast_main_report_reload";
    public static final String BROADCAST_RESTART_APP
            = "broadcast_main_restart_app";
    public static final String BROADCAST_SWITCH_GRAPH_EXPENSE
            = "broadcast_main_switch_graph_expense";
    public static final String BROADCASE_DATA_PERIOD_SCROLLED
            = "broadcast_main_data_period_scrolled";

    public static final String BROADCAST_ADVIEW_TOGGLE
            = "broadcast_main_adview_toggle";

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

    private final BroadcastReceiver mDataPeriodScrolledReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int position = intent.getIntExtra(
                    BroadcastUtil.KEY_DATA_PERIOD_SCROLLED_POSITION, 0);
            int target = intent.getIntExtra(
                    BroadcastUtil.KEY_DATA_PERIOD_SCROLLED_TARGET, 0);
            reportOnPageScrolled(target, position);
        }
    };

    /**
     * Broadcast to restart app
     */
    private final BroadcastReceiver mRestartAppReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finishAffinity();
            System.exit(0);
        }
    };

    /**
     * Check in app billing finished listener
     */
    private IabHelper.QueryInventoryFinishedListener mInventoryListener = new IabHelper.QueryInventoryFinishedListener() {

        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) return;


            boolean hasTaxnotePlus = false;
            Purchase purchasePlus = inventory.getPurchase(UpgradeManger.SKU_TAXNOTE_PLUS_ID);
            if (purchasePlus != null) {
                new CheckBillingAsyncTask().execute(purchasePlus);
                hasTaxnotePlus = true;
            }

            Purchase purchasePlus1 = inventory.getPurchase(UpgradeManger.SKU_TAXNOTE_PLUS_ID1);
            if (purchasePlus1 != null) {
                new CheckBillingAsyncTask().execute(purchasePlus1);
                hasTaxnotePlus = true;
            }

            Purchase purchasePlus2 = inventory.getPurchase(UpgradeManger.SKU_TAXNOTE_PLUS_ID2);
            if (purchasePlus2 != null) {
                new CheckBillingAsyncTask().execute(purchasePlus2);
                hasTaxnotePlus = true;
            }

            Purchase purchasePlus3 = inventory.getPurchase(UpgradeManger.SKU_TAXNOTE_PLUS_ID3);
            if (purchasePlus3 != null) {
                new CheckBillingAsyncTask().execute(purchasePlus3);
                hasTaxnotePlus = true;
            }
            if (!hasTaxnotePlus) {
                SharedPreferencesManager.saveTaxnotePlusExpiryTime(getApplicationContext(), 0);
            }

            Purchase purchaseCloud = inventory.getPurchase(UpgradeManger.SKU_TAXNOTE_CLOUD_ID);
            if (purchaseCloud != null)
                new CheckBillingAsyncTask().execute(purchaseCloud);
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
        registerReceiver(mDataPeriodScrolledReceiver, new IntentFilter(BROADCASE_DATA_PERIOD_SCROLLED));

        DefaultDataInstaller.installDefaultUserAndCategories(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mGraphMenuIsExpense = SharedPreferencesManager.getGraphReportIsExpenseType(this);
        mProjectManager = new ProjectDataManager(this);
        setBottomNavigation();

        TNAppNotification.cancel(this, TNAppNotification.DAILY_ALERT_INPUT_FORGET_ID);

        checkInAppBilling();

        checkAppUpdate();

    }

    private void checkInAppBilling() {
        tnGoogleApi = new TNGoogleApiClient(this);
        mBillingHelper = new IabHelper(this, UpgradeManger.getGooglePlayLicenseKey());
        try {
            mBillingHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {

                public void onIabSetupFinished(IabResult result) {

                    if (result.isFailure()) return;

                    try {
                        mBillingHelper.queryInventoryAsync(mInventoryListener);
                    } catch (IabHelper.IabAsyncInProgressException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem helpMenu = menu.findItem(R.id.help_in_settings_tab);

        MenuItem searchMenu = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchMenu.getActionView();
        MenuItem exportMenu = menu.findItem(R.id.data_export);
        MenuItem delMenu = menu.findItem(R.id.data_delete);
        String targetName = getHistoryTargetName();
        String exportTitle = getString(R.string.export_current_something, targetName);
        String delTitle = getString(R.string.delete_current_something, targetName);
        exportMenu.setTitle(exportTitle);
        delMenu.setTitle(delTitle);

        MenuItem periodDivMenu = menu.findItem(R.id.action_period_div);
        MenuItem profitLossSettingsMenu = menu.findItem(R.id.action_profit_loss_settings);
        MenuItem profitLossExportMenu = menu.findItem(R.id.action_profit_loss_export);
        MenuItem isExpenseMenu = menu.findItem(R.id.action_report_is_expense);

        helpMenu.setVisible(false);
        exportMenu.setVisible(false);
        delMenu.setVisible(false);
        searchMenu.setVisible(false);
        periodDivMenu.setVisible(false);
        profitLossSettingsMenu.setVisible(false);
        profitLossExportMenu.setVisible(false);
        isExpenseMenu.setVisible(false);

        switch (binding.pager.getCurrentItem()) {

            case 0: // ??????
                break;
            case 1: // ?????????
                searchMenu.setVisible(true);
                exportMenu.setVisible(true);
                delMenu.setVisible(true);
                break;
            case 2: // ?????????
                periodDivMenu.setVisible(true);
                profitLossSettingsMenu.setVisible(true);
                profitLossExportMenu.setVisible(true);
                break;
            case 3: // ?????????
                periodDivMenu.setVisible(true);
                isExpenseMenu.setVisible(true);
                String menuTitle = (mGraphMenuIsExpense)
                        ? getString(R.string.Income) : getString(R.string.Expense);
                isExpenseMenu.setTitle(menuTitle);
                break;
            case 4: // ??????
                helpMenu.setVisible(true);
                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.help_in_settings_tab:
                CustomTabsUtils.showHelp(this, CustomTabsUtils.Content.TOP);
                break;

            case R.id.data_export:
                DataExportActivity.start(this,
                        getHistoryTargetName(), null,
                        EntryDataManager.PERIOD_TYPE_ALL, searchView.getQuery().toString());
                break;

            case R.id.action_search:
                break;

            case R.id.data_delete:
                HistoryTabFragment historyTabFragment = (HistoryTabFragment) mTabPagerAdapter
                        .instantiateItem(binding.pager, 1);
                if (historyTabFragment != null)
                    historyTabFragment.showDeleteConfirmDialog();
                break;

            // ???????????????????????????????????????????????????????????????
            case R.id.divide_by_all:
                reportSwitchPeriod(EntryDataManager.PERIOD_TYPE_ALL);
                break;
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

                if (startEndDate != null && startEndDate.length == 0) {
                    ProfitLossExportActivity.start(this, null);
                    break;
                } else if (startEndDate == null || startEndDate[0] <= 0) {
                    DialogManager.showToast(this, getString(R.string.no_data_to_export_message));
                    break;
                }


                ProfitLossExportActivity.start(this, startEndDate);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getHistoryTargetName() {
        String projectName = mProjectManager.getCurrentName();
        String query = searchView.getQuery().toString();
        return query.isEmpty() ? projectName : projectName + " " + query;
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
                        break;
                    case R.id.tab4:
                        binding.pager.setCurrentItem(3, false);
                        setTitle(getString(R.string.Graph));
                        replayGraphAnimate();
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

        SettingsTabFragment settingsFragment = (SettingsTabFragment) mTabPagerAdapter
                .instantiateItem(pager, 4);
        if (settingsFragment != null)
            settingsFragment.afterLogin();
    }

    private void reportSwitchPeriod(int type) {
        if (mTabPagerAdapter == null) return;

        CustomViewPager pager = binding.pager;

        ReportFragment reportFragment = (ReportFragment) mTabPagerAdapter
                .instantiateItem(pager, 2);
        reportFragment.switchReportPeriod(type);

        GraphTabFragment graphFragment = (GraphTabFragment) mTabPagerAdapter
                .instantiateItem(pager, 3);
        graphFragment.switchDataView(type);
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

    private void reportOnPageScrolled(int target, int position) {
        CustomViewPager pager = binding.pager;
        if (target == 0) { // send to Graph Report
            GraphTabFragment graphFragment =
                    (GraphTabFragment) mTabPagerAdapter.instantiateItem(pager, 3);
            graphFragment.pagerOnSelected(position);
        } else { // send to normal report
            ReportFragment reportFragment =
                    (ReportFragment) mTabPagerAdapter.instantiateItem(pager, 2);
            reportFragment.pagerOnSelected(position);
        }
    }

    private void reportReload() {
        if (mTabPagerAdapter == null) return;

        CustomViewPager pager = binding.pager;

        EntryTabFragment entryTabFragment =
                (EntryTabFragment) mTabPagerAdapter.instantiateItem(pager, 0);
        entryTabFragment.reloadData();

        HistoryTabFragment historyFragment =
                (HistoryTabFragment) mTabPagerAdapter.instantiateItem(pager, 1);
        if (historyFragment != null)
            historyFragment.loadHistoryData();

        ReportFragment reportFragment =
                (ReportFragment) mTabPagerAdapter.instantiateItem(pager, 2);
        if (reportFragment != null)
            reportFragment.reloadData();

        GraphTabFragment graphFragment =
                (GraphTabFragment) mTabPagerAdapter.instantiateItem(pager, 3);
        if (graphFragment != null)
            graphFragment.reloadData();

        SettingsTabFragment settingsFragment =
                (SettingsTabFragment) mTabPagerAdapter.instantiateItem(pager, 4);
        if (settingsFragment != null)
            settingsFragment.afterLogin();
    }

    private void replayGraphAnimate() {
        if (mTabPagerAdapter == null) return;

        CustomViewPager pager = binding.pager;
        GraphTabFragment graphFragment =
                (GraphTabFragment) mTabPagerAdapter.instantiateItem(pager, 3);
        if (graphFragment != null)
            graphFragment.replayGraphAnimate();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean isGrantedWriteStorage = false;
        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && grantResults[i] == PermissionChecker.PERMISSION_GRANTED) {
                isGrantedWriteStorage = true;
            }
        }

        if (mTabPagerAdapter == null) return;
        SettingsTabFragment settingsFragment =
                (SettingsTabFragment) mTabPagerAdapter.instantiateItem(binding.pager, 4);
        if (settingsFragment != null && isGrantedWriteStorage) {
            settingsFragment.showDataBackupDialog();
        }
        if (!isGrantedWriteStorage) {
            DialogManager.showToast(this, getString(R.string.device_permission_denied_msg));
        }
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

//        // JSONOBject??????????????????????????????
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

        // InAppReview????????????????????????????????????
//        RateThisApp.showRateDialogIfNeeded(this);

        // Custom message
        RateThisApp.Config config = new RateThisApp.Config(3, 10);
        config.setTitle(R.string.rate_app_title);
        config.setMessage(R.string.rate_app_message);
        config.setYesButtonText(R.string.rate_app_yes_button);
        config.setNoButtonText(R.string.rate_app_no_button);
        config.setCancelButtonText(R.string.rate_app_cancel_button);
        RateThisApp.init(config);
    }

    private void clearSearchEntry() {
        if (mTabPagerAdapter == null) return;

        CustomViewPager pager = binding.pager;
        HistoryTabFragment fragment =
                (HistoryTabFragment) mTabPagerAdapter.instantiateItem(pager, 1);
        if (fragment != null)
            fragment.clearSearchEntry();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (binding.pager == null) return super.onKeyDown(keyCode, event);

        // ??????????????????????????????????????????????????????
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            int currentItem = binding.pager.getCurrentItem();
            if (currentItem != 0) {
                if (currentItem == 1 && searchView != null && !searchView.isIconified()) {
                    clearSearchEntry();
                } else {
                    BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
                    bottomNavigationView.findViewById(R.id.tab1).performClick();
                }
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
        unregisterReceiver(mDataPeriodScrolledReceiver);
        if (appUpdateManager != null) {
            appUpdateManager.unregisterListener(mInstallStateUpdatedListener);
        }
        super.onDestroy();


        if (mBillingHelper != null) {
            try {
                mBillingHelper.disposeWhenFinished();
            } catch (Exception e) {
                Log.e("ERROR", e.getLocalizedMessage());
            }
        }
        mBillingHelper = null;
    }


    private class CheckBillingAsyncTask extends AsyncTask<Purchase, Void, SubscriptionPurchase> {
        private String subscriptionId;
        private Purchase mPurchase;

        @Override
        protected SubscriptionPurchase doInBackground(Purchase... purchases) {
            if (tnGoogleApi == null) cancel(true);

            mPurchase = purchases[0];
            subscriptionId = mPurchase.getSku();
            SubscriptionPurchase subPurchase = tnGoogleApi.getSubscription(subscriptionId, mPurchase.getToken());
            return subPurchase;
        }

        @Override
        protected void onPostExecute(SubscriptionPurchase result) {
            if (subscriptionId == null) return;

            long expiryTime;
            if (result == null) {
                // result?????????????????????????????????????????????????????????????????????????????????????????????????????????
                // ?????????????????????????????????????????????????????????????????????while????????????
                if (mPurchase.getPurchaseState() != 0) {
                    return;
                }
                expiryTime = mPurchase.getPurchaseTime();
                long nowTime = System.currentTimeMillis();
                while (expiryTime <= nowTime) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(expiryTime);
                    if (subscriptionId.equals(UpgradeManger.SKU_TAXNOTE_CLOUD_ID)) {
                        c.add(Calendar.MONTH, 1);
                    } else {
                        c.add(Calendar.YEAR, 1);
                    }
                    expiryTime = c.getTimeInMillis();
                }
            } else {
                expiryTime = result.getExpiryTimeMillis();
            }

            Context context = getApplicationContext();
            switch (subscriptionId) {
                case UpgradeManger.SKU_TAXNOTE_PLUS_ID:
                case UpgradeManger.SKU_TAXNOTE_PLUS_ID1:
                case UpgradeManger.SKU_TAXNOTE_PLUS_ID2:
                case UpgradeManger.SKU_TAXNOTE_PLUS_ID3:
                    SharedPreferencesManager.saveTaxnotePlusExpiryTime(
                            context, expiryTime);
                    break;
                case UpgradeManger.SKU_TAXNOTE_CLOUD_ID:
                    SharedPreferencesManager.saveTaxnoteCloudExpiryTime(
                            context, expiryTime);
                    new TNApiUser(context).saveCloudPurchaseInfo(
                            mPurchase.getOrderId(), mPurchase.getToken());

                    break;
            }
        }
    }

    //--------------------------------------------------------------//
    //    -- App Update --
    //--------------------------------------------------------------//
    private final InstallStateUpdatedListener mInstallStateUpdatedListener = new InstallStateUpdatedListener() {
        @Override
        public void onStateUpdate(InstallState state) {
            switch (state.installStatus()) {
                case InstallStatus.DOWNLOADED:
                    notifyAppUpdateDownloadCompleted();
                    break;
                default:
                    break;
            }
        }
    };

    private void checkAppUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this);
        appUpdateManager.registerListener(mInstallStateUpdatedListener);

        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo result) {
                if (result.installStatus() == InstallStatus.DOWNLOADED) {
                    notifyAppUpdateDownloadCompleted();
                } else if (result.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) &&
                        result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    try {
                        requestAppUpdate(result);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void requestAppUpdate(AppUpdateInfo appUpdateInfo) throws IntentSender.SendIntentException {
        int updateVersionCode = appUpdateInfo.availableVersionCode();
        int lastCheckVersionCode = SharedPreferencesManager.getLastAppUpdateVersionCode(this);
        if (updateVersionCode <= lastCheckVersionCode) {
            appUpdateManager.unregisterListener(mInstallStateUpdatedListener);
            return;
        }
        SharedPreferencesManager.saveLastAppUpdateVersionCode(this, updateVersionCode);

        appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.FLEXIBLE,
                this,
                REQUEST_CODE_FLEXIBLE_UPDATE);
    }

    private void notifyAppUpdateDownloadCompleted() {
        View view = findViewById(R.id.place_snackBar);
        Snackbar.make(view, R.string.app_update_completed_message, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.app_update_completed_action, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (appUpdateManager == null) return;
                        appUpdateManager.completeUpdate();
                        appUpdateManager.unregisterListener(mInstallStateUpdatedListener);
                    }
                })
                .show();
    }
}
