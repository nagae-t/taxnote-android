package com.example.taxnoteandroid;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.example.taxnoteandroid.databinding.ActivityAlertInputForgetBinding;

import java.text.SimpleDateFormat;

/**
 * Created by b0ne on 2017/03/15.
 */

public class AlertInputForgetSettingsActivity extends DefaultCommonActivity {

    private ActivityAlertInputForgetBinding binding;

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

        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        // デフォルトでは現在の5分後
        long defaultTime = System.currentTimeMillis() + (300*1000);
        String timeString = format.format(defaultTime);

        binding.alertTimeValue.setText(timeString);

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
