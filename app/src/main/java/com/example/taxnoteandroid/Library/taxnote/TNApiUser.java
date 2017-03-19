package com.example.taxnoteandroid.Library.taxnote;

import android.content.Context;

import com.example.taxnoteandroid.Library.AsyncOkHttpClient;

import okhttp3.FormBody;
import okhttp3.RequestBody;

/**
 * Created by b0ne on 2017/03/17.
 */

public class TNApiUser extends TNApi {

    private String email;
    private String password;
    private String passwordConfirm;

    public TNApiUser(Context context, String email, String password) {
        this.context = context;

        this.email = email;
        this.password = password;
        setHttpMethod(HTTP_METHOD_POST);
    }

    public String getEmail() {
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

    public void register(AsyncOkHttpClient.Callback callback) {
        setRequestPath(URL_PATH_REGISTER);
        RequestBody requestBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .add("password_confirmation", passwordConfirm)
                .build();
        setRequestBody(requestBody);
        setCallback(callback);
        requestApi();

    }

    public void signIn(AsyncOkHttpClient.Callback callback) {
        setRequestPath(URL_PATH_SIGN_IN);
        RequestBody requestBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .build();
        setRequestBody(requestBody);
        setCallback(callback);
        requestApi();
    }

    public void signOut(AsyncOkHttpClient.Callback callback) {
        setHttpMethod(HTTP_METHOD_DELETE);
        setRequestPath(URL_PATH_SIGN_OUT);

        setRequestBody(null);
        setCallback(callback);
        requestApi();
    }

    public void updatePassword() {
    }


}
