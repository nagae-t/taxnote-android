package com.example.taxnoteandroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.example.taxnoteandroid.Library.UpgradeManger;
import com.example.taxnoteandroid.Library.billing.IabHelper;
import com.example.taxnoteandroid.Library.billing.IabResult;
import com.example.taxnoteandroid.Library.billing.Inventory;
import com.example.taxnoteandroid.Library.billing.Purchase;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityUpgradeBinding;
import com.helpshift.support.Support;
import com.kobakei.ratethisapp.RateThisApp;


public class UpgradeActivity extends AppCompatActivity {

    private ActivityUpgradeBinding binding;
    private static final String LICENSE_KEY_OF_GOOGLE_PLAY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAm+14FzQyLcAO7X2zwFDWXwHDuzN8RA60R71JouG5TO6la3xh0A7uWIQ4Y2k1kvqa/fHRAOble7TxIDsy11GsLjD/2sI+e4p4pE5vDKeY3ARBadcQI7iDc/VVnkzCSrZeoGTYinm+99diGn71cGIlF+7ISnh98Kss1zguKLlY+tCkaDDCe+moghLYTvqVuJg27ShVfxxPpWr4gwMusdSMcbJLR6S4ajeWbEtacGAdEJnzQfuAH6RMnt/ggZa4CFRVbNnJA6Eft/CCQL7GFBwBYnkMfG+Jdr+66BcTHbtPP8cE5WdmjGzDje+iy5HGYyIfqiDTdBs178zgWKUS8TM9QwIDAQAB";
    private static final String TAXNOTE_PLUS_ID = "taxnote.plus.sub";
    private static final int REQUEST_CODE_PURCHASE_PREMIUM = 0;
    private IabHelper mBillingHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_upgrade);
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

        mBillingHelper = new IabHelper(this, LICENSE_KEY_OF_GOOGLE_PLAY);
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

            Purchase purchase = inventory.getPurchase(TAXNOTE_PLUS_ID);

            // Restore purchase
            if (purchase != null) {
                SharedPreferencesManager.saveTaxnotePlusPurchaseTime(UpgradeActivity.this, purchase.getPurchaseTime());
                updateUpgradeStatus();
            }
        }
    };

    private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {

        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

            if (result.isFailure()) return;
            if (purchase == null) return;
            if (purchase.getSku().equals(TAXNOTE_PLUS_ID)) {

                // Upgrade
                SharedPreferencesManager.saveTaxnotePlusPurchaseTime(UpgradeActivity.this, purchase.getPurchaseTime());
                showUpgradeToTaxnotePlusSuccessDialog();
                updateUpgradeStatus();
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
                mBillingHelper.launchSubscriptionPurchaseFlow(UpgradeActivity.this, TAXNOTE_PLUS_ID, REQUEST_CODE_PURCHASE_PREMIUM, mPurchaseFinishedListener);
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
        }
    }
}