package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.BuildConfig;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Account_Schema;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.example.taxnoteandroid.model.Project;
import com.github.gfx.android.orma.AccessThreadConstraint;

import java.util.List;

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

    public Account findByUuid(String uuid) {
        return ormaDatabase.selectFromAccount().uuidEq(uuid).valueOrNull();
    }

    public List<Account> findAllWithIsExpense(boolean isExpense, Context context) {

        // Get the current project
        ProjectDataManager projectDataManager   = new ProjectDataManager(context);
//        Project project                         = projectDataManager.findCurrentProjectWithContext(context);

        //@@ projectを指定すると落ちる
//        List accounts = ormaDatabase.selectFromAccount().where(
//                Account_Schema.INSTANCE.deleted.getQualifiedName() + " = 0  AND " +
//                        Account_Schema.INSTANCE.isExpense.getQualifiedName() + " = ? AND " +
//                        Account_Schema.INSTANCE.project.getQualifiedName() + " = ?",
//                isExpense, project).
//                orderBy(Account_Schema.INSTANCE.order.getQualifiedName()).
//                toList();

        //@@ projectを指定すると落ちる
        List accounts = ormaDatabase.selectFromAccount().where(
                Account_Schema.INSTANCE.deleted.getQualifiedName() + " = 0  AND " +
                        Account_Schema.INSTANCE.isExpense.getQualifiedName() + " = ?",isExpense).orderBy(Account_Schema.INSTANCE.order.getQualifiedName()).toList();

        return accounts;
    }

    public Account findCurrentSelectedAccount(Context context, boolean isExpense) {

        Account account;

        // Get the current project
        ProjectDataManager projectDataManager   = new ProjectDataManager(context);
        Project project                         = projectDataManager.findCurrentProjectWithContext(context);

        // Get the current selected account
        if (isExpense) {
            account = findByUuid(project.accountUuidForExpense);
        } else  {
            account = findByUuid(project.accountUuidForIncome);
        }

        if (account != null) {
            return account;
        }

        // Get the first account from account list
        List<Account> accounts  = findAllWithIsExpense(isExpense, context);
        if (accounts != null && accounts.size() > 0) {
            account = accounts.get(0);
        }

        return account;
    }


    //--------------------------------------------------------------//
    //    -- Update --
    //--------------------------------------------------------------//




    //--------------------------------------------------------------//
    //    -- Delete --
    //--------------------------------------------------------------//

    public int delete(long id) {
        return ormaDatabase.deleteFromAccount().idEq(id).execute();
    }
}