package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.TaxnoteApp;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Project_Schema;

import java.util.List;


public class ProjectDataManager {

    private OrmaDatabase ormaDatabase;
    private Context mContext;

    public ProjectDataManager(Context context) {
        this.mContext = context;
        this.ormaDatabase = TaxnoteApp.getOrmaDatabase();
        ormaDatabase.selectFromProject().count();
    }

    public int allSize() {
        return ormaDatabase.selectFromProject().count();
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

    public List<Project> findAll() {
        List<Project> projectList = ormaDatabase.selectFromProject().toList();

        return projectList;
    }

    public List<Project> findAll(boolean isMaster) {
        int master = (isMaster) ? 1 : 0;
        List<Project> projectList = ormaDatabase.selectFromProject()
                .where(Project_Schema.INSTANCE.isMaster.getQualifiedName() + " = " + master)
                .toList();


        return projectList;
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
