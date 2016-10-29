package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.BuildConfig;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.example.taxnoteandroid.model.Reason;
import com.github.gfx.android.orma.AccessThreadConstraint;

import java.util.List;

public class ReasonDataManager {

    private OrmaDatabase ormaDatabase;

    public ReasonDataManager(Context context) {
        // Ormaの初期設定
        ormaDatabase = OrmaDatabase.builder(context)
                .trace(BuildConfig.DEBUG)
                .writeOnMainThread(AccessThreadConstraint.NONE)
                .readOnMainThread(AccessThreadConstraint.NONE)
                .build();
    }

    public long save(Reason reason) {
        return ormaDatabase.insertIntoReason(reason);
    }

    public List<Reason> findAll() {
        return ormaDatabase.selectFromReason().toList();
    }
}
