package com.example.taxnoteandroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.UpgradeManger;
import com.example.taxnoteandroid.Library.billing.IabHelper;
import com.example.taxnoteandroid.Library.billing.IabResult;
import com.example.taxnoteandroid.Library.billing.Inventory;
import com.example.taxnoteandroid.Library.billing.Purchase;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityUpgradeBinding;
import com.helpshift.support.Support;
import com.kobakei.ratethisapp.RateThisApp;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class UpgradeActivity extends AppCompatActivity {

    private ActivityUpgradeBinding binding;

    private static final String LICENSE_KEY_OF_GOOGLE_PLAY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiqf39c7TtSqe9FV2Xz/Xa2S6dexgD2k5qK1ZnC7uCctI2J+Y8GW1oG2S5wN/zdxB5nlkP/a94GiAZqmxhLknVFqRMq32f4zuT2M8mGxFmCMpqQbvYgI2hDXY0xS7c0EITHNPykTRAqS1tgjuHRDWrNjfae7FuvIEJMe4h41tbYAAdKh8Uv+sv3cVmmTXn2j+Ep42XhE1moLug26orCS7IfKAJjAiRK5lzCaCF3mNqPcjogxjG425P44oVT8Ewnx4+N9qbfkzQueCqkw4mD4UdBABCefjZ6t+N2+ZEwGreV/nu5P7kXOsDZp9SGlNB99rL21Xnpzc+QDQvUkBXlNTWQIDAQAB";
    private static final String TAXNOTE_PLUS_ID = "spp";

    static final String TAG = "Taxnote";

    private static final String SKU_PREMIUM = "testetes";
    private static final String SKU_PREMIUM_SUBSCRIPTION = "spp";

    private static final int REQUEST_CODE_PURCHASE_PREMIUM = 0;
    private static final String BILLING_PUBLIC_KEY = LICENSE_KEY_OF_GOOGLE_PLAY;

    private IabHelper mBillingHelper;
    private boolean mIsPremium = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upgrade);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_upgrade);
        setViews();

        setupBilling();
    }

    @Override
    public void onStart() {
        super.onStart();

        restorePurchases();
        showTestDialog();
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

    private void setupBilling() {

        mBillingHelper = new IabHelper(this, LICENSE_KEY_OF_GOOGLE_PLAY);
        mBillingHelper.enableDebugLogging(true); // Remove before release
        try {
            mBillingHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {

                public void onIabSetupFinished(IabResult result) {
                    Log.d("billing", "Setup finished.");
                    if (result.isFailure()) return;
                    Log.d("billing", "Setup successful. Querying inventory.");

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
            Log.d("billing", "Query inventory finished.");
            if (result.isFailure()) return;
            Log.d("billing", "Query inventory was successful.");
            mIsPremium = inventory.hasPurchase(SKU_PREMIUM);
            Log.d("billing", "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));

            DialogManager.showToast(UpgradeActivity.this, "onQueryInventoryFinished");
        }
    };

    private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {

        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

            String message = "onIabPurchaseFinished";
            DialogManager.showToast(UpgradeActivity.this, message);


            Log.d("billing", "Purchase finished: " + result + ", purchase: " + purchase);
            if (result.isFailure()) return;

            Log.d("billing", "Purchase successful.");

            if (purchase.getSku().equals(SKU_PREMIUM)) {

                Log.d("billing", "Purchase is premium upgrade. Congratulating user.");
                mIsPremium = true;
            }
            if (purchase.getSku().equals(SKU_PREMIUM_SUBSCRIPTION)) {

                Log.d("billing", "Purchase is new subscribing. Congratulating.");

                DialogManager.showToast(UpgradeActivity.this, "Purchase is new subscribing. Congratulating.");

                //@@@
                SharedPreferencesManager.saveTaxnotePlusPurchaseTime(UpgradeActivity.this, purchase.getPurchaseTime());
                showUpgradeToTaxnotePlusSuccessDialog();

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

    private void showTestDialog() {

        long purchaseTime = SharedPreferencesManager.getTaxnotePlusPurchaseTime(this);

        // Get expireTime
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(purchaseTime);

        calendar.add(Calendar.YEAR, 1);

        long expireTime = calendar.getTimeInMillis();
        long now = System.currentTimeMillis();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        String purchaseTimeString = simpleDateFormat.format(purchaseTime);

        String expireTimeString = simpleDateFormat.format(expireTime);

        String nowString = simpleDateFormat.format(now);


        String title = "purchase Time = " + purchaseTimeString;
        String message = "expire Time = " + expireTimeString + ": now = " + nowString;

        DialogManager.showOKOnlyAlert(this, title, message);

    }

    private void upgradeToTaxnotePlus() {

        if (mBillingHelper.subscriptionsSupported()) {
            try {
                mBillingHelper.launchSubscriptionPurchaseFlow(UpgradeActivity.this, SKU_PREMIUM_SUBSCRIPTION, REQUEST_CODE_PURCHASE_PREMIUM, mPurchaseFinishedListener);
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        }
    }

    private void restorePurchases() {

        if (!UpgradeManger.taxnotePlusIsActive(this)) {

        }
    }

    private void showUpgradeToTaxnotePlusSuccessDialog() {

        if (UpgradeManger.taxnotePlusIsActive(this)) {

            binding.upgraded.setText(getResources().getString(R.string.upgrade_is_active));

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