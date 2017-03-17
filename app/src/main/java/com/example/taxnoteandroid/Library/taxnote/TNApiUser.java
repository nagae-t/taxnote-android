package com.example.taxnoteandroid.Library.taxnote;

import com.example.taxnoteandroid.Library.AsyncOkHttpClient;

import okhttp3.FormBody;
import okhttp3.RequestBody;

/**
 * Created by b0ne on 2017/03/17.
 */

public class TNApiUser extends TNApi {

    private String email;
    private String password;

    public TNApiUser(String email, String password) {
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
}
