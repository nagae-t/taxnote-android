package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.TaxnoteApp;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Account_Schema;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.example.taxnoteandroid.model.Project;

import java.util.List;

public class AccountDataManager {

    private OrmaDatabase ormaDatabase;

    public AccountDataManager(Context context) {
        ormaDatabase = TaxnoteApp.getOrmaDatabase();
    }


    //--------------------------------------------------------------//
    //    -- Create --
    //--------------------------------------------------------------//

    public static boolean isSaveSuccess(long id) {
        return id != -1;
    }

    public long save(Account account) {
        return ormaDatabase.insertIntoAccount(account);
    }


    //--------------------------------------------------------------//
    //    -- Read --
    //--------------------------------------------------------------//

    public List<Account> findAll() {
        List<Account> dataList = ormaDatabase.selectFromAccount().toList();

        return dataList;
    }

    public Account findByUuid(String uuid) {
        return ormaDatabase.selectFromAccount().uuidEq(uuid).valueOrNull();
    }

    public List<Account> findAllWithIsExpense(boolean isExpense, Context context) {

        // Get the current project
        ProjectDataManager projectDataManager = new ProjectDataManager(context);
        Project project = projectDataManager.findCurrentProjectWithContext();

        List accounts = ormaDatabase.selectFromAccount().where(Account_Schema.INSTANCE.deleted.getQualifiedName() + " = 0  AND "
                        + Account_Schema.INSTANCE.isExpense.getQualifiedName() + " = ?",
                isExpense)
                .and()
                .projectEq(project)
                .orderBy(Account_Schema.INSTANCE.order.getQualifiedName())
                .toList();

        return accounts;
    }

    public Account findCurrentSelectedAccount(Context context, boolean isExpense) {

        Account account;

        // Get the current project
        ProjectDataManager projectDataManager = new ProjectDataManager(context);
        Project project = projectDataManager.findCurrentProjectWithContext();

        // Get the current selected account
        if (isExpense) {
            account = findByUuid(project.accountUuidForExpense);
        } else {
            account = findByUuid(project.accountUuidForIncome);
        }

        if (account != null) {
            return account;
        }

        // Get the first account from account list
        List<Account> accounts = findAllWithIsExpense(isExpense, context);
        if (accounts != null && accounts.size() > 0) {
            account = accounts.get(0);
        }

        return account;
    }


    //--------------------------------------------------------------//
    //    -- Update --
    //--------------------------------------------------------------//

    public int updateName(long id, String name) {
        return ormaDatabase.updateAccount().idEq(id).name(name).execute();
    }


    //--------------------------------------------------------------//
    //    -- Delete --
    //--------------------------------------------------------------//

    public int delete(long id) {
        return ormaDatabase.deleteFromAccount().idEq(id).execute();
    }

    //--------------------------------------------------------------//
    //    -- Change order --
    //--------------------------------------------------------------//

    public int updateOrder(long id, int order) {
        return ormaDatabase.updateAccount().idEq(id).order(order).execute(); // 2017/01/30 E.Nozaki
    }
}