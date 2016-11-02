package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.BuildConfig;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.example.taxnoteandroid.model.Reason;
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

    public Reason findById(long id) {
        return ormaDatabase.selectFromReason().idEq(id).value();
    }

    //@@ 引数を　isExpenseにして、　ここで　currentProjectを sharedPreferenceからもってきて findAllしたい
    public List<Reason> findAll() {
        return ormaDatabase.selectFromReason().toList();
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