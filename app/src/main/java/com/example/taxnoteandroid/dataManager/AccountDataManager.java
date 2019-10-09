package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.Library.taxnote.TNApiModel;
import com.example.taxnoteandroid.Library.taxnote.TNApiUser;
import com.example.taxnoteandroid.TaxnoteApp;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Account_Schema;
import com.example.taxnoteandroid.model.Account_Updater;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason_Schema;

import java.util.List;

public class AccountDataManager {

    private OrmaDatabase ormaDatabase;
    private Context mContext;
    private Project mCurrentProject;

    public AccountDataManager(Context context) {
        this.mContext = context;
        ormaDatabase = TaxnoteApp.getOrmaDatabase();
        mCurrentProject = new ProjectDataManager(context).findCurrent();
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
        List<Account> dataList = ormaDatabase.selectFromAccount()
                .projectEq(mCurrentProject)
                .toList();

        return dataList;
    }

    public Account findByUuid(String uuid) {
        return ormaDatabase.selectFromAccount().uuidEq(uuid).valueOrNull();
    }

    public Account findByName(String name) {
        return ormaDatabase.selectFromAccount().projectEq(mCurrentProject)
                .where(Account_Schema.INSTANCE.name.getQualifiedName() + " = ?", name)
                .valueOrNull();
    }

    public List<Account> findAllWithIsExpense(boolean isExpense) {

        List accounts = ormaDatabase.selectFromAccount()
                .projectEq(mCurrentProject)
                .where(Account_Schema.INSTANCE.deleted.getQualifiedName() + " = 0 AND "
                        + Account_Schema.INSTANCE.isExpense.getQualifiedName() + " = ?", isExpense)
                .and()
                .orderBy(Account_Schema.INSTANCE.order.getQualifiedName())
                .toList();

        return accounts;
    }

    public Account findCurrentSelectedAccount(boolean isExpense) {

        Account account;

        // Get the current selected account
        if (isExpense) {
            account = findByUuid(mCurrentProject.accountUuidForExpense);
        } else {
            account = findByUuid(mCurrentProject.accountUuidForIncome);
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
                .projectEq(mCurrentProject)
                .where(Account_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .and()
                .where(Account_Schema.INSTANCE.needSave.getQualifiedName() + " = " + needSave)
                .toList();
        return accounts;
    }

    public List<Account> findAllNeedSync(boolean isNeedSync) {
        int needSync = (isNeedSync) ? 1 : 0;
        List<Account> accounts = ormaDatabase.selectFromAccount()
                .projectEq(mCurrentProject)
                .where(Account_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .and()
                .where(Account_Schema.INSTANCE.needSync.getQualifiedName() + " = " + needSync)
                .toList();
        return accounts;
    }

    public List<Account> findAllDeleted(boolean isDeleted) {
        int deleted = (isDeleted) ? 1 : 0;
        List<Account> accounts = ormaDatabase.selectFromAccount()
                .projectEq(mCurrentProject)
                .where(Account_Schema.INSTANCE.deleted.getQualifiedName() + " = " + deleted)
                .toList();
        return accounts;
    }


    //--------------------------------------------------------------//
    //    -- Update --
    //--------------------------------------------------------------//

    public int updateName(long id, String name) {
        return ormaDatabase.updateAccount().idEq(id)
                .name(name)
                .needSync(true)
                .execute();
    }

    public int updateOrder(long id, int order) {
        return ormaDatabase.updateAccount().idEq(id)
                .order(order)
                .needSync(true)
                .execute();
    }

    public int updateNeedSave(long id, boolean needSave) {
        return ormaDatabase.updateAccount().idEq(id).needSave(needSave).execute();
    }

    public int updateNeedSync(long id, boolean needSync) {
        return ormaDatabase.updateAccount().idEq(id).needSync(needSync).execute();
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

    public void updateSetDeleted(String uuid, TNApiModel apiModel) {
        boolean isLoggingIn = TNApiUser.isLoggingIn(mContext);
        Account account = findByUuid(uuid);
        if (account == null) return;

        if (isLoggingIn) {
            ormaDatabase.updateAccount().uuidEq(uuid)
                    .deleted(true)
                    .execute();

            // send api
            apiModel.deleteAccount(uuid, null);
        } else {
            delete(account.id);
        }

    }


    //--------------------------------------------------------------//
    //    -- Delete --
    //--------------------------------------------------------------//

    public int delete(long id) {
        return ormaDatabase.deleteFromAccount().idEq(id).execute();
    }

    public int delete(String uuid) {
        return ormaDatabase.deleteFromAccount().uuidEq(uuid).execute();
    }

}