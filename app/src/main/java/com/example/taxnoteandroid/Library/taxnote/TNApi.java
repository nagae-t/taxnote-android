package com.example.taxnoteandroid.Library.taxnote;

import android.content.Context;

import com.example.taxnoteandroid.BuildConfig;
import com.example.taxnoteandroid.Library.AsyncOkHttpClient;

import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.Headers;
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
    public static final String HTTP_METHOD_PUT = "PUT";
    public static final String HTTP_METHOD_DELETE = "DELETE";


    protected Context context;
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

    private Headers getHeaders() {
        Map<String, String> headerMap = new LinkedHashMap<>();

        headerMap.put("access-token", "3q23DESWpgcZsWPFuZFoOg");
        headerMap.put("client", "vkvrV-wKkCPfTFNBr5W5-Q");
        headerMap.put("uid", "m@m.com");
        return Headers.of(headerMap);
    }

    protected void requestApi() {
        String method = httpMethod;
        if (httpMethod == null) method = HTTP_METHOD_GET;

        AsyncOkHttpClient.execute(getHeaders(),
                method, requestUrl, requestBody, callback);
    }
}
