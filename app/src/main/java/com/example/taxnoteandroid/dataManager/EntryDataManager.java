package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.BuildConfig;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Entry;
import com.example.taxnoteandroid.model.Entry_Schema;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.example.taxnoteandroid.model.Reason;
import com.github.gfx.android.orma.AccessThreadConstraint;
import com.github.gfx.android.orma.OrderSpec;

import java.util.List;

public class EntryDataManager {

    private OrmaDatabase ormaDatabase;

    public EntryDataManager(Context context) {
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

    public long save(Entry entry) {
        return ormaDatabase.insertIntoEntry(entry);
    }

    public static boolean isSaveSuccess(long id) {
        return id != -1;
    }


    //--------------------------------------------------------------//
    //    -- Read --
    //--------------------------------------------------------------//

    public Entry findByUuid(String uuid) {
        //QQ　ここで落ちちゃう
        return ormaDatabase.selectFromEntry().where("uuid = ?", uuid).valueOrNull();

        //エラーコードはこれですよ
//        01-07 06:33:37.500 12043-12043/com.example.taxnoteandroid E/AndroidRuntime: FATAL EXCEPTION: main
//        Process: com.example.taxnoteandroid, PID: 12043
//        java.lang.RuntimeException: Unable to resume activity {com.example.taxnoteandroid/com.example.taxnoteandroid.EntryEditActivity}: android.database.sqlite.SQLiteException: ambiguous column name: uuid (code 1): , while compiling: SELECT `e1`.`date`, `e1`.`updated`, `e1`.`price`, `e1`.`deleted`, `e1`.`isExpense`, `e1`.`needSave`, `e1`.`needSync`, `e1`.`uuid`, `e1`.`memo`, `e1`.`project`, `p2`.`order`, `p2`.`isMaster`, `p2`.`decimal`, `p2`.`deleted`, `p2`.`needSave`, `p2`.`needSync`, `p2`.`uuid`, `p2`.`name`, `p2`.`accountUuidForExpense`, `p2`.`accountUuidForIncome`, `p2`.`id`, `e1`.`reason`, `r3`.`order`, `r3`.`deleted`, `r3`.`isExpense`, `r3`.`needSave`, `r3`.`needSync`, `r3`.`name`, `r3`.`details`, `r3`.`uuid`, `r3`.`project`, `p4`.`order`, `p4`.`isMaster`, `p4`.`decimal`, `p4`.`deleted`, `p4`.`needSave`, `p4`.`needSync`, `p4`.`uuid`, `p4`.`name`, `p4`.`accountUuidForExpense`, `p4`.`accountUuidForIncome`, `p4`.`id`, `r3`.`id`, `e1`.`account`, `a5`.`order`, `a5`.`deleted`, `a5`.`isExpense`, `a5`.`needSave`, `a5`.`needSync`, `a5`.`uuid`, `a5`.`name`, `a5`.`project`, `p6`.`order`, `p6`.`isMaster`, `p6`.`decimal`, `p6`.`deleted`, `p6`.`needSave`, `p6`.`needSync`, `p6`.`uuid`, `p6`.`name`, `p6`.`accountUuidForExpense`, `p6`.`accountUuidForIncome`, `p6`.`id`, `a5`.`id`, `e1`.`id` FROM `Entry` AS `e1` LEFT OUTER JOIN `Project` AS `p2` ON `e1`.`project` = `p2`.`id` LEFT OUTER JOIN `Reason` AS `r3` ON `e1`.`reason` = `r3`.`id` LEFT OUTER JOIN `Project` AS `p4` ON `r3`.`project` = `p4`.`id` LEFT OUTER JOIN `Account` AS `a5` ON `e1`.`account` = `a5`.`id` LEFT OUTER JOIN `Project` AS `p6` ON `a5`.`project` = `p6`.`id` WHERE (uuid = ?) LIMIT 0,1
//        at android.app.ActivityThread.performResumeActivity(ActivityThread.java:2986)
//        at android.app.ActivityThread.handleResumeActivity(ActivityThread.java:3017)
//        at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2392)
//        at android.app.ActivityThread.access$800(ActivityThread.java:151)
//        at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1303)
//        at android.os.Handler.dispatchMessage(Handler.java:102)
//        at android.os.Looper.loop(Looper.java:135)
//        at android.app.ActivityThread.main(ActivityThread.java:5254)
//        at java.lang.reflect.Method.invoke(Native Method)
//        at java.lang.reflect.Method.invoke(Method.java:372)
//        at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:903)
//        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:698)
//        Caused by: android.database.sqlite.SQLiteException: ambiguous column name: uuid (code 1): , while compiling: SELECT `e1`.`date`, `e1`.`updated`, `e1`.`price`, `e1`.`deleted`, `e1`.`isExpense`, `e1`.`needSave`, `e1`.`needSync`, `e1`.`uuid`, `e1`.`memo`, `e1`.`project`, `p2`.`order`, `p2`.`isMaster`, `p2`.`decimal`, `p2`.`deleted`, `p2`.`needSave`, `p2`.`needSync`, `p2`.`uuid`, `p2`.`name`, `p2`.`accountUuidForExpense`, `p2`.`accountUuidForIncome`, `p2`.`id`, `e1`.`reason`, `r3`.`order`, `r3`.`deleted`, `r3`.`isExpense`, `r3`.`needSave`, `r3`.`needSync`, `r3`.`name`, `r3`.`details`, `r3`.`uuid`, `r3`.`project`, `p4`.`order`, `p4`.`isMaster`, `p4`.`decimal`, `p4`.`deleted`, `p4`.`needSave`, `p4`.`needSync`, `p4`.`uuid`, `p4`.`name`, `p4`.`accountUuidForExpense`, `p4`.`accountUuidForIncome`, `p4`.`id`, `r3`.`id`, `e1`.`account`, `a5`.`order`, `a5`.`deleted`, `a5`.`isExpense`, `a5`.`needSave`, `a5`.`needSync`, `a5`.`uuid`, `a5`.`name`, `a5`.`project`, `p6`.`order`, `p6`.`isMaster`, `p6`.`decimal`, `p6`.`deleted`, `p6`.`needSave`, `p6`.`needSync`, `p6`.`uuid`, `p6`.`name`, `p6`.`accountUuidForExpense`, `p6`.`accountUuidForIncome`, `p6`.`id`, `a5`.`id`, `e1`.`id` FROM `Entry` AS `e1` LEFT OUTER JOIN `Project` AS `p2` ON `e1`.`project` = `p2`.`id` LEFT OUTER JOIN `Reason` AS `r3` ON `e1`.`reason` = `r3`.`id` LEFT OUTER JOIN `Project` AS `p4` ON `r3`.`project` = `p4`.`id` LEFT OUTER JOIN `Account` AS `a5` ON `e1`.`account` = `a5`.`id` LEFT OUTER JOIN `Project` AS `p6` ON `a5`.`project` = `p6`.`id` WHERE (uuid = ?) LIMIT 0,
//                01-07 06:33:39.575 12043-12043/? I/Process: Sending signal. PID: 12043 SIG: 9
    }

    //@@ あとでproject指定もいれる
    public List<Entry> findAll() {
        return ormaDatabase.selectFromEntry().orderBy(Entry_Schema.INSTANCE.date.getQualifiedName() + " " + OrderSpec.DESC).toList();
    }


    //--------------------------------------------------------------//
    //    -- Update --
    //--------------------------------------------------------------//

    public int updateDate(long id, long date) {
        return ormaDatabase.updateEntry().idEq(id).date(date).execute();
    }

    public int updateAccount(long id, Account account) {
        return ormaDatabase.updateEntry().idEq(id).account(account).execute();
    }

    public int updateReason(long id, Reason reason) {
        return ormaDatabase.updateEntry().idEq(id).reason(reason).execute();
    }

    public int updateMemo(long id, String memo) {
        return ormaDatabase.updateEntry().idEq(id).memo(memo).execute();
    }

    public int updatePrice(long id, long price) {
        return ormaDatabase.updateEntry().idEq(id).price(price).execute();
    }


    //--------------------------------------------------------------//
    //    -- Delete --
    //--------------------------------------------------------------//

    public int delete(long id) {
        return ormaDatabase.deleteFromEntry().idEq(id).execute();
    }

}
