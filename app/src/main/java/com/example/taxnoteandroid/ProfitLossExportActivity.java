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

import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityProfitLossExportBinding;

import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_CHARACTER_CODE_SHIFTJIS;
import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_CHARACTER_CODE_UTF8;

/**
 * Created by b0ne on 2017/03/14.
 */

public class ProfitLossExportActivity extends DefaultCommonActivity {

    private ActivityProfitLossExportBinding binding;

    public static void start(Context context) {
        Intent intent = new Intent(context, ProfitLossExportActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_profit_loss_export);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // 文字コード
        String charCode = SharedPreferencesManager.getCurrentCharacterCode(this);
        binding.charCodeValue.setText(charCode);

        binding.charCodeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCharCodeMenuDialog();
            }
        });

        //@@ help link
        binding.help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //@@ 対象のヘルプページへ
            }
        });

        // CSV 出力
        binding.csvExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void showCharCodeMenuDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String[] items = {EXPORT_CHARACTER_CODE_UTF8, EXPORT_CHARACTER_CODE_SHIFTJIS};
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        SharedPreferencesManager.saveCurrentCharacterCode(ProfitLossExportActivity.this, EXPORT_CHARACTER_CODE_UTF8);
                        binding.charCodeValue.setText(EXPORT_CHARACTER_CODE_UTF8);
                        DialogManager.showToast(ProfitLossExportActivity.this, EXPORT_CHARACTER_CODE_UTF8);
                        break;
                    case 1:
                        SharedPreferencesManager.saveCurrentCharacterCode(ProfitLossExportActivity.this, EXPORT_CHARACTER_CODE_SHIFTJIS);
                        binding.charCodeValue.setText(EXPORT_CHARACTER_CODE_SHIFTJIS);
                        DialogManager.showToast(ProfitLossExportActivity.this, EXPORT_CHARACTER_CODE_SHIFTJIS);
                        break;
                }
            }
        });
        AlertDialog menuDialog = builder.create();
        menuDialog.setTitle(getString(R.string.character_code_select_please));
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
