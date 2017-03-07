package com.example.taxnoteandroid;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.taxnoteandroid.dataManager.DefaultDataInstaller;
import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.databinding.FragmentSettingsTabBinding;
import com.example.taxnoteandroid.model.Project;
import com.helpshift.support.Support;

import java.util.ArrayList;
import java.util.List;


public class SettingsTabFragment extends Fragment {

    private Context mContext;
    private ProjectDataManager mProjectDataManager;
    private FragmentSettingsTabBinding binding;
    private FragmentManager mFragmentManager;
    private LayoutInflater mInflater;
    private List<Integer> mSubProjectRadioTags;
    private boolean isProjectEditing = false;
    private List<Project> mAllProjects;

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
        mInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mProjectDataManager = new ProjectDataManager(mContext);

        setViews();
    }


    //--------------------------------------------------------------//
    //    -- Display Part --
    //--------------------------------------------------------------//

    private void setViews() {
        setUpgradeView();
        setMultipleProject();
        setDecimalSwitch();
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

                Intent intent = new Intent(getContext(), UpgradeActivity.class);
                startActivity(intent);
            }
        });
    }


    //--------------------------------------------------------------//
    //    -- Multiple Project --
    //--------------------------------------------------------------//

    private void setMultipleProject() {
        mSubProjectRadioTags = new ArrayList<>();
        mAllProjects = mProjectDataManager.findAll();
        // debug
//        for (Project proj : mAllProjects) {
//            Log.v("TEST", "project name: "+proj.name + ", uuid:"+proj.uuid);
//        }

        int projectSize = mProjectDataManager.allSize();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String addTitle = mContext.getString(R.string.add_new_project);
        String deleteOrEditNameTitle = mContext.getString(R.string.delete_project_or_edit_name);
        String[] items = {addTitle, deleteOrEditNameTitle};
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                int projectAllSize = mProjectDataManager.allSize();
                // 無料版はmaster含めてProject3つまで
                if (i == 0 && projectAllSize < 3) {
                    showProjectEditorDialog(ProjectEditorDialogFragment.TYPE_ADD_NEW);
                } else if (i == 0 && projectAllSize >= 3) {
                    //@@ 帳簿数上限に達していて「追加」ボタンをしたら何をだすかここに追加

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

        if (mProjectDataManager.allSize() == 1) return;
        // sub project があれば表示
        List<Project> projects = mProjectDataManager.findAll(false);
        for (int i=0; i<projects.size(); i++) {
            String projectName = projects.get(i).name;
            addSubProjectView(projectName);
        }
        // set current project radio checked
        checkCurrentProjectToRadio();
    }

    private void showProjectEditorDialog(int dialogType) {
        ProjectEditorDialogFragment editDialog = ProjectEditorDialogFragment
                .newInstance(dialogType);
        editDialog.setOnSubmitListener(onProjectEditorSubmitListener);
        editDialog.show(mFragmentManager, null);
    }

    // 新規プロジェクト(帳簿)作成またはプロジェクト名を修正するコールバック
    private ProjectEditorDialogFragment.OnEditorSubmitListener onProjectEditorSubmitListener
            = new ProjectEditorDialogFragment.OnEditorSubmitListener() {
        @Override
        public void onSubmit(DialogInterface dialogInterface,
                             int dialogType, EditText nameEdit, String tag) {
            if (nameEdit == null) return;

            String newName = nameEdit.getText().toString();

            switch (dialogType) {
                case ProjectEditorDialogFragment.TYPE_ADD_NEW: // 新規帳簿を追加する
                    int projectSize = mProjectDataManager.allSize();
                    DefaultDataInstaller.addNewProjectByName(mContext, newName, projectSize);
                    addSubProjectView(newName);
                    checkCurrentProjectToRadio();
                    mAllProjects = mProjectDataManager.findAll();
                    break;
                case ProjectEditorDialogFragment.TYPE_EDIT_NAME:
                    break;
            }
        }
    };

    private void addSubProjectView(String projectName) {
        final View viewRow = mInflater.inflate(R.layout.project_multi_row, binding.subProjectRadioLayout, false);

        // radio btn
        RadioButton projectBtn = (RadioButton)viewRow.findViewById(R.id.project_radio_btn);
        projectBtn.setOnClickListener(projectRadioOnClick);
        projectBtn.setText(projectName);
        int newTag = projectBtn.getId() + mSubProjectRadioTags.size() + 1;
        projectBtn.setTag(newTag);

        mSubProjectRadioTags.add(newTag);

        // delete btn
        ImageButton deleteBtn = (ImageButton)viewRow.findViewById(R.id.delete_btn);
        deleteBtn.setOnClickListener(getSubProjectRemoveOnClick(projectName, viewRow));

        binding.subProjectRadioLayout.addView(viewRow);
    }

    // 帳簿Radioをタップするときの処理
    private View.OnClickListener projectRadioOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int viewId = view.getId();
            RadioButton mainRadio = binding.mainProjectRadio;
            if (isProjectEditing) { // edit mode
                checkCurrentProjectToRadio();
                showProjectEditorDialog(ProjectEditorDialogFragment.TYPE_EDIT_NAME);
                return;
            }

            unCheckAllSubProjectRadio();
            if (viewId == mainRadio.getId()) {
                switchProjectThread(true, null);
                return;
            }
            mainRadio.setChecked(false);

            LinearLayout subProjectView = binding.subProjectRadioLayout;
            String selectedRadioName = null;
            for (int i=0; i<subProjectView.getChildCount(); i++) {
                View subView = subProjectView.getChildAt(i);
                for (int viewTag : mSubProjectRadioTags) {
                    RadioButton radioBtn = (RadioButton) subView.findViewWithTag(viewTag);
                    if (radioBtn != null && radioBtn.getTag() == view.getTag()) {
                        selectedRadioName = radioBtn.getText().toString();
                        radioBtn.setChecked(true);
                        break;
                    }
                }
                if (selectedRadioName != null) break;
            }

            switchProjectThread(false, selectedRadioName);
        }
    };

    private void switchProjectThread(final boolean isMaster, final String projectName) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                for (Project project : mAllProjects) {
                    if (isMaster && project.isMaster) {
                        DefaultDataInstaller.switchProject(mContext, project);
                        break;
                    } else if (!isMaster && projectName != null && project.name.equals(projectName)) {
                        DefaultDataInstaller.switchProject(mContext, project);
                        break;
                    }
                }
            }
        }).start();
    }

    private void unCheckAllSubProjectRadio() {
        LinearLayout subProjectView = binding.subProjectRadioLayout;
        for (int i=0; i<subProjectView.getChildCount(); i++) {
            View subView = subProjectView.getChildAt(i);

            for (int viewTag : mSubProjectRadioTags) {
                RadioButton radioBtn = (RadioButton)subView.findViewWithTag(viewTag);
                if (radioBtn != null) {
                    radioBtn.setChecked(false);
                }
            }
        }
    }

    private void checkCurrentProjectToRadio() {
        Project currentProject = mProjectDataManager.findCurrentProjectWithContext(mContext);
        if (currentProject.isMaster) return;

        LinearLayout subProjectView = binding.subProjectRadioLayout;

        binding.mainProjectRadio.setChecked(false);
        unCheckAllSubProjectRadio();

        for (int i=0; i<subProjectView.getChildCount(); i++) {
            View subView = subProjectView.getChildAt(i);

            for (int viewTag : mSubProjectRadioTags) {
                RadioButton radioBtn = (RadioButton)subView.findViewWithTag(viewTag);
                if (radioBtn != null && radioBtn.getText().equals(currentProject.name) ) {
                    radioBtn.setChecked(true);
                }
            }
        }
    }

    private View.OnClickListener getSubProjectRemoveOnClick(final String projectName, final View parentRowView) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Delete confirm dialog
                new AlertDialog.Builder(getActivity())
                        .setTitle(projectName)
                        .setMessage(getString(R.string.delete_project_confirm_message))
                        .setPositiveButton(getString(R.string.Delete), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                // TODO : remove the Project from DB

                                binding.subProjectRadioLayout.removeView(parentRowView);

                                dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show();

            }
        };
    }

    private void switchSubProjectEdit() {
        // isProjectEditing

        LinearLayout subProjectView = binding.subProjectRadioLayout;
        TextView settingsProjectBtn = binding.settingsProjectButton;
        for (int i=0; i<subProjectView.getChildCount(); i++) {
            View subView = subProjectView.getChildAt(i);

            ImageButton deleteBtn = (ImageButton)subView.findViewById(R.id.delete_btn);

            if (isProjectEditing) {
                deleteBtn.setVisibility(View.GONE);
                settingsProjectBtn.setText(mContext.getString(R.string.project_settings));
            } else {
                deleteBtn.setVisibility(View.VISIBLE);
                settingsProjectBtn.setText(mContext.getString(R.string.project_settings_finish));
            }
        }

        isProjectEditing = !isProjectEditing;
    }


    //--------------------------------------------------------------//
    //    -- Configs --
    //--------------------------------------------------------------//

    private void setDecimalSwitch() {

        // Set current decimal status
        final ProjectDataManager projectDataManager   = new ProjectDataManager(getActivity());
        final Project project                         = projectDataManager.findCurrentProjectWithContext(getActivity());
        binding.decimalSwitch.setChecked(project.decimal);

        binding.decimalSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                projectDataManager.updateDecimal(project, isChecked);
            }
        });
    }


    //--------------------------------------------------------------//
    //    -- Help --
    //--------------------------------------------------------------//

    private void setHelpViews() {

        binding.sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Support.showConversation(getActivity());
            }
        });

        binding.help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Support.showFAQSection(getActivity(),"22");
            }
        });
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