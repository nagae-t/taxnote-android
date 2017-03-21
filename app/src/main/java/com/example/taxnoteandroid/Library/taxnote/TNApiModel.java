package com.example.taxnoteandroid.Library.taxnote;

import android.content.Context;

import com.example.taxnoteandroid.Library.AsyncOkHttpClient;

/**
 * Created by b0ne on 2017/03/17.
 */

public class TNApiModel extends TNApi {

    public TNApiModel(Context context) {
        super(context);
    }

    //--------------------------------------------------------------//
    //    -- Get Method --
    //--------------------------------------------------------------//

    public void getProjects(AsyncOkHttpClient.Callback callback) {
        setHttpMethod(HTTP_METHOD_GET);
        setRequestPath(URL_PATH_PROJECT);

//        RequestBody requestBody = new FormBody.Builder()
//                .add("paramkey", "")
//                .build();
//        setRequestBody(requestBody);
        setCallback(callback);
        requestApi();
    }

    public void getReasons(AsyncOkHttpClient.Callback callback) {
        setHttpMethod(HTTP_METHOD_GET);
        setRequestPath(URL_PATH_REASON);
        setCallback(callback);
        requestApi();
    }

    public void getAccounts(AsyncOkHttpClient.Callback callback) {
        setHttpMethod(HTTP_METHOD_GET);
        setRequestPath(URL_PATH_ACCOUNT);
        setCallback(callback);
        requestApi();
    }

    public void getSummaries(AsyncOkHttpClient.Callback callback) {
        setHttpMethod(HTTP_METHOD_GET);
        setRequestPath(URL_PATH_SUMMARY);
        setCallback(callback);
        requestApi();
    }

    public void getRecurrings(AsyncOkHttpClient.Callback callback) {
        setHttpMethod(HTTP_METHOD_GET);
        setRequestPath(URL_PATH_RECURRING);
        setCallback(callback);
        requestApi();
    }

    public void getEntries(AsyncOkHttpClient.Callback callback) {
        setHttpMethod(HTTP_METHOD_GET);
        setRequestPath(URL_PATH_ENTRY);
        setCallback(callback);
        requestApi();
    }

    //--------------------------------------------------------------//
    //    -- Save Method --
    //--------------------------------------------------------------//

    public void saveProject(String uuid, AsyncOkHttpClient.Callback callback) {
        setHttpMethod(HTTP_METHOD_POST);
        setRequestPath(URL_PATH_PROJECT);

//        setRequestBody();
        setCallback(callback);
        requestApi();
    }

    public void saveReason(String uuid, AsyncOkHttpClient.Callback callback) {
    }

    public void saveAccount(String uuid, AsyncOkHttpClient.Callback callback) {
    }

    public void saveSummary(String uuid, AsyncOkHttpClient.Callback callback) {
    }

    public void saveRecurring(String uuid, AsyncOkHttpClient.Callback callback) {
    }

    public void saveEntry(String uuid, AsyncOkHttpClient.Callback callback) {
    }


    //--------------------------------------------------------------//
    //    -- Update Method --
    //--------------------------------------------------------------//


    //--------------------------------------------------------------//
    //    -- Delete Method --
    //--------------------------------------------------------------//
}
