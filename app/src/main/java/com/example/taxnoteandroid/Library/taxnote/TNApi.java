package com.example.taxnoteandroid.Library.taxnote;

import android.content.Context;

import com.example.taxnoteandroid.BuildConfig;
import com.example.taxnoteandroid.Library.AsyncOkHttpClient;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;

import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;

/**
 * Created by b0ne on 2017/03/04.
 */

public class TNApi {

    private static final String KEY_IS_SYNCING_DATA = "is_syncing_data";

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

    private static final String KEY_USER_UID = "TAXNOTE_USER_UID";
    private static final String KEY_USER_ACCESS_TOKEN = "TAXNOTE_USER_ACCESS_TOKEN";
    private static final String KEY_USER_CLIENT = "TAXNOTE_USER_CLIENT";

    public static final String HTTP_METHOD_GET = "GET";
    public static final String HTTP_METHOD_POST = "POST";
    public static final String HTTP_METHOD_PUT = "PUT";
    public static final String HTTP_METHOD_DELETE = "DELETE";


    protected Context context;
    private String requestUrl;
    private FormBody formBody;
    private AsyncOkHttpClient.Callback callback;
    private String httpMethod;

    private String loginUid;
    private String loginAccessToken;
    private String loginClient;


    public TNApi(Context context) {
        this.context = context;

        String userUid = SharedPreferencesManager.getUserApiLoginValue(context, KEY_USER_UID);
        if (userUid != null) {
            this.loginUid = userUid;
            this.loginAccessToken = SharedPreferencesManager.getUserApiLoginValue(context, KEY_USER_ACCESS_TOKEN);
            this.loginClient = SharedPreferencesManager.getUserApiLoginValue(context, KEY_USER_CLIENT);
        }
    }

    protected void saveLoginValue(String uid, String accessToken, String client) {
        SharedPreferencesManager.saveUserApiLoginValue(context, KEY_USER_UID, uid);
        SharedPreferencesManager.saveUserApiLoginValue(context, KEY_USER_ACCESS_TOKEN, accessToken);
        SharedPreferencesManager.saveUserApiLoginValue(context, KEY_USER_CLIENT, client);
    }

    protected void deleteLoginData() {
        SharedPreferencesManager.saveUserApiLoginValue(context, KEY_USER_UID, null);
        SharedPreferencesManager.saveUserApiLoginValue(context, KEY_USER_ACCESS_TOKEN, null);
        SharedPreferencesManager.saveUserApiLoginValue(context, KEY_USER_CLIENT, null);
    }

    protected String getLoginUid() {
        return loginUid;
    }

    protected void setRequestPath(String path) {
        setRequestUrl(BuildConfig.TAXNOTE_API_URI + path);
    }

    private void setRequestUrl(String url) {
        requestUrl = url;
    }

    public void setFormBody(FormBody body) {
        formBody = body;
    }

    public void setHttpMethod(String method) {
        httpMethod = method;
    }

    public void setCallback(AsyncOkHttpClient.Callback cb) {
        callback = cb;
    }

    private Headers getHeaders() {
        if (loginUid == null) return null;

        Map<String, String> headerMap = new LinkedHashMap<>();
        headerMap.put("access-token", loginAccessToken);
        headerMap.put("client", loginClient);
        headerMap.put("uid", loginUid);
        return Headers.of(headerMap);
    }

    protected void requestApi() {
        String method = httpMethod;
        if (httpMethod == null) method = HTTP_METHOD_GET;

        // GETの場合、URLクエリーを創る
        if (httpMethod.equals(HTTP_METHOD_GET) && formBody != null) {
            HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                    .scheme("https")
                    .host("taxnote") // dummy
                    .addPathSegment("api"); // dummy
            int formSize = formBody.size();
            if (formSize > 0) {
                for (int i = 0; i < formBody.size(); i++) {
                    urlBuilder.addQueryParameter(formBody.name(i), formBody.value(i));
                }
                requestUrl += "?" + urlBuilder.build().query();
            }
            formBody = null;
        }

        Headers headers = getHeaders();
        AsyncOkHttpClient.execute(headers,
                method, requestUrl, formBody, callback);
    }

    public void setIsSyncing(boolean value) {
        SharedPreferencesManager.saveBoolean(context, KEY_IS_SYNCING_DATA, value);
    }

    public boolean isSyncing() {
        return SharedPreferencesManager.getBoolean(context, KEY_IS_SYNCING_DATA);
    }
}
