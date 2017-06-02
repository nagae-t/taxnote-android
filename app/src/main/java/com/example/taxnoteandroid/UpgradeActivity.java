package com.example.taxnoteandroid;

import android.app.ProgressDialog;
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
import com.example.taxnoteandroid.Library.taxnote.TNApi;
import com.example.taxnoteandroid.Library.taxnote.TNApiModel;
import com.example.taxnoteandroid.Library.taxnote.TNApiUser;
import com.example.taxnoteandroid.Library.zeny.ZNUtils;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityUpgradeBinding;
import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.helpshift.support.Support;
import com.kobakei.ratethisapp.RateThisApp;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

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

    private ProgressDialog mLoadingProgress;

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

        mBillingHelper = new IabHelper(this, UpgradeManger.GOOGLE_PLAY_LICENSE_KEY);
//        mBillingHelper.enableDebugLogging(true); // Remove before release
        try {
            mBillingHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {

                public void onIabSetupFinished(IabResult result) {

                    if (result.isFailure()) {
                        Log.v("TEST", "mBillingHelper.startSetup Failure message: " + result.getMessage());
                        return;
                    }

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

            // Restore purchases
            Purchase purchasePlus = inventory.getPurchase(UpgradeManger.SKU_TAXNOTE_PLUS_ID);
            if (purchasePlus != null)
                new CheckBillingAsyncTask(false).execute(purchasePlus);

            Purchase purchasePlus1 = inventory.getPurchase(UpgradeManger.SKU_TAXNOTE_PLUS_ID1);
            if (purchasePlus1 != null)
                new CheckBillingAsyncTask(false).execute(purchasePlus1);

            Purchase purchasePlus2 = inventory.getPurchase(UpgradeManger.SKU_TAXNOTE_PLUS_ID2);
            if (purchasePlus2 != null)
                new CheckBillingAsyncTask(false).execute(purchasePlus2);

            Purchase purchaseCloud = inventory.getPurchase(UpgradeManger.SKU_TAXNOTE_CLOUD_ID);
            if (purchaseCloud != null)
                new CheckBillingAsyncTask(false).execute(purchaseCloud);

            if (ZNUtils.isZeny()) {
                Log.v("TEST", "Zeny QueryInventoryFinishedListener");
                Purchase purchaseZeny = inventory.getPurchase(UpgradeManger.SKU_ZENY_PREMIUM_ID);
                if (purchaseZeny != null) {
                    Log.v("TEST", "Zeny QueryInventoryFinishedListener purchaseZeny = " + purchaseZeny.toString());
                    new CheckBillingAsyncTask(false).execute(purchaseZeny);
                }
            }
        }
    };

    private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {

        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

            if (result.isFailure()) return;
            if (purchase == null) return;

            // check billing
            mLoadingProgress.show();
            new CheckBillingAsyncTask(true).execute(purchase);

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mBillingHelper.handleActivityResult(requestCode, resultCode, data)) {
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
                BroadcastUtil.sendAfterLogin(UpgradeActivity.this, true);
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
        // ログインしていて、有効期限が切れた場合
        if (mApiUser.isLoggingIn() && !mApiModel.isCloudActive()) {
            binding.purchaseInfoStatus.setText(R.string.cloud_purchase_status_off);
            binding.purchaseInfoStatus.setTextColor(ContextCompat.getColor(this, R.color.expense));

        }

    }

    private void setHelpView() {

        binding.help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ZNUtils.isZeny()) {
                    Support.showSingleFAQ(UpgradeActivity.this, "117");
                } else {
                    Support.showSingleFAQ(UpgradeActivity.this, "108");
                }
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
                        UpgradeManger.SKU_TAXNOTE_PLUS_ID2,
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
//        if (!UpgradeManger.taxnoteCloudIsActive(this)) return;
        Log.v("TEST", "showUpgradeToTaxnoteCloudSuccessDialog 0");
        if (!mApiModel.isCloudActive()) return;
        Log.v("TEST", "showUpgradeToTaxnoteCloudSuccessDialog 1");

        MixpanelAPI mixpanel = MixpanelAPI.getInstance(this, MIXPANEL_TOKEN);
        if (ZNUtils.isZeny()) {
            mixpanel.track("Zeny Premium Upgraded");
        } else {
            mixpanel.track("Taxnote Cloud Upgraded");
        }

        if (!isFinishing()) return;
        Log.v("TEST", "showUpgradeToTaxnoteCloudSuccessDialog 2");
        // Taxnoteアカウント作成するようダイアログを表示
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

    // 課金情報とTaxnoteアカウントを調べる
    private void checkTransaction(final boolean isMoveNext) {
        String transactionId = TNApiUser.getCloudOrderId(this);
        if (transactionId == null || isFinishing()) return;

        mLoadingProgress.show();
        mApiUser.checkUniqueOfSubscription(transactionId, new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                mLoadingProgress.dismiss();

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
                mLoadingProgress.dismiss();
                if (content == null || content.length() == 0) {
                    if (isMoveNext) {
                        LoginCloudActivity.startForResult(UpgradeActivity.this,
                                REQUEST_CODE_CLOUD_REGISTER,
                                LoginCloudActivity.VIEW_TYPE_REGISTER);
                    } else {
                        // Taxnoteアカウント作成するようダイアログを表示
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
                        showConfirmDeleteAccount();
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
        if (userEmail != null) { // Taxnoteアカウントでログインしている場合
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
        if (!mBillingHelper.subscriptionsSupported()) return;

        // Taxnoteプラス購入済みか確認してダイアログを表示する
        if (!ZNUtils.isZeny() && !UpgradeManger.taxnotePlusIsActive(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(R.string.plus_not_bought_title)
                    .setMessage(R.string.plus_not_bought_message)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
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
        } else {
            String skuId = (ZNUtils.isZeny()) ? UpgradeManger.SKU_ZENY_PREMIUM_ID
                    : UpgradeManger.SKU_TAXNOTE_CLOUD_ID;
            try {
                    mBillingHelper.launchSubscriptionPurchaseFlow(this, skuId,
                            REQUEST_CODE_PURCHASE_PREMIUM, mPurchaseFinishedListener);
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        }

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
                    case 0: // ログアウト
                        sendSignOut();
                        break;
                    case 1: //@@ パスワードの変更
                        if (TNApi.isNetworkConnected(getApplicationContext())) {
                            ChangePasswordActivity.startForResult(
                                    UpgradeActivity.this, REQUEST_CODE_CLOUD_CHANGE_PASSWD);
                        } else {
                            DialogManager.showOKOnlyAlert(UpgradeActivity.this,
                                    null, getString(R.string.network_not_connection));
                        }
                        break;
                    case 2: // アカウントの削除
                        showConfirmDeleteAccount();
                        break;
                }
            }
        });
        AlertDialog menuDialog = builder.create();
        menuDialog.show();
    }

    /**
     * 同期を完了させてからログアウトする
     */
    private void sendSignOut() {
        // Progress dialog
        mLoadingProgress.show();

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

                // 保存しているtokenを削除
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

    private void showConfirmDeleteAccount() {
        if (!mApiUser.isLoggingIn()) return;

        if (!TNApi.isNetworkConnected(this)) {
            DialogManager.showOKOnlyAlert(UpgradeActivity.this,
                    null, getString(R.string.network_not_connection));
            return;
        }

        String email = mApiUser.getEmail();
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
        mLoadingProgress.show();

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
            subscriptionId =  mPurchase.getSku();
            SubscriptionPurchase subPurchase = tnGoogleApi.getSubscription(subscriptionId, mPurchase.getToken());
            return subPurchase;
        }

        @Override
        protected void onPostExecute(SubscriptionPurchase result) {
            if (mLoadingProgress.isShowing()) mLoadingProgress.dismiss();
            if (result == null || subscriptionId == null) return;
            if (isFinishing()) return;

            Context context = getApplicationContext();
            mApiModel = new TNApiModel(context);
            Log.v("TEST", "CheckBillingAsyncTask subscriptionId : " + subscriptionId
                    + ", test 0");
            switch (subscriptionId) {
                case UpgradeManger.SKU_TAXNOTE_PLUS_ID:
                case UpgradeManger.SKU_TAXNOTE_PLUS_ID1:
                case UpgradeManger.SKU_TAXNOTE_PLUS_ID2:
                    SharedPreferencesManager.saveTaxnotePlusExpiryTime(
                            context, result.getExpiryTimeMillis());

                    if (isNewPurchased)
                        showUpgradeToTaxnotePlusSuccessDialog();
                    break;
                case UpgradeManger.SKU_TAXNOTE_CLOUD_ID:
                case UpgradeManger.SKU_ZENY_PREMIUM_ID:
                    Log.v("TEST", "CheckBillingAsyncTask subscriptionId : " + subscriptionId
                        + ", test 1");
                    long expiryTime = result.getExpiryTimeMillis();
                    if (ZNUtils.isZeny()) {
                        SharedPreferencesManager.saveZenyPremiumExpiryTime(
                                context, expiryTime);
                        BroadcastUtil.sendAdviewToggle(UpgradeActivity.this);
                    } else {
                        SharedPreferencesManager.saveTaxnoteCloudExpiryTime(
                                context, expiryTime);
                    }

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
        }
    }
}