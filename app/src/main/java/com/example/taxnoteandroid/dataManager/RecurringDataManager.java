package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.Library.taxnote.TNApiModel;
import com.example.taxnoteandroid.Library.taxnote.TNApiUser;
import com.example.taxnoteandroid.R;
import com.example.taxnoteandroid.TaxnoteApp;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Recurring;
import com.example.taxnoteandroid.model.Recurring_Schema;
import com.example.taxnoteandroid.model.Recurring_Updater;
import com.github.gfx.android.orma.OrderSpec;

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

    public List<Recurring> findCurrentAll() {
        ProjectDataManager projectDataManager = new ProjectDataManager(mContext);
        Project project = projectDataManager.findCurrent();
        List<Recurring> dataList = ormaDatabase.selectFromRecurring()
                .where(Recurring_Schema.INSTANCE.deleted.getQualifiedName() + " = " + 0)
                .projectEq(project)
                .orderBy(Recurring_Schema.INSTANCE.dateIndex.getQualifiedName() + " " + OrderSpec.ASC)
                .toList();

        return dataList;
    }

    public Recurring findByUuid(String uuid) {
        return ormaDatabase.selectFromRecurring().uuidEq(uuid).valueOrNull();
    }

    public List<Recurring> findAllNeedSave(boolean isNeedSave) {
        int needSave = (isNeedSave) ? 1 : 0;
        List<Recurring> recurrings = ormaDatabase.selectFromRecurring()
                .where(Recurring_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .and()
                .where(Recurring_Schema.INSTANCE.needSave.getQualifiedName() + " = " + needSave)
                .toList();
        return recurrings;
    }

    public List<Recurring> findAllNeedSync(boolean isNeedSync) {
        int needSync = (isNeedSync) ? 1 : 0;
        List<Recurring> recurrings = ormaDatabase.selectFromRecurring()
                .where(Recurring_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .and()
                .where(Recurring_Schema.INSTANCE.needSync.getQualifiedName() + " = " + needSync)
                .toList();
        return recurrings;
    }

    public List<Recurring> findAllDeleted(boolean isDeleted) {
        int deleted = (isDeleted) ? 1 : 0;
        List<Recurring> recurrings = ormaDatabase.selectFromRecurring()
                .where(Recurring_Schema.INSTANCE.deleted.getQualifiedName() + " = " + deleted)
                .toList();
        return recurrings;
    }


    //--------------------------------------------------------------//
    //    -- Update --
    //--------------------------------------------------------------//


    public int updateNeedSave(long id, boolean needSave) {
        return ormaDatabase.updateRecurring().idEq(id).needSave(needSave).execute();
    }

    public int updateNeedSync(long id, boolean needSync) {
        return ormaDatabase.updateRecurring().idEq(id).needSync(needSync).execute();
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

    public void updateSetDeleted(String uuid, TNApiModel apiModel) {
        boolean isLoggingIn = TNApiUser.isLoggingIn(mContext);
        Recurring recurring = findByUuid(uuid);
        if (recurring == null) return;

        if (isLoggingIn) {
            ormaDatabase.updateRecurring().uuidEq(uuid)
                    .deleted(true)
                    .execute();

            // send api
            apiModel.deleteRecurring(uuid, null);
        } else {
            delete(recurring.id);
        }
    }


    //--------------------------------------------------------------//
    //    -- Delete --
    //--------------------------------------------------------------//

    public int delete(long id) {
        return ormaDatabase.deleteFromRecurring().idEq(id).execute();
    }

    public int delete(String uuid) {
        return ormaDatabase.deleteFromRecurring().uuidEq(uuid).execute();
    }


    //--------------------------------------------------------------//
    //    -- Other --
    //--------------------------------------------------------------//

    /**
     * 指定日一覧を取得する
     */
    public String[] getDesignatedDateList() {
        String[] dayOfMonthList = mContext.getResources().getStringArray(R.array.day_of_month_list);
        String[] dateList = mContext.getResources().getStringArray(R.array.normal_date_list);
        String[] result = new String[dayOfMonthList.length + dateList.length];
        int count = 0;
        for (String day : dayOfMonthList) {
            result[count] = day;
            count++;
        }
        for (String day : dateList) {
            result[count] = day;
            count++;
        }
        return result;
    }
}