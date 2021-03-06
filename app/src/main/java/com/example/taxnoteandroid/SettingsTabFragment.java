package com.example.taxnoteandroid;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.taxnoteandroid.Library.AsyncOkHttpClient;
import com.example.taxnoteandroid.Library.BroadcastUtil;
import com.example.taxnoteandroid.Library.DataExportManager;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.taxnote.TNApi;
import com.example.taxnoteandroid.Library.taxnote.TNApiModel;
import com.example.taxnoteandroid.Library.taxnote.TNApiUser;
import com.example.taxnoteandroid.dataManager.DefaultDataInstaller;
import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.FragmentSettingsTabBinding;
import com.example.taxnoteandroid.misc.CustomTabsUtils;
import com.example.taxnoteandroid.model.Project;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.Response;


public class SettingsTabFragment extends Fragment {

    private Context mContext;
    private ProjectDataManager mProjectDataManager;
    private FragmentSettingsTabBinding binding;
    private FragmentManager mFragmentManager;
    private LayoutInflater mInflater;
    private boolean isProjectEditing = false;
    private List<Project> mAllProjects;
    private Project mEditingProject;
    private Project mCurrentProject;

    private TNApiUser mApiUser;
    private TNApiModel mApiModel;

    private static int CREATE_FILE = 1;

    public SettingsTabFragment() {
        // Required empty public constructor
    }

    public static SettingsTabFragment newInstance() {

        SettingsTabFragment fragment = new SettingsTabFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSettingsTabBinding.inflate(inflater, container, false);
        binding.version.setText(BuildConfig.VERSION_NAME);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity().getApplicationContext();

        mFragmentManager = getFragmentManager();
        mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mProjectDataManager = new ProjectDataManager(mContext);

        setViews();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_FILE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null) {

                DataExportManager dataExportManager = new DataExportManager((AppCompatActivity) getActivity());
                String dataJsonString = dataExportManager.generateDbToJson();
                OutputStream outputStream = null;
                try {
                    outputStream = getActivity().getContentResolver().openOutputStream(uri);
                    if (outputStream != null) {
                        outputStream.write(dataJsonString.getBytes());
                    }
                } catch (Exception e) {
                    Log.e("ERROR", "data export : " + e.getMessage());
                    e.printStackTrace();
                }

                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ShareCompat.IntentBuilder.from(getActivity())
                        .setType("application/json")
                        .setChooserTitle("Backup file")
                        .setStream(uri)
                        .startChooser();
            }
        }
    }

    public void afterLogin() {
        mProjectDataManager = new ProjectDataManager(mContext);
        setMultipleProject();
    }

    //--------------------------------------------------------------//
    //    -- Display Part --
    //--------------------------------------------------------------//

    private void setViews() {
        // ??????????????????????????????
        binding.inputRecurringSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputRecurringListActivity.start(getActivity());
            }
        });

        setUpgradeView();
        setMultipleProject();
        setDecimalSwitch();
        setPasscodeSwitch();
        setAlertInputForget();
        setDataBackup();
        setHelpViews();
        setShareButton();

    }


    //--------------------------------------------------------------//
    //    -- Upgrade --
    //--------------------------------------------------------------//

    private void setUpgradeView() {

        binding.upgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UpgradeActivity.start(getContext());
            }
        });
    }


    //--------------------------------------------------------------//
    //    -- Multiple Project --
    //--------------------------------------------------------------//

    private void setMultipleProject() {
        final int MAX_PROJECT_COUNT = 6;
        mApiUser = new TNApiUser(mContext);
        mApiModel = new TNApiModel(mContext);

        mAllProjects = mProjectDataManager.findAllDeleted(false);
        binding.subProjectRadioLayout.removeAllViewsInLayout();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String addTitle = mContext.getString(R.string.add_new_project);
        String deleteOrEditNameTitle = mContext.getString(R.string.delete_project_or_edit_name);
        String[] items = {addTitle, deleteOrEditNameTitle};
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                int projectAllSize = mProjectDataManager.allSize();

                if (i == 0 && projectAllSize < MAX_PROJECT_COUNT) {
                    if (mApiUser.isCloudActive()) {
                        showProjectEditorDialog(ProjectEditorDialogFragment.TYPE_ADD_NEW);
                    } else {
                        showCloudConfirmAddNewProject();
                    }
                } else if (i == 0 && projectAllSize >= MAX_PROJECT_COUNT) {
                    // ????????????????????????????????????????????????????????????????????????????????????????????????
                    DialogManager.showToast(mContext, mContext.getString(R.string.max_add_project_message));
                } else if (i == 1) {
                    switchSubProjectEdit();
                }
            }
        });
        final AlertDialog menuDialog = builder.create();
        binding.settingsProjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isProjectEditing) {
                    switchSubProjectEdit();
                } else {
                    menuDialog.show();
                }
            }
        });
        binding.mainProjectRadio.setOnClickListener(projectRadioOnClick);
        CompoundButtonCompat.setButtonTintList(binding.mainProjectRadio,
                ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.primary)));
        for (Project project : mAllProjects) {
            if (project.isMaster && !project.name.equals("master")) {
                binding.mainProjectRadio.setText(project.name);
                break;
            }
        }

        if (mProjectDataManager.allSize() == 1) {
            mCurrentProject = mProjectDataManager.findCurrent();
            return;
        }
        // sub project ??????????????????
        List<Project> projects = mProjectDataManager.findAll(false);
        for (int i = 0; i < projects.size(); i++) {
            Project _pj = projects.get(i);
            _pj.order = i + 1;
            addSubProjectView(_pj);
        }

        // set current project radio checked
        checkCurrentProjectToRadio();
    }

    private void showCloudConfirmAddNewProject() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.taxnote_cloud_first_free)
                .setMessage(R.string.add_project_without_cloud)
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.benefits_of_upgrade, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        UpgradeActivity.start(getContext());
                    }
                }).setPositiveButton(R.string.add_new_project, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showProjectEditorDialog(ProjectEditorDialogFragment.TYPE_ADD_NEW);
                    }
                });
        builder.create().show();
    }

    private void showProjectEditorDialog(int dialogType) {
        ProjectEditorDialogFragment editDialog = ProjectEditorDialogFragment
                .newInstance(dialogType);
        editDialog.setOnSubmitListener(onProjectEditorSubmitListener);
        editDialog.show(mFragmentManager, null);
    }

    // ????????????????????????(??????)?????????????????????????????????????????????????????????????????????
    private ProjectEditorDialogFragment.OnEditorSubmitListener onProjectEditorSubmitListener
            = new ProjectEditorDialogFragment.OnEditorSubmitListener() {
        @Override
        public void onSubmit(DialogInterface dialogInterface,
                             int dialogType, EditText nameEdit, String tag) {
            if (nameEdit == null) return;

            String newName = nameEdit.getText().toString();

            // hide keyboard
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(nameEdit.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            switch (dialogType) {
                case ProjectEditorDialogFragment.TYPE_ADD_NEW: // ???????????????????????????

                    int projectSize = mProjectDataManager.allSize();
                    Project newProject = DefaultDataInstaller.addNewProjectByName(mContext, newName, projectSize);
                    addSubProjectView(newProject);
                    mAllProjects = mProjectDataManager.findAllDeleted(false);
                    checkCurrentProjectToRadio();

                    // show message
                    DialogManager.showToast(mContext, mContext.getString(R.string.created_new_project_message));

                    //@@ ??????????????????????????????current???????????????????????????????????????
                    // ??????????????????????????????????????????????????????????????????????????????

                    // ??????????????????
                    sendCloudSyncData();

                    break;
                case ProjectEditorDialogFragment.TYPE_EDIT_NAME:
                    editProjectName(newName);
                    break;
            }
        }
    };

    private void sendCloudSyncData() {
        if (!TNApi.isNetworkConnected(mContext) || !mApiUser.isLoggingIn()
                || !mApiModel.isCloudActive()
                || mApiModel.isSyncing()) return;

        mApiModel.saveAllNeedSaveSyncDeletedData(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e("ERROR", "sendCloudSyncData saveAllNeedSaveSyncDeletedData onFailure");
            }

            @Override
            public void onSuccess(Response response, String content) {
            }
        });
    }

    private void addSubProjectView(Project project) {
        final View viewRow = mInflater.inflate(R.layout.project_multi_row, binding.subProjectRadioLayout, false);

        // radio btn
        AppCompatRadioButton projectBtn = viewRow.findViewById(R.id.project_radio_btn);
        projectBtn.setOnClickListener(projectRadioOnClick);
        projectBtn.setText(project.name);
        projectBtn.setTag(project.uuid);

        int radioBtnColorRes = 0;
        int projectOrder = Integer.valueOf(String.valueOf(project.order));
        switch (projectOrder) {
            case 1:
            case 4:
                radioBtnColorRes = R.color.second_primary;
                break;
            case 2:
            case 5:
                radioBtnColorRes = R.color.third_primary;
                break;
            case 3:
                radioBtnColorRes = R.color.primary;
                break;
        }
        CompoundButtonCompat.setButtonTintList(projectBtn,
                ColorStateList.valueOf(ContextCompat.getColor(mContext, radioBtnColorRes)));

        // delete btn
        ImageButton deleteBtn = (ImageButton) viewRow.findViewById(R.id.delete_btn);
        deleteBtn.setOnClickListener(getSubProjectRemoveOnClick(project.name, viewRow));

        binding.subProjectRadioLayout.addView(viewRow);
    }

    private void editProjectName(String newName) {
        if (mEditingProject == null) return;
        mEditingProject.name = newName;
        mProjectDataManager.updateName(mEditingProject);

        mApiModel.updateProject(mEditingProject.uuid, null);

        mAllProjects = mProjectDataManager.findAllDeleted(false);
        if (mEditingProject.isMaster) {
            binding.mainProjectRadio.setText(newName);
            mEditingProject = null;
            return;
        }

        LinearLayout subProjectView = binding.subProjectRadioLayout;
        for (int i = 0; i < subProjectView.getChildCount(); i++) {
            View subView = subProjectView.getChildAt(i);
            AppCompatRadioButton radioBtn = subView.findViewWithTag(mEditingProject.uuid);
            if (radioBtn != null) {
                radioBtn.setText(newName);
            }
        }

        mEditingProject = null;
    }

    // ??????Radio?????????????????????????????????
    private View.OnClickListener projectRadioOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int viewId = view.getId();
            AppCompatRadioButton mainRadio = binding.mainProjectRadio;
            if (isProjectEditing) { // edit mode
                setProjectEditing(view);
                showProjectEditorDialog(ProjectEditorDialogFragment.TYPE_EDIT_NAME);
                checkCurrentProjectToRadio();
                return;
            }

            if (viewId == mainRadio.getId()) { // master project radio
                if (mCurrentProject.isMaster) return;

                SharedPreferencesManager.saveAppThemeStyle(mContext, 0);
                switchUseProject(true, null);
                return;
            }
            mainRadio.setChecked(false);

            LinearLayout subProjectView = binding.subProjectRadioLayout;
            String tagUuid = null;
            for (int i = 0; i < subProjectView.getChildCount(); i++) {
                View subView = subProjectView.getChildAt(i);
                AppCompatRadioButton radioBtn = subView.findViewById(R.id.project_radio_btn);
                String radioTagUuid = radioBtn.getTag().toString();
                if (!mCurrentProject.uuid.equals(radioTagUuid)) {
                    if (radioTagUuid.equals(view.getTag())) {
                        tagUuid = radioBtn.getTag().toString();
                        radioBtn.setChecked(true);

                        SharedPreferencesManager.saveAppThemeStyle(mContext, i + 1);
                        break;
                    }
                }
            }
            if (tagUuid != null)
                switchUseProject(false, tagUuid);
        }
    };

    private void setProjectEditing(View selectedView) {
        if (selectedView.getId() == binding.mainProjectRadio.getId()) {
            mEditingProject = getProjectEditing(true, null);
            return;
        }

        LinearLayout subProjectView = binding.subProjectRadioLayout;
        String tagUuid = null;
        for (int i = 0; i < subProjectView.getChildCount(); i++) {
            View subView = subProjectView.getChildAt(i);
            AppCompatRadioButton radioBtn = subView.findViewById(R.id.project_radio_btn);
            if (radioBtn != null && radioBtn.getTag() == selectedView.getTag()) {
                tagUuid = radioBtn.getTag().toString();
                break;
            }
        }
        mEditingProject = getProjectEditing(false, tagUuid);
    }

    private Project getProjectEditing(boolean isMaster, String uuid) {
        for (Project project : mAllProjects) {
            if (isMaster && project.isMaster) {
                return project;
            } else if (!isMaster && uuid != null && project.uuid.equals(uuid)) {
                return project;
            }
        }
        return null;
    }

    private void switchUseProject(boolean isMaster, String uuid) {
        DefaultDataInstaller.switchProject(mContext, getProjectEditing(isMaster, uuid));
        // restart
        DefaultDataInstaller.restartApp((AppCompatActivity) getActivity());
    }

    private void unCheckAllSubProjectRadio() {
        LinearLayout subProjectView = binding.subProjectRadioLayout;
        for (int i = 0; i < subProjectView.getChildCount(); i++) {
            View subView = subProjectView.getChildAt(i);
            AppCompatRadioButton radioBtn = subView.findViewById(R.id.project_radio_btn);
            if (radioBtn != null) {
                radioBtn.setChecked(false);
                radioBtn.jumpDrawablesToCurrentState();
            }

        }
    }

    private void checkCurrentProjectToRadio() {
        mCurrentProject = mProjectDataManager.findCurrent();
        if (mCurrentProject.isMaster) {
            unCheckAllSubProjectRadio();
            return;
        }

        LinearLayout subProjectView = binding.subProjectRadioLayout;

        binding.mainProjectRadio.setChecked(false);
        // https://stackoverflow.com/questions/45926301/radio-button-is-only-partially-checked
        // Radio???Checkbox???bug??????????????????jumpDrawablesToCurrentState?????????
        binding.mainProjectRadio.jumpDrawablesToCurrentState();
        unCheckAllSubProjectRadio();

        for (int i = 0; i < subProjectView.getChildCount(); i++) {
            View subView = subProjectView.getChildAt(i);
            AppCompatRadioButton radioBtn = subView.findViewById(R.id.project_radio_btn);
            if (radioBtn != null && radioBtn.getTag().toString().equals(mCurrentProject.uuid)) {
                radioBtn.setChecked(true);
                radioBtn.jumpDrawablesToCurrentState();
            }

        }
    }

    /**
     * ????????????????????????????????????
     *
     * @param projectName
     * @param parentRowView
     * @return
     */
    private View.OnClickListener getSubProjectRemoveOnClick(final String projectName, final View parentRowView) {
        final AppCompatRadioButton radioBtn = parentRowView.findViewById(R.id.project_radio_btn);
        final String selectedUuid = radioBtn.getTag().toString();

        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // master ??????????????????????????????
                mEditingProject = mProjectDataManager.findByUuid(selectedUuid);
                if (mEditingProject.isMaster) return;

                // can not remove current project
                if (selectedUuid.equals(mCurrentProject.uuid)) {
                    DialogManager.showOKOnlyAlert(getActivity(), null,
                            mContext.getString(R.string.delete_current_project_message));
                    return;
                }

                // Delete confirm dialog
                new AlertDialog.Builder(getActivity())
                        .setTitle(projectName)
                        .setMessage(getString(R.string.delete_project_confirm_message))
                        .setPositiveButton(getString(R.string.Delete), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                mProjectDataManager.updateSetDeleted(mEditingProject.uuid, mApiModel);

                                binding.subProjectRadioLayout.removeView(parentRowView);
                                mAllProjects = mProjectDataManager.findAllDeleted(false);

                                dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show();

            }
        };
    }

    private void switchSubProjectEdit() {

        LinearLayout subProjectView = binding.subProjectRadioLayout;
        TextView settingsProjectBtn = binding.settingsProjectButton;
        for (int i = 0; i < subProjectView.getChildCount(); i++) {
            View subView = subProjectView.getChildAt(i);

            ImageButton deleteBtn = (ImageButton) subView.findViewById(R.id.delete_btn);

            if (isProjectEditing) {
                deleteBtn.setVisibility(View.GONE);
            } else {
                deleteBtn.setVisibility(View.VISIBLE);
            }
        }
        settingsProjectBtn.setText(mContext.getString(R.string.project_settings));
        if (!isProjectEditing) {
            settingsProjectBtn.setText(mContext.getString(R.string.project_settings_finish));
            DialogManager.showToast(mContext, mContext.getString(R.string.tap_project_to_edit_name));
        }

        isProjectEditing = !isProjectEditing;

    }


    //--------------------------------------------------------------//
    //    -- Configs --
    //--------------------------------------------------------------//

    private void setDecimalSwitch() {

        // Set current decimal status
        final ProjectDataManager projectDataManager = new ProjectDataManager(getActivity());
        final Project project = projectDataManager.findCurrent();
        binding.decimalSwitch.setChecked(project.decimal);

        binding.decimalSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                projectDataManager.updateDecimal(project, isChecked);

                mApiModel.updateProject(project.uuid, null);
                BroadcastUtil.sendReloadReport(getActivity());
            }
        });
    }

    private void setPasscodeSwitch() {

        // under Android 6.0(API level 23)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            binding.passcodeSwitch.setVisibility(View.GONE);
            return;
        }

        // Set current passcode status
        final ProjectDataManager projectDataManager = new ProjectDataManager(getActivity());
        final Project project = projectDataManager.findCurrent();
        binding.passcodeSwitch.setChecked(project.passcode);

        binding.passcodeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                projectDataManager.updatePasscode(project, isChecked);

                mApiModel.updateProject(project.uuid, null);
                BroadcastUtil.sendReloadReport(getActivity());
            }
        });
    }


    //--------------------------------------------------------------//
    //    -- Alert Input Forget --
    //--------------------------------------------------------------//

    private void setAlertInputForget() {

        binding.alertInputForget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertInputForgetSettingsActivity.start(mContext);
            }
        });
    }


    //--------------------------------------------------------------//
    //    -- Data Backup --
    //--------------------------------------------------------------//

    private void setDataBackup() {
        // set click data backup
        binding.dataBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDataBackupDialog();
            }
        });
    }

    public void showDataBackupDialog() {

        new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.data_backup))
                .setMessage(getResources().getString(R.string.data_backup_message))
                .setPositiveButton(getResources().getString(R.string.data_backup_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(System.currentTimeMillis());
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                                mContext.getResources().getString(R.string.date_string_format_for_custom_range)
                                        + "_HHmmss",
                                Locale.getDefault());
                        String dateString = simpleDateFormat.format(cal.getTime());

                        String fileName = "taxnote_android_" + dateString + ".json";

                        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("application/json");
                        intent.putExtra(Intent.EXTRA_TITLE, fileName);

                        startActivityForResult(intent, CREATE_FILE);

                    }
                })
                .setNeutralButton(getResources().getString(R.string.help), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        CustomTabsUtils.showHelp(requireContext(), CustomTabsUtils.Content.BACKUP);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .show();
    }


    //--------------------------------------------------------------//
    //    -- Help --
    //--------------------------------------------------------------//

    private void setHelpViews() {

        binding.sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUrlInCustomTabs(getString(R.string.url_send_message));
            }
        });

        binding.help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomTabsUtils.showHelp(requireContext(), CustomTabsUtils.Content.TOP);
            }
        });

        binding.terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUrlInCustomTabs(getString(R.string.url_terms));
            }
        });

        binding.privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUrlInCustomTabs(getString(R.string.url_privacy_policy));
            }
        });
    }

    private void showUrlInCustomTabs(final String url) {
        CustomTabsUtils.showUrl(requireContext(), url);
    }


    //--------------------------------------------------------------//
    //    -- Share --
    //--------------------------------------------------------------//

    private void setShareButton() {

        binding.shareTaxnoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(getActivity());

                builder.setChooserTitle(getString(R.string.share_taxnote));
                builder.setSubject(getString(R.string.share_taxnote_title));
                builder.setText(getString(R.string.share_taxnote_message));

                builder.setType("text/plain");
                builder.startChooser();
            }
        });

    }

}