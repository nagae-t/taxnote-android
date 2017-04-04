package com.example.taxnoteandroid.Library.taxnote;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.example.taxnoteandroid.Library.AsyncOkHttpClient;
import com.example.taxnoteandroid.Library.BroadcastUtil;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.ValueConverter;
import com.example.taxnoteandroid.R;
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

    private static final String LTAG = TNApiModel.class.getSimpleName();

    private static final String KEY_SYNC_UPDATED_PROJECT = "KEY_SYNC_UPDATED_PROJECT";
    private static final String KEY_SYNC_UPDATED_ACCOUNT = "KEY_SYNC_UPDATED_ACCOUNT";
    private static final String KEY_SYNC_UPDATED_REASON = "KEY_SYNC_UPDATED_REASON";
    private static final String KEY_SYNC_UPDATED_ENTRY = "KEY_SYNC_UPDATED_ENTRY";
    private static final String KEY_SYNC_UPDATED_SUMMARY = "KEY_SYNC_UPDATED_SUMMARY";
    private static final String KEY_SYNC_UPDATED_RECURRING = "KEY_SYNC_UPDATED_RECURRING";

    private JsonParser jsParser;
    private int mCount = 0;
    private boolean mEntrySaveAllAgain = false;

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
                executeUpdateDbTask(UpdateDbAsyncTask.TYPE_PROJECT,
                        jsArray, callback, response, content);
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
                executeUpdateDbTask(UpdateDbAsyncTask.TYPE_REASON,
                        jsArray, callback, response, content);
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
                executeUpdateDbTask(UpdateDbAsyncTask.TYPE_ACCOUNT,
                        jsArray, callback, response, content);
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
                executeUpdateDbTask(UpdateDbAsyncTask.TYPE_SUMMARY,
                        jsArray, callback, response, content);
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
                executeUpdateDbTask(UpdateDbAsyncTask.TYPE_RECURRING,
                        jsArray, callback, response, content);
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
                executeUpdateDbTask(UpdateDbAsyncTask.TYPE_ENTRY,
                        jsArray, callback, response, content);
            }
        });
        requestApi();
    }

    private FormBody getUpdatedAtParams(String key) {
        long updated = getSyncUpdated(key);
        if (updated == 0) return null;


        String timeStr = String.valueOf(updated);
        StringBuilder sb = new StringBuilder();
        sb.append(timeStr);
        sb.insert(timeStr.length()-3, ".");
        String updatedString = new String(sb);
        FormBody form = new FormBody.Builder()
                .add("updated_at", updatedString)
                .build();

        return form;
    }

    private void getAllData(final AsyncOkHttpClient.Callback callback) {
        getProjects(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e(LTAG, "getAllData onFailure --- Projects");
                callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {

                Log.v("TEST", "getAllData onSuccess----Projects :");

                getReasons(new AsyncOkHttpClient.Callback() {
                    @Override
                    public void onFailure(Response response, Throwable throwable) {
                        Log.e(LTAG, "getAllData onFailure --- Reasons");
                        callback.onFailure(response, throwable);
                    }

                    @Override
                    public void onSuccess(Response response, String content) {
                        Log.v("TEST", "getAllData onSuccess----Reasons :");

                        getAccounts(new AsyncOkHttpClient.Callback() {
                            @Override
                            public void onFailure(Response response, Throwable throwable) {
                                Log.e(LTAG, "getAllData onFailure --- Accounts");
                                callback.onFailure(response, throwable);
                            }

                            @Override
                            public void onSuccess(Response response, String content) {
                                Log.v("TEST", "getAllData onSuccess----Accounts :");

                                getSummaries(new AsyncOkHttpClient.Callback() {
                                    @Override
                                    public void onFailure(Response response, Throwable throwable) {
                                        Log.e(LTAG, "getAllData onFailure --- Summaries");
                                        callback.onFailure(response, throwable);
                                    }

                                    @Override
                                    public void onSuccess(Response response, String content) {
                                        Log.v("TEST", "getAllData onSuccess----Summaries :");

                                        getRecurrings(new AsyncOkHttpClient.Callback() {
                                            @Override
                                            public void onFailure(Response response, Throwable throwable) {
                                                Log.e(LTAG, "getAllData onFailure --- Recurrings");
                                                callback.onFailure(response, throwable);
                                            }

                                            @Override
                                            public void onSuccess(Response response, String content) {
                                                Log.v("TEST", "getAllData onSuccess----Recurrings :");

                                                getEntries(new AsyncOkHttpClient.Callback() {
                                                    @Override
                                                    public void onFailure(Response response, Throwable throwable) {
                                                        Log.e(LTAG, "getAllData onFailure --- Entries");
                                                        callback.onFailure(response, throwable);
                                                    }

                                                    @Override
                                                    public void onSuccess(Response response, String content) {
                                                        Log.v("TEST", "getAllData onSuccess----Entries :");

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

    public void getAllDataAfterLogin(final AsyncOkHttpClient.Callback callback) {
        resetAllUpdatedKeys();
        getAllData(callback);
    }


    //--------------------------------------------------------------//
    //    -- Save Method --
    //--------------------------------------------------------------//

    private void saveProject(String uuid, final AsyncOkHttpClient.Callback callback) {

        Project project = mProjectDataManager.findByUuid(uuid);
        final long projectId = project.id;

        // dummy
        String subsExpires = "2017-05-01 23:23:21 Etc/GMT";
        String subsId = "xxxxxxxxxxxxxxxxxxxxx01";
        String subsType = "subs_type_xxxxx01";
        String subsReceipt = "subs_receipt_xxxxx01";

        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("project[uuid]", project.uuid)
                .add("project[name]", project.name)
                .add("project[order]", String.valueOf(project.order))
                .add("project[master]", String.valueOf(project.isMaster))
                .add("project[decimal]", String.valueOf(project.decimal))
                .add("project[account_for_expense]", project.accountUuidForExpense)
                .add("project[account_for_income]", project.accountUuidForIncome)
                .add("project[subscription_expires]", subsExpires)
                .add("project[subscription_transaction]", subsId)
                .add("project[subscription_type]", subsType)
                .add("project[appstore_receipt]", subsReceipt);

        setHttpMethod(HTTP_METHOD_POST);
        setRequestPath(URL_PATH_PROJECT);

        setFormBody(formBuilder.build());
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e(LTAG, "saveProject(uuid) onFailure");
                if (response != null) {
                    Log.e(LTAG, "saveProject(uuid) onFailure response.code: " + response.code()
                            + ", message: " + response.message());
                }
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
        if (!isLoggingIn() || !TNApi.isNetworkConnected(context)) {
            if (callback != null)
                callback.onSuccess(null, null);
            return;
        }

        Reason reason = mReasonDataManager.findByUuid(uuid);
        final long reasonId = reason.id;

        String details = (reason.details != null) ? reason.details : "";

        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("reason[uuid]", reason.uuid)
                .add("reason[name]", reason.name)
                .add("reason[details]", details)
                .add("reason[order]", String.valueOf(reason.order))
                .add("reason[is_expense]", String.valueOf(reason.isExpense))
                .add("reason[project_uuid]", reason.project.uuid);

        setHttpMethod(HTTP_METHOD_POST);
        setRequestPath(URL_PATH_REASON);

        setFormBody(formBuilder.build());
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e(LTAG, "saveReason(uuid) onFailure");
                if (response != null) {
                    Log.e(LTAG, "saveReason(uuid) onFailure response.code: " + response.code()
                            + ", message: " + response.message());
                }
                if (callback != null)
                    callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                mReasonDataManager.updateNeedSave(reasonId, false);
                if (callback != null)
                    callback.onSuccess(response, content);
            }
        });
        requestApi();
    }

    public void saveAccount(String uuid, final AsyncOkHttpClient.Callback callback) {
        if (!isLoggingIn() || !TNApi.isNetworkConnected(context)) {
            if (callback != null)
                callback.onSuccess(null, null);
            return;
        }

        Account account = mAccountDataManager.findByUuid(uuid);
        final long accountId = account.id;

        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("account[uuid]", account.uuid)
                .add("account[name]", account.name)
                .add("account[order]", String.valueOf(account.order))
                .add("account[is_expense]", String.valueOf(account.isExpense))
                .add("account[project_uuid]", account.project.uuid);

        setHttpMethod(HTTP_METHOD_POST);
        setRequestPath(URL_PATH_ACCOUNT);

        setFormBody(formBuilder.build());
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e(LTAG, "saveAccount(uuid) onFailure");
                if (response != null) {
                    Log.e(LTAG, "saveAccount(uuid) onFailure response.code: " + response.code()
                            + ", message: " + response.message());
                }
                if (callback != null)
                    callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                mAccountDataManager.updateNeedSave(accountId, false);
                if (callback != null)
                    callback.onSuccess(response, content);
            }
        });
        requestApi();
    }

    public void saveSummary(String uuid, final AsyncOkHttpClient.Callback callback) {
        if (!isLoggingIn() || !TNApi.isNetworkConnected(context)) {
            if (callback != null)
                callback.onSuccess(null, null);
            return;
        }

        Summary summary = mSummaryDataManager.findByUuid(uuid);
        final long summaryId = summary.id;

        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("summary[uuid]", summary.uuid)
                .add("summary[name]", summary.name)
                .add("summary[order]", String.valueOf(summary.order))
                .add("summary[reason_uuid]", summary.reason.uuid)
                .add("summary[project_uuid]", summary.project.uuid);

        setHttpMethod(HTTP_METHOD_POST);
        setRequestPath(URL_PATH_SUMMARY);

        setFormBody(formBuilder.build());
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e(LTAG, "saveSummary(uuid) onFailure");
                if (response != null) {
                    Log.e(LTAG, "saveSummary(uuid) onFailure response.code: " + response.code()
                            + ", message: " + response.message());
                }
                if (callback != null)
                    callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                mSummaryDataManager.updateNeedSave(summaryId, false);
                if (callback != null)
                    callback.onSuccess(response, content);
            }
        });
        requestApi();
    }

    private void saveRecurring(String uuid, final AsyncOkHttpClient.Callback callback) {
        Recurring recurring = mRecurringDataManager.findByUuid(uuid);
        final long recurringId = recurring.id;

        String memo = (recurring.memo != null) ? recurring.memo : "";

        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("recurring[uuid]", recurring.uuid)
                .add("recurring[date]", String.valueOf(recurring.dateIndex))
                .add("recurring[timezone]", recurring.timezone)
                .add("recurring[memo]", memo)
                .add("recurring[price]", String.valueOf(recurring.price))
                .add("recurring[is_expense]", String.valueOf(recurring.isExpense))
                .add("recurring[order]", String.valueOf(recurring.order))
                .add("recurring[reason_uuid]", recurring.reason.uuid)
                .add("recurring[account_uuid]", recurring.account.uuid)
                .add("recurring[project_uuid]", recurring.project.uuid);

        setHttpMethod(HTTP_METHOD_POST);
        setRequestPath(URL_PATH_RECURRING);

        setFormBody(formBuilder.build());
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e(LTAG, "saveRecurring(uuid) onFailure");
                if (response != null) {
                    Log.e(LTAG, "saveRecurring(uuid) onFailure response.code: " + response.code()
                            + ", message: " + response.message());
                }
                callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                mRecurringDataManager.updateNeedSave(recurringId, false);
                callback.onSuccess(response, content);
            }
        });
        requestApi();
    }

    public void saveEntry(String uuid, final AsyncOkHttpClient.Callback callback) {
        if (!isLoggingIn() || !TNApi.isNetworkConnected(context)) {
            if (callback != null)
                callback.onSuccess(null, null);
            return;
        }

        Entry entry = mEntryDataManager.findByUuid(uuid);
        final long entryId = entry.id;

        String memo = (entry.memo != null) ? entry.memo : "";

        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("entry[uuid]", entry.uuid)
                .add("entry[date]", ValueConverter.long2dateString(entry.date))
                .add("entry[memo]", memo)
                .add("entry[price]", String.valueOf(entry.price))
                .add("entry[is_expense]", String.valueOf(entry.isExpense))
                .add("entry[updated_mobile]", ValueConverter.long2dateString(entry.updated))
                .add("entry[reason_uuid]", entry.reason.uuid)
                .add("entry[account_uuid]", entry.account.uuid)
                .add("entry[project_uuid]", entry.project.uuid);

        setHttpMethod(HTTP_METHOD_POST);
        setRequestPath(URL_PATH_ENTRY);

        setFormBody(formBuilder.build());
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e(LTAG, "saveEntry(uuid) onFailure");
                if (response != null) {
                    Log.e(LTAG, "saveEntry(uuid) onFailure response.code: " + response.code()
                            + ", message: " + response.message());
                }
                if (callback != null)
                    callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                mEntryDataManager.updateNeedSave(entryId, false);
                if (callback != null)
                    callback.onSuccess(response, content);
            }
        });
        requestApi();
    }

    private void saveAllNeedSaveProjects(final AsyncOkHttpClient.Callback callback) {
        List<Project> projects = mProjectDataManager.findAllNeedSave(true);
        final int projectSize = projects.size();
        if (projectSize == 0) {
            callback.onSuccess(null, null);
        }

        //@@ ログインしている、かつ 課金有効の場合のみ
        if (!isLoggingIn()) {
            callback.onSuccess(null, null);
            return;
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

    private void saveAllNeedSaveReasons(final AsyncOkHttpClient.Callback callback) {
        List<Reason> reasons = mReasonDataManager.findAllNeedSave(true);
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
                    mCount++;
                    if (mCount >= reasonSize)
                        callback.onSuccess(response, content);
                }
            });
        }
    }

    private void saveAllNeedSaveAccounts(final AsyncOkHttpClient.Callback callback) {
        List<Account> accounts = mAccountDataManager.findAllNeedSave(true);
        final int accountSize = accounts.size();
        if (accountSize == 0) {
            callback.onSuccess(null, null);
        }

        mCount = 0;
        for (Account account : accounts) {
            saveAccount(account.uuid, new AsyncOkHttpClient.Callback() {
                @Override
                public void onFailure(Response response, Throwable throwable) {
                    callback.onFailure(response, throwable);
                }

                @Override
                public void onSuccess(Response response, String content) {
                    mCount++;
                    if (mCount >= accountSize)
                        callback.onSuccess(response, content);
                }
            });
        }
    }

    private void saveAllNeedSaveSummaries(final AsyncOkHttpClient.Callback callback) {
        List<Summary> summaries = mSummaryDataManager.findAllNeedSave(true);
        final int summarySize = summaries.size();
        if (summarySize == 0) {
            callback.onSuccess(null, null);
        }

        mCount = 0;
        for (Summary summary : summaries) {
            saveSummary(summary.uuid, new AsyncOkHttpClient.Callback() {
                @Override
                public void onFailure(Response response, Throwable throwable) {
                    callback.onFailure(response, throwable);
                }

                @Override
                public void onSuccess(Response response, String content) {
                    mCount++;
                    if (mCount >= summarySize)
                        callback.onSuccess(response, content);
                }
            });
        }
    }

    private void saveAllNeedSaveRecurrings(final AsyncOkHttpClient.Callback callback) {
        List<Recurring> recurrings = mRecurringDataManager.findAllNeedSave(true);
        final int recurringSize = recurrings.size();
        if (recurringSize == 0) {
            callback.onSuccess(null, null);
        }

        mCount = 0;
        for (Recurring recurring : recurrings) {
            saveRecurring(recurring.uuid, new AsyncOkHttpClient.Callback() {
                @Override
                public void onFailure(Response response, Throwable throwable) {
                    callback.onFailure(response, throwable);
                }

                @Override
                public void onSuccess(Response response, String content) {
                    mCount++;
                    if (mCount >= recurringSize)
                        callback.onSuccess(response, content);
                }
            });
        }
    }

    private void saveAllNeedSaveEntries(final AsyncOkHttpClient.Callback callback) {
        int limitSendCount = 70;
        List<Entry> entries = mEntryDataManager.findAllNeedSave(true);
        if (entries.size() == 0) {
            callback.onSuccess(null, null);
        }

        mEntrySaveAllAgain = false;

        //@@ iOSでは100件ずつ繰り返してやるらしい
        // サーバー側の負荷を考慮してまずは50件ずつ
        if (entries.size() > limitSendCount) {
            entries = entries.subList(0, limitSendCount);
            Log.v("TEST", "SAVE ENTRIES set save again = true ");
            mEntrySaveAllAgain = true;
        }
        final int entrySize = entries.size();

        mCount = 0;
        for (final Entry entry : entries) {
            new Handler().postDelayed(
                new Runnable() {

                      @Override
                      public void run() {

                          saveEntry(entry.uuid, new AsyncOkHttpClient.Callback() {
                              @Override
                              public void onFailure(Response response, Throwable throwable) {
                                  callback.onFailure(response, throwable);
                              }

                              @Override
                              public void onSuccess(Response response, String content) {
                                  mCount++;
                                  if (mCount >= entrySize) {
                                      if (mEntrySaveAllAgain) {
                                          // call save entries again after delay 1.5 sec
                                          new Handler().postDelayed(new Runnable() {
                                              @Override
                                              public void run() {
                                                  Log.v("TEST", "SAVE ENTRIES runt save again ");
                                                  saveAllNeedSaveEntries(callback);
                                              }
                                          }, 1200);

                                      } else {
                                          Log.v("TEST", "SAVE ENTRIES finished ");
                                          callback.onSuccess(response, content);
                                      }
                                  }
                              }
                          });
                      }
                  }, 60);
        }
    }

    private void saveAllNeedSaveData(final AsyncOkHttpClient.Callback callback) {

        saveAllNeedSaveProjects(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e(LTAG, "saveAllNeedSaveData onFailure --- Projects");
                callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                Log.v("TEST", "saveAllNeedSaveData onSuccess --- Projects");

                saveAllNeedSaveReasons(new AsyncOkHttpClient.Callback() {
                    @Override
                    public void onFailure(Response response, Throwable throwable) {
                        Log.e(LTAG, "saveAllNeedSaveData onFailure --- Reasons");
                        callback.onFailure(response, throwable);
                    }

                    @Override
                    public void onSuccess(Response response, String content) {
                        Log.v("TEST", "saveAllNeedSaveData onSuccess --- Reasons");

                        saveAllNeedSaveAccounts(new AsyncOkHttpClient.Callback() {
                            @Override
                            public void onFailure(Response response, Throwable throwable) {
                                Log.e(LTAG, "saveAllNeedSaveData onFailure --- Accounts");
                                callback.onFailure(response, throwable);
                            }

                            @Override
                            public void onSuccess(Response response, String content) {
                                Log.v("TEST", "saveAllNeedSaveData onSuccess --- Accounts");

                                saveAllNeedSaveSummaries(new AsyncOkHttpClient.Callback() {
                                    @Override
                                    public void onFailure(Response response, Throwable throwable) {
                                        Log.e(LTAG, "saveAllNeedSaveData onFailure --- Summaries");
                                        callback.onFailure(response, throwable);
                                    }

                                    @Override
                                    public void onSuccess(Response response, String content) {
                                        Log.v("TEST", "saveAllNeedSaveData onSuccess --- Summaries");

                                        saveAllNeedSaveRecurrings(new AsyncOkHttpClient.Callback() {
                                            @Override
                                            public void onFailure(Response response, Throwable throwable) {
                                                Log.e(LTAG, "saveAllNeedSaveData onFailure --- Recurrings");
                                                callback.onFailure(response, throwable);
                                            }

                                            @Override
                                            public void onSuccess(Response response, String content) {
                                                Log.v("TEST", "saveAllNeedSaveData onSuccess --- Recurrings");

                                                saveAllNeedSaveEntries(callback);
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

    public void saveAllDataAfterRegister(AsyncOkHttpClient.Callback callback) {
        saveAllNeedSaveData(callback);
    }

    public void saveAllNeedSaveSyncDeletedData(final AsyncOkHttpClient.Callback callback) {
        //@@ check network

        // check login
        if (!isLoggingIn()) {
            if (callback != null)
                callback.onSuccess(null, null);
            return;
        }

        if (isSyncing()) {
            if (callback != null)
                callback.onSuccess(null, null);
            return;
        }

        setIsSyncing(true);

        saveAllNeedSaveData(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                setIsSyncing(false);
            }

            @Override
            public void onSuccess(Response response, String content) {

                updateAllNeedSyncData(new AsyncOkHttpClient.Callback() {
                    @Override
                    public void onFailure(Response response, Throwable throwable) {
                        setIsSyncing(false);
                        if (callback != null)
                            callback.onFailure(response, throwable);
                    }

                    @Override
                    public void onSuccess(Response response, String content) {
                        updateAllDeletedData(new AsyncOkHttpClient.Callback() {
                            @Override
                            public void onFailure(Response response, Throwable throwable) {
                                setIsSyncing(false);
                                if (callback != null)
                                    callback.onFailure(response, throwable);
                            }

                            @Override
                            public void onSuccess(Response response, String content) {
                                setIsSyncing(false);
                                if (callback != null)
                                    callback.onSuccess(response, content);
                            }
                        });
                    }
                });
            }
        });
    }

    //--------------------------------------------------------------//
    //    -- Update Method --
    //--------------------------------------------------------------//

    private void updateDbProjects(JsonArray array) {
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

    private void updateDbReasons(JsonArray array) {
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

    private void updateDbAccounts(JsonArray array) {
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

    private void updateDbSummaries(JsonArray array) {
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

    private void updateDbRecurrings(JsonArray array) {

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

    private void updateDbEntries(JsonArray array) {

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

    private void executeUpdateDbTask(int type, JsonArray jsonArray,
                                      AsyncOkHttpClient.Callback callback,
                                      Response response, String resContent) {

        new UpdateDbAsyncTask(type, callback, response, resContent)
                .execute(jsonArray);
    }
    private class UpdateDbAsyncTask extends AsyncTask<JsonArray, Void, Void> {
        private final int mType;
        private final AsyncOkHttpClient.Callback callback;
        private final Response mResponse;
        private final String mResContent;

        private static final int TYPE_PROJECT = 1;
        private static final int TYPE_REASON = 2;
        private static final int TYPE_ACCOUNT = 3;
        private static final int TYPE_SUMMARY = 4;
        private static final int TYPE_RECURRING = 5;
        private static final int TYPE_ENTRY = 6;


        private UpdateDbAsyncTask(int type, AsyncOkHttpClient.Callback callback,
                                  Response response, String resContent) {
            this.mType = type;
            this.callback = callback;
            this.mResponse = response;
            this.mResContent = resContent;
        }

        @Override
        protected Void doInBackground(JsonArray... jsonArrays) {
            JsonArray array = jsonArrays[0];
            switch (mType) {
                case TYPE_PROJECT:
                    updateDbProjects(array);
                    break;
                case TYPE_REASON:
                    updateDbReasons(array);
                    break;
                case TYPE_ACCOUNT:
                    updateDbAccounts(array);
                    break;
                case TYPE_SUMMARY:
                    updateDbSummaries(array);
                    break;
                case TYPE_RECURRING:
                    updateDbRecurrings(array);
                    break;
                case TYPE_ENTRY:
                    updateDbEntries(array);
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            callback.onSuccess(mResponse, mResContent);
        }
    }

    public void updateProject(String uuid, final AsyncOkHttpClient.Callback callback) {
        if (!isLoggingIn() || !TNApi.isNetworkConnected(context)) {
            if (callback != null)
                callback.onSuccess(null, null);
            return;
        }

        Project project = mProjectDataManager.findByUuid(uuid);
        final long projectId = project.id;

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder
                .add("project[name]", project.name)
                .add("project[order]", String.valueOf(project.order))
                .add("project[master]", String.valueOf(project.isMaster))
                .add("project[decimal]", String.valueOf(project.decimal))
                .add("project[account_for_expense]", project.accountUuidForExpense)
                .add("project[account_for_income]", project.accountUuidForIncome);
        if (project.isMaster) {
            // dummy
            String subsExpires = "2017-05-01 23:23:21 Etc/GMT";
            String subsId = "xxxxxxxxxxxxxxxxxxxxx01";
            String subsType = "subs_type_xxxxx01";
            String subsReceipt = "subs_receipt_xxxxx01";

            formBuilder
                    .add("project[subscription_expires]", subsExpires)
                    .add("project[subscription_transaction]", subsId)
                    .add("project[subscription_type]", subsType)
                    .add("project[appstore_receipt]", subsReceipt);
        }

        setHttpMethod(HTTP_METHOD_PUT);
        setRequestPath(URL_PATH_PROJECT + "/" + project.uuid);

        setFormBody(formBuilder.build());
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e(LTAG, "updateProject(uuid) onFailure");
                if (response != null) {
                    Log.e(LTAG, "updateProject(uuid) onFailure response.code: " + response.code()
                            + ", message: " + response.message());
                }
                if (callback != null)
                    callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                mProjectDataManager.updateNeedSync(projectId, false);
                if (callback != null)
                    callback.onSuccess(response, content);
            }
        });

        requestApi();
    }

    public void updateReason(String uuid, final AsyncOkHttpClient.Callback callback) {
        if (!isLoggingIn() || !TNApi.isNetworkConnected(context)) {
            if (callback != null)
                callback.onSuccess(null, null);
            return;
        }

        Reason reason = mReasonDataManager.findByUuid(uuid);
        final long reasonId = reason.id;

        String details = (reason.details != null) ? reason.details : "";

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder
                .add("reason[name]", reason.name)
                .add("reason[details]", details)
                .add("reason[order]", String.valueOf(reason.order))
                .add("reason[is_expense]", String.valueOf(reason.isExpense));

        setHttpMethod(HTTP_METHOD_PUT);
        setRequestPath(URL_PATH_REASON + "/" + reason.uuid);

        setFormBody(formBuilder.build());
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e(LTAG, "updateReason(uuid) onFailure");
                if (response != null) {
                    Log.e(LTAG, "updateReason(uuid) onFailure response.code: " + response.code()
                            + ", message: " + response.message());
                }
                if (callback != null)
                    callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                mReasonDataManager.updateNeedSync(reasonId, false);
                if (callback != null)
                    callback.onSuccess(response, content);
            }
        });

        requestApi();
    }

    public void updateAccount(String uuid, final AsyncOkHttpClient.Callback callback) {
        if (!isLoggingIn() || !TNApi.isNetworkConnected(context)) {
            if (callback != null)
                callback.onSuccess(null, null);
            return;
        }

        Account account = mAccountDataManager.findByUuid(uuid);
        final long accountId = account.id;

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder
                .add("account[name]", account.name)
                .add("account[order]", String.valueOf(account.order))
                .add("account[is_expense]", String.valueOf(account.isExpense));

        setHttpMethod(HTTP_METHOD_PUT);
        setRequestPath(URL_PATH_ACCOUNT + "/" + account.uuid);

        setFormBody(formBuilder.build());
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e(LTAG, "updateAccount(uuid) onFailure");
                if (response != null) {
                    Log.e(LTAG, "updateAccount(uuid) onFailure response.code: " + response.code()
                            + ", message: " + response.message());
                }
                if (callback != null)
                    callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                mAccountDataManager.updateNeedSync(accountId, false);
                if (callback != null)
                    callback.onSuccess(response, content);
            }
        });

        requestApi();
    }

    public void updateSummary(String uuid, final AsyncOkHttpClient.Callback callback) {
        if (!isLoggingIn() || !TNApi.isNetworkConnected(context)) {
            if (callback != null)
                callback.onSuccess(null, null);
            return;
        }

        Summary summary = mSummaryDataManager.findByUuid(uuid);
        final long summaryId = summary.id;


        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder
                .add("summary[name]", summary.name)
                .add("summary[order]", String.valueOf(summary.order));

        setHttpMethod(HTTP_METHOD_PUT);
        setRequestPath(URL_PATH_SUMMARY + "/" + summary.uuid);

        setFormBody(formBuilder.build());
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e(LTAG, "updateSummay(uuid) onFailure");
                if (response != null) {
                    Log.e(LTAG, "updateSummay(uuid) onFailure response.code: " + response.code()
                            + ", message: " + response.message());
                }
                if (callback != null)
                    callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                mSummaryDataManager.updateNeedSync(summaryId, false);
                if (callback != null)
                    callback.onSuccess(response, content);
            }
        });

        requestApi();
    }

    private void updateRecurring(String uuid, final AsyncOkHttpClient.Callback callback) {
        Recurring recurring = mRecurringDataManager.findByUuid(uuid);
        final long recurringId = recurring.id;

        String memo = (recurring.memo != null) ? recurring.memo : "";

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder
                .add("recurring[date]", String.valueOf(recurring.dateIndex))
                .add("recurring[timezone]", recurring.timezone)
                .add("recurring[memo]", memo)
                .add("recurring[price]", String.valueOf(recurring.price))
                .add("recurring[is_expense]", String.valueOf(recurring.isExpense))
                .add("recurring[order]", String.valueOf(recurring.order))
                .add("recurring[reason_uuid]", recurring.reason.uuid)
                .add("recurring[account_uuid]", recurring.account.uuid)
                .add("recurring[project_uuid]", recurring.project.uuid);

        setHttpMethod(HTTP_METHOD_PUT);
        setRequestPath(URL_PATH_RECURRING + "/" + recurring.uuid);

        setFormBody(formBuilder.build());
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e(LTAG, "updateRecurring(uuid) onFailure");
                if (response != null) {
                    Log.e(LTAG, "updateRecurring(uuid) onFailure response.code: " + response.code()
                            + ", message: " + response.message());
                }
                callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                mRecurringDataManager.updateNeedSync(recurringId, false);
                callback.onSuccess(response, content);
            }
        });

        requestApi();
    }

    public void updateEntry(String uuid, final AsyncOkHttpClient.Callback callback) {
        if (!isLoggingIn() || !TNApi.isNetworkConnected(context)) {
            if (callback != null)
                callback.onSuccess(null, null);
            return;
        }

        Entry entry = mEntryDataManager.findByUuid(uuid);
        final long entryId = entry.id;

        String memo = (entry.memo != null) ? entry.memo : "";

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder
                .add("entry[date]", ValueConverter.long2dateString(entry.date))
                .add("entry[memo]", memo)
                .add("entry[price]", String.valueOf(entry.price))
                .add("entry[is_expense]", String.valueOf(entry.isExpense))
                .add("entry[updated_mobile]", ValueConverter.long2dateString(entry.updated))
                .add("entry[reason_uuid]", entry.reason.uuid)
                .add("entry[account_uuid]", entry.account.uuid)
                .add("entry[project_uuid]", entry.project.uuid);

        setHttpMethod(HTTP_METHOD_PUT);
        setRequestPath(URL_PATH_ENTRY + "/" + entry.uuid);

        setFormBody(formBuilder.build());
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e(LTAG, "updateEntry(uuid) onFailure");
                if (response != null) {
                    Log.e(LTAG, "updateEntry(uuid) onFailure response.code: " + response.code()
                            + ", message: " + response.message());
                }
                if (callback != null)
                    callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                mEntryDataManager.updateNeedSync(entryId, false);
                if (callback != null)
                    callback.onSuccess(response, content);
            }
        });

        requestApi();
    }

    private void updateAllNeedSyncProjects(final AsyncOkHttpClient.Callback callback) {
        List<Project> projects = mProjectDataManager.findAllNeedSync(true);
        final int projectSize = projects.size();
        if (projectSize == 0) {
            callback.onSuccess(null, null);
        }

        mCount = 0;
        for (Project project : projects) {
            updateProject(project.uuid, new AsyncOkHttpClient.Callback() {
                @Override
                public void onFailure(Response response, Throwable throwable) {
                    if (callback != null)
                        callback.onFailure(response, throwable);
                }

                @Override
                public void onSuccess(Response response, String content) {
                    mCount++;
                    if (mCount >= projectSize) {
                        if (callback != null)
                            callback.onSuccess(response, content);
                    }
                }
            });
        }
    }

    private void updateAllNeedSyncReasons(final AsyncOkHttpClient.Callback callback) {
        List<Reason> reasons = mReasonDataManager.findAllNeedSync(true);
        final int reasonSize = reasons.size();
        if (reasonSize == 0) {
            callback.onSuccess(null, null);
        }

        mCount = 0;
        for (Reason reason : reasons) {
            updateReason(reason.uuid, new AsyncOkHttpClient.Callback() {
                @Override
                public void onFailure(Response response, Throwable throwable) {
                    callback.onFailure(response, throwable);
                }

                @Override
                public void onSuccess(Response response, String content) {
                    mCount++;
                    if (mCount >= reasonSize)
                        callback.onSuccess(response, content);
                }
            });
        }
    }

    private void updateAllNeedSyncAccounts(final AsyncOkHttpClient.Callback callback) {
        List<Account> accounts = mAccountDataManager.findAllNeedSync(true);
        final int accountSize = accounts.size();
        if (accountSize == 0) {
            callback.onSuccess(null, null);
        }

        mCount = 0;
        for (Account account : accounts) {
            updateAccount(account.uuid, new AsyncOkHttpClient.Callback() {
                @Override
                public void onFailure(Response response, Throwable throwable) {
                    callback.onFailure(response, throwable);
                }

                @Override
                public void onSuccess(Response response, String content) {
                    mCount++;
                    if (mCount >= accountSize)
                        callback.onSuccess(response, content);
                }
            });
        }
    }

    private void updateAllNeedSyncSummaries(final AsyncOkHttpClient.Callback callback) {
        List<Summary> summaries = mSummaryDataManager.findAllNeedSync(true);
        final int summarySize = summaries.size();
        if (summarySize == 0) {
            callback.onSuccess(null, null);
        }

        mCount = 0;
        for (Summary summary : summaries) {
            updateSummary(summary.uuid, new AsyncOkHttpClient.Callback() {
                @Override
                public void onFailure(Response response, Throwable throwable) {
                    callback.onFailure(response, throwable);
                }

                @Override
                public void onSuccess(Response response, String content) {
                    mCount++;
                    if (mCount >= summarySize)
                        callback.onSuccess(response, content);
                }
            });
        }
    }

    private void updateAllNeedSyncRecurrings(final AsyncOkHttpClient.Callback callback) {
        List<Recurring> recurrings = mRecurringDataManager.findAllNeedSync(true);
        final int recurringSize = recurrings.size();
        if (recurringSize == 0) {
            callback.onSuccess(null, null);
        }

        mCount = 0;
        for (Recurring recurring : recurrings) {
            updateRecurring(recurring.uuid, new AsyncOkHttpClient.Callback() {
                @Override
                public void onFailure(Response response, Throwable throwable) {
                    callback.onFailure(response, throwable);
                }

                @Override
                public void onSuccess(Response response, String content) {
                    mCount++;
                    if (mCount >= recurringSize)
                        callback.onSuccess(response, content);
                }
            });
        }
    }

    private void updateAllNeedSyncEntries(final AsyncOkHttpClient.Callback callback) {
        List<Entry> entries = mEntryDataManager.findAllNeedSync(true);
        final int entrySize = entries.size();
        if (entrySize == 0) {
            callback.onSuccess(null, null);
        }

        mCount = 0;
        for (Entry entry : entries) {
            updateEntry(entry.uuid, new AsyncOkHttpClient.Callback() {
                @Override
                public void onFailure(Response response, Throwable throwable) {
                    callback.onFailure(response, throwable);
                }

                @Override
                public void onSuccess(Response response, String content) {
                    mCount++;
                    if (mCount >= entrySize)
                        callback.onSuccess(response, content);
                }
            });
        }
    }

    private void updateAllNeedSyncData(final AsyncOkHttpClient.Callback callback) {
        if (!isLoggingIn()) {
            if (callback != null)
                callback.onSuccess(null, null);
            return;
        }

        updateAllNeedSyncProjects(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e(LTAG, "updateAllNeedSyncData onFailure --- Projects");
                callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                updateAllNeedSyncReasons(new AsyncOkHttpClient.Callback() {
                    @Override
                    public void onFailure(Response response, Throwable throwable) {
                        Log.e(LTAG, "updateAllNeedSyncData onFailure --- Reasons");
                        callback.onFailure(response, throwable);
                    }

                    @Override
                    public void onSuccess(Response response, String content) {
                        updateAllNeedSyncAccounts(new AsyncOkHttpClient.Callback() {
                            @Override
                            public void onFailure(Response response, Throwable throwable) {
                                Log.e(LTAG, "updateAllNeedSyncData onFailure --- Accounts");
                                callback.onFailure(response, throwable);
                            }

                            @Override
                            public void onSuccess(Response response, String content) {
                                updateAllNeedSyncSummaries(new AsyncOkHttpClient.Callback() {
                                    @Override
                                    public void onFailure(Response response, Throwable throwable) {
                                        Log.e(LTAG, "updateAllNeedSyncData onFailure --- Summaries");
                                        callback.onFailure(response, throwable);
                                    }

                                    @Override
                                    public void onSuccess(Response response, String content) {
                                        updateAllNeedSyncRecurrings(new AsyncOkHttpClient.Callback() {
                                            @Override
                                            public void onFailure(Response response, Throwable throwable) {
                                                Log.e(LTAG, "updateAllNeedSyncData onFailure --- Recurrings");
                                                callback.onFailure(response, throwable);
                                            }

                                            @Override
                                            public void onSuccess(Response response, String content) {

                                                updateAllNeedSyncEntries(callback);
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
    //    -- Delete Method --
    //--------------------------------------------------------------//

    public void deleteProject(final String uuid, final AsyncOkHttpClient.Callback callback) {
        if (!isLoggingIn() || !TNApi.isNetworkConnected(context)) {
            if (callback != null)
                callback.onSuccess(null, null);
            return;
        }

        setHttpMethod(HTTP_METHOD_DELETE);
        setRequestPath(URL_PATH_PROJECT + "/" + uuid);

        setFormBody(null);
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e(LTAG, "deleteProject(uuid) onFailure");
                if (response != null) {
                    Log.e(LTAG, "deleteProject(uuid) onFailure response.code: " + response.code()
                            + ", message: " + response.message());
                }
                if (callback != null)
                    callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                mProjectDataManager.delete(uuid);
                if (callback != null)
                    callback.onSuccess(response, content);
            }
        });

        requestApi();
    }

    public void deleteReason(final String uuid, final AsyncOkHttpClient.Callback callback) {
        if (!isLoggingIn() || !TNApi.isNetworkConnected(context)) {
            if (callback != null)
                callback.onSuccess(null, null);
            return;
        }

        setHttpMethod(HTTP_METHOD_DELETE);
        setRequestPath(URL_PATH_REASON + "/" + uuid);

        setFormBody(null);
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e(LTAG, "deleteReason(uuid) onFailure");
                if (response != null) {
                    Log.e(LTAG, "deleteReason(uuid) onFailure response.code: " + response.code()
                            + ", message: " + response.message());
                }
                if (callback != null)
                    callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                mReasonDataManager.delete(uuid);
                if (callback != null)
                    callback.onSuccess(response, content);
            }
        });

        requestApi();
    }

    public void deleteAccount(final String uuid, final AsyncOkHttpClient.Callback callback) {
        if (!isLoggingIn() || !TNApi.isNetworkConnected(context)) {
            if (callback != null)
                callback.onSuccess(null, null);
            return;
        }

        setHttpMethod(HTTP_METHOD_DELETE);
        setRequestPath(URL_PATH_ACCOUNT + "/" + uuid);

        setFormBody(null);
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e(LTAG, "deleteAccount(uuid) onFailure");
                if (response != null) {
                    Log.e(LTAG, "deleteAccount(uuid) onFailure response.code: " + response.code()
                            + ", message: " + response.message());
                }
                if (callback != null)
                    callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                mAccountDataManager.delete(uuid);
                if (callback != null)
                    callback.onSuccess(response, content);
            }
        });

        requestApi();

    }

    public void deleteSummary(final String uuid, final AsyncOkHttpClient.Callback callback) {
        if (!isLoggingIn() || !TNApi.isNetworkConnected(context)) {
            if (callback != null)
                callback.onSuccess(null, null);
            return;
        }

        setHttpMethod(HTTP_METHOD_DELETE);
        setRequestPath(URL_PATH_SUMMARY + "/" + uuid);

        setFormBody(null);
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e(LTAG, "deleteSummary(uuid) onFailure");
                if (response != null) {
                    Log.e(LTAG, "deleteSummary(uuid) onFailure response.code: " + response.code()
                            + ", message: " + response.message());
                }
                if (callback != null)
                    callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                mSummaryDataManager.delete(uuid);
                if (callback != null)
                    callback.onSuccess(response, content);
            }
        });

        requestApi();
    }

    public void deleteRecurring(final String uuid, final AsyncOkHttpClient.Callback callback) {
        if (!isLoggingIn() || !TNApi.isNetworkConnected(context)) {
            if (callback != null)
                callback.onSuccess(null, null);
            return;
        }

        setHttpMethod(HTTP_METHOD_DELETE);
        setRequestPath(URL_PATH_RECURRING + "/" + uuid);

        setFormBody(null);
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e(LTAG, "deleteRecurring(uuid) onFailure");
                if (response != null) {
                    Log.e(LTAG, "deleteRecurring(uuid) onFailure response.code: " + response.code()
                            + ", message: " + response.message());
                }
                if (callback != null)
                    callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                mRecurringDataManager.delete(uuid);
                if (callback != null)
                    callback.onSuccess(response, content);
            }
        });

        requestApi();
    }

    public void deleteEntry(final String uuid, final AsyncOkHttpClient.Callback callback) {
        if (!isLoggingIn() || !TNApi.isNetworkConnected(context)) {
            if (callback != null)
                callback.onSuccess(null, null);
            return;
        }

        setHttpMethod(HTTP_METHOD_DELETE);
        setRequestPath(URL_PATH_ENTRY + "/" + uuid);

        setFormBody(null);
        setCallback(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e(LTAG, "deleteEntry(uuid) onFailure");
                if (response != null) {
                    Log.e(LTAG, "deleteEntry(uuid) onFailure response.code: " + response.code()
                            + ", message: " + response.message());
                }
                if (callback != null)
                    callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                mEntryDataManager.delete(uuid);
                if (callback != null)
                    callback.onSuccess(response, content);
            }
        });

        requestApi();
    }

    private void deleteAllProjects(final AsyncOkHttpClient.Callback callback) {
        List<Project> projects = mProjectDataManager.findAllDeleted(true);
        final int projectSize = projects.size();
        if (projectSize == 0) {
            callback.onSuccess(null, null);
        }

        mCount = 0;
        for (Project project : projects) {
            deleteProject(project.uuid, new AsyncOkHttpClient.Callback() {
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

    private void deleteAllReasons(final AsyncOkHttpClient.Callback callback) {
        List<Reason> reasons = mReasonDataManager.findAllDeleted(true);
        final int reasonSize = reasons.size();
        if (reasonSize == 0) {
            callback.onSuccess(null, null);
        }

        mCount = 0;
        for (Reason reason : reasons) {
            deleteReason(reason.uuid, new AsyncOkHttpClient.Callback() {
                @Override
                public void onFailure(Response response, Throwable throwable) {
                    callback.onFailure(response, throwable);
                }

                @Override
                public void onSuccess(Response response, String content) {
                    mCount++;
                    if (mCount >= reasonSize)
                        callback.onSuccess(response, content);
                }
            });
        }
    }

    private void deleteAllAccounts(final AsyncOkHttpClient.Callback callback) {
        List<Account> accounts = mAccountDataManager.findAllDeleted(true);
        final int accountSize = accounts.size();
        if (accountSize == 0) {
            callback.onSuccess(null, null);
        }

        mCount = 0;
        for (Account account : accounts) {
            deleteAccount(account.uuid, new AsyncOkHttpClient.Callback() {
                @Override
                public void onFailure(Response response, Throwable throwable) {
                    callback.onFailure(response, throwable);
                }

                @Override
                public void onSuccess(Response response, String content) {
                    mCount++;
                    if (mCount >= accountSize)
                        callback.onSuccess(response, content);
                }
            });
        }
    }

    private void deleteAllSummaries(final AsyncOkHttpClient.Callback callback) {
        List<Summary> summaries = mSummaryDataManager.findAllDeleted(true);
        final int summarySize = summaries.size();
        if (summarySize == 0) {
            callback.onSuccess(null, null);
        }

        mCount = 0;
        for (Summary summary : summaries) {
            deleteSummary(summary.uuid, new AsyncOkHttpClient.Callback() {
                @Override
                public void onFailure(Response response, Throwable throwable) {
                    callback.onFailure(response, throwable);
                }

                @Override
                public void onSuccess(Response response, String content) {
                    mCount++;
                    if (mCount >= summarySize)
                        callback.onSuccess(response, content);
                }
            });
        }
    }

    private void deleteAllRecurrings(final AsyncOkHttpClient.Callback callback) {
        List<Recurring> recurrings = mRecurringDataManager.findAllDeleted(true);
        final int recurringSize = recurrings.size();
        if (recurringSize == 0) {
            callback.onSuccess(null, null);
        }

        mCount = 0;
        for (Recurring recurring : recurrings) {
            deleteRecurring(recurring.uuid, new AsyncOkHttpClient.Callback() {
                @Override
                public void onFailure(Response response, Throwable throwable) {
                    callback.onFailure(response, throwable);
                }

                @Override
                public void onSuccess(Response response, String content) {
                    mCount++;
                    if (mCount >= recurringSize)
                        callback.onSuccess(response, content);
                }
            });
        }
    }

    private void deleteAllEntries(final AsyncOkHttpClient.Callback callback) {
        List<Entry> entries = mEntryDataManager.findAllDeleted(true);
        final int entrySize = entries.size();
        if (entrySize == 0) {
            callback.onSuccess(null, null);
        }

        mCount = 0;
        for (Entry entry : entries) {
            deleteEntry(entry.uuid, new AsyncOkHttpClient.Callback() {
                @Override
                public void onFailure(Response response, Throwable throwable) {
                    callback.onFailure(response, throwable);
                }

                @Override
                public void onSuccess(Response response, String content) {
                    mCount++;
                    if (mCount >= entrySize)
                        callback.onSuccess(response, content);
                }
            });
        }
    }

    public void updateAllDeletedData(final AsyncOkHttpClient.Callback callback) {
        if (!isLoggingIn()) {
            callback.onSuccess(null, null);
            return;
        }

        deleteAllProjects(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e(LTAG, "updateAllDeletedData onFailure --- Projects");
                callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                deleteAllReasons(new AsyncOkHttpClient.Callback() {
                    @Override
                    public void onFailure(Response response, Throwable throwable) {
                        Log.e(LTAG, "updateAllDeletedData onFailure --- Reasons");
                        callback.onFailure(response, throwable);
                    }

                    @Override
                    public void onSuccess(Response response, String content) {
                        deleteAllAccounts(new AsyncOkHttpClient.Callback() {
                            @Override
                            public void onFailure(Response response, Throwable throwable) {
                                Log.e(LTAG, "updateAllDeletedData onFailure --- Accounts");
                                callback.onFailure(response, throwable);
                            }

                            @Override
                            public void onSuccess(Response response, String content) {
                                deleteAllSummaries(new AsyncOkHttpClient.Callback() {
                                    @Override
                                    public void onFailure(Response response, Throwable throwable) {
                                        Log.e(LTAG, "updateAllDeletedData onFailure --- Summaries");
                                        callback.onFailure(response, throwable);
                                    }

                                    @Override
                                    public void onSuccess(Response response, String content) {
                                        deleteAllRecurrings(new AsyncOkHttpClient.Callback() {
                                            @Override
                                            public void onFailure(Response response, Throwable throwable) {
                                                Log.e(LTAG, "updateAllDeletedData onFailure --- Recurrings");
                                                callback.onFailure(response, throwable);
                                            }

                                            @Override
                                            public void onSuccess(Response response, String content) {
                                                deleteAllEntries(callback);
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


    // Sync Data

    public void syncData(final Activity activity, boolean isShowMessage, final AsyncOkHttpClient.Callback callback) {
        if (!isLoggingIn() || !isNetworkConnected(context) || isSyncing()) {
            if (callback != null)
                callback.onSuccess(null, null);
            return;
        }

        //@@ If premium is not available, check the latest premium expired date from Taxnote Cloud server
        //@@ iOSでは subscriptionのチェックなどを行う


        // show updating data message
        if (!isSyncing()) {
            DialogManager.showToast(context, context.getString(R.string.updating_data));
        }

        // Start sync data
        saveAllNeedSaveSyncDeletedData(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e(LTAG, "syncData onFailure");
                if (callback != null)
                    callback.onFailure(response, throwable);
            }

            @Override
            public void onSuccess(Response response, String content) {
                setIsSyncing(true);

                getAllData(new AsyncOkHttpClient.Callback() {
                    @Override
                    public void onFailure(Response response, Throwable throwable) {
                        setIsSyncing(false);
                        if (callback != null)
                            callback.onFailure(response, throwable);
                    }

                    @Override
                    public void onSuccess(Response response, String content) {
                        setIsSyncing(false);
                        if (callback != null)
                            callback.onSuccess(response, content);

                        BroadcastUtil.sendReloadReport(activity);
                    }
                });
            }
        });
    }

}
