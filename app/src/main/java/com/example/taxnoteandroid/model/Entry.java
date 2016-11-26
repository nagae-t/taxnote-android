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
    public long updated;

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

    @Column(unique = true)
    public String uuid;

    @Column
    public String memo = "";


    // HasOne Relation
    @Column(indexed = true)
    public Project project;

    @Column(indexed = true)
    public Reason reason;

    @Column(indexed = true)
    public Account account;

    @Override
    public String toString() {
        return "Entry{" +
                "id=" + id +
                ", date=" + date +
                ", updated=" + updated +
                ", price=" + price +
                ", deleted=" + deleted +
                ", isExpense=" + isExpense +
                ", needSave=" + needSave +
                ", needSync=" + needSync +
                ", uuid='" + uuid + '\'' +
                ", memo='" + memo + '\'' +
                ", project=" + project +
                ", reason=" + reason +
                ", account=" + account +
                '}';
    }
}
