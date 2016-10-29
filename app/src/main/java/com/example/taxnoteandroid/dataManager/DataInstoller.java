package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.R;
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

public class DataInstoller {

    public static void installDefaultUserAndCategories(Context context) {
        if (SharedPreferencesManager.isDefaultDataBaseSet(context)) {
            return;
        }

        Project project = new Project();
        project.isMaster = true;
        project.name = "master";
        project.order = 0;
        project.uuid = UUID.randomUUID().toString();
        project.accountUuidForExpense = "";
        project.accountUuidForIncome = "";
        project.decimal = context.getResources().getBoolean(R.bool.is_decimal);

        long id = new ProjectDataManager(context).save(project);

        // TODO:Accountのsaveも書く

        setDefaultReasonData(context, project);

        if (!SharedPreferencesManager.saveUuidForCurrent(context, project.uuid)) {
            // TODO:error
        }

        SharedPreferencesManager.setDefaultDatabaseSet(context);
    }

    private static void setDefaultReasonData(Context context, Project project) {
        Type type = new TypeToken<List<Reason>>() {
        }.getType();
        InputStream inputStream = context.getResources().openRawResource(R.raw.default_reason);
        JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream));
        Gson gson = new Gson();
        List<Reason> reasons = gson.fromJson(jsonReader, type);

        ReasonDataManager reasonDataManager = new ReasonDataManager(context);

        for (int i = 0, size = reasons.size(); i < size; i++) {
            Reason reason = reasons.get(i);
            reason.order = i;
            reason.uuid = UUID.randomUUID().toString();
            reason.project = project;

            reasonDataManager.save(reason);
        }
    }
}
