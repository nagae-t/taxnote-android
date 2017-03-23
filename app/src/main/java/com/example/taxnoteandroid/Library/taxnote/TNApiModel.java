package com.example.taxnoteandroid.Library.taxnote;

import android.content.Context;
import android.util.Log;

import com.example.taxnoteandroid.Library.AsyncOkHttpClient;
import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.ReasonDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Map;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by b0ne on 2017/03/17.
 */

public class TNApiModel extends TNApi {

    private static final String KEY_SYNC_UPDATED_PROJECT = "KEY_SYNC_UPDATED_PROJECT";
    private static final String KEY_SYNC_UPDATED_ACCOUNT = "KEY_SYNC_UPDATED_ACCOUNT";
    private static final String KEY_SYNC_UPDATED_REASON = "KEY_SYNC_UPDATED_REASON";
    private static final String KEY_SYNC_UPDATED_ENTRY = "KEY_SYNC_UPDATED_ENTRY";
    private static final String KEY_SYNC_UPDATED_SUMMARY = "KEY_SYNC_UPDATED_SUMMARY";
    private static final String KEY_SYNC_UPDATED_RECURRING = "KEY_SYNC_UPDATED_RECURRING";


    public TNApiModel(Context context) {
        super(context);
    }

    //--------------------------------------------------------------//
    //    -- Sync updated time --
    //--------------------------------------------------------------//

    public void resetAllUpdatedKeys() {
        SharedPreferencesManager.saveSyncUpdatedAt(context, KEY_SYNC_UPDATED_PROJECT, 0);
        SharedPreferencesManager.saveSyncUpdatedAt(context, KEY_SYNC_UPDATED_ACCOUNT, 0);
        SharedPreferencesManager.saveSyncUpdatedAt(context, KEY_SYNC_UPDATED_REASON, 0);
        SharedPreferencesManager.saveSyncUpdatedAt(context, KEY_SYNC_UPDATED_ENTRY, 0);
        SharedPreferencesManager.saveSyncUpdatedAt(context, KEY_SYNC_UPDATED_SUMMARY, 0);
        SharedPreferencesManager.saveSyncUpdatedAt(context, KEY_SYNC_UPDATED_RECURRING, 0);
    }

    public void saveSyncUpdated(String key, long value) {
        SharedPreferencesManager.saveSyncUpdatedAt(context, key, value);
    }

    public long getSyncUpdated(String key) {
        return SharedPreferencesManager.getSyncUpdatedAt(context, key);
    }


    //--------------------------------------------------------------//
    //    -- Get Method --
    //--------------------------------------------------------------//

    public void getProjects(AsyncOkHttpClient.Callback callback) {
        setHttpMethod(HTTP_METHOD_GET);
        setRequestPath(URL_PATH_PROJECT);

        setRequestBody(getUpdatedAtParams(KEY_SYNC_UPDATED_PROJECT));
        setCallback(callback);
        requestApi();
    }

    public void getReasons(AsyncOkHttpClient.Callback callback) {
        setHttpMethod(HTTP_METHOD_GET);
        setRequestPath(URL_PATH_REASON);

        setRequestBody(getUpdatedAtParams(KEY_SYNC_UPDATED_REASON));
        setCallback(callback);
        requestApi();
    }

    public void getAccounts(AsyncOkHttpClient.Callback callback) {
        setHttpMethod(HTTP_METHOD_GET);
        setRequestPath(URL_PATH_ACCOUNT);

        setRequestBody(getUpdatedAtParams(KEY_SYNC_UPDATED_ACCOUNT));
        setCallback(callback);
        requestApi();
    }

    public void getSummaries(AsyncOkHttpClient.Callback callback) {
        setHttpMethod(HTTP_METHOD_GET);
        setRequestPath(URL_PATH_SUMMARY);

        setRequestBody(getUpdatedAtParams(KEY_SYNC_UPDATED_SUMMARY));
        setCallback(callback);
        requestApi();
    }

    public void getRecurrings(AsyncOkHttpClient.Callback callback) {
        setHttpMethod(HTTP_METHOD_GET);
        setRequestPath(URL_PATH_RECURRING);

        setRequestBody(getUpdatedAtParams(KEY_SYNC_UPDATED_RECURRING));
        setCallback(callback);
        requestApi();
    }

    public void getEntries(AsyncOkHttpClient.Callback callback) {
        setHttpMethod(HTTP_METHOD_GET);
        setRequestPath(URL_PATH_ENTRY);

        setRequestBody(getUpdatedAtParams(KEY_SYNC_UPDATED_ENTRY));
        setCallback(callback);
        requestApi();
    }

    private RequestBody getUpdatedAtParams(String key) {
        long updated = getSyncUpdated(key);
        if (updated == 0) return null;

        return new FormBody.Builder()
                .add("updated_at", String.valueOf(updated))
                .build();
    }

    private void showLogOnSuccess(String content) {
        JsonParser parser = new JsonParser();
        JsonArray jsArr = parser.parse(content).getAsJsonArray();
        jsArr.iterator();
        for (JsonElement jsElement : jsArr) {
            JsonObject obj = jsElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                Log.v("TEST", "key: " + entry.getKey() + " | " + entry.getValue());
            }
            Log.v("TEST", "........................");
        }
    }
    public void getAllData(final AsyncOkHttpClient.Callback callback) {
        if (isSyncing()) return;

        setIsSyncing(true);

        getProjects(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                setIsSyncing(false);
                callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {

                Log.v("TEST", "onSuccess----Projects :");
                showLogOnSuccess(content);

                getReasons(new AsyncOkHttpClient.Callback() {
                    @Override
                    public void onFailure(Response response, Throwable throwable) {
                        setIsSyncing(false);
                        callback.onFailure(response, throwable);
                    }

                    @Override
                    public void onSuccess(Response response, String content) {
                        Log.v("TEST", "onSuccess----Reasons :");
                        showLogOnSuccess(content);

                        getAccounts(new AsyncOkHttpClient.Callback() {
                            @Override
                            public void onFailure(Response response, Throwable throwable) {
                                setIsSyncing(false);
                                callback.onFailure(response, throwable);
                            }

                            @Override
                            public void onSuccess(Response response, String content) {
                                Log.v("TEST", "onSuccess----Accounts :");
                                showLogOnSuccess(content);

                                getSummaries(new AsyncOkHttpClient.Callback() {
                                    @Override
                                    public void onFailure(Response response, Throwable throwable) {
                                        setIsSyncing(false);
                                        callback.onFailure(response, throwable);
                                    }

                                    @Override
                                    public void onSuccess(Response response, String content) {
                                        Log.v("TEST", "onSuccess----Summaries :");
                                        showLogOnSuccess(content);

                                        getRecurrings(new AsyncOkHttpClient.Callback() {
                                            @Override
                                            public void onFailure(Response response, Throwable throwable) {
                                                setIsSyncing(false);
                                                callback.onFailure(response, throwable);
                                            }

                                            @Override
                                            public void onSuccess(Response response, String content) {
                                                Log.v("TEST", "onSuccess----Recurrings :");
                                                showLogOnSuccess(content);

                                                getEntries(new AsyncOkHttpClient.Callback() {
                                                    @Override
                                                    public void onFailure(Response response, Throwable throwable) {
                                                        setIsSyncing(false);
                                                        callback.onFailure(response, throwable);
                                                    }

                                                    @Override
                                                    public void onSuccess(Response response, String content) {
                                                        Log.v("TEST", "onSuccess----Entries :");
                                                        showLogOnSuccess(content);

                                                        setIsSyncing(false);
                                                        callback.onSuccess(response, content);
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
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

    private void updateProjects(JsonArray array) {
        ProjectDataManager projectDm = new ProjectDataManager(context);
        for (JsonElement jsElement : array) {
            JsonObject obj = jsElement.getAsJsonObject();

            boolean isNewProject = false;
            boolean deleted = obj.get("deleted").getAsBoolean();
            String uuid = obj.get("uuid").getAsString();
            Project project = projectDm.findByUuid(uuid);

            // Delete it if deleted is YES
            if (deleted) {
                if (project != null) {
                    projectDm.delete(project.id);
                }

            } else { // Update
                if (project == null) {
                    isNewProject = true;
                    project = new Project();
                    project.needSave = false;
                }

                project.uuid = uuid;
                project.name = obj.get("name").getAsString();
                project.order = obj.get("order").getAsLong();
                project.isMaster = obj.get("master").getAsBoolean();
                project.decimal = obj.get("decimal").getAsBoolean();
                project.accountUuidForExpense = obj.get("account_for_expense").getAsString();
                project.accountUuidForIncome = obj.get("account_for_income").getAsString();

                if (project.isMaster) {
                    // Set master as current project
                    SharedPreferencesManager.saveUuidForCurrentProject(context, uuid);

                    // Save in keychain when premium expires date updated
                    //@@ 購読（課金）の有効期限の保存処理?
                    String expiresDateString = obj.get("subscription_expires").getAsString();
                    String subscriptionType = obj.get("subscription_type").getAsString();
                }

            }
            if (isNewProject) {
                projectDm.save(project);
            } else {
                projectDm.update(project);
            }

            // save sync updated
            long nowTime = System.currentTimeMillis() + 1000; // 1秒足すようだ
            saveSyncUpdated(KEY_SYNC_UPDATED_PROJECT, nowTime);
        }
    }

    private void updateReasons(JsonArray array) {
        ReasonDataManager reasonDm = new ReasonDataManager(context);
        ProjectDataManager projectDm = new ProjectDataManager(context);
        for (JsonElement jsElement : array) {
            JsonObject obj = jsElement.getAsJsonObject();

            boolean isNewReason = false;
            boolean deleted = obj.get("deleted").getAsBoolean();
            String uuid = obj.get("uuid").getAsString();
            Reason reason = reasonDm.findByUuid(uuid);

            // Delete it if deleted is YES
            if (deleted) {
                if (reason != null) reasonDm.delete(reason.id);
            } else { // Update
                if (reason == null) {
                    isNewReason = true;
                    reason = new Reason();
                    reason.needSave = false;
                }

                reason.uuid = uuid;
                reason.name = obj.get("name").getAsString();
                reason.order = obj.get("order").getAsLong();
                reason.isExpense = obj.get("is_expense").getAsBoolean();
                reason.details = obj.get("details").getAsString();
                reason.project = projectDm.findByUuid(obj.get("project_uuid").getAsString());
            }
            if (isNewReason) {
                reasonDm.save(reason);
            } else {
                reasonDm.update(reason);
            }

            // save sync updated
            long nowTime = System.currentTimeMillis() + 1000; // 1秒足すようだ
            saveSyncUpdated(KEY_SYNC_UPDATED_REASON, nowTime);
        }
    }

    private void updateAccounts(JsonArray array) {
        for (JsonElement jsElement : array) {
            JsonObject obj = jsElement.getAsJsonObject();
        }
    }

    private void updateSummaries(JsonArray array) {
        for (JsonElement jsElement : array) {
            JsonObject obj = jsElement.getAsJsonObject();
        }
    }

    private void updateRecurrings(JsonArray array) {

        for (JsonElement jsElement : array) {
            JsonObject obj = jsElement.getAsJsonObject();
        }
    }

    private void updateEntries(JsonArray array) {

        for (JsonElement jsElement : array) {
            JsonObject obj = jsElement.getAsJsonObject();
        }
    }


    //--------------------------------------------------------------//
    //    -- Delete Method --
    //--------------------------------------------------------------//
}
