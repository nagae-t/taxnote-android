package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.TaxnoteApp;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason;
import com.example.taxnoteandroid.model.Summary;
import com.example.taxnoteandroid.model.Summary_Schema;

import java.util.List;

public class SummaryDataManager {

    private OrmaDatabase ormaDatabase;

    public SummaryDataManager(Context context) {
      ormaDatabase = TaxnoteApp.getOrmaDatabase();
    }


    //--------------------------------------------------------------//
    //    -- Create --
    //--------------------------------------------------------------//

    public long save(Summary summary) {
        return ormaDatabase.insertIntoSummary(summary);
    }

    public static boolean isSaveSuccess(long id) {
        return id != -1;
    }


    //--------------------------------------------------------------//
    //    -- Read --
    //--------------------------------------------------------------//

    public Summary findByUuid(String uuid) {
        return ormaDatabase.selectFromSummary().uuidEq(uuid).valueOrNull();
    }

    public List<Summary> findAllWithReason(Reason reason, Context context) {

        // Get the current project
        ProjectDataManager projectDataManager   = new ProjectDataManager(context);
        Project project                         = projectDataManager.findCurrentProjectWithContext(context);

        List summaries = ormaDatabase.selectFromSummary()
                .where(Summary_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .and()
                .reasonEq(reason)
                .and()
                .projectEq(project)
                .orderBy(Summary_Schema.INSTANCE.order.getQualifiedName())
                .toList();

        return summaries;
    }


    //--------------------------------------------------------------//
    //    -- Update --
    //--------------------------------------------------------------//

    public int updateName(long id, String name) {
        return ormaDatabase.updateSummary().idEq(id).name(name).execute();
    }


    //--------------------------------------------------------------//
    //    -- Delete --
    //--------------------------------------------------------------//

    public int delete(long id) {
        return ormaDatabase.deleteFromSummary().idEq(id).execute();
    }
}