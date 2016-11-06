package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.R;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class DefaultDataInstaller {

    public static void installDefaultUserAndCategories(Context context) {

        // Check if data is already set
        if (SharedPreferencesManager.isDefaultDataBaseSet(context)) {
            return;
        }

        // Set project
        Project project = new Project();
        project.isMaster = true;
        project.name = "master";
        project.order = 0;
        project.uuid = UUID.randomUUID().toString();
        project.accountUuidForExpense = "";
        project.accountUuidForIncome = "";
        project.decimal = context.getResources().getBoolean(R.bool.is_decimal);

        long id = new ProjectDataManager(context).save(project);
        project.id = id;

        // Set categories
        setDefaultReasonData(context, project);
        setDefaultAccountData(context, project);

        // set default summarydata
        // @@ ここちょっと構造が違う
        

        // Save shared preferences
        SharedPreferencesManager.saveUuidForCurrentProject(context, project.uuid);
        SharedPreferencesManager.saveDefaultDatabaseSet(context);
    }

    private static void setDefaultReasonData(Context context, Project project) {

        Type type = new TypeToken<List<Reason>>() {
        }.getType();

        // Get default json list
        InputStream inputStream = context.getResources().openRawResource(R.raw.default_reason);
        JsonReader jsonReader   = new JsonReader(new InputStreamReader(inputStream));
        Gson gson               = new Gson();
        List<Reason> reasons    = gson.fromJson(jsonReader, type);

        ReasonDataManager reasonDataManager = new ReasonDataManager(context);

        for (int i = 0, size = reasons.size(); i < size; i++) {

            Reason reason   = reasons.get(i);
            reason.order    = i;
            reason.uuid     = UUID.randomUUID().toString();
            reason.project  = project;

            reasonDataManager.save(reason);
        }


    //QQ なんかおちる
//        11-03 05:25:38.423 2193-2202/? W/SQLiteConnectionPool: A SQLiteConnection object for database '/data/data/com.example.taxnoteandroid/databases/com.example.taxnoteandroid.orma.db' was leaked!  Please fix your application to end transactions in progress properly and to close the database when it is no longer needed.
//        reasonDataManager.saveAll(reasons);
    }

    private static void setDefaultAccountData(Context context, Project project) {

        Type type = new TypeToken<List<Account>>() {
        }.getType();

        // Get default json list
        InputStream inputStream = context.getResources().openRawResource(R.raw.default_account);
        JsonReader jsonReader   = new JsonReader(new InputStreamReader(inputStream));
        Gson gson               = new Gson();
        List<Account> accounts  = gson.fromJson(jsonReader, type);

        AccountDataManager accountDataManager = new AccountDataManager(context);

        for (int i = 0, size = accounts.size(); i < size; i++) {

            Account account   = accounts.get(i);
            account.order    = i;
            account.uuid     = UUID.randomUUID().toString();
            account.project  = project;

            accountDataManager.save(account);
        }
    }
}
