package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.TaxnoteApp;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason;
import com.example.taxnoteandroid.model.Reason_Schema;
import com.example.taxnoteandroid.model.Reason_Updater;
import com.github.gfx.android.orma.Inserter;

import java.util.List;

import static android.R.attr.order;

public class ReasonDataManager {

    private OrmaDatabase ormaDatabase;
    private Context mContext;

    public ReasonDataManager(Context context) {
        this.mContext = context;
        ormaDatabase = TaxnoteApp.getOrmaDatabase();
    }


    //--------------------------------------------------------------//
    //    -- Create --
    //--------------------------------------------------------------//

    public long save(Reason reason) {
        return ormaDatabase.insertIntoReason(reason);
    }

    public void saveAll(final List<Reason> reasons) {
        ormaDatabase.transactionSync(new Runnable() {
            @Override
            public void run() {
                Inserter<Reason> reasonInserter = ormaDatabase.prepareInsertIntoReason();
                reasonInserter.executeAll(reasons);
            }
        });
    }

    public static boolean isSaveSuccess(long id) {
        return id != -1;
    }


    //--------------------------------------------------------------//
    //    -- Read --
    //--------------------------------------------------------------//

    public List<Reason> findAll() {
        List<Reason> dataList = ormaDatabase.selectFromReason().toList();

        return dataList;
    }

    public Reason findByUuid(String uuid) {
        return ormaDatabase.selectFromReason().uuidEq(uuid).valueOrNull();
    }

    public List<Reason> findAllWithIsExpense(Boolean isExpense) {

        // Get the current project
        ProjectDataManager projectDataManager   = new ProjectDataManager(mContext);
        Project project                         = projectDataManager.findCurrentProjectWithContext();

        List reasons = ormaDatabase.selectFromReason().where(Reason_Schema.INSTANCE.deleted.getQualifiedName() + " = 0  AND "
                        + Reason_Schema.INSTANCE.isExpense.getQualifiedName() + " = ?",
                isExpense)
                .and()
                .projectEq(project)
                .orderBy(Reason_Schema.INSTANCE.order.getQualifiedName())
                .toList();
        return reasons;
    }

    public List<Reason> findAllNeedSave(boolean isNeedSave) {
        int needSave = (isNeedSave) ? 1 : 0;
        List<Reason> reasons = ormaDatabase.selectFromReason()
                .where(Reason_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .and()
                .where(Reason_Schema.INSTANCE.needSave.getQualifiedName() + " = " + needSave)
                .toList();
        return reasons;
    }

    public List<Reason> findAllNeedSync(boolean isNeedSync) {
        int needSync = (isNeedSync) ? 1 : 0;
        List<Reason> reasons = ormaDatabase.selectFromReason()
                .where(Reason_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .and()
                .where(Reason_Schema.INSTANCE.needSync.getQualifiedName() + " = " + needSync)
                .toList();
        return reasons;
    }

    public List<Reason> findAllDeleted(boolean isDeleted) {
        int deleted = (isDeleted) ? 1 : 0;
        List<Reason> reasons = ormaDatabase.selectFromReason()
                .where(Reason_Schema.INSTANCE.deleted.getQualifiedName() + " = " + deleted)
                .toList();
        return reasons;
    }


    //--------------------------------------------------------------//
    //    -- Update --
    //--------------------------------------------------------------//

    public int updateName(long id, String name) {
        return ormaDatabase.updateReason().idEq(id).name(name).execute();
    }

    public int updateOrder(long id, int order) {
        return ormaDatabase.updateReason().idEq(id).order(order).execute();
    }

    public int updateNeedSave(long id, boolean needSave) {
        return ormaDatabase.updateReason().idEq(id).needSave(needSave).execute();
    }

    public int updateNeedSync(long id, boolean needSync) {
        return ormaDatabase.updateReason().idEq(id).needSync(needSync).execute();
    }

    public void update(Reason reason) {
        Reason_Updater updater = ormaDatabase.updateReason();
        updater.idEq(reason.id)
                .order(reason.order)
                .deleted(reason.deleted)
                .isExpense(reason.isExpense)
                .needSync(reason.needSync)
                .needSave(reason.needSave)
                .name(reason.name)
                .details(reason.details)
                .uuid(reason.uuid)
                .project(reason.project)
                .execute();
    }

    
    //--------------------------------------------------------------//
    //    -- Delete --
    //--------------------------------------------------------------//

    public int delete(long id) {
        return ormaDatabase.deleteFromReason().idEq(id).execute();
    }

    public int delete(String uuid) {
        return ormaDatabase.deleteFromReason().uuidEq(uuid).execute();
    }

}