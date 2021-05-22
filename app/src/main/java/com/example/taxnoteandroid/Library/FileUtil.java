package com.example.taxnoteandroid.Library;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.taxnoteandroid.Library.taxnote.TNApiModel;
import com.example.taxnoteandroid.TaxnoteApp;
import com.example.taxnoteandroid.dataManager.AccountDataManager;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.ReasonDataManager;
import com.example.taxnoteandroid.dataManager.RecurringDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.dataManager.SummaryDataManager;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Entry;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason;
import com.example.taxnoteandroid.model.Recurring;
import com.example.taxnoteandroid.model.Summary;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.util.List;

/**
 * Created by Eiichi on 2017/01/18.
 */

public class FileUtil {
    /**
     * 共有されたファイルから内容を読み込んでJsonに変換する
     *
     * @param context
     * @param uri
     */
    public static void dataImport(Context context, Uri uri) {
        // ファイルの読み込み
        InputStream input;
        Gson gson = new Gson();
        try {

            input = context.getContentResolver().openInputStream(uri);
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();

            // Json読み込み
            String jsonString = new String(buffer);

            JsonParser parser = new JsonParser();
            JsonObject jsonObj = parser.parse(jsonString).getAsJsonObject();

            // DBテーブルの初期化
            OrmaDatabase _db = TaxnoteApp.getOrmaDatabase();
            try {

                // クラウド連携している場合、帳簿削除の同期処理するように
                ProjectDataManager projectDataManager = new ProjectDataManager(context);
                List<Project> projectList = projectDataManager.findAll();
                for (Project project : projectList) {
                    projectDataManager.updateSetDeleted(project.uuid, new TNApiModel(context));
                }
                Thread.sleep(400);

                _db.deleteAll();
                Thread.sleep(500);

            } catch (Exception e) {
                Log.e("ERROR", "dataImport: reset DB error: " + e.getMessage());
//                e.printStackTrace();
            }

            // project
            JsonArray jsonList = jsonObj.get("project").getAsJsonArray();
            ProjectDataManager projectDataManager = new ProjectDataManager(context);
            for (JsonElement projectJson : jsonList) {
                Project newProject = gson.fromJson(projectJson, Project.class);
                newProject.needSave = true;
                long newProjectId = projectDataManager.save(newProject);
                newProject.id = newProjectId;

                if (newProject.isMaster) {
                    SharedPreferencesManager.saveUuidForCurrentProject(context, newProject.uuid);
                }

                // account
                jsonList = jsonObj.get("account").getAsJsonArray();
                AccountDataManager accountDataManager = new AccountDataManager(context);
                for (JsonElement jsItem : jsonList) {
                    Account newData = gson.fromJson(jsItem, Account.class);
                    newData.needSave = true;
                    if (newData.project.uuid.equals(newProject.uuid)) {
                        newData.project = newProject;
                        accountDataManager.save(newData);
                    }
                }

                // reason
                jsonList = jsonObj.get("reason").getAsJsonArray();
                ReasonDataManager reasonDataManager = new ReasonDataManager(context);
                for (JsonElement jsItem : jsonList) {
                    Reason newData = gson.fromJson(jsItem, Reason.class);
                    newData.needSave = true;
                    if (newData.project.uuid.equals(newProject.uuid)) {
                        newData.project = newProject;
                        reasonDataManager.save(newData);
                    }
                }

                // entry
                jsonList = jsonObj.get("entry").getAsJsonArray();
                EntryDataManager entryDataManager = new EntryDataManager(context);
                for (JsonElement jsItem : jsonList) {
                    Entry newData = gson.fromJson(jsItem, Entry.class);
                    newData.needSave = true;
                    if (newData.project.uuid.equals(newProject.uuid)) {
                        newData.project = newProject;
                        newData.account = accountDataManager.findByUuid(newData.account.uuid);
                        newData.reason = reasonDataManager.findByUuid(newData.reason.uuid);
                        if (newData.account != null && newData.reason != null)
                            entryDataManager.save(newData);
                    }
                }

                // summary
                jsonList = jsonObj.get("summary").getAsJsonArray();
                SummaryDataManager summDataManager = new SummaryDataManager(context);
                for (JsonElement jsItem : jsonList) {
                    Summary newData = gson.fromJson(jsItem, Summary.class);
                    newData.needSave = true;
                    if (newData.project.uuid.equals(newProject.uuid)) {
                        newData.project = newProject;
                        newData.reason = reasonDataManager.findByUuid(newData.reason.uuid);
                        if (newData.reason != null)
                            summDataManager.save(newData);
                    }
                }

                // recurring
                jsonList = jsonObj.get("recurring").getAsJsonArray();
                RecurringDataManager recDataManager = new RecurringDataManager(context);
                for (JsonElement jsItem : jsonList) {
                    Recurring newData = gson.fromJson(jsItem, Recurring.class);
                    newData.needSave = true;
                    if (newData.project.uuid.equals(newProject.uuid)) {
                        newData.project = newProject;
                        newData.account = accountDataManager.findByUuid(newData.account.uuid);
                        newData.reason = reasonDataManager.findByUuid(newData.reason.uuid);

                        if (newData.account != null && newData.reason != null)
                            recDataManager.save(newData);
                    }
                }
            }

            SharedPreferencesManager.saveDefaultDatabaseSet(context);
        } catch (Exception e) {
            Log.e("ERROR", "import error: " + e.getMessage());
        }
    }

}
