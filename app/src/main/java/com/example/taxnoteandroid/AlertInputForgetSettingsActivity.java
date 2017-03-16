package com.example.taxnoteandroid;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TimePicker;

import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityAlertInputForgetBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by b0ne on 2017/03/15.
 */

public class AlertInputForgetSettingsActivity extends DefaultCommonActivity {

    private ActivityAlertInputForgetBinding binding;
    private SimpleDateFormat formatHourMin;

    public static void start(Context context) {
        Intent intent = new Intent(context, AlertInputForgetSettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_alert_input_forget);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        formatHourMin = new SimpleDateFormat("HH:mm");

        String savedAlertTime = SharedPreferencesManager.getDailyAlertInputForgetTime(this);
        if (savedAlertTime == null) {
            // デフォルトでは現在の5分後
            long defaultTime = System.currentTimeMillis() + (300 * 1000);
            String timeString = formatHourMin.format(defaultTime);

            binding.alertTimeValue.setText(timeString);
        } else {
//            String[] timeStrings = savedAlertTime.split(":");
//            Log.v("TEST", "saved time = " + timeStrings[0]
//                + "h, "+timeStrings[1]+"m");
            binding.alertTimeValue.setText(savedAlertTime);
        }

        // 通知する、しないの設定
        Boolean notifyEnable = SharedPreferencesManager.getDailyAlertInputForgetEnable(this);
        binding.notifySwitch.setChecked(notifyEnable);
        binding.notifySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SharedPreferencesManager.saveDailyAlertInputForgetEnable(getApplicationContext(), isChecked);
            }
        });

        // 時間の設定
        binding.alertTimeRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog();
            }
        });
    }

    private void showTimePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener(){
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DATE),
                                hourOfDay, minute);
                        String timeString = formatHourMin.format(calendar.getTime());
                        binding.alertTimeValue.setText(timeString);
                        SharedPreferencesManager.saveDailyAlertInputForgetTime(getApplicationContext(), timeString);
                    }
                }, hour, minute, true);
        dialog.show();
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
}
