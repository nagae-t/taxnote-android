package com.example.taxnoteandroid.model;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

@Table
public class Entry {

    @PrimaryKey(autoincrement = true)
    public long id;
    @Column
    public long date;
    @Column
    public boolean deleted;
    @Column
    public boolean isExpense;
    @Column
    public String memo;
    @Column
    public boolean needSave = true;
    @Column
    public boolean needSync;
    @Column
    public long price;
    @Column
    public String uuid;
    @Column
    public long updated;
}