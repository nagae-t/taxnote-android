package com.example.taxnoteandroid.Library.taxnote;

import android.content.Context;

import com.example.taxnoteandroid.Library.AsyncOkHttpClient;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.RequestBody;

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

    public void register(AsyncOkHttpClient.Callback callback) {
        setRequestPath(URL_PATH_REGISTER);
        RequestBody requestBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .add("password_confirmation", passwordConfirm)
                .build();
        setFormBody(requestBody);
        setCallback(callback);
        requestApi();

    }

    public void signIn(AsyncOkHttpClient.Callback callback) {
        setRequestPath(URL_PATH_SIGN_IN);
        RequestBody requestBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .build();
        setFormBody(requestBody);
        setCallback(callback);
        requestApi();
    }

    public void signOut(AsyncOkHttpClient.Callback callback) {
        setHttpMethod(HTTP_METHOD_DELETE);
        setRequestPath(URL_PATH_SIGN_OUT);

        setFormBody(null);
        setCallback(callback);
        requestApi();
    }

    public void sendForgotPassword() {
    }

    public void updatePassword() {
    }


}
