package com.example.taxnoteandroid;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.CompoundButton;

import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityProfitLossSettingsBinding;

/**
 * Created by b0ne on 2017/03/09.
 */

public class ProfitLossSettingsActivity extends DefaultCommonActivity {
    private ActivityProfitLossSettingsBinding binding;

    public static void start(Context context) {
        Intent intent = new Intent(context, ProfitLossSettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_profit_loss_settings);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


        // Balance carry forward
        boolean prefBalanceVal = SharedPreferencesManager.getBalanceCarryForward(this);
        binding.balanceCarryForward.setChecked(prefBalanceVal);
        binding.balanceCarryForward.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SharedPreferencesManager.saveBalanceCarryForward(getApplicationContext(), isChecked);
            }
        });



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
