package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.BuildConfig;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Account_Schema;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.example.taxnoteandroid.model.Project;
import com.github.gfx.android.orma.AccessThreadConstraint;

import java.util.List;

import static java.util.stream.Collectors.toList;

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

    public List<Account> findAllByIsExpense(boolean isExpense) {
        return ormaDatabase.selectFromAccount()
                .where(Account_Schema.INSTANCE.deleted.getQualifiedName() + " = 0  AND " + Account_Schema.INSTANCE.isExpense.getQualifiedName() + " = ?", isExpense)
                .orderBy(Account_Schema.INSTANCE.order.getQualifiedName()).toList();
    }

    public Account findCurrentSelectedAccount(Context context, boolean isExpense) {

        Account account;

        // Get the current project
        String currentProjectUuid               = SharedPreferencesManager.getUuidForCurrentProject(context);
        ProjectDataManager projectDataManager   = new ProjectDataManager(context);
        Project project                         = projectDataManager.findByUuid(currentProjectUuid);

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
        List<Account> accounts  = findAllByIsExpense(isExpense);
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