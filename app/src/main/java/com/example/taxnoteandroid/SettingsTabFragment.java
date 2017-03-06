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

import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.databinding.FragmentSettingsTabBinding;
import com.example.taxnoteandroid.model.Project;
import com.helpshift.support.Support;


public class SettingsTabFragment extends Fragment {

    private Context mContext;
    private FragmentSettingsTabBinding binding;
    private FragmentManager mFragmentManager;

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

        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View projectMultiRow = inflater.inflate(R.layout.project_multi_row, null);

        // edit dialog fragment
        final ProjectEditorDialogFragment editDialog = ProjectEditorDialogFragment.newInstance(ProjectEditorDialogFragment.TYPE_ADD_NEW);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] items = {"帳簿を追加", "帳簿を削除・名前変更"};
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0: // 追加
                        editDialog.show(mFragmentManager, null);
                        break;
                    case 1: // 削除、名前変更
                        break;
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