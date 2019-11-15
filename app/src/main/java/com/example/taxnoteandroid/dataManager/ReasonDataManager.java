package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.Library.taxnote.TNApiModel;
import com.example.taxnoteandroid.Library.taxnote.TNApiUser;
import com.example.taxnoteandroid.TaxnoteApp;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason;
import com.example.taxnoteandroid.model.Reason_Schema;
import com.example.taxnoteandroid.model.Reason_Updater;
import com.github.gfx.android.orma.Inserter;

import java.util.List;

public class ReasonDataManager {

    private OrmaDatabase ormaDatabase;
    private Context mContext;
    private Project mCurrentProject;

    public ReasonDataManager(Context context) {
        this.mContext = context;
        ormaDatabase = TaxnoteApp.getOrmaDatabase();
        mCurrentProject = new ProjectDataManager(context).findCurrent();
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

    public Reason findByName(String name) {
        return ormaDatabase.selectFromReason().projectEq(mCurrentProject)
                .where(Reason_Schema.INSTANCE.name.getQualifiedName() + " = ?", name)
                .valueOrNull();
    }

    public List<Reason> findAllWithIsExpense(Boolean isExpense) {

        List reasons = ormaDatabase.selectFromReason()
                .projectEq(mCurrentProject)
                .where(Reason_Schema.INSTANCE.deleted.getQualifiedName() + " = 0  AND "
                        + Reason_Schema.INSTANCE.isExpense.getQualifiedName() + " = ?", isExpense)
                .and()
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

    public int countNeedSave(boolean isNeedSave) {
        int needSave = (isNeedSave) ? 1 : 0;
        return ormaDatabase.selectFromReason()
                .where(Reason_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .and()
                .where(Reason_Schema.INSTANCE.needSave.getQualifiedName() + " = " + needSave)
                .count();
    }


    //--------------------------------------------------------------//
    //    -- Update --
    //--------------------------------------------------------------//

    public int updateName(long id, String name) {
        return ormaDatabase.updateReason().idEq(id)
                .name(name)
                .needSync(true)
                .execute();
    }

    public int updateOrder(long id, int order) {
        return ormaDatabase.updateReason().idEq(id)
                .order(order)
                .needSync(true)
                .execute();
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

    public void updateSetDeleted(String uuid) {
        updateSetDeleted(uuid, null);
    }

    public void updateSetDeleted(String uuid, TNApiModel apiModel) {
        boolean isLoggingIn = TNApiUser.isLoggingIn(mContext);
        Reason reason = findByUuid(uuid);
        if (reason == null) return;

        if (isLoggingIn) {
            ormaDatabase.updateReason().uuidEq(uuid)
                    .deleted(true)
                    .execute();

            // send api
            if (apiModel != null)
                apiModel.deleteReason(uuid, null);

        } else {
            delete(reason.id);
        }

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