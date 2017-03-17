package com.example.taxnoteandroid.Library.taxnote;

import com.example.taxnoteandroid.BuildConfig;
import com.example.taxnoteandroid.Library.AsyncOkHttpClient;

import okhttp3.RequestBody;

/**
 * Created by b0ne on 2017/03/04.
 */

public class TNApi {

    protected static final String URL_PATH_RECURRING = "/v1/recurrings";
    protected static final String URL_PATH_SUBSCRIPTION = "/v1/subscriptions";
    protected static final String URL_PATH_PROJECT = "/v1/projects";
    protected static final String URL_PATH_REASON = "/v1/reasons";
    protected static final String URL_PATH_ACCOUNT = "/v1/accounts";
    protected static final String URL_PATH_SUMMARY = "/v1/summaries";
    protected static final String URL_PATH_ENTRY = "/v1/entries";
    protected static final String URL_PATH_REGISTER = "/auth";
    protected static final String URL_PATH_SIGN_IN = "/auth/sign_in";
    protected static final String URL_PATH_SIGN_OUT = "/auth/sign_out";
    protected static final String URL_PATH_PASSWORD_RESET = "/auth/password";

    public static final String HTTP_METHOD_GET = "GET";
    public static final String HTTP_METHOD_POST = "POST";


    private String requestUrl;
    private RequestBody requestBody;
    private AsyncOkHttpClient.Callback callback;
    private String httpMethod;


    public TNApi() {
    }

    protected void setRequestPath(String path) {
        setRequestUrl(BuildConfig.TAXNOTE_API_URI + path);
    }

    private void setRequestUrl(String url) {
        requestUrl = url;
    }

    public void setRequestBody(RequestBody reqBody) {
        requestBody = reqBody;
    }

    public void setHttpMethod(String method) {
        httpMethod = method;
    }

    public void setCallback(AsyncOkHttpClient.Callback cb) {
        callback = cb;
    }

    protected void requestApi() {
        if (httpMethod.equals(HTTP_METHOD_GET)) {
            AsyncOkHttpClient.get(requestUrl, requestBody, callback);
        } else {
            AsyncOkHttpClient.post(requestUrl, requestBody, callback);
        }
    }
}
