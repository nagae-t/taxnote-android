package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.TaxnoteApp;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.example.taxnoteandroid.model.Recurring;
import com.example.taxnoteandroid.model.Recurring_Updater;

import java.util.List;

public class RecurringDataManager {

    private OrmaDatabase ormaDatabase;
    private Context mContext;

    public RecurringDataManager(Context context) {
        this.mContext = context;
        ormaDatabase = TaxnoteApp.getOrmaDatabase();
    }


    //--------------------------------------------------------------//
    //    -- Create --
    //--------------------------------------------------------------//

    public long save(Recurring recurring) {
        return ormaDatabase.insertIntoRecurring(recurring);
    }

    public static boolean isSaveSuccess(long id) {
        return id != -1;
    }


    //--------------------------------------------------------------//
    //    -- Read --
    //--------------------------------------------------------------//

    public List<Recurring> findAll() {
        List<Recurring> dataList = ormaDatabase.selectFromRecurring().toList();

        return dataList;
    }

    public Recurring findByUuid(String uuid) {
        return ormaDatabase.selectFromRecurring().uuidEq(uuid).valueOrNull();
    }


    //--------------------------------------------------------------//
    //    -- Update --
    //--------------------------------------------------------------//

    public int updateRecurring(Recurring recurring) {
        return ormaDatabase.updateRecurring().idEq(recurring.id).execute();
    }

    public int updateNeedSave(long id, boolean needSave) {
        return ormaDatabase.updateRecurring().idEq(id).needSave(needSave).execute();
    }

    public void update(Recurring rec) {
        Recurring_Updater updater = ormaDatabase.updateRecurring();
        updater.idEq(rec.id)
                .order(rec.order)
                .dateIndex(rec.dateIndex)
                .price(rec.price)
                .deleted(rec.deleted)
                .isExpense(rec.isExpense)
                .needSave(rec.needSave)
                .needSync(rec.needSync)
                .uuid(rec.uuid)
                .timezone(rec.timezone)
                .memo(rec.memo)
                .project(rec.project)
                .reason(rec.reason)
                .account(rec.account)
                .execute();
    }


    //--------------------------------------------------------------//
    //    -- Delete --
    //--------------------------------------------------------------//

    public int delete(long id) {
        return ormaDatabase.deleteFromRecurring().idEq(id).execute();
    }
}