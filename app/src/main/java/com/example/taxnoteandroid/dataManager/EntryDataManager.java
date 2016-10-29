package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.BuildConfig;
import com.example.taxnoteandroid.model.Entry;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.github.gfx.android.orma.AccessThreadConstraint;

public class EntryDataManager {

    private OrmaDatabase ormaDatabase;

    public EntryDataManager(Context context) {
        // Ormaの初期設定
        ormaDatabase = OrmaDatabase.builder(context)
                .trace(BuildConfig.DEBUG)
                .writeOnMainThread(AccessThreadConstraint.NONE)
                .readOnMainThread(AccessThreadConstraint.NONE)
                .build();
    }

    public long save(Entry entry) {
        return ormaDatabase.insertIntoEntry(entry);
    }

    public Entry findById(long id) {
        return ormaDatabase.selectFromEntry().idEq(id).value();
    }

    public int delete(long id) {
        return ormaDatabase.deleteFromEntry().idEq(id).execute();
    }

    public int updatePrice(long id, long price) {
        return ormaDatabase.updateEntry().idEq(id).price(price).execute();
    }

    public static boolean isSaveSuccess(long id) {
        return id != -1;
    }
}
