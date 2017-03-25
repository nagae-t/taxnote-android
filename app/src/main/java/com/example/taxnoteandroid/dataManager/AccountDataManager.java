package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.TaxnoteApp;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Account_Schema;
import com.example.taxnoteandroid.model.Account_Updater;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason;
import com.example.taxnoteandroid.model.Reason_Schema;

import java.util.List;

public class AccountDataManager {

    private OrmaDatabase ormaDatabase;
    private Context mContext;

    public AccountDataManager(Context context) {
        this.mContext = context;
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

    public List<Account> findAllWithIsExpense(boolean isExpense) {

        // Get the current project
        ProjectDataManager projectDataManager = new ProjectDataManager(mContext);
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

    public Account findCurrentSelectedAccount(boolean isExpense) {

        Account account;

        // Get the current project
        ProjectDataManager projectDataManager = new ProjectDataManager(mContext);
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
        List<Account> accounts = findAllWithIsExpense(isExpense);
        if (accounts != null && accounts.size() > 0) {
            account = accounts.get(0);
        }

        return account;
    }

    public List<Account> findAllNeedSave(boolean isNeedSave) {
        int needSave = (isNeedSave) ? 1 : 0;
        List<Account> accounts = ormaDatabase.selectFromAccount()
                .where(Account_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .and()
                .where(Account_Schema.INSTANCE.needSave.getQualifiedName() + " = " + needSave)
                .toList();
        return accounts;
    }


    //--------------------------------------------------------------//
    //    -- Update --
    //--------------------------------------------------------------//

    public int updateName(long id, String name) {
        return ormaDatabase.updateAccount().idEq(id).name(name).execute();
    }

    public int updateNeedSave(long id, boolean needSave) {
        return ormaDatabase.updateAccount().idEq(id).needSave(needSave).execute();
    }

    public void update(Account account) {
        Account_Updater updater = ormaDatabase.updateAccount();
        updater.idEq(account.id)
                .order(account.order)
                .deleted(account.deleted)
                .isExpense(account.isExpense)
                .needSave(account.needSave)
                .needSync(account.needSync)
                .uuid(account.uuid)
                .name(account.name)
                .project(account.project)
                .execute();
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