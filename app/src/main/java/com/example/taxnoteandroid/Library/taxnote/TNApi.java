package com.example.taxnoteandroid.Library.taxnote;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.taxnoteandroid.BuildConfig;
import com.example.taxnoteandroid.Library.AsyncOkHttpClient;
import com.example.taxnoteandroid.Library.UpgradeManger;
import com.example.taxnoteandroid.Library.zeny.ZNUtils;
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

    private static final String SECRET_FOR_SUBSCRIPTION_CHECK = "pgehz98bptpu6j9dnp7wf33sz";
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

    private static final String PATH_BULK_CREATE = "/bulk_create";
    private static final String PATH_BULK_UPDATE = "/bulk_update";
    private static final String PATH_BULK_DESTROY = "/bulk_destroy";

    protected static final String URL_PATH_ENTRY_BULK_SAVE = URL_PATH_ENTRY + PATH_BULK_CREATE;
    protected static final String URL_PATH_REASON_BULK_SAVE = URL_PATH_REASON+ PATH_BULK_CREATE;
    protected static final String URL_PATH_ACCOUNT_BULK_SAVE = URL_PATH_ACCOUNT + PATH_BULK_CREATE;
    protected static final String URL_PATH_SUMARY_BULK_SAVE = URL_PATH_SUMMARY + PATH_BULK_CREATE;
    protected static final String URL_PATH_RECURRING_BULK_SAVE = URL_PATH_RECURRING + PATH_BULK_CREATE;

    private static final String KEY_USER_UID = "TAXNOTE_USER_UID";
    private static final String KEY_USER_ACCESS_TOKEN = "TAXNOTE_USER_ACCESS_TOKEN";
    private static final String KEY_USER_CLIENT = "TAXNOTE_USER_CLIENT";

    public static final String HTTP_METHOD_GET = "GET";
    public static final String HTTP_METHOD_POST = "POST";
    public static final String HTTP_METHOD_PUT = "PUT";
    public static final String HTTP_METHOD_DELETE = "DELETE";


    protected Context context;
    private String requestUrlPath;
    private String requestUrl;
    private FormBody formBody;
    private AsyncOkHttpClient.Callback callback;
    private AsyncOkHttpClient.ResponseCallback respCallback;
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

        this.loginUid = uid;
        this.loginAccessToken = accessToken;
        this.loginClient = client;
    }

    protected void deleteLoginData() {
        SharedPreferencesManager.saveUserApiLoginValue(context, KEY_USER_UID, null);
        SharedPreferencesManager.saveUserApiLoginValue(context, KEY_USER_ACCESS_TOKEN, null);
        SharedPreferencesManager.saveUserApiLoginValue(context, KEY_USER_CLIENT, null);
        this.loginUid = null;
    }

    protected String getLoginUid() {
        return loginUid;
    }

    public boolean isLoggingIn() {
        if (loginUid != null) return true;
        return false;
    }

    protected void setRequestPath(String path) {
        requestUrlPath = path;
        setRequestUrl(BuildConfig.SERVICE_API_URI + path);
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

    public void setRespCallback(AsyncOkHttpClient.ResponseCallback cb) {
        respCallback = cb;
    }

    private Headers getHeaders() {
        Map<String, String> headerMap = new LinkedHashMap<>();
        if (requestUrlPath.startsWith(URL_PATH_SUBSCRIPTION)) {
            headerMap.put("shared-secret", SECRET_FOR_SUBSCRIPTION_CHECK);
            return Headers.of(headerMap);
        }

        if (loginUid == null) return null;
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

    protected void requestBulkApi() {
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
                method, requestUrl, formBody, respCallback);
    }

    public void setIsSyncing(boolean value) {
        SharedPreferencesManager.saveBoolean(context, KEY_IS_SYNCING_DATA, value);
    }

    public boolean isSyncing() {
        return SharedPreferencesManager.getBoolean(context, KEY_IS_SYNCING_DATA);
    }

    /**
     * ネットワークアクセス可能かどうか確認する
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null) {
            return cm.getActiveNetworkInfo().isConnected();
        }
        return false;
    }

    public boolean isCloudActive() {
        if (BuildConfig.IS_DEBUG_CLOUD) {
            return true;
        }

        if (ZNUtils.isZeny()) return UpgradeManger.zenyPremiumIsActive(context);

        return UpgradeManger.taxnoteCloudIsActive(context);
    }
}
