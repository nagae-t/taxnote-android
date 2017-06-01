package com.example.taxnoteandroid.Library.taxnote;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.example.taxnoteandroid.Library.AsyncOkHttpClient;
import com.example.taxnoteandroid.Library.BroadcastUtil;
import com.example.taxnoteandroid.R;
import com.example.taxnoteandroid.UpgradeActivity;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Response;

/**
 * Created by b0ne on 2017/03/17.
 */

public class TNApiUser extends TNApi {

    private static final String LTAG = TNApiUser.class.getSimpleName();

    private String email;
    private String password;
    private String passwordConfirm;

    private static final String KEY_USER_UID = "TAXNOTE_USER_UID";

    private static final String KEY_CLOUD_ORDER_ID = "TAXNOTE_CLOUD_ORDER_ID";
    private static final String KEY_CLOUD_PURCHASE_TOKEN = "TAXNOTE_CLOUD_PURCHASE_TOKEN";

    public TNApiUser(Context context) {
        super(context);
    }

    public TNApiUser(Context context, String email, String password) {
        super(context);

        this.email = email;
        this.password = password;
        setHttpMethod(HTTP_METHOD_POST);
    }

    public String getEmail() {
        String loginUid = getLoginUid();
        if (email == null && loginUid != null)
            return loginUid;

        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    @Override
    public void setCallback(AsyncOkHttpClient.Callback callback) {
        super.setCallback(callback);
    }

    public void saveLoginWithHttpHeaders(Headers headers) {
        String accessToken = headers.get("Access-Token");
        String client = headers.get("Client");
        String uid = headers.get("Uid");
        super.saveLoginValue(uid, accessToken, client);
    }

    public void saveCloudPurchaseInfo(String orderId, String purchaseToken) {
        SharedPreferencesManager.saveUserApiLoginValue(context,
                KEY_CLOUD_ORDER_ID, orderId);
        SharedPreferencesManager.saveUserApiLoginValue(context,
                KEY_CLOUD_PURCHASE_TOKEN, purchaseToken);
    }

    public static String getCloudOrderId(Context context) {
        return SharedPreferencesManager.getUserApiLoginValue(context, KEY_CLOUD_ORDER_ID);
    }

    static String getCloudPurchaseToken(Context context) {
        return SharedPreferencesManager.getUserApiLoginValue(context, KEY_CLOUD_PURCHASE_TOKEN);
    }

    static String getCloudExpiryString(Context context) {
        long cloudExpiry = SharedPreferencesManager.getTaxnoteCloudExpiryTime(context);
        if (cloudExpiry == 0) return "";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'Etc/GMT'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(cloudExpiry);
        return sdf.format(cal.getTime());
    }

    @Override
    public void deleteLoginData() {
        super.deleteLoginData();
    }

    public void register(final AsyncOkHttpClient.Callback callback) {
        setRequestPath(URL_PATH_REGISTER);
        final FormBody requestBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .add("password_confirmation", passwordConfirm)
                .build();
        setFormBody(requestBody);
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e("ERROR", "register onFailure  ");
                if (response != null) {
                    Log.e("ERROR", "register onFailure code: " + response.code()
                            + ", message: " + response.message());
                }
                callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                saveLoginWithHttpHeaders(response.headers());
                callback.onSuccess(response, content);
            }
        });
        requestApi();

    }

    public void signIn(final AsyncOkHttpClient.Callback callback) {
        setRequestPath(URL_PATH_SIGN_IN);
        FormBody requestBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .build();
        setFormBody(requestBody);
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                Headers headers = response.headers();
                saveLoginWithHttpHeaders(headers);
                callback.onSuccess(response, content);
            }
        });
        requestApi();
    }

    private void signOut(final TNApiModel apiModel, final AsyncOkHttpClient.Callback callback) {
        setHttpMethod(HTTP_METHOD_DELETE);
        setRequestPath(URL_PATH_SIGN_OUT);

        setFormBody(null);
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e(LTAG, "signOut onFailure ");
                if (response != null) {
                    Log.e(LTAG, "signOut onFailure code: " + response.code()
                            + ", message: " + response.message());
                }
                callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                clearAccountData(apiModel);

                callback.onSuccess(response, content);
            }
        });
        requestApi();
    }

    public void clearAccountData(TNApiModel apiModel) {
        //@@ 保存しているtokenを削除
        //@@ iOS [KPTaxnoteApiUserHandler logOutFromSubscriptionAccount];

        // subscription情報を削除
        saveCloudPurchaseInfo(null, null);
        SharedPreferencesManager.saveTaxnoteCloudExpiryTime(context, 0);
        SharedPreferencesManager.saveZenyPremiumExpiryTime(context, 0);

        deleteLoginData();
        apiModel.resetAllUpdatedKeys();
    }


    public void signOutAfterSaveAllData(final TNApiModel apiModel, final AsyncOkHttpClient.Callback callback) {

        apiModel.saveAllNeedSaveSyncDeletedData(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                signOut(apiModel, callback);
            }
        });
    }

    public void sendForgotPassword(final AsyncOkHttpClient.Callback callback) {

        setHttpMethod(HTTP_METHOD_POST);
        setRequestPath(URL_PATH_PASSWORD_RESET);

        FormBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("redirect_url", "http://taxnoteapp.com/")
                .build();

        setFormBody(formBody);
        setCallback(callback);
        requestApi();
    }

    public void updatePassword(String password, final AsyncOkHttpClient.Callback callback) {
        setHttpMethod(HTTP_METHOD_PUT);
        setRequestPath(URL_PATH_PASSWORD_RESET);
        FormBody requestBody = new FormBody.Builder()
                .add("password", password)
                .add("password_confirmation", password)
                .build();
        setFormBody(requestBody);
        setCallback(callback);
        requestApi();
    }

    public void checkUniqueOfSubscription(String transactionId, final AsyncOkHttpClient.Callback callback) {
        setHttpMethod(HTTP_METHOD_GET);
        setRequestPath(URL_PATH_SUBSCRIPTION + "/" + transactionId);

        setFormBody(null);
        setCallback(callback);
        requestApi();
    }

    public void deleteSubscriptionAccount(final TNApiModel apiModel, final AsyncOkHttpClient.Callback callback) {
        setHttpMethod(HTTP_METHOD_DELETE);

        //@@ 課金のTransactionIdはここで指定する
        String transactionId = getCloudOrderId(context);
        setRequestPath(URL_PATH_SUBSCRIPTION + "/" + transactionId);
        setFormBody(null);

        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                //@@ ログアウト処理など
                clearAccountData(apiModel);

                callback.onSuccess(response, content);
            }
        });

        requestApi();
    }

    public void handleAccountError(Activity activity, TNApiModel apiModel) {
        clearAccountData(apiModel);
        BroadcastUtil.sendAfterLogin(activity, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.re_login_title)
                .setMessage(R.string.re_login_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        UpgradeActivity.start(context);
                    }
                });
        builder.create().show();

    }

    public static boolean isLoggingIn(Context context) {
        String userUid = SharedPreferencesManager.getUserApiLoginValue(context, KEY_USER_UID);
        if (userUid != null) {
            return true;
        }
        return false;
    }
}
