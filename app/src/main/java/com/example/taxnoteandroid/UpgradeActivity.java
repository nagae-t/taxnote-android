package com.example.taxnoteandroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

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

    private static final String LICENSE_KEY_OF_GOOGLE_PLAY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArqj6H3BADGx1hwO9Z7VN0k/rUpA84li83denEBFui/bqomHsYd2LKV3DCp7P6D1Saw8xIQx9AIw6ZQZ17Jxlor9r9Wo+E7Ue0NlgEcdbNVIub9S0CHosdE0H4m6LxeZobxZHX8NjnHulZ2pP3s5DpiMspVHq/DEe82raIltcqDqK7pAbVu7qjew33Xr+d2v+CPRFpWplE+RsTsZB2S3dB3eu/Nupgk7WnMVoSStIaJW6clIu44PeEPyAJKs3wCtlmLyMp6x3n3SOk+YdPolEcm1G7Np3o3Eg4pbguzoQ2bf5sxK+b2QfafD7leufIB2dtWPpkiA8srjR3bmDv7X7kwIDAQAB";
    private static final String TAXNOTE_PLUS_ID = "sssp";

    static final String TAG = "Taxnote";
    private static final int REQUEST_CODE_PURCHASE_PREMIUM = 0;

    private IabHelper mBillingHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upgrade);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_upgrade);
        setViews();

        setupBilling();
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


//            String message = "Query inventory finished + inventory = " + inventory + " result = " + result;
//            Log.d("billing", message);
//

//            DialogManager.showToast(UpgradeActivity.this, message);
//            DialogManager.showOKOnlyAlert(UpgradeActivity.this, "log", message);

            if (result.isFailure()) return;

            Purchase purchase = inventory.getPurchase(TAXNOTE_PLUS_ID);

            // Restore purchase
            if (purchase != null) {
                SharedPreferencesManager.saveTaxnotePlusPurchaseTime(UpgradeActivity.this, purchase.getPurchaseTime());
                showUpgradeToTaxnotePlusSuccessDialog();
            }

            Log.d("billing", "Query inventory was successful.");
//
            DialogManager.showToast(UpgradeActivity.this, "Query inventory was successful.");
//            DialogManager.showOKOnlyAlert(UpgradeActivity.this, "log", "Query inventory was successful.");
        }
    };

    private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {

        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

            String message = "onIabPurchaseFinished + purchase = " + purchase + " result = " + result;
            DialogManager.showToast(UpgradeActivity.this, message);

            Toast toast = Toast.makeText(UpgradeActivity.this, message, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            DialogManager.showOKOnlyAlert(UpgradeActivity.this, "log", message);


            Log.d("billing", "Purchase finished: " + result + ", purchase: " + purchase);


            if (result.isFailure()) return;

            Log.d("billing", "Purchase successful.");

            DialogManager.showOKOnlyAlert(UpgradeActivity.this, "log", "Purchase successful.");


            if (purchase == null) return;


            if (purchase.getSku().equals(TAXNOTE_PLUS_ID)) {

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
                mBillingHelper.launchSubscriptionPurchaseFlow(UpgradeActivity.this, TAXNOTE_PLUS_ID, REQUEST_CODE_PURCHASE_PREMIUM, mPurchaseFinishedListener);
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
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