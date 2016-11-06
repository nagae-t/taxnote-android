package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.BuildConfig;
import com.example.taxnoteandroid.model.Account;
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
        return ormaDatabase.selectFromAccount().uuidEq(uuid).value();
    }

    public List<Account> findAllByIsExpense(boolean isExpense) {
        return ormaDatabase.selectFromAccount().where("deleted = false AND ORDER BY order AND isExpense = ?", isExpense).toList();
    }

    //@@
    public Account findCurrentSelectedAccount(Context context, boolean isExpense) {

        Account account;

        // Get the current project
        String currentProjectUuid               = SharedPreferencesManager.getUuidForCurrentProject(context);
        ProjectDataManager projectDataManager   = new ProjectDataManager(context);
        Project project                         = projectDataManager.findByUuid(currentProjectUuid);

        if (isExpense) {
            account = findByUuid(project.accountUuidForExpense);
        } else  {
            account = findByUuid(project.accountUuidForIncome);
        }

        if (account != null) {
            return account;
        }

        List<Account> accounts  = findAllByIsExpense(isExpense);
        if (accounts != null && accounts.size() > 0) {
            account = accounts.get(0);
        }

        return account;
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