package com.example.taxnoteandroid;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;

import com.example.taxnoteandroid.Library.TNGoogleApiClient;
import com.example.taxnoteandroid.Library.UpgradeManger;
import com.example.taxnoteandroid.Library.billing.IabHelper;
import com.example.taxnoteandroid.Library.billing.IabResult;
import com.example.taxnoteandroid.Library.billing.Inventory;
import com.example.taxnoteandroid.Library.billing.Purchase;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityUpgradeBinding;
import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.helpshift.support.Support;
import com.kobakei.ratethisapp.RateThisApp;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import static com.example.taxnoteandroid.TaxnoteConsts.MIXPANEL_TOKEN;


public class UpgradeActivity extends DefaultCommonActivity {

    private ActivityUpgradeBinding binding;
    private static final int REQUEST_CODE_PURCHASE_PREMIUM = 0;
    private IabHelper mBillingHelper;

    private TNGoogleApiClient tnGoogleApi;

    private ProgressDialog mLoadingProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_upgrade);
        tnGoogleApi = new TNGoogleApiClient(this);

        mLoadingProgress = new ProgressDialog(this);
        mLoadingProgress.setMessage(getString(R.string.loading));
        mLoadingProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mLoadingProgress.setCancelable(false);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setViews();
        setupBilling();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBillingHelper != null) {
            try {
                mBillingHelper.dispose();
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        }
        mBillingHelper = null;
    }


    //--------------------------------------------------------------//
    //    -- Billing --
    //--------------------------------------------------------------//

    private void setupBilling() {

        mBillingHelper = new IabHelper(this, UpgradeManger.GOOGLE_PLAY_LICENSE_KEY);
//        mBillingHelper.enableDebugLogging(true); // Remove before release
        try {
            mBillingHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {

                public void onIabSetupFinished(IabResult result) {

                    if (result.isFailure()) return;

                    try {
                        mBillingHelper.queryInventoryAsync(mGotInventoryListener);
                    } catch (IabHelper.IabAsyncInProgressException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    private IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {

        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

            if (result.isFailure()) return;

            // Restore purchase
            Purchase purchasePlus = inventory.getPurchase(UpgradeManger.SKU_TAXNOTE_PLUS_ID);
            if (purchasePlus != null)
                new CheckBillingAsyncTask(false).execute(purchasePlus);

            Purchase purchasePlus1 = inventory.getPurchase(UpgradeManger.SKU_TAXNOTE_PLUS_ID1);
            if (purchasePlus1 != null)
                new CheckBillingAsyncTask(false).execute(purchasePlus1);

        }
    };

    private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {

        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

            if (result.isFailure()) return;
            if (purchase == null) return;
            if (purchase.getSku().equals(UpgradeManger.SKU_TAXNOTE_PLUS_ID1)) {

                // Upgrade
                mLoadingProgress.show();
                new CheckBillingAsyncTask(true).execute(purchase);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mBillingHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    //--------------------------------------------------------------//
    //    -- Display Part --
    //--------------------------------------------------------------//

    private void setViews() {
        setUpgradeToTaxnotePlusView();
        setHelpView();
    }

    private void setUpgradeToTaxnotePlusView() {

        binding.upgradeToPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upgradeToTaxnotePlus();
            }
        });

        updateUpgradeStatus();
    }

    private void updateUpgradeStatus() {

        if (UpgradeManger.taxnotePlusIsActive(this)) {
            binding.upgraded.setText(getResources().getString(R.string.upgrade_is_active));
        }
    }

    private void setHelpView() {

        binding.help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Support.showSingleFAQ(UpgradeActivity.this, "108");
            }
        });
    }


    //--------------------------------------------------------------//
    //    -- Upgrade --
    //--------------------------------------------------------------//

    private void upgradeToTaxnotePlus() {

        if (mBillingHelper.subscriptionsSupported()) {
            try {
                mBillingHelper.launchSubscriptionPurchaseFlow(UpgradeActivity.this,
                        UpgradeManger.SKU_TAXNOTE_PLUS_ID1,
                        REQUEST_CODE_PURCHASE_PREMIUM,
                        mPurchaseFinishedListener);
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        }
    }

    private void showUpgradeToTaxnotePlusSuccessDialog() {

        if (UpgradeManger.taxnotePlusIsActive(this)) {

            // Show dialog message
            new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.taxnote_plus))
                    .setMessage(getResources().getString(R.string.thanks_for_purchase))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            // Ask rating
                            RateThisApp.showRateDialog(UpgradeActivity.this);
                        }
                    })
                    .show();

            MixpanelAPI mixpanel = MixpanelAPI.getInstance(this, MIXPANEL_TOKEN);
            mixpanel.track("Taxnote Plus Upgraded");
        }
    }

    private class CheckBillingAsyncTask extends AsyncTask<Purchase, Void, SubscriptionPurchase> {
        private String subscriptionId;
        private final boolean isNewPurchased;

        private CheckBillingAsyncTask(boolean isNewPurchased) {
            this.isNewPurchased = isNewPurchased;
        }

        @Override
        protected SubscriptionPurchase doInBackground(Purchase... purchases) {
            if (tnGoogleApi == null) cancel(true);

            Purchase purchase = purchases[0];
            subscriptionId =  purchase.getSku();
            SubscriptionPurchase subPurchase = tnGoogleApi.getSubscription(subscriptionId, purchase.getToken());
            return subPurchase;
        }

        @Override
        protected void onPostExecute(SubscriptionPurchase result) {
            if (mLoadingProgress.isShowing()) mLoadingProgress.dismiss();

            if (result == null || subscriptionId == null) {
                return;
            }

            switch (subscriptionId) {
                case UpgradeManger.SKU_TAXNOTE_PLUS_ID:
                    SharedPreferencesManager.saveTaxnotePlusExpiryTime(
                            getApplicationContext(), result.getExpiryTimeMillis());
                    break;
                case UpgradeManger.SKU_TAXNOTE_PLUS_ID1:
                    SharedPreferencesManager.saveTaxnotePlusExpiryTime(
                            getApplicationContext(), result.getExpiryTimeMillis());

                    if (isNewPurchased)
                        showUpgradeToTaxnotePlusSuccessDialog();
                    break;
                case UpgradeManger.SKU_TAXNOTE_CLOUD_ID:
                    SharedPreferencesManager.saveTaxnoteCloudExpiryTime(
                            getApplicationContext(), result.getExpiryTimeMillis());
                    break;
            }

            updateUpgradeStatus();
        }
    }
}