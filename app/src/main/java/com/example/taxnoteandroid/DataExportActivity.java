package com.example.taxnoteandroid;

import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.taxnoteandroid.databinding.ActivityDataExportBinding;


public class DataExportActivity extends AppCompatActivity {

    private ActivityDataExportBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_data_export);
        setViews();
    }


    //--------------------------------------------------------------//
    //    -- Display Part --
    //--------------------------------------------------------------//

    private void setViews() {

        binding = DataBindingUtil.setContentView(this, R.layout.activity_data_export);
        setTitle();
        setCharacterCodeView();
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


    //--------------------------------------------------------------//
    //    -- Character Code --
    //--------------------------------------------------------------//

    private void setCharacterCodeView() {

        binding.characterCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectCharacterCode();
            }
        });

    }

    //QQ Pickerの作り方これでよいかな？
    private void selectCharacterCode() {

        CharSequence codes[] = new CharSequence[] {"UTF8", "ShiftJIS"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.character_code));
        builder.setItems(codes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {
                    case 0:


                        break;
                    case 1:
                        break;
                }
            }
        });
        builder.show();
    }

}
