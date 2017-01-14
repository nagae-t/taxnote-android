package com.example.taxnoteandroid;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.example.taxnoteandroid.databinding.ActivityUpgradeBinding;


public class UpgradeActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    private ActivityUpgradeBinding binding;

    BillingProcessor billingProcessor;
    private static final String LICENSE_KEY_OF_GOOGLE_PLAY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiqf39c7TtSqe9FV2Xz/Xa2S6dexgD2k5qK1ZnC7uCctI2J+Y8GW1oG2S5wN/zdxB5nlkP/a94GiAZqmxhLknVFqRMq32f4zuT2M8mGxFmCMpqQbvYgI2hDXY0xS7c0EITHNPykTRAqS1tgjuHRDWrNjfae7FuvIEJMe4h41tbYAAdKh8Uv+sv3cVmmTXn2j+Ep42XhE1moLug26orCS7IfKAJjAiRK5lzCaCF3mNqPcjogxjG425P44oVT8Ewnx4+N9qbfkzQueCqkw4mD4UdBABCefjZ6t+N2+ZEwGreV/nu5P7kXOsDZp9SGlNB99rL21Xnpzc+QDQvUkBXlNTWQIDAQAB";
    private static final String TAXNOTE_PLUS_ID = "taxnote.plus";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upgrade);

        binding             = DataBindingUtil.setContentView(this, R.layout.activity_upgrade);
        billingProcessor    = new BillingProcessor(this, LICENSE_KEY_OF_GOOGLE_PLAY, this);
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
        setTitle();
        setUpgradeToTaxnotePlusView();
        setRestorePurchasesView();
        setHelpView();
    }

    private void setTitle() {
        setTitle(getResources().getString(R.string.benefits_of_upgrade));
    }

    private void setUpgradeToTaxnotePlusView() {

        binding.upgradeToPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                billingProcessor.purchase(UpgradeActivity.this, TAXNOTE_PLUS_ID);

            }
        });
    }

    private void setRestorePurchasesView() {

        binding.restorePurchases.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                billingProcessor.loadOwnedPurchasesFromGoogle();
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
    //    -- IBillingHandler --
    //--------------------------------------------------------------//

    @Override
    public void onBillingInitialized() {
        /*
         * Called when BillingProcessor was initialized and it's ready to purchase
         */
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        /*
         * Called when requested PRODUCT ID was successfully purchased
         */
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        /*
         * Called when some error occurred. See Constants class for more details
         *
         * Note - this includes handling the case where the user canceled the buy dialog:
         * errorCode = Constants.BILLING_RESPONSE_RESULT_USER_CANCELED
         */
    }

    @Override
    public void onPurchaseHistoryRestored() {
        /*
         * Called when purchase history was restored and the list of all owned PRODUCT ID's
         * was loaded from Google Play
         */
    }

}
