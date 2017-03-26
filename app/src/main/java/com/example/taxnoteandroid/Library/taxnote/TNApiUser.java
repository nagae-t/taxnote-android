package com.example.taxnoteandroid.Library.taxnote;

import android.content.Context;
import android.util.Log;

import com.example.taxnoteandroid.Library.AsyncOkHttpClient;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Response;

/**
 * Created by b0ne on 2017/03/17.
 */

public class TNApiUser extends TNApi {


    private String email;
    private String password;
    private String passwordConfirm;

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
                Log.v("TEST", "register onFailure  ");
                if (response != null) {
                    Log.v("TEST", "register onFailure code: " + response.code()
                            + ", message: " + response.message());
                }
                callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                Log.v("TEST", "register onSuccess headers : " + response.headers().toString());
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

    private void clearAccountData(TNApiModel apiModel) {
        //@@ 保存しているtokenを削除
        //@@ iOS [KPTaxnoteApiUserHandler logOutFromSubscriptionAccount];
        //@@ Sbscription情報を削除
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

    public void sendForgotPassword() {
    }

    public void updatePassword() {
    }

    public void checkUniqeOfSubscription(AsyncOkHttpClient.Callback callback) {
        //@@ 課金IDはここで指定する
        String subscriptionId = "";
        setRequestPath(URL_PATH_SUBSCRIPTION + "/" + subscriptionId);

        setFormBody(null);
        setCallback(callback);
        requestApi();
    }


}
