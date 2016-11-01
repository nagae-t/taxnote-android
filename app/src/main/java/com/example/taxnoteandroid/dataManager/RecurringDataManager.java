package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.BuildConfig;
import com.example.taxnoteandroid.model.Recurring;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.github.gfx.android.orma.AccessThreadConstraint;

public class RecurringDataManager {

    private OrmaDatabase ormaDatabase;

    public RecurringDataManager(Context context) {
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

    public long save(Recurring recurring) {
        return ormaDatabase.insertIntoRecurring(recurring);
    }

    public static boolean isSaveSuccess(long id) {
        return id != -1;
    }


    //--------------------------------------------------------------//
    //    -- Read --
    //--------------------------------------------------------------//

    public Recurring findById(long id) {
        return ormaDatabase.selectFromRecurring().idEq(id).value();
    }


    //--------------------------------------------------------------//
    //    -- Update --
    //--------------------------------------------------------------//

    public int updateRecurring(Recurring recurring) {
        return ormaDatabase.updateRecurring().idEq(recurring.id).execute();
    }


    //--------------------------------------------------------------//
    //    -- Delete --
    //--------------------------------------------------------------//

    public int delete(long id) {
        return ormaDatabase.deleteFromRecurring().idEq(id).execute();
    }
}