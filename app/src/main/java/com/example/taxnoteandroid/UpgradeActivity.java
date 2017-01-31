package com.example.taxnoteandroid;

import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.Constants;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.UpgradeManger;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityUpgradeBinding;
import com.helpshift.support.Support;
import com.kobakei.ratethisapp.RateThisApp;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class UpgradeActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    private ActivityUpgradeBinding binding;

    BillingProcessor billingProcessor;
    private static final String LICENSE_KEY_OF_GOOGLE_PLAY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiqf39c7TtSqe9FV2Xz/Xa2S6dexgD2k5qK1ZnC7uCctI2J+Y8GW1oG2S5wN/zdxB5nlkP/a94GiAZqmxhLknVFqRMq32f4zuT2M8mGxFmCMpqQbvYgI2hDXY0xS7c0EITHNPykTRAqS1tgjuHRDWrNjfae7FuvIEJMe4h41tbYAAdKh8Uv+sv3cVmmTXn2j+Ep42XhE1moLug26orCS7IfKAJjAiRK5lzCaCF3mNqPcjogxjG425P44oVT8Ewnx4+N9qbfkzQueCqkw4mD4UdBABCefjZ6t+N2+ZEwGreV/nu5P7kXOsDZp9SGlNB99rL21Xnpzc+QDQvUkBXlNTWQIDAQAB";

    //    private static final String TAXNOTE_PLUS_ID = "taxnote.plus";
    private static final String TAXNOTE_PLUS_ID = "taxnotetest";
    private boolean googlePlayPurchaseIsAvailable = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upgrade);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_upgrade);
        initBillingProcessor();
        billingProcessor.loadOwnedPurchasesFromGoogle();
        setViews();
    }

    @Override
    public void onDestroy() {
        if (billingProcessor != null)
            billingProcessor.release();

        super.onDestroy();
    }


    //--------------------------------------------------------------//
    //    -- Display Part --
    //--------------------------------------------------------------//

    private void setViews() {
        setUpgradeToTaxnotePlusView();
        setHelpView();

        //@@
//        showTestDialog();
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

//        calendar.add(Calendar.YEAR, 1);

        calendar.add(Calendar.YEAR, 1);

        long expireTime = calendar.getTimeInMillis();

        long now = System.currentTimeMillis();


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        String purchaseTimeString = simpleDateFormat.format(purchaseTime);

        String expireTimeString = simpleDateFormat.format(expireTime);

        String nowString = simpleDateFormat.format(now);


        String title = "purchase Time = " + purchaseTimeString;
        String message = "expire Time = " + expireTimeString + ": now = " + nowString;

        DialogManager.showOKOnlyAlert(this,title,message);

    }

    private void upgradeToTaxnotePlus() {

        if (googlePlayPurchaseIsAvailable) {
            billingProcessor.subscribe(UpgradeActivity.this, TAXNOTE_PLUS_ID);
            restorePurchases();
        }
    }

    private void restorePurchases() {

        if (!UpgradeManger.taxnotePlusIsActive(this)) {

            TransactionDetails details = billingProcessor.getSubscriptionTransactionDetails(TAXNOTE_PLUS_ID);

            if (details != null) {
                UpgradeManger.updateTaxnotePlusSubscriptionStatus(this, details);

                if (UpgradeManger.taxnotePlusIsActive(this)) {
                    binding.upgraded.setText(getResources().getString(R.string.upgrade_is_active));
                }
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


    //--------------------------------------------------------------//
    //    -- IBillingHandler --
    //--------------------------------------------------------------//

    private void initBillingProcessor() {
        billingProcessor = new BillingProcessor(this, LICENSE_KEY_OF_GOOGLE_PLAY, this);
    }

    @Override
    public void onBillingInitialized() {

        boolean isOneTimePurchaseSupported = billingProcessor.isOneTimePurchaseSupported();

        if (isOneTimePurchaseSupported) {
            googlePlayPurchaseIsAvailable = true;
        }
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {

        if (productId.equals(TAXNOTE_PLUS_ID)) {

            UpgradeManger.updateTaxnotePlusSubscriptionStatus(this, details);
            showUpgradeToTaxnotePlusSuccessDialog();
        }
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        /*
         * Called when some error occurred. See Constants class for more details
         *
         * Note - this includes handling the case where the user canceled the buy dialog:
         * errorCode = Constants.BILLING_RESPONSE_RESULT_USER_CANCELED
         */

        if (errorCode == Constants.BILLING_RESPONSE_RESULT_USER_CANCELED) {
            return;
        }

        // Show error dialog
        String title = getResources().getString(R.string.Error);
        String message = error.getLocalizedMessage();
        DialogManager.showOKOnlyAlert(this, title, message);
    }

    @Override
    public void onPurchaseHistoryRestored() {
        /*
         * Called when purchase history was restored and the list of all owned PRODUCT ID's
         * was loaded from Google Play
         */
    }
}
