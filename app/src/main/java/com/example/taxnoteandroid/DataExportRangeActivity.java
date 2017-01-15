package com.example.taxnoteandroid;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioGroup;

import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityDataExportRangeBinding;

public class DataExportRangeActivity extends AppCompatActivity {

    private ActivityDataExportRangeBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_data_export_range);
        setViews();
    }


    //--------------------------------------------------------------//
    //    -- Display Part --
    //--------------------------------------------------------------//

    private void setViews() {

        binding = DataBindingUtil.setContentView(this, R.layout.activity_data_export_range);

        setSelectDateRangeTypeRadioGroup();
    }


    //--------------------------------------------------------------//
    //    -- Select Date Range Type --
    //--------------------------------------------------------------//

    private void setSelectDateRangeTypeRadioGroup() {

        String exportRange = SharedPreferencesManager.getCurrentExportRange(DataExportRangeActivity.this);

        if (exportRange.equals("all")) {
            binding.exportRangeRadioGroup.check(R.id.data_export_all_range);
        }

        if (exportRange.equals("this_month")) {
            binding.exportRangeRadioGroup.check(R.id.data_export_this_month);
        }

        if (exportRange.equals("last_month")) {
            binding.exportRangeRadioGroup.check(R.id.data_export_last_month);
        }

        if (exportRange.equals("custom")) {
            binding.exportRangeRadioGroup.check(R.id.data_export_custom_range);
        }

        binding.exportRangeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {

                    case R.id.data_export_all_range:
                        SharedPreferencesManager.saveCurrentExportRange(DataExportRangeActivity.this, "all");
                        break;

                    case R.id.data_export_this_month:
                        SharedPreferencesManager.saveCurrentExportRange(DataExportRangeActivity.this, "this_month");
                        break;

                    case R.id.data_export_last_month:
                        SharedPreferencesManager.saveCurrentExportRange(DataExportRangeActivity.this, "last_month");
                        break;

                    case R.id.data_export_custom_range:
                        SharedPreferencesManager.saveCurrentExportRange(DataExportRangeActivity.this, "custom");
                        break;
                }
            }
        });
    }
}
