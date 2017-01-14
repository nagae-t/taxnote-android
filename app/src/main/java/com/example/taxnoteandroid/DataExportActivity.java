package com.example.taxnoteandroid;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.taxnoteandroid.databinding.ActivityDataExportBinding;


public class DataExportActivity extends AppCompatActivity {

    private ActivityDataExportBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_data_export);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_data_export);
        setViews();
    }


    //--------------------------------------------------------------//
    //    -- Display Part --
    //--------------------------------------------------------------//

    private void setViews() {
        setTitle();
        setHelpView();
    }

    private void setTitle() {
        setTitle(getResources().getString(R.string.data_export));
    }

    private void setHelpView() {

        binding.dataExportHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }



}
