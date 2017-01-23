package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.TaxnoteApp;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.example.taxnoteandroid.model.Recurring;

public class RecurringDataManager {

    private OrmaDatabase ormaDatabase;

    public RecurringDataManager(Context context) {
      ormaDatabase = TaxnoteApp.getOrmaDatabase();
    }


    //--------------------------------------------------------------//
    //    -- Create --
    //--------------------------------------------------------------//

    public long save(Recurring recurring) {
        return ormaDatabase.insertIntoRecurring(recurring);
    }

    public static boolean isSaveSuccess(long id) {
        return id != -1;
    }


    //--------------------------------------------------------------//
    //    -- Read --
    //--------------------------------------------------------------//

    public Recurring findByUuid(String uuid) {
        return ormaDatabase.selectFromRecurring().uuidEq(uuid).valueOrNull();
    }


    //--------------------------------------------------------------//
    //    -- Update --
    //--------------------------------------------------------------//

    public int updateRecurring(Recurring recurring) {
        return ormaDatabase.updateRecurring().idEq(recurring.id).execute();
    }


    //--------------------------------------------------------------//
    //    -- Delete --
    //--------------------------------------------------------------//

    public int delete(long id) {
        return ormaDatabase.deleteFromRecurring().idEq(id).execute();
    }
}