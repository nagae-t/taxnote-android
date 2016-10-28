package com.example.taxnoteandroid.model;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

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
    @Column
    public String uuid;
}