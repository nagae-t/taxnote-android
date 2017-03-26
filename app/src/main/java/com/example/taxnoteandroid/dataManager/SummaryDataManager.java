package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.TaxnoteApp;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Account_Schema;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason;
import com.example.taxnoteandroid.model.Summary;
import com.example.taxnoteandroid.model.Summary_Schema;
import com.example.taxnoteandroid.model.Summary_Updater;

import java.util.List;

public class SummaryDataManager {

    private OrmaDatabase ormaDatabase;
    private Context mContext;

    public SummaryDataManager(Context context) {
        this.mContext = context;
        ormaDatabase = TaxnoteApp.getOrmaDatabase();
    }


    //--------------------------------------------------------------//
    //    -- Create --
    //--------------------------------------------------------------//

    public static boolean isSaveSuccess(long id) {
        return id != -1;
    }

    public long save(Summary summary) {
        return ormaDatabase.insertIntoSummary(summary);
    }


    //--------------------------------------------------------------//
    //    -- Read --
    //--------------------------------------------------------------//

    public List<Summary> findAll() {
        List<Summary> dataList = ormaDatabase.selectFromSummary().toList();

        return dataList;
    }

    public Summary findByUuid(String uuid) {
        return ormaDatabase.selectFromSummary().uuidEq(uuid).valueOrNull();
    }

    public List<Summary> findAllWithReason(Reason reason) {

        // Get the current project
        ProjectDataManager projectDataManager = new ProjectDataManager(mContext);
        Project project = projectDataManager.findCurrentProjectWithContext();

        List summaries = ormaDatabase.selectFromSummary()
                .where(Summary_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .reasonEq(reason)
                .projectEq(project)
                .orderBy(Summary_Schema.INSTANCE.order.getQualifiedName())
                .toList();

        return summaries;
    }

    public List<Summary> findAllNeedSave(boolean isNeedSave) {
        int needSave = (isNeedSave) ? 1 : 0;
        List<Summary> summaries = ormaDatabase.selectFromSummary()
                .where(Summary_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .and()
                .where(Summary_Schema.INSTANCE.needSave.getQualifiedName() + " = " + needSave)
                .toList();
        return summaries;
    }

    public List<Summary> findAllNeedSync(boolean isNeedSync) {
        int needSync = (isNeedSync) ? 1 : 0;
        List<Summary> summaries = ormaDatabase.selectFromSummary()
                .where(Summary_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .and()
                .where(Summary_Schema.INSTANCE.needSync.getQualifiedName() + " = " + needSync)
                .toList();
        return summaries;
    }

    public List<Summary> findAllDeleted(boolean isDeleted) {
        int deleted = (isDeleted) ? 1 : 0;
        List<Summary> summaries = ormaDatabase.selectFromSummary()
                .where(Summary_Schema.INSTANCE.deleted.getQualifiedName() + " = " + deleted)
                .toList();
        return summaries;
    }


    //--------------------------------------------------------------//
    //    -- Update --
    //--------------------------------------------------------------//

    public int updateName(long id, String name) {
        return ormaDatabase.updateSummary().idEq(id).name(name).execute();
    }

    public int updateNeedSave(long id, boolean needSave) {
        return ormaDatabase.updateSummary().idEq(id).needSave(needSave).execute();
    }

    public int updateNeedSync(long id, boolean needSync) {
        return ormaDatabase.updateSummary().idEq(id).needSync(needSync).execute();
    }

    public void update(Summary summary) {
        Summary_Updater updater = ormaDatabase.updateSummary();
        updater.idEq(summary.id)
                .order(summary.order)
                .needSave(summary.needSave)
                .needSync(summary.needSync)
                .uuid(summary.uuid)
                .name(summary.name)
                .project(summary.project)
                .reason(summary.reason)
                .execute();
    }


    //--------------------------------------------------------------//
    //    -- Delete --
    //--------------------------------------------------------------//

    public int delete(long id) {
        return ormaDatabase.deleteFromSummary().idEq(id).execute();
    }

    public int delete(String uuid) {
        return ormaDatabase.deleteFromSummary().uuidEq(uuid).execute();
    }

    //--------------------------------------------------------------//
    //    -- Change order --
    //--------------------------------------------------------------//

    public int updateOrder(long id, int order) {
        return ormaDatabase.updateSummary().idEq(id).order(order).execute(); // 2017/01/30 E.Nozaki
    }
}