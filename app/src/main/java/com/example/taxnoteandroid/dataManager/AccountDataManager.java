package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.BuildConfig;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.github.gfx.android.orma.AccessThreadConstraint;

public class AccountDataManager {

    private OrmaDatabase ormaDatabase;

    public AccountDataManager(Context context) {
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

    public long save(Account account) {
        return ormaDatabase.insertIntoAccount(account);
    }

    public static boolean isSaveSuccess(long id) {
        return id != -1;
    }


    //--------------------------------------------------------------//
    //    -- Read --
    //--------------------------------------------------------------//

    public Account findById(long id) {
        return ormaDatabase.selectFromAccount().idEq(id).value();
    }


    //--------------------------------------------------------------//
    //    -- Update --
    //--------------------------------------------------------------//

    public int updateAccount(Account account) {
        return ormaDatabase.updateAccount().idEq(account.id).execute();
    }


    //--------------------------------------------------------------//
    //    -- Delete --
    //--------------------------------------------------------------//

    public int delete(long id) {
        return ormaDatabase.deleteFromAccount().idEq(id).execute();
    }
}