package com.example.taxnoteandroid.model;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

import org.parceler.Parcel;

/**
 * アカウントだ！（全体データの扱いを分けるため）
 */

@Parcel
@Table
public class Project {

    @PrimaryKey(autoincrement = true)
    public long id;

    @Column
    public long order;

    @Column
    public boolean isMaster;

    @Column
    public boolean decimal;

    @Column(defaultExpr = "0")
    public boolean passcode;

    @Column
    public boolean deleted;

    @Column
    public boolean needSave = true;

    @Column
    public boolean needSync;

    @Column(unique = true, indexed = true)
    public String uuid;

    @Column
    public String name;

    @Column
    public String accountUuidForExpense;

    @Column
    public String accountUuidForIncome;


    // HasMany Relation
    public Account_Relation getAccounts(OrmaDatabase ormaDatabase) {
        return ormaDatabase.relationOfAccount().projectEq(this);
    }

    public Reason_Relation getReasons(OrmaDatabase ormaDatabase) {
        return ormaDatabase.relationOfReason().projectEq(this);
    }

    public Summary_Relation getSummaries(OrmaDatabase ormaDatabase) {
        return ormaDatabase.relationOfSummary().projectEq(this);
    }

    public Entry_Relation getEntries(OrmaDatabase ormaDatabase) {
        return ormaDatabase.relationOfEntry().projectEq(this);
    }

    public Recurring_Relation getRecurrings(OrmaDatabase ormaDatabase) {
        return ormaDatabase.relationOfRecurring().projectEq(this);
    }
}
