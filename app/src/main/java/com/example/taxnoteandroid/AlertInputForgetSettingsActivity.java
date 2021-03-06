package com.example.taxnoteandroid;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TimePicker;

import com.example.taxnoteandroid.Library.DailyAlertInputForgetService;
import com.example.taxnoteandroid.Library.DailyScheduler;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityAlertInputForgetBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by b0ne on 2017/03/15.
 */

public class AlertInputForgetSettingsActivity extends DefaultCommonActivity {

    private ActivityAlertInputForgetBinding binding;
    private SimpleDateFormat formatHourMin;
    private DailyScheduler dailyScheduler;
    private String dailyAlertTimeString;
    private Boolean notifyEnable;

    public static final int DAILY_ALERT_SERVICE_ID = 1;

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

        dailyScheduler = new DailyScheduler(this);
        formatHourMin = new SimpleDateFormat("HH:mm", Locale.getDefault());

        dailyAlertTimeString = SharedPreferencesManager.getDailyAlertInputForgetTime(this);
        if (dailyAlertTimeString == null) {
            // デフォルトでは現在の5分後
            long defaultTime = System.currentTimeMillis() + (300 * 1000);
            String timeString = formatHourMin.format(defaultTime);

            binding.alertTimeValue.setText(timeString);
        } else {
            binding.alertTimeValue.setText(dailyAlertTimeString);
        }

        // 通知する、しないの設定
        notifyEnable = SharedPreferencesManager.getDailyAlertInputForgetEnable(this);
        binding.notifySwitch.setChecked(notifyEnable);
        binding.notifySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SharedPreferencesManager.saveDailyAlertInputForgetEnable(getApplicationContext(), isChecked);
                notifyEnable = isChecked;
                checkNotifyEnable(isChecked);
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
                        checkNotifyEnable(notifyEnable);
                    }
                }, hour, minute, true);
        dialog.show();
    }

    private void checkNotifyEnable(boolean isEnable) {
        // まずスケジュールをクリアする
        dailyScheduler.cancel(DailyAlertInputForgetService.class, DAILY_ALERT_SERVICE_ID);

        if (isEnable) {
            dailyScheduler.setBySavedDailyAlertInputForget(DAILY_ALERT_SERVICE_ID);
        }
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
