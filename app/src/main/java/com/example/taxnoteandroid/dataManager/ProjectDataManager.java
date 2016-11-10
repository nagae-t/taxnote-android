package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.BuildConfig;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.example.taxnoteandroid.model.Project;
import com.github.gfx.android.orma.AccessThreadConstraint;

public class ProjectDataManager {

    private OrmaDatabase ormaDatabase;

    public ProjectDataManager(Context context) {
        // Ormaの初期設定
        ormaDatabase = OrmaDatabase.builder(context)
                .trace(BuildConfig.DEBUG)
                .writeOnMainThread(AccessThreadConstraint.NONE)
                .readOnMainThread(AccessThreadConstraint.NONE)
                .build();
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


    //--------------------------------------------------------------//
    //    -- Update --
    //--------------------------------------------------------------//

    public int updateAccountUuidForExpense(Project project) {
        return ormaDatabase.updateProject().idEq(project.id).accountUuidForExpense(project.accountUuidForExpense).execute();
    }

    public int updateAccountUuidForIncome(Project project) {
        return ormaDatabase.updateProject().idEq(project.id).accountUuidForIncome(project.accountUuidForIncome).execute();
    }


    //--------------------------------------------------------------//
    //    -- Delete --
    //--------------------------------------------------------------//

    public int delete(long id) {
        return ormaDatabase.deleteFromProject().idEq(id).execute();
    }
}
