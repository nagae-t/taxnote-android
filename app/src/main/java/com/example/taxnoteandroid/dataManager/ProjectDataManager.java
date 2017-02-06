package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.TaxnoteApp;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.example.taxnoteandroid.model.Project;

public class ProjectDataManager {

    private OrmaDatabase ormaDatabase;

    public ProjectDataManager(Context context) {
      ormaDatabase = TaxnoteApp.getOrmaDatabase();
    }


    //--------------------------------------------------------------//
    //    -- Create --
    //--------------------------------------------------------------//

    public long save(Project project) {
        return ormaDatabase.insertIntoProject(project);
    }

    public static boolean isSaveSuccess(long id) {
        return id != -1;
    }


    //--------------------------------------------------------------//
    //    -- Read --
    //--------------------------------------------------------------//

    public Project findCurrentProjectWithContext(Context context) {

        String currentProjectUuid   = SharedPreferencesManager.getUuidForCurrentProject(context);
        Project project             = findByUuid(currentProjectUuid);
        return project;
    }

    public Project findByUuid(String uuid) {
        return ormaDatabase.selectFromProject().uuidEq(uuid).valueOrNull();
    }

    public boolean getDecimalStatusWithContect(Context context) {

        String currentProjectUuid   = SharedPreferencesManager.getUuidForCurrentProject(context);
        Project project             = findByUuid(currentProjectUuid);
        return project.decimal;
    }


    //--------------------------------------------------------------//
    //    -- Update --
    //--------------------------------------------------------------//

    public int updateAccountUuidForExpense(Project project) {
        return ormaDatabase.updateProject().idEq(project.id).accountUuidForExpense(project.accountUuidForExpense).execute();
    }

    public int updateAccountUuidForIncome(Project project) {
        return ormaDatabase.updateProject().idEq(project.id).accountUuidForIncome(project.accountUuidForIncome).execute();
    }

    public int updateDecimal(Project project, boolean decimalStatus) {
        return ormaDatabase.updateProject().idEq(project.id).decimal(decimalStatus).execute();
    }


    //--------------------------------------------------------------//
    //    -- Delete --
    //--------------------------------------------------------------//

    public int delete(long id) {
        return ormaDatabase.deleteFromProject().idEq(id).execute();
    }
}
