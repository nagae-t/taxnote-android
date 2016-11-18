package com.example.taxnoteandroid.model;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

import org.parceler.Parcel;

@Parcel
@Table
public class Summary {

    @PrimaryKey(autoincrement = true)
    public long id;

    @Column
    public long order;

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


    // HasOne Relation
    @Column(indexed = true)
    public Project project;

    @Column(indexed = true)
    public Reason reason;
}
