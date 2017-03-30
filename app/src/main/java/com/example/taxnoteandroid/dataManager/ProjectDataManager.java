package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.Library.taxnote.TNApiModel;
import com.example.taxnoteandroid.Library.taxnote.TNApiUser;
import com.example.taxnoteandroid.TaxnoteApp;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Project_Schema;
import com.example.taxnoteandroid.model.Project_Updater;

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

    public Project findCurrentProjectWithContext() {

        String currentProjectUuid   = SharedPreferencesManager.getUuidForCurrentProject(mContext);
        Project project             = findByUuid(currentProjectUuid);
        return project;
    }

    public Project findByUuid(String uuid) {
        return ormaDatabase.selectFromProject().uuidEq(uuid).valueOrNull();
    }

    public boolean getDecimalStatusWithContect() {

        String currentProjectUuid   = SharedPreferencesManager.getUuidForCurrentProject(mContext);
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
                .where(Project_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .toList();


        return projectList;
    }

    public List<Project> findAllNeedSave(boolean isNeedSave) {
        int needSave = (isNeedSave) ? 1 : 0;
        List<Project> projectList = ormaDatabase.selectFromProject()
                .where(Project_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .where(Project_Schema.INSTANCE.needSave.getQualifiedName() + " = " + needSave)
                .toList();
        return projectList;
    }

    public List<Project> findAllNeedSync(boolean isNeedSync) {
        int needSync = (isNeedSync) ? 1 : 0;
        List<Project> projectList = ormaDatabase.selectFromProject()
                .where(Project_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .where(Project_Schema.INSTANCE.needSync.getQualifiedName() + " = " + needSync)
                .toList();
        return projectList;
    }

    public List<Project> findAllDeleted(boolean isDeleted) {
        int deleted = (isDeleted) ? 1 : 0;
        List<Project> projectList = ormaDatabase.selectFromProject()
                .where(Project_Schema.INSTANCE.deleted.getQualifiedName() + " = " + deleted)
                .toList();
        return projectList;
    }


    //--------------------------------------------------------------//
    //    -- Update --
    //--------------------------------------------------------------//

    public int updateAccountUuidForExpense(Project project) {
        return ormaDatabase.updateProject().idEq(project.id)
                .accountUuidForExpense(project.accountUuidForExpense)
                .needSync(true)
                .execute();
    }

    public int updateAccountUuidForIncome(Project project) {
        return ormaDatabase.updateProject().idEq(project.id)
                .accountUuidForIncome(project.accountUuidForIncome)
                .needSync(true)
                .execute();
    }

    public int updateDecimal(Project project, boolean decimalStatus) {
        return ormaDatabase.updateProject().idEq(project.id)
                .decimal(decimalStatus)
                .needSync(true)
                .execute();
    }

    public void updateName(Project project) {
        ormaDatabase.updateProject()
                .idEq(project.id)
                .name(project.name)
                .needSync(true)
                .execute();
    }

    public int updateNeedSave(long id, boolean needSave) {
        return ormaDatabase.updateProject().idEq(id).needSave(needSave).execute();
    }

    public int updateNeedSync(long id, boolean needSync) {
        return ormaDatabase.updateProject().idEq(id).needSync(needSync).execute();
    }

    public void update(Project project) {
        Project_Updater updater = ormaDatabase.updateProject();
        updater.idEq(project.id)
                .order(project.order)
                .decimal(project.decimal)
                .deleted(project.deleted)
                .needSave(project.needSave)
                .needSync(project.needSync)
                .uuid(project.uuid)
                .name(project.name)
                .accountUuidForExpense(project.accountUuidForExpense)
                .accountUuidForIncome(project.accountUuidForIncome)
                .execute();
    }

    public void updateSetDeleted(String uuid, TNApiModel apiModel) {
        boolean isLoggingIn = TNApiUser.isLoggingIn(mContext);
        Project project = findByUuid(uuid);
        if (project == null) return;

        if (isLoggingIn) {
            ormaDatabase.updateProject().uuidEq(uuid)
                    .deleted(true)
                    .execute();

            // send api
            apiModel.deleteProject(uuid, null);
        } else {
            delete(project.id);
        }

    }


    //--------------------------------------------------------------//
    //    -- Delete --
    //--------------------------------------------------------------//

    public int delete(long id) {
        return ormaDatabase.deleteFromProject().idEq(id).execute();
    }

    public int delete(String uuid) {
        return ormaDatabase.deleteFromProject().uuidEq(uuid).execute();
    }
}
