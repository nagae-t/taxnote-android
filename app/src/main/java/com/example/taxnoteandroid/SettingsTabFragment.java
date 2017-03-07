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

import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.databinding.FragmentSettingsTabBinding;
import com.example.taxnoteandroid.model.Project;
import com.helpshift.support.Support;

import java.util.ArrayList;
import java.util.List;


public class SettingsTabFragment extends Fragment {

    private Context mContext;
    private FragmentSettingsTabBinding binding;
    private FragmentManager mFragmentManager;
    private LayoutInflater mInflater;
    private List<Integer> mSubProjectRadioTags;

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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] items = {"帳簿を追加", "帳簿を削除・名前変更"};
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int countSubProject = binding.subProjectRadioLayout.getChildCount();
                if (i == 0 && countSubProject < 2) {
                    ProjectEditorDialogFragment editDialog = ProjectEditorDialogFragment
                            .newInstance(ProjectEditorDialogFragment.TYPE_ADD_NEW);
                    editDialog.setOnSubmitListener(onProjectEditorSubmitListener);
                    editDialog.show(mFragmentManager, null);
                } else if (i == 1) {
                    // TODO: set radio layout to show remove btn
                    switchSubProjectEdit();
                }
            }
        });
        final AlertDialog menuDialog = builder.create();
        binding.settingsProjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuDialog.show();
            }
        });
        binding.mainProjectRadio.setOnClickListener(projectRadioOnClick);
    }

    private ProjectEditorDialogFragment.OnEditorSubmitListener onProjectEditorSubmitListener
            = new ProjectEditorDialogFragment.OnEditorSubmitListener() {
        @Override
        public void onSubmit(DialogInterface dialogInterface, EditText nameEdit, String tag) {
            if (nameEdit == null) return;

            final View viewRow = mInflater.inflate(R.layout.project_multi_row, binding.subProjectRadioLayout, false);
            RadioButton projectBtn = (RadioButton)viewRow.findViewById(R.id.project_radio_btn);
            projectBtn.setOnClickListener(projectRadioOnClick);
            String newName = nameEdit.getText().toString();
            projectBtn.setText(newName);
            int newTag = projectBtn.getId() + mSubProjectRadioTags.size() + 1;
            projectBtn.setTag(newTag);
            mSubProjectRadioTags.add(newTag);

            // delete btn
            ImageButton deleteBtn = (ImageButton)viewRow.findViewById(R.id.delete_btn);
            deleteBtn.setOnClickListener(getSubProjectRemoveOnClick(viewRow));

            binding.subProjectRadioLayout.addView(viewRow);
        }
    };

    private View.OnClickListener projectRadioOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int viewId = view.getId();
            unCheckAllSubProjectRadio();

            RadioButton mainRadio = binding.mainProjectRadio;
            if (viewId == mainRadio.getId()) {
                if (mainRadio.isChecked()) return;
                mainRadio.setChecked(true);
                return;
            }
            mainRadio.setChecked(false);

            LinearLayout subProjectView = binding.subProjectRadioLayout;
            for (int i=0; i<subProjectView.getChildCount(); i++) {
                View subView = subProjectView.getChildAt(i);
                for (int viewTag : mSubProjectRadioTags) {
                    RadioButton radioBtn = (RadioButton) subView.findViewWithTag(viewTag);
                    if (radioBtn != null && radioBtn.getTag() == view.getTag()) {
                        radioBtn.setChecked(true);
                    }
                }
            }
        }
    };

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

    private View.OnClickListener getSubProjectRemoveOnClick(final View parentRowView) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.subProjectRadioLayout.removeView(parentRowView);
            }
        };
    }

    private void switchSubProjectEdit() {
        LinearLayout subProjectView = binding.subProjectRadioLayout;
        for (int i=0; i<subProjectView.getChildCount(); i++) {
            View subView = subProjectView.getChildAt(i);

            ImageButton deleteBtn = (ImageButton)subView.findViewById(R.id.delete_btn);
            deleteBtn.setVisibility(View.VISIBLE);
        }
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