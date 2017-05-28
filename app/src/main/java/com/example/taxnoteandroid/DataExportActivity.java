package com.example.taxnoteandroid;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.example.taxnoteandroid.Library.DataExportManager;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityDataExportBinding;
import com.helpshift.support.Support;

import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_CHARACTER_CODE_SHIFTJIS;
import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_CHARACTER_CODE_UTF8;
import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_FORMAT_TYPE_CSV;
import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_FORMAT_TYPE_FREEE;
import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_FORMAT_TYPE_MFCLOUD;
import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_FORMAT_TYPE_YAYOI;
import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_RANGE_TYPE_ALL;
import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_RANGE_TYPE_CUSTOM;
import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_RANGE_TYPE_LAST_MONTH;
import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_RANGE_TYPE_THIS_MONTH;


public class DataExportActivity extends DefaultCommonActivity {

    private ActivityDataExportBinding binding;

    private static final String TAG_EXPORT_SUBJECT_DIALOG_FRAGMENT = "export_subject_dialog_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_export);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setViews();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

        setExportButton();
        setCharacterCodeView();
        setSelectFormatRadioGroup();
        setHelpView();
    }


    //--------------------------------------------------------------//
    //    -- Export Button --
    //--------------------------------------------------------------//

    private void setExportButton() {

        binding.dataExportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 2017/01/20 E.Nozaki
                String format = SharedPreferencesManager.getCurrentExportFormat(DataExportActivity.this);
                String characterCode = SharedPreferencesManager.getCurrentCharacterCode(DataExportActivity.this);
                DataExportManager manager = new DataExportManager(DataExportActivity.this, format, characterCode);
                manager.export(); // Generate CSV file and send it by email.
            }
        });

        // 補助科目のボタン
        boolean subjectEnable = SharedPreferencesManager.getExportSujectEnable(this);
        int enableValRes = (subjectEnable) ? R.string.settings_valid : R.string.settings_invalid;
        binding.dataExportSubjectVal.setText(enableValRes);
        binding.dataExportSubjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExportSubjectDialog();
            }
        });
    }

    private void showExportSubjectDialog() {
        final TNSimpleDialogFragment dialogFragment = TNSimpleDialogFragment.newInstance();
        dialogFragment.setTitle(getString(R.string.data_export_subject));

        View contentView = LayoutInflater.from(this).inflate(R.layout.export_subject_dialog_layout, null);
        dialogFragment.setDialogView(contentView);
        Button validBtn = (Button)contentView.findViewById(R.id.subject_valid_btn);
        Button invalidBtn = (Button)contentView.findViewById(R.id.subject_invalid_btn);
        Button helpBtn = (Button)contentView.findViewById(R.id.subject_help_btn);
        validBtn.setOnClickListener(exportSubjectBtnOnClick);
        invalidBtn.setOnClickListener(exportSubjectBtnOnClick);
        helpBtn.setOnClickListener(exportSubjectBtnOnClick);

        dialogFragment.setNegativeBtnText(getString(android.R.string.cancel));

        getSupportFragmentManager().beginTransaction()
                .add(dialogFragment, TAG_EXPORT_SUBJECT_DIALOG_FRAGMENT)
                .commitAllowingStateLoss();
    }
    // 補助科目の設定
    private View.OnClickListener exportSubjectBtnOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int viewId = view.getId();
            Context context = view.getContext();
            switch (viewId) {
                case R.id.subject_valid_btn:
                    SharedPreferencesManager.saveExportSubjectEnable(context, true);
                    binding.dataExportSubjectVal.setText(R.string.settings_valid);
                    break;
                case R.id.subject_invalid_btn:
                    SharedPreferencesManager.saveExportSubjectEnable(context, false);
                    binding.dataExportSubjectVal.setText(R.string.settings_invalid);
                    break;
                case R.id.subject_help_btn:
                    Support.showSingleFAQ(DataExportActivity.this,"104");
                    break;
            }
            Fragment fragment = getSupportFragmentManager()
                    .findFragmentByTag(TAG_EXPORT_SUBJECT_DIALOG_FRAGMENT);
            getSupportFragmentManager().beginTransaction()
                    .remove(fragment).commitAllowingStateLoss();
        }
    };


    //--------------------------------------------------------------//
    //    -- Export Range --
    //--------------------------------------------------------------//

    private void setExportRangeView() {

        String exportRange = SharedPreferencesManager.getExportRangeType(DataExportActivity.this);

        if (exportRange.equals(EXPORT_RANGE_TYPE_ALL)) {
            binding.dataExportRangeButtonRight.setText(getResources().getString(R.string.data_export_all_range));
        }

        if (exportRange.equals(EXPORT_RANGE_TYPE_THIS_MONTH)) {
            binding.dataExportRangeButtonRight.setText(getResources().getString(R.string.data_export_this_month));
        }

        if (exportRange.equals(EXPORT_RANGE_TYPE_LAST_MONTH)) {
            binding.dataExportRangeButtonRight.setText(getResources().getString(R.string.data_export_last_month));
        }

        if (exportRange.equals(EXPORT_RANGE_TYPE_CUSTOM)) {
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

        CharSequence codes[] = new CharSequence[] {EXPORT_CHARACTER_CODE_UTF8, EXPORT_CHARACTER_CODE_SHIFTJIS};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.character_code_select_please));
        builder.setItems(codes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {
                    case 0:
                        SharedPreferencesManager.saveCurrentCharacterCode(DataExportActivity.this, EXPORT_CHARACTER_CODE_UTF8);
                        binding.characterCodeButtonRight.setText(EXPORT_CHARACTER_CODE_UTF8);
                        DialogManager.showToast(DataExportActivity.this, EXPORT_CHARACTER_CODE_UTF8);
                        break;
                    case 1:
                        SharedPreferencesManager.saveCurrentCharacterCode(DataExportActivity.this, EXPORT_CHARACTER_CODE_SHIFTJIS);
                        binding.characterCodeButtonRight.setText(EXPORT_CHARACTER_CODE_SHIFTJIS);
                        DialogManager.showToast(DataExportActivity.this, EXPORT_CHARACTER_CODE_SHIFTJIS);
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

        if (exportFormat.equals(EXPORT_FORMAT_TYPE_CSV)) {
            binding.exportRadioGroup.check(R.id.csv_format);
        }

        if (exportFormat.equals(EXPORT_FORMAT_TYPE_YAYOI)) {
            binding.exportRadioGroup.check(R.id.yayoi_format);
        }

        if (exportFormat.equals(EXPORT_FORMAT_TYPE_FREEE)) {
            binding.exportRadioGroup.check(R.id.freee_format);
        }

        if (exportFormat.equals(EXPORT_FORMAT_TYPE_MFCLOUD)) {
            binding.exportRadioGroup.check(R.id.mfcloud_format);
        }

        binding.exportRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {

                    case R.id.csv_format:
                        SharedPreferencesManager.saveCurrentExportFormat(DataExportActivity.this, EXPORT_FORMAT_TYPE_CSV);
                        break;

                    case R.id.yayoi_format:
                        SharedPreferencesManager.saveCurrentExportFormat(DataExportActivity.this, EXPORT_FORMAT_TYPE_YAYOI);
                        break;

                    case R.id.freee_format:
                        SharedPreferencesManager.saveCurrentExportFormat(DataExportActivity.this, EXPORT_FORMAT_TYPE_FREEE);
                        break;

                    case R.id.mfcloud_format:
                        SharedPreferencesManager.saveCurrentExportFormat(DataExportActivity.this, EXPORT_FORMAT_TYPE_MFCLOUD);
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
                Support.showFAQSection(DataExportActivity.this,"36");
            }
        });
    }

}
