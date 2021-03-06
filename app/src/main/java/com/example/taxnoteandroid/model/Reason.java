package com.example.taxnoteandroid.model;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

import org.parceler.Parcel;

/**
 * 勘定科目選択のモデル
 *
 */

@Parcel
@Table
public class Reason {

    @PrimaryKey(autoincrement = true)
    public long id;

    @Column
    public long order;

    @Column
    public boolean deleted;

    @Column
    public boolean isExpense;

    @Column
    public boolean needSave = true;

    @Column
    public boolean needSync;

    @Column
    public String name;

    @Column
    public String details;

    @Column(unique = true, indexed = true)
    public String uuid;


    // HasOne Relation
    @Column(indexed = true)
    public Project project;


    // HasMany Relation
    public Entry_Relation getEntries(OrmaDatabase ormaDatabase) {
        return ormaDatabase.relationOfEntry().reasonEq(this);
    }

    public Summary_Relation getSummaries(OrmaDatabase ormaDatabase){
        return ormaDatabase.relationOfSummary().reasonEq(this);
    }

    public Recurring_Relation getRecurrings(OrmaDatabase ormaDatabase) {
        return ormaDatabase.relationOfRecurring().reasonEq(this);
    }
}
