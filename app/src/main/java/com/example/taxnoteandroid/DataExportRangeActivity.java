package com.example.taxnoteandroid;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityDataExportRangeBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_RANGE_TYPE_ALL;
import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_RANGE_TYPE_CUSTOM;
import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_RANGE_TYPE_LAST_MONTH;
import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_RANGE_TYPE_THIS_MONTH;

public class DataExportRangeActivity extends DefaultCommonActivity {

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
        setCustomDateRangeView();
    }


    //--------------------------------------------------------------//
    //    -- Select Date Range Type --
    //--------------------------------------------------------------//

    private void setSelectDateRangeTypeRadioGroup() {

        String exportRange = SharedPreferencesManager.getExportRangeType(DataExportRangeActivity.this);

        if (exportRange.equals(EXPORT_RANGE_TYPE_ALL)) {
            binding.exportRangeRadioGroup.check(R.id.data_export_all_range);
        }

        if (exportRange.equals(EXPORT_RANGE_TYPE_THIS_MONTH)) {
            binding.exportRangeRadioGroup.check(R.id.data_export_this_month);
        }

        if (exportRange.equals(EXPORT_RANGE_TYPE_LAST_MONTH)) {
            binding.exportRangeRadioGroup.check(R.id.data_export_last_month);
        }

        if (exportRange.equals(EXPORT_RANGE_TYPE_CUSTOM)) {
            binding.exportRangeRadioGroup.check(R.id.data_export_custom_range);
        }

        binding.exportRangeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {

                    case R.id.data_export_all_range:
                        SharedPreferencesManager.saveExportRangeType(DataExportRangeActivity.this, EXPORT_RANGE_TYPE_ALL);
                        break;

                    case R.id.data_export_this_month:
                        SharedPreferencesManager.saveExportRangeType(DataExportRangeActivity.this, EXPORT_RANGE_TYPE_THIS_MONTH);
                        break;

                    case R.id.data_export_last_month:
                        SharedPreferencesManager.saveExportRangeType(DataExportRangeActivity.this, EXPORT_RANGE_TYPE_LAST_MONTH);
                        break;

                    case R.id.data_export_custom_range:
                        SharedPreferencesManager.saveExportRangeType(DataExportRangeActivity.this, EXPORT_RANGE_TYPE_CUSTOM);
                        break;
                }
            }
        });
    }


    //--------------------------------------------------------------//
    //    -- Set Custom Date Range --
    //--------------------------------------------------------------//

    private void setCustomDateRangeView() {

        // Get saved date
        final long dateRangeBeginDate   = SharedPreferencesManager.getDateRangeBeginDate(DataExportRangeActivity.this);
        final long dateRangeEndDate     = SharedPreferencesManager.getDateRangeEndDate(DataExportRangeActivity.this);

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources().getString(R.string.date_string_format_to_year_month_day));
        String beginDateString                  = simpleDateFormat.format(dateRangeBeginDate);
        String endDateString                    = simpleDateFormat.format(dateRangeEndDate);

        // Set date string
        binding.dataExportBeginDateRight.setText(beginDateString);
        binding.dataExportEndDateRight.setText(endDateString);

        binding.dataExportBeginDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Show date picker
                DatePickerDialogFragment fragment = DatePickerDialogFragment.newInstance(dateRangeBeginDate, null);

                fragment.setOnDateSetListener(new DatePickerDialogFragment.OnDateSetListener() {
                    @Override
                    public void onDateSet(Calendar calendar) {

                        // Save new date
                        long newDate = calendar.getTimeInMillis();
                        SharedPreferencesManager.saveDateRangeBeginDate(DataExportRangeActivity.this, newDate);

                        // Update view
                        String dateString = simpleDateFormat.format(newDate);
                        binding.dataExportBeginDateRight.setText(dateString);

                        DialogManager.showToast(DataExportRangeActivity.this, dateString);

                        // Change date range type
                        binding.exportRangeRadioGroup.check(R.id.data_export_custom_range);
                        SharedPreferencesManager.saveExportRangeType(DataExportRangeActivity.this, EXPORT_RANGE_TYPE_CUSTOM);
                    }
                });
                fragment.show(getSupportFragmentManager(), DatePickerDialogFragment.class.getName());
            }
        });

        binding.dataExportEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Show date picker
                DatePickerDialogFragment fragment = DatePickerDialogFragment.newInstance(dateRangeEndDate, null);

                fragment.setOnDateSetListener(new DatePickerDialogFragment.OnDateSetListener() {
                    @Override
                    public void onDateSet(Calendar calendar) {

                        // Save new date
                        long newDate = calendar.getTimeInMillis();
                        SharedPreferencesManager.saveDateRangeEndDate(DataExportRangeActivity.this, newDate);

                        // Update view
                        String dateString = simpleDateFormat.format(newDate);
                        binding.dataExportEndDateRight.setText(dateString);

                        DialogManager.showToast(DataExportRangeActivity.this, dateString);

                        // Change date range type
                        binding.exportRangeRadioGroup.check(R.id.data_export_custom_range);
                        SharedPreferencesManager.saveExportRangeType(DataExportRangeActivity.this, EXPORT_RANGE_TYPE_CUSTOM);
                    }
                });
                fragment.show(getSupportFragmentManager(), DatePickerDialogFragment.class.getName());
            }
        });

    }
}