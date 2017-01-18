package com.example.taxnoteandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taxnoteandroid.databinding.FragmentSettingsTabBinding;
import com.helpshift.support.Support;


public class SettingsTabFragment extends Fragment {

    private FragmentSettingsTabBinding binding;

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

        setViews();

        return binding.getRoot();
    }


    //--------------------------------------------------------------//
    //    -- Display Part --
    //--------------------------------------------------------------//

    private void setViews() {
        setUpgradeView();
        setHelpViews();
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

}