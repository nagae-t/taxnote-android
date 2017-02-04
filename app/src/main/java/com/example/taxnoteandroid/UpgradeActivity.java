package com.example.taxnoteandroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.UpgradeManger;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityUpgradeBinding;
import com.helpshift.support.Support;
import com.kobakei.ratethisapp.RateThisApp;

import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.BillingRequests;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.EmptyRequestListener;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.Purchase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.Nonnull;

import static org.solovyev.android.checkout.ProductTypes.SUBSCRIPTION;


//public class UpgradeActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

public class UpgradeActivity extends AppCompatActivity {


        private ActivityUpgradeBinding binding;

//    BillingProcessor billingProcessor;
    private static final String LICENSE_KEY_OF_GOOGLE_PLAY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiqf39c7TtSqe9FV2Xz/Xa2S6dexgD2k5qK1ZnC7uCctI2J+Y8GW1oG2S5wN/zdxB5nlkP/a94GiAZqmxhLknVFqRMq32f4zuT2M8mGxFmCMpqQbvYgI2hDXY0xS7c0EITHNPykTRAqS1tgjuHRDWrNjfae7FuvIEJMe4h41tbYAAdKh8Uv+sv3cVmmTXn2j+Ep42XhE1moLug26orCS7IfKAJjAiRK5lzCaCF3mNqPcjogxjG425P44oVT8Ewnx4+N9qbfkzQueCqkw4mD4UdBABCefjZ6t+N2+ZEwGreV/nu5P7kXOsDZp9SGlNB99rL21Xnpzc+QDQvUkBXlNTWQIDAQAB";
    private static final String TAXNOTE_PLUS_ID = "subb";
    private boolean googlePlayPurchaseIsAvailable = false;


    private final ActivityCheckout mCheckout = Checkout.forActivity(this, TaxnoteApp.get().getBilling());
    private Inventory mInventory;

    private final List<Inventory.Callback> mInventoryCallbacks = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upgrade);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_upgrade);
//        initBillingProcessor();
        setViews();



        mCheckout.start();
        mCheckout.createPurchaseFlow(new PurchaseListener());

        mInventory = mCheckout.makeInventory();
        mInventory.load(Inventory.Request.create()
                .loadAllPurchases()
                .loadSkus(SUBSCRIPTION, TAXNOTE_PLUS_ID), new InventoryCallback());

        reloadInventory();

    }

    @Override
    public void onStart() {
        super.onStart();
//        billingProcessor.loadOwnedPurchasesFromGoogle();
        restorePurchases();
    }

    @Override
    public void onDestroy() {
//        if (billingProcessor != null)
//            billingProcessor.release();

        mCheckout.stop();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCheckout.onActivityResult(requestCode, resultCode, data);
    }

    private class PurchaseListener extends EmptyRequestListener<Purchase> {

        @Override
        public void onSuccess(@Nonnull Purchase result) {
//
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
//            String purchaseTimeString = simpleDateFormat.format(result.time);
//
//            // Show dialog message
//            new AlertDialog.Builder(UpgradeActivity.this)
//                    .setTitle(result.packageName)
//                    .setMessage(purchaseTimeString)
//                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//
//                        }
//                    })
//                    .show();
//
//
//
//            SharedPreferencesManager.saveTaxnotePlusPurchaseTime(UpgradeActivity.this,result.time);
//
//            showUpgradeToTaxnotePlusSuccessDialog();

            reloadInventory();
        }

        @Override
        public void onError(int response, @Nonnull Exception e) {

            // Show error dialog
            String title = getResources().getString(R.string.Error);
            String message = e.getLocalizedMessage();
            DialogManager.showOKOnlyAlert(UpgradeActivity.this, title, message);

            reloadInventory();
        }
    }

    private class InventoryCallback implements Inventory.Callback {
        @Override
        public void onLoaded(Inventory.Products products) {
            // your code here

            for (Inventory.Product product : products) {


//                // Show dialog message
//                new AlertDialog.Builder(UpgradeActivity.this)
//                        .setTitle(product.toString())
//                        .setMessage(product.id)
//                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//
//                            }
//                        })
//                        .show();
            }
        }
    }

    private void reloadInventory() {

        final Inventory.Request request = Inventory.Request.create();
        request.loadPurchases(SUBSCRIPTION);
        request.loadSkus(SUBSCRIPTION, TAXNOTE_PLUS_ID);
        mCheckout.loadInventory(request, new Inventory.Callback() {
            @Override
            public void onLoaded(@Nonnull Inventory.Products products) {

                for (Inventory.Product product : products) {

//                    // Show dialog message
//                    new AlertDialog.Builder(UpgradeActivity.this)
//                            .setTitle(product.r)
//                            .setMessage(product.getSku(.id))
//                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//
//                                }
//                            })
//                            .show();


                    Purchase purchase = product.getPurchaseInState(TAXNOTE_PLUS_ID, Purchase.State.PURCHASED);
//
                    if (purchase != null) {
                        SharedPreferencesManager.saveTaxnotePlusPurchaseTime(UpgradeActivity.this,purchase.time);
                        showUpgradeToTaxnotePlusSuccessDialog();
                    } else {
                        DialogManager.showToast(UpgradeActivity.this, "purchase no");
                    }



                    for (Purchase purchase1 : product.getPurchases()) {

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
                        String purchaseTimeString = simpleDateFormat.format(purchase1.time);

                        DialogManager.showOKOnlyAlert(UpgradeActivity.this, purchase1.packageName, purchaseTimeString);
                    }

                }


//                for (Inventory.Callback callback : mInventoryCallbacks) {
//                    callback.onLoaded(products);
//                }
            }
        });
    }


    //--------------------------------------------------------------//
    //    -- Display Part --
    //--------------------------------------------------------------//

    private void setViews() {
        setUpgradeToTaxnotePlusView();
        setHelpView();

        showTestDialog();
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

        mCheckout.whenReady(new Checkout.EmptyListener() {
            @Override
            public void onReady(BillingRequests requests) {
                requests.purchase(SUBSCRIPTION, TAXNOTE_PLUS_ID, null, mCheckout.getPurchaseFlow());
            }
        });


//        if (googlePlayPurchaseIsAvailable) {
//            billingProcessor.subscribe(UpgradeActivity.this, TAXNOTE_PLUS_ID);
//            restorePurchases();
//        }
    }

    private void restorePurchases() {

        if (!UpgradeManger.taxnotePlusIsActive(this)) {

//            TransactionDetails details = billingProcessor.getSubscriptionTransactionDetails(TAXNOTE_PLUS_ID);
//
//            if (details != null) {
//                UpgradeManger.updateTaxnotePlusSubscriptionStatus(this, details);
//
//                if (UpgradeManger.taxnotePlusIsActive(this)) {
//                    binding.upgraded.setText(getResources().getString(R.string.upgrade_is_active));
//                }
//            }
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


//    //--------------------------------------------------------------//
//    //    -- IBillingHandler --
//    //--------------------------------------------------------------//
//
//    private void initBillingProcessor() {
//        billingProcessor = new BillingProcessor(this, LICENSE_KEY_OF_GOOGLE_PLAY, this);
//    }
//
//    @Override
//    public void onBillingInitialized() {
//
//        boolean isOneTimePurchaseSupported = billingProcessor.isOneTimePurchaseSupported();
//
//        if (isOneTimePurchaseSupported) {
//            googlePlayPurchaseIsAvailable = true;
//        }
//    }
//
//    @Override
//    public void onProductPurchased(String productId, TransactionDetails details) {
//
//
//
//        if (productId.equals(TAXNOTE_PLUS_ID)) {
//
//            //@@@
//            // Show dialog message
//            new AlertDialog.Builder(this)
//                    .setTitle("onProductPurchased")
//                    .setMessage("onProductPurchased Called With TAXNOTE_PLUS_ID")
//                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//
//                        }
//                    })
//                    .show();
//
//
//
//            UpgradeManger.updateTaxnotePlusSubscriptionStatus(this, details);
//            showUpgradeToTaxnotePlusSuccessDialog();
//
//            MixpanelAPI mixpanel = MixpanelAPI.getInstance(this, MIXPANEL_TOKEN);
//            mixpanel.track("Plus Upgraded");
//
//            //QQ JSONOBjectでエラーがでますねん
////            JSONObject props = new JSONObject();
////            props.put("User Status", "Plus");
////            mixpanel.registerSuperProperties(props);
//        }
//    }
//
//    @Override
//    public void onBillingError(int errorCode, Throwable error) {
//        /*
//         * Called when some error occurred. See Constants class for more details
//         *
//         * Note - this includes handling the case where the user canceled the buy dialog:
//         * errorCode = Constants.BILLING_RESPONSE_RESULT_USER_CANCELED
//         */
//
//        if (errorCode == Constants.BILLING_RESPONSE_RESULT_USER_CANCELED) {
//            return;
//        }
//
//        // Show error dialog
//        String title = getResources().getString(R.string.Error);
//        String message = error.getLocalizedMessage();
//        DialogManager.showOKOnlyAlert(this, title, message);
//    }
//
//    @Override
//    public void onPurchaseHistoryRestored() {
//        /*
//         * Called when purchase history was restored and the list of all owned PRODUCT ID's
//         * was loaded from Google Play
//         */
//    }
}
