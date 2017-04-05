package com.example.taxnoteandroid;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.example.taxnoteandroid.Library.AsyncOkHttpClient;
import com.example.taxnoteandroid.Library.BroadcastUtil;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.UpgradeManger;
import com.example.taxnoteandroid.Library.billing.IabHelper;
import com.example.taxnoteandroid.Library.billing.IabResult;
import com.example.taxnoteandroid.Library.billing.Inventory;
import com.example.taxnoteandroid.Library.billing.Purchase;
import com.example.taxnoteandroid.Library.taxnote.TNApi;
import com.example.taxnoteandroid.Library.taxnote.TNApiModel;
import com.example.taxnoteandroid.Library.taxnote.TNApiUser;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityUpgradeBinding;
import com.helpshift.support.Support;
import com.kobakei.ratethisapp.RateThisApp;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import okhttp3.Response;

import static com.example.taxnoteandroid.TaxnoteConsts.MIXPANEL_TOKEN;


public class UpgradeActivity extends DefaultCommonActivity {

    private ActivityUpgradeBinding binding;
//    private static final String LICENSE_KEY_OF_GOOGLE_PLAY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAm+14FzQyLcAO7X2zwFDWXwHDuzN8RA60R71JouG5TO6la3xh0A7uWIQ4Y2k1kvqa/fHRAOble7TxIDsy11GsLjD/2sI+e4p4pE5vDKeY3ARBadcQI7iDc/VVnkzCSrZeoGTYinm+99diGn71cGIlF+7ISnh98Kss1zguKLlY+tCkaDDCe+moghLYTvqVuJg27ShVfxxPpWr4gwMusdSMcbJLR6S4ajeWbEtacGAdEJnzQfuAH6RMnt/ggZa4CFRVbNnJA6Eft/CCQL7GFBwBYnkMfG+Jdr+66BcTHbtPP8cE5WdmjGzDje+iy5HGYyIfqiDTdBs178zgWKUS8TM9QwIDAQAB";
//    private static final String TAXNOTE_PLUS_ID = "taxnote.plus.sub";
    private static final int REQUEST_CODE_PURCHASE_PREMIUM = 0;
    private static final int REQUEST_CODE_CLOUD_LOGIN = 2;
    private static final int REQUEST_CODE_CLOUD_REGISTER = 3;
    private static final int REQUEST_CODE_CLOUD_CHANGE_PASSWD = 4;
    private IabHelper mBillingHelper;
    private TNApiUser mApiUser;
    private TNApiModel mApiModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_upgrade);
        mApiUser = new TNApiUser(this);
        mApiModel = new TNApiModel(this);
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

            Purchase purchasePlus = inventory.getPurchase(UpgradeManger.SKU_TAXNOTE_PLUS_ID);
            Purchase purchaseCloud = inventory.getPurchase(UpgradeManger.SKU_TAXNOTE_CLOUD_ID);

            // Restore purchase for Taxnote Plus
            if (purchasePlus != null) {
                SharedPreferencesManager.saveTaxnotePlusPurchaseTime(UpgradeActivity.this, purchasePlus.getPurchaseTime());
                updateUpgradeStatus();
            }

            // Restore purchase for Taxnote Cloud
            if (purchaseCloud != null) {
                //@@ save taxnote cloud purchase time ?
//                SharedPreferencesManager.saveTaxnotePlusPurchaseTime(UpgradeActivity.this, purchaseCloud.getPurchaseTime());
                updateUpgradeStatus();
            }
        }
    };

    private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {

        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

            if (result.isFailure()) return;
            if (purchase == null) return;
            if (purchase.getSku().equals(UpgradeManger.SKU_TAXNOTE_PLUS_ID)) {

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

        if (resultCode == RESULT_OK) {
            if (requestCode != REQUEST_CODE_CLOUD_CHANGE_PASSWD) {
                binding.cloudLoginLayout.setVisibility(View.GONE);
                mApiUser = new TNApiUser(this);

                if (requestCode == REQUEST_CODE_CLOUD_LOGIN) {
                    DialogManager.showOKOnlyAlert(this,
                            R.string.thx_for_waiting,
                            R.string.fetch_all_after_login_done);
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
            binding.upgraded.setText(getResources().getString(R.string.upgrade_is_active));
        }

        // taxnote cloud is active
//        if (UpgradeManger) {
//
//        }
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
                mBillingHelper.launchSubscriptionPurchaseFlow(UpgradeActivity.this,
                        UpgradeManger.SKU_TAXNOTE_PLUS_ID,
                        REQUEST_CODE_PURCHASE_PREMIUM, mPurchaseFinishedListener);
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
            binding.cloudRightTv.setText(R.string.cloud);
            binding.cloudLeftTv.setText(userEmail);

            binding.purchaseInfoLayout.setVisibility(View.VISIBLE);
        }

        //@@ クラウド購入しているけど、ログインしている、していない、の表示別


    }
    private View.OnClickListener taxnoteCloudOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int viewId = view.getId();
            switch (viewId) {
//                case R.id.cloud_register_layout:
//                    if (TNApi.isNetworkConnected(getApplicationContext())) {
//                        LoginCloudActivity.startForResult(UpgradeActivity.this,
//                                REQUEST_CODE_CLOUD_REGISTER,
//                                LoginCloudActivity.VIEW_TYPE_REGISTER);
//                    } else {
//                        DialogManager.showOKOnlyAlert(UpgradeActivity.this,
//                                null, getString(R.string.network_not_connection));
//                    }
//                    break;
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
                    if (mApiUser.isLoggingIn()) {
                        showMemberDialogItems();
                    } else {
                        if (TNApi.isNetworkConnected(getApplicationContext())) {
                            LoginCloudActivity.startForResult(UpgradeActivity.this,
                                    REQUEST_CODE_CLOUD_LOGIN,
                                    LoginCloudActivity.VIEW_TYPE_LOGIN);
                        } else {
                            DialogManager.showOKOnlyAlert(UpgradeActivity.this,
                                    null, getString(R.string.network_not_connection));
                        }
                    }
                    break;
                case R.id.purchase_info_layout:
                    String receiptUrl = "https://play.google.com/store/account?feature=gp_receipt";
                    Uri uri = Uri.parse(receiptUrl);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                    break;
            }
        }
    };

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
                    case 2: //@@ アカウントの削除
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
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.loading));
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.show();

        if (!TNApi.isNetworkConnected(this)) {
            mApiUser.clearAccountData(mApiModel);
            binding.cloudLoginLayout.setVisibility(View.VISIBLE);
            dialog.dismiss();
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
                dialog.dismiss();

                // 保存しているtokenを削除
                mApiUser.clearAccountData(mApiModel);
                binding.cloudLoginLayout.setVisibility(View.VISIBLE);
                BroadcastUtil.sendAfterLogin(UpgradeActivity.this, false);

            }

            @Override
            public void onSuccess(Response response, String content) {
                dialog.dismiss();

                mApiUser = new TNApiUser(getApplicationContext());
//                binding.cloudLoginLayout.setVisibility(View.VISIBLE);
                binding.cloudRightTv.setText(R.string.login);

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
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.loading));
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.show();

        mApiUser.deleteSubscriptionAccount(mApiModel, new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e("ERROR", "sendSignOut onFailure ");
                if (response != null) {
                    Log.e("ERROR", "sendSignOut response code: " + response.code()
                            + ", message: " + response.message());
                }
                dialog.dismiss();

//                binding.cloudLoginLayout.setVisibility(View.VISIBLE);
//                BroadcastUtil.sendAfterLogin(UpgradeActivity.this, false);
            }

            @Override
            public void onSuccess(Response response, String content) {
                dialog.dismiss();

                mApiUser = new TNApiUser(getApplicationContext());
                binding.cloudLoginLayout.setVisibility(View.VISIBLE);
                BroadcastUtil.sendAfterLogin(UpgradeActivity.this, false);
            }
        });
    }
}