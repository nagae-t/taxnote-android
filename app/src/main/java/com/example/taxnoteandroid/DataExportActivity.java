package com.example.taxnoteandroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioGroup;

import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityDataExportBinding;


public class DataExportActivity extends AppCompatActivity {

    private ActivityDataExportBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_data_export);
        setViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        setExportRangeView();
    }


    //--------------------------------------------------------------//
    //    -- Display Part --
    //--------------------------------------------------------------//

    private void setViews() {

        binding = DataBindingUtil.setContentView(this, R.layout.activity_data_export);

        setCharacterCodeView();
        setSelectFormatRadioGroup();
        setHelpView();
    }


    //--------------------------------------------------------------//
    //    -- Export Range --
    //--------------------------------------------------------------//

    private void setExportRangeView() {

        String exportRange = SharedPreferencesManager.getExportRangeType(DataExportActivity.this);

        if (exportRange.equals("all")) {
            binding.dataExportRangeButtonRight.setText(getResources().getString(R.string.data_export_all_range));
        }

        if (exportRange.equals("this_month")) {
            binding.dataExportRangeButtonRight.setText(getResources().getString(R.string.data_export_this_month));
        }

        if (exportRange.equals("last_month")) {
            binding.dataExportRangeButtonRight.setText(getResources().getString(R.string.data_export_last_month));
        }

        if (exportRange.equals("custom")) {
            binding.dataExportRangeButtonRight.setText(getResources().getString(R.string.data_export_custom_range));
        }

        binding.dataExportRangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(DataExportActivity.this, DataExportRangeActivity.class);
                startActivity(intent);
            }
        });
    }

    //--------------------------------------------------------------//
    //    -- Character Code --
    //--------------------------------------------------------------//

    private void setCharacterCodeView() {

        String characterCode = SharedPreferencesManager.getCurrentCharacterCode(DataExportActivity.this);
        binding.characterCodeButtonRight.setText(characterCode);

        binding.characterCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCharacterCodePicker();
            }
        });
    }

    private void showCharacterCodePicker() {

        CharSequence codes[] = new CharSequence[] {"UTF8", "ShiftJIS"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.character_code_select_please));
        builder.setItems(codes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {
                    case 0:
                        SharedPreferencesManager.saveCurrentCharacterCode(DataExportActivity.this, "UTF8");
                        binding.characterCodeButtonRight.setText("UTF8");
                        DialogManager.showToast(DataExportActivity.this, "UTF8");
                        break;
                    case 1:
                        SharedPreferencesManager.saveCurrentCharacterCode(DataExportActivity.this, "ShiftJIS");
                        binding.characterCodeButtonRight.setText("ShiftJIS");
                        DialogManager.showToast(DataExportActivity.this, "ShiftJIS");
                        break;
                }
            }
        });
        builder.show();
    }


    //--------------------------------------------------------------//
    //    -- Select Format --
    //--------------------------------------------------------------//

    private void setSelectFormatRadioGroup() {

        String exportFormat = SharedPreferencesManager.getCurrentExportFormat(DataExportActivity.this);

        if (exportFormat.equals("csv")) {
            binding.exportRadioGroup.check(R.id.csv_format);
        }

        if (exportFormat.equals("yayoi")) {
            binding.exportRadioGroup.check(R.id.yayoi_format);
        }

        if (exportFormat.equals("freee")) {
            binding.exportRadioGroup.check(R.id.freee_format);
        }

        if (exportFormat.equals("mfcloud")) {
            binding.exportRadioGroup.check(R.id.mfcloud_format);
        }

        binding.exportRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {

                    case R.id.csv_format:
                        SharedPreferencesManager.saveCurrentExportFormat(DataExportActivity.this, "csv");
                        break;

                    case R.id.yayoi_format:
                        SharedPreferencesManager.saveCurrentExportFormat(DataExportActivity.this, "yayoi");
                        break;

                    case R.id.freee_format:
                        SharedPreferencesManager.saveCurrentExportFormat(DataExportActivity.this, "freee");
                        break;

                    case R.id.mfcloud_format:
                        SharedPreferencesManager.saveCurrentExportFormat(DataExportActivity.this, "mfcloud");
                        break;
                }
            }
        });
    }


    //--------------------------------------------------------------//
    //    -- Help --
    //--------------------------------------------------------------//

    private void setHelpView() {

        binding.dataExportHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

}
