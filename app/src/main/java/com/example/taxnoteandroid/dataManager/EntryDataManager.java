package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.BuildConfig;
import com.example.taxnoteandroid.model.Entry;
import com.example.taxnoteandroid.model.Entry_Schema;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.github.gfx.android.orma.AccessThreadConstraint;
import com.github.gfx.android.orma.OrderSpec;

import java.util.List;

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
        return ormaDatabase.selectFromEntry().where("uuid = ?", uuid).valueOrNull();
    }

    //@@ あとでproject指定もいれる
    public List<Entry> findAll() {
        return ormaDatabase.selectFromEntry().orderBy(Entry_Schema.INSTANCE.date.getQualifiedName() + " " + OrderSpec.DESC).toList();
    }


    //--------------------------------------------------------------//
    //    -- Update --
    //--------------------------------------------------------------//

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
