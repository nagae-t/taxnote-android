package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.BuildConfig;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason;
import com.example.taxnoteandroid.model.Reason_Schema;
import com.github.gfx.android.orma.AccessThreadConstraint;
import com.github.gfx.android.orma.Inserter;

import java.util.List;

public class ReasonDataManager {

    private OrmaDatabase ormaDatabase;

    public ReasonDataManager(Context context) {
        // Ormaの初期設定
        ormaDatabase = OrmaDatabase.builder(context)
                .trace(BuildConfig.DEBUG)
                .writeOnMainThread(AccessThreadConstraint.NONE)
                .readOnMainThread(AccessThreadConstraint.NONE)
                .build();
    }


    //--------------------------------------------------------------//
    //    -- Create --
    //--------------------------------------------------------------//

    public long save(Reason reason) {
        return ormaDatabase.insertIntoReason(reason);
    }

    // @@ 他のもの作る
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

        //QQ projectを指定すると落ちる
//        List reasons = ormaDatabase.selectFromReason().where(Reason_Schema.INSTANCE.deleted.getQualifiedName() + " = 0  AND "
//                + Reason_Schema.INSTANCE.isExpense.getQualifiedName() + " = ? AND ", isExpense
//                + Reason_Schema.INSTANCE.project.getQualifiedName() + " = ?", project).
//                orderBy(Reason_Schema.INSTANCE.order.getQualifiedName()).
//                toList();

        //エラーログ
        //        D/AndroidRuntime: Shutting down VM
//        W/dalvikvm: threadid=1: thread exiting with uncaught exception (group=0x9cc91b20)
//        E/AndroidRuntime: FATAL EXCEPTION: main
//        Process: com.example.taxnoteandroid, PID: 2355
//        android.database.sqlite.SQLiteException: near ")": syntax error (code 1): , while compiling: SELECT `r1`.`order`, `r1`.`deleted`, `r1`.`isExpense`, `r1`.`needSave`, `r1`.`needSync`, `r1`.`name`, `r1`.`details`, `r1`.`uuid`, `r1`.`project`, `p2`.`order`, `p2`.`isMaster`, `p2`.`decimal`, `p2`.`deleted`, `p2`.`needSave`, `p2`.`needSync`, `p2`.`uuid`, `p2`.`name`, `p2`.`accountUuidForExpense`, `p2`.`accountUuidForIncome`, `p2`.`id`, `r1`.`id` FROM `Reason` AS `r1` LEFT OUTER JOIN `Project` AS `p2` ON `r1`.`project` = `p2`.`id` WHERE (`r1`.`deleted` = 0  AND `r1`.`isExpense` = ? AND ) ORDER BY `r1`.`order`
//        at android.database.sqlite.SQLiteConnection.nativePrepareStatement(Native Method)
//        at android.database.sqlite.SQLiteConnection.acquirePreparedStatement(SQLiteConnection.java:889)
//        at android.database.sqlite.SQLiteConnection.prepare(SQLiteConnection.java:500)


        List reasons = ormaDatabase.selectFromReason().where(Reason_Schema.INSTANCE.deleted.getQualifiedName() + " = 0  AND "
                + Reason_Schema.INSTANCE.isExpense.getQualifiedName()
                + " = ?", isExpense).orderBy(Reason_Schema.INSTANCE.order.getQualifiedName()).
                toList();

        return reasons;
    }


    //--------------------------------------------------------------//
    //    -- Update --
    //--------------------------------------------------------------//

    public int updateReason(Reason reason) {
        return ormaDatabase.updateReason().idEq(reason.id).execute();
    }


    //--------------------------------------------------------------//
    //    -- Delete --
    //--------------------------------------------------------------//

    public int delete(long id) {
        return ormaDatabase.deleteFromReason().idEq(id).execute();
    }
}