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

import java.util.List;

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
    private int mCount = 0;

    private ProjectDataManager mProjectDataManager;
    private ReasonDataManager mReasonDataManager;
    private AccountDataManager mAccountDataManager;
    private SummaryDataManager mSummaryDataManager;
    private RecurringDataManager mRecurringDataManager;
    private EntryDataManager mEntryDataManager;

    public TNApiModel(Context context) {
        super(context);
        this.jsParser = new JsonParser();
        this.mProjectDataManager = new ProjectDataManager(context);
        this.mReasonDataManager = new ReasonDataManager(context);
        this.mAccountDataManager = new AccountDataManager(context);
        this.mSummaryDataManager = new SummaryDataManager(context);
        this.mRecurringDataManager = new RecurringDataManager(context);
        this.mEntryDataManager = new EntryDataManager(context);
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

    public void saveProject(String uuid, final AsyncOkHttpClient.Callback callback) {
        if (!isLoggingIn()) {
            callback.onSuccess(null, null);
            return;
        }

        Project project = mProjectDataManager.findByUuid(uuid);
        final long projectId = project.id;

        setHttpMethod(HTTP_METHOD_POST);
        setRequestPath(URL_PATH_PROJECT);

        // dummy
        String subsExpires = "2017-05-01 23:23:21 Etc/GMT";
        String subsId = "xxxxxxxxxxxxxxxxxxxxx01";
        String subsType = "subs_type_xxxxx01";
        String subsReceipt = "subs_receipt_xxxxx01";

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("project[uuid]", project.uuid);
        formBuilder.add("project[name]", project.name);
        formBuilder.add("project[order]", String.valueOf(project.order));
        formBuilder.add("project[master]", String.valueOf(project.isMaster));
        formBuilder.add("project[account_for_expense]", project.accountUuidForExpense);
        formBuilder.add("project[account_for_income]", project.accountUuidForIncome);
        formBuilder.add("project[subscription_expires]", subsExpires);
        formBuilder.add("project[subscription_transaction]", subsId);
        formBuilder.add("project[subscription_type]", subsType);
        formBuilder.add("project[appstore_receipt]", subsReceipt);

        setFormBody(formBuilder.build());
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                mProjectDataManager.updateNeedSave(projectId, false);
                callback.onSuccess(response, content);
            }
        });

        requestApi();
    }

    public void saveReason(String uuid, final AsyncOkHttpClient.Callback callback) {
    }

    public void saveAccount(String uuid, final AsyncOkHttpClient.Callback callback) {
    }

    public void saveSummary(String uuid, final AsyncOkHttpClient.Callback callback) {
    }

    public void saveRecurring(String uuid, final AsyncOkHttpClient.Callback callback) {
    }

    public void saveEntry(String uuid, final AsyncOkHttpClient.Callback callback) {
    }

    public void saveAllNeedSaveProjects(final AsyncOkHttpClient.Callback callback) {
        List<Project> projects = mProjectDataManager.findAllNeedSave(true);
        final int projectSize = projects.size();
        if (projectSize == 0) {
            callback.onSuccess(null, null);
        }

        mCount = 0;
        for (Project project : projects) {
            saveProject(project.uuid, new AsyncOkHttpClient.Callback() {
                @Override
                public void onFailure(Response response, Throwable throwable) {
                    callback.onFailure(response, throwable);
                }

                @Override
                public void onSuccess(Response response, String content) {
                    mCount++;
                    if (mCount >= projectSize)
                        callback.onSuccess(response, content);
                }
            });
        }
    }

    public void saveAllNeedSaveReasons(final AsyncOkHttpClient.Callback callback) {
        ReasonDataManager reasonDm = new ReasonDataManager(context);
        List<Reason> reasons = reasonDm.findAllNeedSave(true);
        final int reasonSize = reasons.size();
        if (reasonSize == 0) {
            callback.onSuccess(null, null);
        }

        mCount = 0;
        for (Reason reason : reasons) {
            saveReason(reason.uuid, new AsyncOkHttpClient.Callback() {
                @Override
                public void onFailure(Response response, Throwable throwable) {
                    callback.onFailure(response, throwable);
                }

                @Override
                public void onSuccess(Response response, String content) {

                }
            });
        }
    }

    public void saveAllNeedSaveAccounts(final AsyncOkHttpClient.Callback callback) {

    }

    public void saveAllNeedSaveSummaries(final AsyncOkHttpClient.Callback callback) {

    }

    public void saveAllNeedSaveRecurrings(final AsyncOkHttpClient.Callback callback) {

    }

    public void saveAllNeedSaveEntries(final AsyncOkHttpClient.Callback callback) {

    }

    public void saveAllDataAfterRegister() {

    }

    //--------------------------------------------------------------//
    //    -- Update Method --
    //--------------------------------------------------------------//

    private void updateProjects(JsonArray array) {
        for (JsonElement jsElement : array) {
            JsonObject obj = jsElement.getAsJsonObject();

            boolean isNewProject = false;
            boolean deleted = obj.get("deleted").getAsBoolean();
            String uuid = obj.get("uuid").getAsString();
            Project project = mProjectDataManager.findByUuid(uuid);

            // Delete it if deleted is true
            if (deleted) {
                if (project != null) {
                    mProjectDataManager.delete(project.id);
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
                    mProjectDataManager.save(project);
                } else {
                    mProjectDataManager.update(project);
                }
            }

        }
        // save sync updated
        long nowTime = System.currentTimeMillis() + 1000; // 1秒足すようだ
        saveSyncUpdated(KEY_SYNC_UPDATED_PROJECT, nowTime);
    }

    private void updateReasons(JsonArray array) {
        for (JsonElement jsElement : array) {
            JsonObject obj = jsElement.getAsJsonObject();

            boolean isNewReason = false;
            boolean deleted = obj.get("deleted").getAsBoolean();
            String uuid = obj.get("uuid").getAsString();
            Reason reason = mReasonDataManager.findByUuid(uuid);

            // Delete it if deleted is true
            if (deleted) {
                if (reason != null) mRecurringDataManager.delete(reason.id);
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
                reason.project = mProjectDataManager.findByUuid(obj.get("project_uuid").getAsString());

                if (isNewReason) {
                    mReasonDataManager.save(reason);
                } else {
                    mReasonDataManager.update(reason);
                }
            }

        }
        // save sync updated
        long nowTime = System.currentTimeMillis() + 1000; // 1秒足すようだ
        saveSyncUpdated(KEY_SYNC_UPDATED_REASON, nowTime);
    }

    private void updateAccounts(JsonArray array) {
        for (JsonElement jsElement : array) {
            JsonObject obj = jsElement.getAsJsonObject();

            boolean isNewAcccount = false;
            boolean deleted = obj.get("deleted").getAsBoolean();
            String uuid = obj.get("uuid").getAsString();
            Account account = mAccountDataManager.findByUuid(uuid);

            // Delete id if deleted is true
            if (deleted) {
                if (account != null) mAccountDataManager.delete(account.id);

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
                account.project = mProjectDataManager.findByUuid(obj.get("project_uuid").getAsString());

                if (isNewAcccount) {
                    mAccountDataManager.save(account);
                } else {
                    mAccountDataManager.update(account);
                }
            }


        }
        // save sync updated
        long nowTime = System.currentTimeMillis() + 1000; // 1秒足すようだ
        saveSyncUpdated(KEY_SYNC_UPDATED_ACCOUNT, nowTime);
    }

    private void updateSummaries(JsonArray array) {
        for (JsonElement jsElement : array) {
            JsonObject obj = jsElement.getAsJsonObject();

            boolean isNewSummary = false;
            boolean deleted = obj.get("deleted").getAsBoolean();
            String uuid = obj.get("uuid").getAsString();
            Summary summary = mSummaryDataManager.findByUuid(uuid);

            if (deleted) {
                if (summary != null) mSummaryDataManager.delete(summary.id);
            } else { // Update

                if (summary == null) {
                    isNewSummary = true;
                    summary = new Summary();
                    summary.needSave = false;
                }

                summary.uuid = uuid;
                summary.name = obj.get("name").getAsString();
                summary.order = obj.get("order").getAsLong();
                summary.project = mProjectDataManager.findByUuid(obj.get("project_uuid").getAsString());
                summary.reason = mReasonDataManager.findByUuid(obj.get("reason_uuid").getAsString());

                if (isNewSummary) {
                    mSummaryDataManager.save(summary);
                } else {
                    mSummaryDataManager.update(summary);
                }
            }
        }
        // save sync updated
        long nowTime = System.currentTimeMillis() + 1000; // 1秒足すようだ
        saveSyncUpdated(KEY_SYNC_UPDATED_SUMMARY, nowTime);
    }

    private void updateRecurrings(JsonArray array) {

        for (JsonElement jsElement : array) {
            JsonObject obj = jsElement.getAsJsonObject();

            boolean isNewRec = false;
            boolean deleted = obj.get("deleted").getAsBoolean();
            String uuid = obj.get("uuid").getAsString();
            Recurring recurring = mRecurringDataManager.findByUuid(uuid);

            if (deleted) {
                if (recurring != null) mRecurringDataManager.delete(recurring.id);

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
                recurring.reason = mReasonDataManager.findByUuid(obj.get("reason_uuid").getAsString());
                recurring.account = mAccountDataManager.findByUuid(obj.get("account_uuid").getAsString());
                recurring.project = mProjectDataManager.findByUuid(obj.get("project_uuid").getAsString());

                if (isNewRec) {
                    mRecurringDataManager.save(recurring);
                } else {
                    mRecurringDataManager.update(recurring);
                }
            }
        }

        // save sync updated
        long nowTime = System.currentTimeMillis() + 1000; // 1秒足すようだ
        saveSyncUpdated(KEY_SYNC_UPDATED_RECURRING, nowTime);
    }

    private void updateEntries(JsonArray array) {

        for (JsonElement jsElement : array) {
            JsonObject obj = jsElement.getAsJsonObject();

            boolean isNewEntry = false;
            boolean deleted = obj.get("deleted").getAsBoolean();
            String uuid = obj.get("uuid").getAsString();
            Entry entry = mEntryDataManager.findByUuid(uuid);

            if (deleted) {
                if (entry != null) mEntryDataManager.delete(entry.id);

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
                entry.project = mProjectDataManager.findByUuid(obj.get("project_uuid").getAsString());
                entry.reason = mReasonDataManager.findByUuid(obj.get("reason_uuid").getAsString());
                entry.account = mAccountDataManager.findByUuid(obj.get("account_uuid").getAsString());

                if (isNewEntry) {
                    mEntryDataManager.save(entry);
                } else {
                    mEntryDataManager.update(entry);
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
