package com.example.taxnoteandroid.model;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

@Table
public class Recurring {

    @PrimaryKey(autoincrement = true)
    public long id;

    @Column
    public long order;

    @Column
    public long dateIndex;

    @Column
    public long price;

    @Column
    public boolean deleted;

    @Column
    public boolean isExpense;

    @Column
    public boolean needSave = true;

    @Column
    public boolean needSync;

    @Column
    public String uuid;

    @Column
    public String timezone;

    @Column
    public String memo;


    // HasOne Relation
    @Column(indexed = true)
    public Project project;

    @Column(indexed = true)
    public Reason reason;

    @Column(indexed = true)
    public Account account;
}