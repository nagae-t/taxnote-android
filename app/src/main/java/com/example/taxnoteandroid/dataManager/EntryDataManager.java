package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.BuildConfig;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Entry;
import com.example.taxnoteandroid.model.Entry_Schema;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason;
import com.github.gfx.android.orma.AccessThreadConstraint;
import com.github.gfx.android.orma.OrderSpec;

import java.util.List;

public class EntryDataManager {

    private OrmaDatabase ormaDatabase;

    public EntryDataManager(Context context) {

        // Init Orma
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
        return ormaDatabase.selectFromEntry().where(Entry_Schema.INSTANCE.uuid.getQualifiedName() + " = ?", uuid).valueOrNull();
    }

    public List<Entry> findAll(Context context) {

        ProjectDataManager projectDataManager   = new ProjectDataManager(context);
        Project project                         = projectDataManager.findCurrentProjectWithContext(context);

        List entries = ormaDatabase.selectFromEntry().
                where(Entry_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .and()
                .projectEq(project)
                .orderBy(Entry_Schema.INSTANCE.date.getQualifiedName() + " " + OrderSpec.DESC)
                .toList();

        return entries;
    }

    public Entry hasReasonInEntryData(Reason reason) {
        return ormaDatabase.selectFromEntry().and().reasonEq(reason).valueOrNull();
    }

    public Entry hasAccountInEntryData(Account account) {
        return ormaDatabase.selectFromEntry().and().accountEq(account).valueOrNull();
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
