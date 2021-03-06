package com.example.taxnoteandroid;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;

import com.example.taxnoteandroid.Library.AsyncOkHttpClient;
import com.example.taxnoteandroid.Library.BroadcastUtil;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.TNGoogleApiClient;
import com.example.taxnoteandroid.Library.UpgradeManger;
import com.example.taxnoteandroid.Library.billing.IabHelper;
import com.example.taxnoteandroid.Library.billing.IabResult;
import com.example.taxnoteandroid.Library.billing.Inventory;
import com.example.taxnoteandroid.Library.billing.Purchase;
import com.example.taxnoteandroid.Library.billing.SkuDetails;
import com.example.taxnoteandroid.Library.taxnote.TNApi;
import com.example.taxnoteandroid.Library.taxnote.TNApiModel;
import com.example.taxnoteandroid.Library.taxnote.TNApiUser;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityUpgradeBinding;
import com.example.taxnoteandroid.misc.CustomTabsUtils;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.Task;
import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kobakei.ratethisapp.RateThisApp;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Response;

import static com.example.taxnoteandroid.TaxnoteConsts.MIXPANEL_TOKEN;


public class UpgradeActivity extends DefaultCommonActivity {

    private ActivityUpgradeBinding binding;
    private static final int REQUEST_CODE_PURCHASE_PREMIUM = 0;
    private static final int REQUEST_CODE_CLOUD_LOGIN = 2;
    private static final int REQUEST_CODE_CLOUD_REGISTER = 3;
    private static final int REQUEST_CODE_CLOUD_CHANGE_PASSWD = 4;
    private IabHelper mBillingHelper;
    private TNApiUser mApiUser;
    private TNApiModel mApiModel;
    private TNGoogleApiClient tnGoogleApi;

    private TNSimpleDialogFragment mLoadingProgress;

    public static void start(Context context) {
        Intent intent = new Intent(context, UpgradeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_upgrade);
        mApiUser = new TNApiUser(this);
        mApiModel = new TNApiModel(this);
        tnGoogleApi = new TNGoogleApiClient(this);

        mLoadingProgress = DialogManager.getLoading(this);

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
                mBillingHelper.disposeWhenFinished();
            } catch (Exception e) {
                Log.e("ERROR", e.getLocalizedMessage());
            }
        }
        mBillingHelper = null;
    }


    //--------------------------------------------------------------//
    //    -- Billing --
    //--------------------------------------------------------------//

    private void setupBilling() {

        mBillingHelper = new IabHelper(this, UpgradeManger.getGooglePlayLicenseKey());
//        mBillingHelper.enableDebugLogging(true); // Remove before release
        try {
            mBillingHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {

                public void onIabSetupFinished(IabResult result) {

                    if (result.isFailure()) {
                        Log.e("ERROR", "mBillingHelper.startSetup Failure message: " + result.getMessage());
                        return;
                    }

                    try {
                        List<String> moreSubSkus = new ArrayList<>();
                        moreSubSkus.add(UpgradeManger.SKU_TAXNOTE_PLUS_ID3);
                        moreSubSkus.add(UpgradeManger.SKU_TAXNOTE_CLOUD_ID);

                        mBillingHelper.queryInventoryAsync(true, null, moreSubSkus, mGotInventoryListener);
                    } catch (IabHelper.IabAsyncInProgressException e) {
                        Log.e("ERROR", "mBillingHelper.startSetup catch e: " + e.getMessage());
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

            if (result.isFailure()) {
                Log.e("ERROR", "IabHelper.QueryInventoryFinishedListener Failure message: " + result.getMessage());
                return;
            }

            // Restore purchases
            boolean hasTaxnotePlus = false;
            Purchase purchasePlus = inventory.getPurchase(UpgradeManger.SKU_TAXNOTE_PLUS_ID);
            if (purchasePlus != null) {
                new CheckBillingAsyncTask(false).execute(purchasePlus);
                hasTaxnotePlus = true;
            }

            Purchase purchasePlus1 = inventory.getPurchase(UpgradeManger.SKU_TAXNOTE_PLUS_ID1);
            if (purchasePlus1 != null) {
                new CheckBillingAsyncTask(false).execute(purchasePlus1);
                hasTaxnotePlus = true;
            }

            Purchase purchasePlus2 = inventory.getPurchase(UpgradeManger.SKU_TAXNOTE_PLUS_ID2);
            if (purchasePlus2 != null) {
                new CheckBillingAsyncTask(false).execute(purchasePlus2);
                hasTaxnotePlus = true;
            }

            Purchase purchasePlus3 = inventory.getPurchase(UpgradeManger.SKU_TAXNOTE_PLUS_ID3);
            if (purchasePlus3 != null) {
                new CheckBillingAsyncTask(false).execute(purchasePlus3);
                hasTaxnotePlus = true;
            } else { // ???????????????????????????
                SkuDetails plusSkuDetail = inventory.getSkuDetails(UpgradeManger.SKU_TAXNOTE_PLUS_ID3);
                if (plusSkuDetail != null) {
                    String plusPriceString = plusSkuDetail.getPrice();
                    if (plusPriceString != null) {
                        plusPriceString += " " + getApplicationContext().getString(R.string.taxnote_plus_show_price_tail);
                        binding.upgraded.setText(plusPriceString);
                    }
                }
            }
            if (!hasTaxnotePlus) {
                SharedPreferencesManager.saveTaxnotePlusExpiryTime(getApplicationContext(), 0);
            }

            Purchase purchaseCloud = inventory.getPurchase(UpgradeManger.SKU_TAXNOTE_CLOUD_ID);
            if (purchaseCloud != null) {
                new CheckBillingAsyncTask(false).execute(purchaseCloud);
            } else { // ???????????????????????????
                SkuDetails cloudSkuDetail = inventory.getSkuDetails(UpgradeManger.SKU_TAXNOTE_CLOUD_ID);
                if (cloudSkuDetail != null) {
                    String cloudPriceString = cloudSkuDetail.getPrice();
                    if (cloudPriceString != null) {
                        cloudPriceString += " " + getApplicationContext().getString(R.string.taxnote_cloud_show_price_tail);
                        binding.cloudRightTv.setText(cloudPriceString);
                    }
                }
            }
        }
    };

    private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {

        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

            if (result.isFailure()) return;
            if (purchase == null) return;

            // check billing
            mLoadingProgress.show(getSupportFragmentManager(), null);
            new CheckBillingAsyncTask(true).execute(purchase);

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mBillingHelper.handleActivityResult(requestCode, resultCode, data) || BuildConfig.IS_DEBUG_CLOUD) {
            super.onActivityResult(requestCode, resultCode, data);
        }

        if (resultCode == RESULT_OK) {
            if (requestCode != REQUEST_CODE_CLOUD_CHANGE_PASSWD) {
                binding.cloudLoginLayout.setVisibility(View.GONE);
                mApiUser = new TNApiUser(this);

                binding.cloudLeftTv.setText(R.string.cloud);
                binding.cloudRightTv.setText(mApiUser.getEmail());
                binding.cloudPurchaseLayout.setVisibility(View.VISIBLE);
                binding.purchaseInfoLayout.setVisibility(View.VISIBLE);

                BroadcastUtil.sendAfterLogin(UpgradeActivity.this, true);
                if (requestCode == REQUEST_CODE_CLOUD_LOGIN) {
                    DialogManager.showOKOnlyAlert(this,
                            R.string.thx_for_waiting,
                            R.string.fetch_all_after_login_done);
                    updateUpgradeStatus();
                    BroadcastUtil.sendReloadReport(UpgradeActivity.this);

                } else if (requestCode == REQUEST_CODE_CLOUD_REGISTER) {
                    DialogManager.showOKOnlyAlert(this,
                            R.string.thx_for_waiting,
                            R.string.upload_all_after_register_done);
                }
            } else {
                DialogManager.showOKOnlyAlert(this, null, getString(R.string.change_password_done));
            }
        }
    }


    //--------------------------------------------------------------//
    //    -- Display Part --
    //--------------------------------------------------------------//

    private void setViews() {
        setUpgradeToTaxnotePlusView();
        setTaxnoteCloud();
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
            binding.upgraded.setText(R.string.upgrade_is_active);
            binding.upgradeToPlus.setOnClickListener(null);
        }

        // taxnote cloud is active
        if (UpgradeManger.taxnoteCloudIsActive(this) && !mApiUser.isLoggingIn()) {
            binding.purchaseInfoLayout.setVisibility(View.VISIBLE);
            binding.cloudPurchaseLayout.setVisibility(View.GONE);
        }

        if (mApiModel.isCloudActive()) {
            TypedValue statusOnTv = new TypedValue();
            getTheme().resolveAttribute(R.attr.colorPrimary, statusOnTv, true);
            binding.purchaseInfoStatus.setText(R.string.cloud_purchase_status_on);
            binding.purchaseInfoStatus.setTextColor(ContextCompat.getColor(this, statusOnTv.resourceId));
        }
        // ?????????????????????????????????????????????????????????
        if (mApiUser.isLoggingIn() && !mApiModel.isCloudActive()) {
            binding.purchaseInfoStatus.setText(R.string.cloud_purchase_status_off);
            binding.purchaseInfoStatus.setTextColor(ContextCompat.getColor(this, R.color.expense));

        }

    }

    private void setHelpView() {

        binding.help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomTabsUtils.showHelp(UpgradeActivity.this, CustomTabsUtils.Content.UPGRADE);
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
                        UpgradeManger.SKU_TAXNOTE_PLUS_ID3,
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

    private void showUpgradeToTaxnoteCloudSuccessDialog() {
        if (!mApiModel.isCloudActive()) return;

        MixpanelAPI mixpanel = MixpanelAPI.getInstance(this, MIXPANEL_TOKEN);
        mixpanel.track("Taxnote Cloud Upgraded");

        if (isFinishing()) return;

        // Taxnote?????????????????????????????????????????????????????????
        new AlertDialog.Builder(this)
                .setTitle(R.string.cloud_sign_up_title)
                .setMessage(R.string.cloud_sign_up_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        checkTransaction(true);
                    }
                })
                .setCancelable(false)
                .create().show();
    }

    // ???????????????Taxnote???????????????????????????
    private void checkTransaction(final boolean isMoveNext) {
        String transactionId = TNApiUser.getCloudOrderId(this);
        if (transactionId == null || isFinishing()) return;

        final TNSimpleDialogFragment loadingProg = DialogManager.getLoading(this);
        loadingProg.show(getSupportFragmentManager(), "loading-prog");
        mApiUser.checkUniqueOfSubscription(transactionId, new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                loadingProg.dismiss();

                Log.e("ERROR", "checkUniqueOfSubscription onFailure ");

                String errorMsg = "";
                if (response != null) {
                    errorMsg = response.message();
                } else if (throwable != null) {
                    errorMsg = throwable.getLocalizedMessage();
                }
                Log.e("ERROR", "checkUniqueOfSubscription : " + errorMsg);
            }

            @Override
            public void onSuccess(Response response, String content) {
                loadingProg.dismiss();
                if (content == null || content.length() == 0) {
                    if (isMoveNext) {
                        LoginCloudActivity.startForResult(UpgradeActivity.this,
                                REQUEST_CODE_CLOUD_REGISTER,
                                LoginCloudActivity.VIEW_TYPE_REGISTER);
                    } else {
                        // Taxnote?????????????????????????????????????????????????????????
                        new AlertDialog.Builder(UpgradeActivity.this)
                                .setTitle(R.string.cloud_sign_up_title)
                                .setMessage(R.string.cloud_sign_up_message)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        LoginCloudActivity.startForResult(UpgradeActivity.this,
                                                REQUEST_CODE_CLOUD_REGISTER,
                                                LoginCloudActivity.VIEW_TYPE_REGISTER);
                                    }
                                })
                                .setCancelable(false)
                                .create().show();
                    }
                    return;
                }
                JsonParser jsonParser = new JsonParser();
                JsonObject obj = jsonParser.parse(content).getAsJsonObject();
                String email = obj.get("email").getAsString();
                showAlreadyLoginEmailDialog(email);
            }
        });
    }

    private void showAlreadyLoginEmailDialog(final String email) {
        mApiUser.setEmail(email);
        new AlertDialog.Builder(this)
                .setTitle(email)
                .setMessage(R.string.cloud_account_exists_message)
                .setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        LoginCloudActivity.startForResult(UpgradeActivity.this,
                                REQUEST_CODE_CLOUD_LOGIN,
                                LoginCloudActivity.VIEW_TYPE_LOGIN, email);
                    }
                })
                .setNeutralButton(R.string.delete_account, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showConfirmDeleteAccount(email);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(false)
                .create().show();
    }

    //--------------------------------------------------------------//
    //    -- Taxnote cloud login etc... --
    //--------------------------------------------------------------//

    private void setTaxnoteCloud() {
        binding.cloudLoginLayout.setOnClickListener(taxnoteCloudOnClick);
        binding.cloudPurchaseLayout.setOnClickListener(taxnoteCloudOnClick);
        binding.purchaseInfoLayout.setOnClickListener(taxnoteCloudOnClick);

        String userEmail = mApiUser.getEmail();
        if (userEmail != null) { // Taxnote????????????????????????????????????????????????
            binding.cloudLoginLayout.setVisibility(View.GONE);
            binding.cloudLeftTv.setText(R.string.cloud);
            binding.cloudRightTv.setText(userEmail);

            binding.purchaseInfoLayout.setVisibility(View.VISIBLE);
        }
    }

    private View.OnClickListener taxnoteCloudOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int viewId = view.getId();
            switch (viewId) {
                case R.id.cloud_login_layout:
                    if (TNApi.isNetworkConnected(getApplicationContext())) {
                        LoginCloudActivity.startForResult(UpgradeActivity.this,
                                REQUEST_CODE_CLOUD_LOGIN,
                                LoginCloudActivity.VIEW_TYPE_LOGIN);
                    } else {
                        DialogManager.showOKOnlyAlert(UpgradeActivity.this,
                                null, getString(R.string.network_not_connection));
                    }
                    break;
                case R.id.cloud_purchase_layout:
                    checkCloudPurchaseAction();
                    break;
                case R.id.purchase_info_layout:
                    if (mApiModel.isCloudActive()) {
                        String receiptUrl = "https://play.google.com/store/account?feature=gp_receipt";
                        Uri uri = Uri.parse(receiptUrl);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    } else {
                        upgradeToTaxnoteCloud();
                    }
                    break;
            }
        }
    };

    private void checkCloudPurchaseAction() {
        if (BuildConfig.IS_DEBUG_CLOUD) {
            if (mApiUser.isLoggingIn()) {
                showMemberDialogItems();
            } else {
                LoginCloudActivity.startForResult(UpgradeActivity.this,
                        REQUEST_CODE_CLOUD_REGISTER,
                        LoginCloudActivity.VIEW_TYPE_REGISTER);
            }
            return;
        }

        if (!mApiUser.isLoggingIn() && !mApiModel.isCloudActive()) {
            upgradeToTaxnoteCloud();
        } else {
            if (mApiUser.isLoggingIn()) {
                showMemberDialogItems();
            } else {
                if (TNApi.isNetworkConnected(getApplicationContext())) {
                    LoginCloudActivity.startForResult(UpgradeActivity.this,
                            REQUEST_CODE_CLOUD_REGISTER,
                            LoginCloudActivity.VIEW_TYPE_REGISTER);
                } else {
                    DialogManager.showOKOnlyAlert(UpgradeActivity.this,
                            null, getString(R.string.network_not_connection));
                }
            }
        }
    }

    private void upgradeToTaxnoteCloud() {
        if (!BuildConfig.DEBUG) {
            if (!mBillingHelper.subscriptionsSupported()) return;
        }

        // Taxnote??????????????????????????????????????????????????????????????????
        if (!UpgradeManger.taxnotePlusIsActive(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(R.string.caution)
                    .setMessage(R.string.confirm_cloud_without_plus_msg)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setNeutralButton(R.string.view_help, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CustomTabsUtils.showHelp(UpgradeActivity.this, CustomTabsUtils.Content.UPGRADE);
                        }
                    })
                    .setPositiveButton(R.string.buy_taxnote_cloud_with_taxnote_plus_limit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            confirmUpgradeToTaxnoteCloundWithOutPlus();
                        }
                    });
            builder.create().show();
        } else {
            try {
                mBillingHelper.launchSubscriptionPurchaseFlow(this, UpgradeManger.SKU_TAXNOTE_CLOUD_ID,
                        REQUEST_CODE_PURCHASE_PREMIUM, mPurchaseFinishedListener);
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        }

    }

    private void confirmUpgradeToTaxnoteCloundWithOutPlus() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.plus_not_bought_title)
                .setMessage(R.string.confirm_cloud_without_plus_msg_again)
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.view_help, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CustomTabsUtils.showHelp(UpgradeActivity.this, CustomTabsUtils.Content.UPGRADE);
                    }
                })
                .setPositiveButton(R.string.buy_taxnote_cloud_with_taxnote_plus_limit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            mBillingHelper.launchSubscriptionPurchaseFlow(UpgradeActivity.this,
                                    UpgradeManger.SKU_TAXNOTE_CLOUD_ID,
                                    REQUEST_CODE_PURCHASE_PREMIUM, mPurchaseFinishedListener);
                        } catch (IabHelper.IabAsyncInProgressException e) {
                            e.printStackTrace();
                        }
                    }
                });
        builder.create().show();
    }

    private void showMemberDialogItems() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] items = {getString(R.string.logout),
                getString(R.string.change_password),
                getString(R.string.delete_account)};
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0: // ???????????????
                        sendSignOut();
                        break;
                    case 1: //@@ ????????????????????????
                        if (TNApi.isNetworkConnected(getApplicationContext())) {
                            ChangePasswordActivity.startForResult(
                                    UpgradeActivity.this, REQUEST_CODE_CLOUD_CHANGE_PASSWD);
                        } else {
                            DialogManager.showOKOnlyAlert(UpgradeActivity.this,
                                    null, getString(R.string.network_not_connection));
                        }
                        break;
                    case 2: // ????????????????????????
                        showConfirmDeleteAccount(null);
                        break;
                }
            }
        });
        AlertDialog menuDialog = builder.create();
        menuDialog.show();
    }

    /**
     * ???????????????????????????????????????????????????
     */
    private void sendSignOut() {
        mLoadingProgress.show(getSupportFragmentManager(), null);

        if (!TNApi.isNetworkConnected(this)) {
            mApiUser.clearAccountData(mApiModel);
            mApiUser = new TNApiUser(getApplicationContext());
            binding.cloudRightTv.setText(R.string.cloud_register);
            binding.cloudPurchaseLayout.setVisibility(View.GONE);
            binding.cloudLoginLayout.setVisibility(View.VISIBLE);
            mLoadingProgress.dismiss();
            BroadcastUtil.sendAdviewToggle(UpgradeActivity.this);
            return;
        }

        mApiUser.signOutAfterSaveAllData(mApiModel, new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e("ERROR", "sendSignOut onFailure ");
                if (response != null) {
                    Log.e("ERROR", "sendSignOut response code: " + response.code()
                            + ", message: " + response.message());
                }
                mLoadingProgress.dismiss();

                // ??????????????????token?????????
                mApiUser.clearAccountData(mApiModel);
                mApiUser = new TNApiUser(getApplicationContext());
                binding.cloudRightTv.setText(R.string.cloud_register);
                binding.cloudPurchaseLayout.setVisibility(View.GONE);
                binding.cloudLoginLayout.setVisibility(View.VISIBLE);
                BroadcastUtil.sendAfterLogin(UpgradeActivity.this, false);

            }

            @Override
            public void onSuccess(Response response, String content) {
                mLoadingProgress.dismiss();

                mApiUser = new TNApiUser(getApplicationContext());
                binding.cloudRightTv.setText(R.string.cloud_register);
                binding.cloudPurchaseLayout.setVisibility(View.GONE);
                binding.cloudLoginLayout.setVisibility(View.VISIBLE);

                BroadcastUtil.sendAfterLogin(UpgradeActivity.this, false);
            }
        });
    }

    private void showConfirmDeleteAccount(String email) {
        if (email == null && !mApiUser.isLoggingIn()) return;

        if (!TNApi.isNetworkConnected(this)) {
            DialogManager.showOKOnlyAlert(UpgradeActivity.this,
                    null, getString(R.string.network_not_connection));
            return;
        }

        if (email == null)
            email = mApiUser.getEmail();
        String deleteAccMsg = email + "\n" + getString(R.string.delete_account_desc);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.delete_account)
                .setMessage(deleteAccMsg)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.Delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendDeleteAccount();
                    }
                });
        builder.create().show();

    }

    private void sendDeleteAccount() {
        if (!TNApi.isNetworkConnected(this)) {
            DialogManager.showToast(this, getString(R.string.network_not_connection));
            return;
        }

        // Progress dialog
        mLoadingProgress.show(getSupportFragmentManager(), null);

        mApiUser.deleteSubscriptionAccount(mApiModel, new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e("ERROR", "sendDeleteAccount onFailure ");
                if (response != null) {
                    Log.e("ERROR", "sendDeleteAccount response code: " + response.code()
                            + ", message: " + response.message());
                    String errorMsg = response.message();
                    DialogManager.showOKOnlyAlert(UpgradeActivity.this,
                            getString(R.string.Error), errorMsg);
                }
                mLoadingProgress.dismiss();

            }

            @Override
            public void onSuccess(Response response, String content) {
                mLoadingProgress.dismiss();

                DialogManager.showToast(getApplicationContext(), getString(R.string.delete_done));

                mApiUser = new TNApiUser(getApplicationContext());
                binding.cloudRightTv.setText(R.string.cloud_register);
                binding.cloudPurchaseLayout.setVisibility(View.GONE);
                binding.cloudLoginLayout.setVisibility(View.VISIBLE);
                BroadcastUtil.sendAfterLogin(UpgradeActivity.this, false);
            }
        });
    }

    private class CheckBillingAsyncTask extends AsyncTask<Purchase, Void, SubscriptionPurchase> {
        private String subscriptionId;
        private final boolean isNewPurchased;
        private Purchase mPurchase;

        private CheckBillingAsyncTask(boolean isNewPurchased) {
            this.isNewPurchased = isNewPurchased;
        }

        @Override
        protected SubscriptionPurchase doInBackground(Purchase... purchases) {
            if (tnGoogleApi == null) cancel(true);

            mPurchase = purchases[0];
            subscriptionId = mPurchase.getSku();
            return tnGoogleApi.getSubscription(subscriptionId, mPurchase.getToken());
        }

        @Override
        protected void onPostExecute(SubscriptionPurchase result) {
            if (mLoadingProgress != null) {
                try {
                    mLoadingProgress.dismissAllowingStateLoss();
                } catch (Exception ee) {
                }
            }
            if (subscriptionId == null) return;
            if (isFinishing()) return;

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
            mApiModel = new TNApiModel(context);
            switch (subscriptionId) {
                case UpgradeManger.SKU_TAXNOTE_PLUS_ID:
                case UpgradeManger.SKU_TAXNOTE_PLUS_ID1:
                case UpgradeManger.SKU_TAXNOTE_PLUS_ID2:
                case UpgradeManger.SKU_TAXNOTE_PLUS_ID3:
                    SharedPreferencesManager.saveTaxnotePlusExpiryTime(
                            context, expiryTime);

                    if (isNewPurchased)
                        showUpgradeToTaxnotePlusSuccessDialog();
                    break;
                case UpgradeManger.SKU_TAXNOTE_CLOUD_ID:
                    SharedPreferencesManager.saveTaxnoteCloudExpiryTime(
                            context, expiryTime);

                    String orderId = mPurchase.getOrderId();
                    String purchaseToken = mPurchase.getToken();
                    if (orderId == null || orderId.length() == 0)
                        orderId = purchaseToken.substring(0, 24);
                    mApiUser.saveCloudPurchaseInfo(orderId, mPurchase.getToken());

                    if (isNewPurchased)
                        showUpgradeToTaxnoteCloudSuccessDialog();

                    if (!isNewPurchased && !mApiUser.isLoggingIn())
                        checkTransaction(false);
                    break;
            }

            updateUpgradeStatus();
            if (isNewPurchased) {
                review();
            }
        }
    }

    private void review() {
        final ReviewManager manager = ReviewManagerFactory.create(this);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(new OnCompleteListener<ReviewInfo>() {
            @Override
            public void onComplete(Task<ReviewInfo> task) {
                if (task.isSuccessful()) {
                    ReviewInfo reviewInfo = task.getResult();
                    manager.launchReviewFlow(UpgradeActivity.this, reviewInfo);
                }
            }
        });
    }
}