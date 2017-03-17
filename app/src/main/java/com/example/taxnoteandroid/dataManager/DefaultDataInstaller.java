package com.example.taxnoteandroid.dataManager;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;

import com.example.taxnoteandroid.MainActivity;
import com.example.taxnoteandroid.R;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason;
import com.example.taxnoteandroid.model.Summary;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_CHARACTER_CODE_SHIFTJIS;
import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_CHARACTER_CODE_UTF8;
import static com.helpshift.util.HelpshiftContext.getApplicationContext;

public class DefaultDataInstaller {

    private class DefaultSummary {
        String reasonName;
        List<String> summary;
    }

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

        // Save shared preferences
        SharedPreferencesManager.saveUuidForCurrentProject(context, project.uuid);
        SharedPreferencesManager.saveDefaultDatabaseSet(context);

        // 文字コードデフォルト値
        String localeLanguage = Locale.getDefault().getLanguage();
        if (localeLanguage.equals("ja")) {
            SharedPreferencesManager.saveCurrentCharacterCode(context, EXPORT_CHARACTER_CODE_SHIFTJIS);
        } else {
            SharedPreferencesManager.saveCurrentCharacterCode(context, EXPORT_CHARACTER_CODE_UTF8);
        }

    }

    /**
     * 新しいProject（帳簿）を追加する
     * @param context
     * @param name
     * @return
     */
    public static Project addNewProjectByName(Context context, String name, int order) {
        ProjectDataManager projectDataManager = new ProjectDataManager(context);

        // Set New Project
        Project project = new Project();
        project.isMaster = false;
        project.name = name;
        project.order = order;
        project.uuid = UUID.randomUUID().toString();
        project.accountUuidForExpense = "";
        project.accountUuidForIncome = "";
        project.decimal = context.getResources().getBoolean(R.bool.is_decimal);

        long newId = projectDataManager.save(project);
        project.id = newId;

        // Set categories
        setDefaultReasonData(context, project);
        setDefaultAccountData(context, project);

        return project;
    }

    /**
     * 帳簿(Project)を切り替える
     * @param context
     * @param targetProject
     */
    public static void switchProject(Context context, Project targetProject) {
        // Save shared preferences
        SharedPreferencesManager.saveUuidForCurrentProject(context, targetProject.uuid);
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

            long id = reasonDataManager.save(reason);
            reason.id = id;

            // Set summary data related to reason name
            setDefaultSummaryData(context, project, reason);
        }
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

    private static void setDefaultSummaryData(Context context, Project project, Reason reason) {

        Type type = new TypeToken<List<DefaultSummary>>() {
        }.getType();

        // Get default json list
        InputStream inputStream = context.getResources().openRawResource(R.raw.default_summary);
        JsonReader jsonReader   = new JsonReader(new InputStreamReader(inputStream));
        Gson gson               = new Gson();
        List<DefaultSummary> summaries  = gson.fromJson(jsonReader, type);

        // Catch reason name from summary list
        for (int i = 0, size = summaries.size(); i < size; i++) {

            if (reason.name.equals(summaries.get(i).reasonName)) {

                // Save summary
                List<String> summaryStrings = summaries.get(i).summary;
                saveSummaryFromSummaryStrings(summaryStrings, context, project, reason);
            }
        }
    }

    private static void saveSummaryFromSummaryStrings(List<String> summaryStrings, Context context, Project project, Reason reason) {

        SummaryDataManager summaryDataManager = new SummaryDataManager(context);

        for (int i = 0, size = summaryStrings.size(); i < size; i++) {

            Summary summary = new Summary();
            summary.order   = i;
            summary.uuid    = UUID.randomUUID().toString();
            summary.name    = summaryStrings.get(i);
            summary.project = project;
            summary.reason  = reason;

            summaryDataManager.save(summary);
        }
    }

    public static void restartApp(AppCompatActivity activity) {
        Context context = activity.getApplicationContext();
        context.sendBroadcast(new Intent(MainActivity.BROADCAST_RESTART_APP));
        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        Intent intentCompat = IntentCompat.makeRestartActivityTask(mainIntent.getComponent());
        activity.startActivity(intentCompat);
        activity.finish();
    }
}
