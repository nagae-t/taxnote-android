package com.example.taxnoteandroid.model;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

import org.parceler.Parcel;

@Parcel
@Table
public class Account {

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

    @Column(unique = true, indexed = true)
    public String uuid;

    @Column
    public String name;


    // HasOne Relation
    @Column(indexed = true)
    public Project project;


    // HasMany Relation
    public Entry_Relation getEntries(OrmaDatabase ormaDatabase) {
        return ormaDatabase.relationOfEntry().accountEq(this);
    }

    public Recurring_Relation getRecurrings(OrmaDatabase ormaDatabase) {
        return ormaDatabase.relationOfRecurring().accountEq(this);
    }
}
