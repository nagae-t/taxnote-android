package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.TaxnoteApp;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason;
import com.example.taxnoteandroid.model.Reason_Schema;
import com.github.gfx.android.orma.Inserter;

import java.util.List;

public class ReasonDataManager {

    private OrmaDatabase ormaDatabase;

    public ReasonDataManager(Context context) {
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

    public Reason findByUuid(String uuid) {
        return ormaDatabase.selectFromReason().uuidEq(uuid).valueOrNull();
    }

    public List<Reason> findAllWithIsExpense(Boolean isExpense, Context context) {

        // Get the current project
        ProjectDataManager projectDataManager   = new ProjectDataManager(context);
        Project project                         = projectDataManager.findCurrentProjectWithContext(context);

        List reasons = ormaDatabase.selectFromReason().where(Reason_Schema.INSTANCE.deleted.getQualifiedName() + " = 0  AND "
                        + Reason_Schema.INSTANCE.isExpense.getQualifiedName() + " = ?",
                isExpense)
                .and()
                .projectEq(project)
                .orderBy(Reason_Schema.INSTANCE.order.getQualifiedName())
                .toList();
        return reasons;
    }


    //--------------------------------------------------------------//
    //    -- Update --
    //--------------------------------------------------------------//

    public int updateName(long id, String name) {
        return ormaDatabase.updateReason().idEq(id).name(name).execute();
    }
    
    
    //--------------------------------------------------------------//
    //    -- Delete --
    //--------------------------------------------------------------//

    public int delete(long id) {
        return ormaDatabase.deleteFromReason().idEq(id).execute();
    }


  //--------------------------------------------------------------//
  //    -- Change order --
  //--------------------------------------------------------------//

  public int updateOrder(long id, int order) {
    return ormaDatabase.updateReason().idEq(id).order(order).execute(); // 2017/01/17 E.Nozaki
  }
}