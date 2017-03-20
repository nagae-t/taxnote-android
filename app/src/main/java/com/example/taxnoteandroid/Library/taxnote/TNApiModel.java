package com.example.taxnoteandroid.Library.taxnote;

import android.content.Context;

import com.example.taxnoteandroid.Library.AsyncOkHttpClient;

/**
 * Created by b0ne on 2017/03/17.
 */

public class TNApiModel extends TNApi {

    public TNApiModel(Context context) {
        this.context = context;
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

    public void getReasons() {
    }

    public void getAccounts() {
    }

    public void getSummaries() {
    }

    public void getRecurrings() {
    }

    public void getEntries() {
    }

    //--------------------------------------------------------------//
    //    -- Save Method --
    //--------------------------------------------------------------//


    //--------------------------------------------------------------//
    //    -- Update Method --
    //--------------------------------------------------------------//


    //--------------------------------------------------------------//
    //    -- Delete Method --
    //--------------------------------------------------------------//
}
