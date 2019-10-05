package com.example.taxnoteandroid;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.example.taxnoteandroid.Library.BroadcastUtil;
import com.example.taxnoteandroid.Library.DialogManager;
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

        boolean combAll = SharedPreferencesManager.getCombAllAccounts(this);
        binding.combAllAcc.setChecked(combAll);
        binding.combAllAcc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SharedPreferencesManager.saveCombAllAccounts(getApplicationContext(), isChecked);

                if (isChecked) {
                    String title = getApplicationContext().getString(R.string.combine_all_accounts);
                    String message = getApplicationContext().getString(R.string.combine_all_accounts_message);
                    DialogManager.showCustomAlertDialog(ProfitLossSettingsActivity.this,
                            getSupportFragmentManager(),
                            title, message);
                    BroadcastUtil.sendReloadReport(ProfitLossSettingsActivity.this);
                }
            }
        });

        boolean fixOrderVal = SharedPreferencesManager.getFixedCateOrder(this);
        binding.fixedCateOrder.setChecked(fixOrderVal);
        binding.fixedCateOrder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SharedPreferencesManager.saveFixedCateOrder(getApplicationContext(), isChecked);
                if (isChecked) {
                    String title = getApplicationContext().getString(R.string.fixed_cate_order);
                    String message = getApplicationContext().getString(R.string.fixed_cate_order_message);
                    DialogManager.showCustomAlertDialog(ProfitLossSettingsActivity.this,
                            getSupportFragmentManager(),
                            title, message);
                    BroadcastUtil.sendReloadReport(ProfitLossSettingsActivity.this);
                }
            }
        });

        // Monthly closing date
        final String[] dateList = getResources().getStringArray(R.array.close_date_list);
        int dateIndex = SharedPreferencesManager.getMonthlyClosingDateIndex(this);
        binding.monthlyClosingDateValue.setText(dateList[dateIndex]);
        binding.monthlyClosingDateRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = getString(R.string.monthly_closing_date);
                showListMenuDialog(title, dateList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferencesManager.saveMonthlyClosingDateIndex(
                                getApplicationContext(), i);
                        binding.monthlyClosingDateValue.setText(dateList[i]);
                        BroadcastUtil.sendReloadReport(ProfitLossSettingsActivity.this);
                    }
                });
            }
        });


        // Start month of year row
        final String[] monthItems = getResources().getStringArray(R.array.month_list);
        int monthIndex = SharedPreferencesManager.getStartMonthOfYearIndex(this);
        binding.startMonthOfYearValue.setText(monthItems[monthIndex]);
        binding.startMonthOfYearRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = getString(R.string.start_month_of_year);
                showListMenuDialog(title, monthItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferencesManager.saveStartMonthOfYearIndex(
                                getApplicationContext(), i);
                        binding.startMonthOfYearValue.setText(monthItems[i]);
                        BroadcastUtil.sendReloadReport(ProfitLossSettingsActivity.this);
                    }
                });
            }
        });
    }

    private void showListMenuDialog(String title, String[] items, DialogInterface.OnClickListener clickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setItems(items, clickListener);
        AlertDialog menuDialog = builder.create();
        menuDialog.setTitle(title);
        menuDialog.show();
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
