package com.example.taxnoteandroid;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.Constants;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityUpgradeBinding;


public class UpgradeActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    private ActivityUpgradeBinding binding;

    BillingProcessor billingProcessor;
    private static final String LICENSE_KEY_OF_GOOGLE_PLAY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiqf39c7TtSqe9FV2Xz/Xa2S6dexgD2k5qK1ZnC7uCctI2J+Y8GW1oG2S5wN/zdxB5nlkP/a94GiAZqmxhLknVFqRMq32f4zuT2M8mGxFmCMpqQbvYgI2hDXY0xS7c0EITHNPykTRAqS1tgjuHRDWrNjfae7FuvIEJMe4h41tbYAAdKh8Uv+sv3cVmmTXn2j+Ep42XhE1moLug26orCS7IfKAJjAiRK5lzCaCF3mNqPcjogxjG425P44oVT8Ewnx4+N9qbfkzQueCqkw4mD4UdBABCefjZ6t+N2+ZEwGreV/nu5P7kXOsDZp9SGlNB99rL21Xnpzc+QDQvUkBXlNTWQIDAQAB";
    private static final String TAXNOTE_PLUS_ID = "taxnote.plus";
    private boolean googlePlayPurchaseIsAvailable = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upgrade);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_upgrade);
        initBillingProcessor();
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
        setRestorePurchasesView();
        setHelpView();
    }

    private void setUpgradeToTaxnotePlusView() {

        final boolean taxnotePlusIsActive = SharedPreferencesManager.taxnotePlusIsActive(this);

        binding.upgradeToPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upgradeToTaxnotePlus();
            }
        });

        if (taxnotePlusIsActive) {
            binding.upgraded.setText(getResources().getString(R.string.upgraded_already));
        }
    }

    private void setRestorePurchasesView() {

        binding.restorePurchases.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (googlePlayPurchaseIsAvailable) {
                    billingProcessor.loadOwnedPurchasesFromGoogle();
                }
            }
        });
    }

    private void setHelpView() {

        binding.help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }


    //--------------------------------------------------------------//
    //    -- Upgrade --
    //--------------------------------------------------------------//

    //QQ 一回購入したら、次回は一瞬で呼ばれて、購入のダイアログがでなくなる
    // テストする時はしょうがないのかな。googleplayのキャッシュの問題かな
    private void upgradeToTaxnotePlus() {

        final boolean taxnotePlusIsActive = SharedPreferencesManager.taxnotePlusIsActive(this);

        if (!taxnotePlusIsActive) {

            if (googlePlayPurchaseIsAvailable) {
                billingProcessor.purchase(UpgradeActivity.this, TAXNOTE_PLUS_ID);
            }
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
        /*
         * Called when BillingProcessor was initialized and it's ready to purchase
         */

        boolean isOneTimePurchaseSupported = billingProcessor.isOneTimePurchaseSupported();

        if (isOneTimePurchaseSupported) {
            googlePlayPurchaseIsAvailable = true;
        }
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        /*
         * Called when requested PRODUCT ID was successfully purchased
         */

        if (productId.equals(TAXNOTE_PLUS_ID)) {

            // Upgrade to Taxnote Plus
            boolean success = SharedPreferencesManager.saveTaxnotePlusStatus(this);

            if (success) {

                binding.upgraded.setText(getResources().getString(R.string.upgraded_already));

                // Show dialog message
                String title    = getResources().getString(R.string.taxnote_plus);
                String message  = getResources().getString(R.string.thanks_for_purchase);
                DialogManager.showOKOnlyAlert(this, title, message);
            }
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

        if (errorCode == Constants.BILLING_RESPONSE_RESULT_USER_CANCELED ) {
            return;
        }

        // Show error dialog
        String title    = getResources().getString(R.string.Error);
        String message  = error.getLocalizedMessage();
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
