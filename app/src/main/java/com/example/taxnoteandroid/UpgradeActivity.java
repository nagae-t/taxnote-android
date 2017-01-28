package com.example.taxnoteandroid;

import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.Constants;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityUpgradeBinding;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.helpshift.support.Support;

import java.text.SimpleDateFormat;


public class UpgradeActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    private ActivityUpgradeBinding binding;

    BillingProcessor billingProcessor;
    private static final String LICENSE_KEY_OF_GOOGLE_PLAY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiqf39c7TtSqe9FV2Xz/Xa2S6dexgD2k5qK1ZnC7uCctI2J+Y8GW1oG2S5wN/zdxB5nlkP/a94GiAZqmxhLknVFqRMq32f4zuT2M8mGxFmCMpqQbvYgI2hDXY0xS7c0EITHNPykTRAqS1tgjuHRDWrNjfae7FuvIEJMe4h41tbYAAdKh8Uv+sv3cVmmTXn2j+Ep42XhE1moLug26orCS7IfKAJjAiRK5lzCaCF3mNqPcjogxjG425P44oVT8Ewnx4+N9qbfkzQueCqkw4mD4UdBABCefjZ6t+N2+ZEwGreV/nu5P7kXOsDZp9SGlNB99rL21Xnpzc+QDQvUkBXlNTWQIDAQAB";
//    private static final String TAXNOTE_PLUS_ID = "taxnote.plus";

    private static final String TAXNOTE_PLUS_ID = "taxnote.plus.subscription";

    private boolean googlePlayPurchaseIsAvailable = false;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upgrade);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_upgrade);
        initBillingProcessor();
        setViews();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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
                restorePurchases();
            }
        });
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

        final boolean taxnotePlusIsActive = SharedPreferencesManager.taxnotePlusIsActive(this);

        if (!taxnotePlusIsActive) {

            if (googlePlayPurchaseIsAvailable) {

                if (billingProcessor.isSubscribed(TAXNOTE_PLUS_ID)) {
//                if (billingProcessor.isPurchased(TAXNOTE_PLUS_ID)) {

                    showRestoreTaxnotePlusSuccessDialong();
                } else {

                    billingProcessor.subscribe(UpgradeActivity.this, TAXNOTE_PLUS_ID);

//                    billingProcessor.purchase(UpgradeActivity.this, TAXNOTE_PLUS_ID);
                }
            }
        }
    }

    private void restorePurchases() {

        final boolean taxnotePlusIsActive = SharedPreferencesManager.taxnotePlusIsActive(this);

        if (!taxnotePlusIsActive) {

            //@@ここあとでちゃんとテストしないと、キャッシュがない時の動作がわからない
            billingProcessor.loadOwnedPurchasesFromGoogle();

            if (billingProcessor.isPurchased(TAXNOTE_PLUS_ID)) {
                showRestoreTaxnotePlusSuccessDialong();
            } else {
                showNoPurchaseHistoryDialong();
            }
        }
    }

    private void showRestoreTaxnotePlusSuccessDialong() {

        // Upgrade to Taxnote Plus
        boolean success = SharedPreferencesManager.saveTaxnotePlusStatus(this);

        if (success) {

            binding.upgraded.setText(getResources().getString(R.string.upgraded_already));

            // Show dialog message
            String title = getResources().getString(R.string.taxnote_plus);
            String message = getResources().getString(R.string.upgrade_restored_success_message);
            DialogManager.showOKOnlyAlert(this, title, message);
        }
    }

    private void showUpgradeToTaxnotePlusSuccessDialong() {

        // Upgrade to Taxnote Plus
        boolean success = SharedPreferencesManager.saveTaxnotePlusStatus(this);

        if (success) {

            binding.upgraded.setText(getResources().getString(R.string.upgraded_already));

            // Show dialog message
            String title = getResources().getString(R.string.taxnote_plus);
            String message = getResources().getString(R.string.thanks_for_purchase);
            DialogManager.showOKOnlyAlert(this, title, message);
        }
    }

    private void showNoPurchaseHistoryDialong() {

        String title = getResources().getString(R.string.Error);
        String message = getResources().getString(R.string.upgrade_no_purchase_history_message);
        DialogManager.showOKOnlyAlert(this, title, message);
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


            //@@@
            // ここでexipreTimeをセーぶしたりする
            Gson gson = new Gson();

            // JSONからStringへの変換
            String str = gson.fromJson(details.purchaseInfo.responseData, String.class);
            System.out.println("String: " + str);


            // JSONから配列への変換
            int[] array = gson.fromJson(details.purchaseInfo.responseData, int[].class);
            System.out.println("int[]: " + array[0] + ",　" + array[1] + ",　" + array[2]);


            long expiryTime =  array[2];

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources().getString(R.string.date_string_format_for_custom_range));
            String expireString  = simpleDateFormat.format(expiryTime);


            DialogManager.showOKOnlyAlert(this,"ExpireTime",expireString);

            DialogManager.showOKOnlyAlert(this,"json",str);


//            https://developers.google.com/android-publisher/api-ref/purchases/subscriptions?hl=ja
//            {
//                "kind": "androidpublisher#subscriptionPurchase",
//                    "startTimeMillis": long,
//                "expiryTimeMillis": long,
//                "autoRenewing": boolean,
//                "priceCurrencyCode": string,
//                    "priceAmountMicros": long,
//                "countryCode": string,
//                    "developerPayload": string,
//                    "paymentState": integer,
//                    "cancelReason": integer
//            }



//            JSONObject json = null;
//
//
//            try {
//                json = new JSONObject(responseData);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//
//            json.opt()
//
//            long expiryTime = json.opt("expiryTimeMillis");


//            PurchaseData data = new PurchaseData();
//            data.orderId = json.optString("orderId");
//            data.packageName = json.optString("packageName");
//            data.productId = json.optString("productId");
//            long purchaseTimeMillis = json.optLong("purchaseTime", 0);
//            data.purchaseTime = purchaseTimeMillis != 0 ? new Date(purchaseTimeMillis) : null;
//            data.purchaseState = PurchaseState.values()[json.optInt("purchaseState", 1)];
//            data.developerPayload = json.optString("developerPayload");
//            data.purchaseToken = json.getString("purchaseToken");
//            data.autoRenewing = json.optBoolean("autoRenewing");


            showUpgradeToTaxnotePlusSuccessDialong();
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

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Upgrade Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
