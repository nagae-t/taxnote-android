package com.example.taxnoteandroid.Library.taxnote;

import android.content.Context;
import android.util.Log;

import com.example.taxnoteandroid.Library.AsyncOkHttpClient;
import com.example.taxnoteandroid.Library.ValueConverter;
import com.example.taxnoteandroid.dataManager.AccountDataManager;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.ReasonDataManager;
import com.example.taxnoteandroid.dataManager.RecurringDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.dataManager.SummaryDataManager;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Entry;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason;
import com.example.taxnoteandroid.model.Recurring;
import com.example.taxnoteandroid.model.Summary;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.FormBody;
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

    private JsonParser jsParser;

    public TNApiModel(Context context) {
        super(context);
        this.jsParser = new JsonParser();
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

    public void testGetProjects(AsyncOkHttpClient.Callback callback) {
        setHttpMethod(HTTP_METHOD_GET);
        setRequestPath(URL_PATH_PROJECT);

        setFormBody(getUpdatedAtParams(KEY_SYNC_UPDATED_PROJECT));
        setCallback(callback);

        requestApi();
    }

    private void getProjects(final AsyncOkHttpClient.Callback callback) {
        setHttpMethod(HTTP_METHOD_GET);
        setRequestPath(URL_PATH_PROJECT);

        setFormBody(getUpdatedAtParams(KEY_SYNC_UPDATED_PROJECT));
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {

                JsonArray jsArray = jsParser.parse(content).getAsJsonArray();
                updateProjects(jsArray);
                callback.onSuccess(response, content);
            }
        });

        requestApi();
    }

    private void getReasons(final AsyncOkHttpClient.Callback callback) {
        setHttpMethod(HTTP_METHOD_GET);
        setRequestPath(URL_PATH_REASON);

        setFormBody(getUpdatedAtParams(KEY_SYNC_UPDATED_REASON));
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {

                JsonArray jsArray = jsParser.parse(content).getAsJsonArray();
                updateReasons(jsArray);
                callback.onSuccess(response, content);
            }
        });
        requestApi();
    }

    private void getAccounts(final AsyncOkHttpClient.Callback callback) {
        setHttpMethod(HTTP_METHOD_GET);
        setRequestPath(URL_PATH_ACCOUNT);

        setFormBody(getUpdatedAtParams(KEY_SYNC_UPDATED_ACCOUNT));
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {

                JsonArray jsArray = jsParser.parse(content).getAsJsonArray();
                updateAccounts(jsArray);
                callback.onSuccess(response, content);
            }
        });
        requestApi();
    }

    private void getSummaries(final AsyncOkHttpClient.Callback callback) {
        setHttpMethod(HTTP_METHOD_GET);
        setRequestPath(URL_PATH_SUMMARY);

        setFormBody(getUpdatedAtParams(KEY_SYNC_UPDATED_SUMMARY));
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {

                JsonArray jsArray = jsParser.parse(content).getAsJsonArray();
                updateSummaries(jsArray);
                callback.onSuccess(response, content);
            }
        });
        requestApi();
    }

    private void getRecurrings(final AsyncOkHttpClient.Callback callback) {
        setHttpMethod(HTTP_METHOD_GET);
        setRequestPath(URL_PATH_RECURRING);

        setFormBody(getUpdatedAtParams(KEY_SYNC_UPDATED_RECURRING));
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {

                JsonArray jsArray = jsParser.parse(content).getAsJsonArray();
                updateRecurrings(jsArray);
                callback.onSuccess(response, content);
            }
        });
        requestApi();
    }

    private void getEntries(final AsyncOkHttpClient.Callback callback) {
        setHttpMethod(HTTP_METHOD_GET);
        setRequestPath(URL_PATH_ENTRY);

        setFormBody(getUpdatedAtParams(KEY_SYNC_UPDATED_ENTRY));
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {

                JsonArray jsArray = jsParser.parse(content).getAsJsonArray();
                updateEntries(jsArray);
                callback.onSuccess(response, content);
            }
        });
        requestApi();
    }

    private FormBody getUpdatedAtParams(String key) {
        long updated = getSyncUpdated(key);
        if (updated == 0) return null;

        FormBody form = new FormBody.Builder()
                .add("updated_at", String.valueOf(updated))
                .build();

        return form;
    }

    private void showLogOnSuccess(String content) {
        /*
        JsonParser parser = new JsonParser();
        JsonArray jsArr = parser.parse(content).getAsJsonArray();
        for (JsonElement jsElement : jsArr) {
            JsonObject obj = jsElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                Log.v("TEST", "key: " + entry.getKey() + " | " + entry.getValue());
            }
            Log.v("TEST", "........................");
        }*/
    }
    public void getAllDataAfterLogin(final AsyncOkHttpClient.Callback callback) {
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

//        setFormBody();
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

            // Delete it if deleted is true
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

                if (isNewProject) {
                    projectDm.save(project);
                } else {
                    projectDm.update(project);
                }
            }

        }
        // save sync updated
        long nowTime = System.currentTimeMillis() + 1000; // 1秒足すようだ
        saveSyncUpdated(KEY_SYNC_UPDATED_PROJECT, nowTime);
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

            // Delete it if deleted is true
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

                if (isNewReason) {
                    reasonDm.save(reason);
                } else {
                    reasonDm.update(reason);
                }
            }

        }
        // save sync updated
        long nowTime = System.currentTimeMillis() + 1000; // 1秒足すようだ
        saveSyncUpdated(KEY_SYNC_UPDATED_REASON, nowTime);
    }

    private void updateAccounts(JsonArray array) {
        AccountDataManager accountDm = new AccountDataManager(context);
        ProjectDataManager projectDm = new ProjectDataManager(context);
        for (JsonElement jsElement : array) {
            JsonObject obj = jsElement.getAsJsonObject();

            boolean isNewAcccount = false;
            boolean deleted = obj.get("deleted").getAsBoolean();
            String uuid = obj.get("uuid").getAsString();
            Account account = accountDm.findByUuid(uuid);

            // Delete id if deleted is true
            if (deleted) {
                if (account != null) accountDm.delete(account.id);

            } else { // Update

                if (account == null) {
                    isNewAcccount = true;
                    account = new Account();
                    account.needSave = false;
                }

                account.uuid = uuid;
                account.name = obj.get("name").getAsString();
                account.order = obj.get("order").getAsLong();
                account.isExpense = obj.get("is_expense").getAsBoolean();
                account.project = projectDm.findByUuid(obj.get("project_uuid").getAsString());

                if (isNewAcccount) {
                    accountDm.save(account);
                } else {
                    accountDm.update(account);
                }
            }


        }
        // save sync updated
        long nowTime = System.currentTimeMillis() + 1000; // 1秒足すようだ
        saveSyncUpdated(KEY_SYNC_UPDATED_ACCOUNT, nowTime);
    }

    private void updateSummaries(JsonArray array) {
        SummaryDataManager summaryDm = new SummaryDataManager(context);
        ReasonDataManager reasonDm = new ReasonDataManager(context);
        ProjectDataManager projectDm = new ProjectDataManager(context);
        for (JsonElement jsElement : array) {
            JsonObject obj = jsElement.getAsJsonObject();

            boolean isNewSummary = false;
            boolean deleted = obj.get("deleted").getAsBoolean();
            String uuid = obj.get("uuid").getAsString();
            Summary summary = summaryDm.findByUuid(uuid);

            if (deleted) {
                if (summary != null) summaryDm.delete(summary.id);
            } else { // Update

                if (summary == null) {
                    isNewSummary = true;
                    summary = new Summary();
                    summary.needSave = false;
                }

                summary.uuid = uuid;
                summary.name = obj.get("name").getAsString();
                summary.order = obj.get("order").getAsLong();
                summary.project = projectDm.findByUuid(obj.get("project_uuid").getAsString());
                summary.reason = reasonDm.findByUuid(obj.get("reason_uuid").getAsString());

                if (isNewSummary) {
                    summaryDm.save(summary);
                } else {
                    summaryDm.update(summary);
                }
            }
        }
        // save sync updated
        long nowTime = System.currentTimeMillis() + 1000; // 1秒足すようだ
        saveSyncUpdated(KEY_SYNC_UPDATED_SUMMARY, nowTime);
    }

    private void updateRecurrings(JsonArray array) {
        RecurringDataManager recDm = new RecurringDataManager(context);
        ReasonDataManager reasonDm = new ReasonDataManager(context);
        AccountDataManager accountDm = new AccountDataManager(context);
        ProjectDataManager projectDm = new ProjectDataManager(context);

        for (JsonElement jsElement : array) {
            JsonObject obj = jsElement.getAsJsonObject();

            boolean isNewRec = false;
            boolean deleted = obj.get("deleted").getAsBoolean();
            String uuid = obj.get("uuid").getAsString();
            Recurring recurring = recDm.findByUuid(uuid);

            if (deleted) {
                if (recurring != null) recDm.delete(recurring.id);

            } else { // Update

                if (recurring == null) {
                    isNewRec = true;
                    recurring = new Recurring();
                    recurring.needSave = false;
                }

                recurring.uuid = uuid;
                recurring.dateIndex = obj.get("date").getAsLong();
                recurring.timezone = obj.get("timezone").getAsString();
                recurring.memo = obj.get("memo").getAsString();
                recurring.price = obj.get("price").getAsLong();
                recurring.isExpense = obj.get("is_expense").getAsBoolean();
                recurring.order = obj.get("order").getAsLong();
                recurring.reason = reasonDm.findByUuid(obj.get("reason_uuid").getAsString());
                recurring.account = accountDm.findByUuid(obj.get("account_uuid").getAsString());
                recurring.project = projectDm.findByUuid(obj.get("project_uuid").getAsString());

                if (isNewRec) {
                    recDm.save(recurring);
                } else {
                    recDm.update(recurring);
                }
            }
        }

        // save sync updated
        long nowTime = System.currentTimeMillis() + 1000; // 1秒足すようだ
        saveSyncUpdated(KEY_SYNC_UPDATED_RECURRING, nowTime);
    }

    private void updateEntries(JsonArray array) {
        EntryDataManager entryDm = new EntryDataManager(context);
        ReasonDataManager reasonDm = new ReasonDataManager(context);
        AccountDataManager accountDm = new AccountDataManager(context);
        ProjectDataManager projectDm = new ProjectDataManager(context);

        for (JsonElement jsElement : array) {
            JsonObject obj = jsElement.getAsJsonObject();

            boolean isNewEntry = false;
            boolean deleted = obj.get("deleted").getAsBoolean();
            String uuid = obj.get("uuid").getAsString();
            Entry entry = entryDm.findByUuid(uuid);

            if (deleted) {
                if (entry != null) entryDm.delete(entry.id);

            } else { // Update

                if (entry == null) {
                    isNewEntry = true;
                    entry = new Entry();
                    entry.needSave = false;
                }

                entry.uuid = uuid;
                entry.date = ValueConverter.dateString2long(obj.get("date").getAsString());
                entry.updated = ValueConverter.dateString2long(obj.get("updated_mobile").getAsString());
                entry.memo = obj.get("memo").getAsString();
                entry.price = obj.get("price").getAsLong();
                entry.isExpense = obj.get("is_expense").getAsBoolean();
                entry.project = projectDm.findByUuid(obj.get("project_uuid").getAsString());
                entry.reason = reasonDm.findByUuid(obj.get("reason_uuid").getAsString());
                entry.account = accountDm.findByUuid(obj.get("account_uuid").getAsString());

                if (isNewEntry) {
                    entryDm.save(entry);
                } else {
                    entryDm.update(entry);
                }
            }


        }

        // save sync updated
        long nowTime = System.currentTimeMillis() + 1000; // 1秒足すようだ
        saveSyncUpdated(KEY_SYNC_UPDATED_ENTRY, nowTime);
    }


    //--------------------------------------------------------------//
    //    -- Delete Method --
    //--------------------------------------------------------------//
}
