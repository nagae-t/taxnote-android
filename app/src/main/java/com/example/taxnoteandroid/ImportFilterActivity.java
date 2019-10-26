package com.example.taxnoteandroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.util.Log;
import android.view.View;

import com.example.taxnoteandroid.Library.AsyncOkHttpClient;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.FileUtil;
import com.example.taxnoteandroid.Library.taxnote.TNApiModel;
import com.example.taxnoteandroid.dataManager.DefaultDataInstaller;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityImportFilterBinding;

import okhttp3.Response;

/**
 * Created by b0ne on 2017/03/08.
 */

public class ImportFilterActivity extends DefaultCommonActivity {

    private ActivityImportFilterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_import_filter);

        Intent intent = getIntent();
        if (intent == null) finish();

        String action = intent.getAction();
        if (action == null || !action.equals(Intent.ACTION_SEND))
            finish();

        ShareCompat.IntentReader intentReader = ShareCompat.IntentReader.from(this);
        if (!intentReader.isShareIntent()) finish();

        Uri intentUri = intentReader.getStream();
        if (intentUri == null) finish();

        binding.btnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DefaultDataInstaller.restartApp(ImportFilterActivity.this);

            }
        });

        DialogManager.showImportDataConfirm(this, getSupportFragmentManager(),
                getConfirmDialogListener(intentUri));

    }

    private TNSimpleDialogFragment.TNSimpleDialogListener getConfirmDialogListener(final Uri importFileUri) {
        return new TNSimpleDialogFragment.TNSimpleDialogListener() {
            @Override
            public void onPositiveBtnClick(DialogInterface dialogInterface, int i, String tag) {
                dialogInterface.dismiss();

                new DataImportTask().execute(importFileUri);
            }

            @Override
            public void onNeutralBtnClick(DialogInterface dialogInterface, int i, String tag) {

            }

            @Override
            public void onNegativeBtnClick(DialogInterface dialogInterface, int i, String tag) {
                finish();
            }

            @Override
            public void onDialogCancel(DialogInterface dialogInterface, String tag) {
            }

            @Override
            public void onDialogDismiss(DialogInterface dialogInterface, String tag) {
            }
        };
    }


    private class DataImportTask extends AsyncTask<Uri, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Uri... params) {
            FileUtil.dataImport(getApplicationContext(), params[0]);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // set theme style to default
            SharedPreferencesManager.saveAppThemeStyle(getApplicationContext(), 0);

            final TNApiModel apiModel = new TNApiModel(getApplicationContext());
            if (!apiModel.isLoggingIn() || apiModel.isSyncing()) {
                binding.loadingLayout.setVisibility(View.GONE);
                binding.backupFinishedLayout.setVisibility(View.VISIBLE);
                return;
            }

            // ログインしていればデータを同期するように
            apiModel.setIsSyncing(true);
            apiModel.saveAllDataAfterRegister(new AsyncOkHttpClient.ResponseCallback() {
                @Override
                public void onFailure(Response response, Throwable throwable) {
                    Log.e("Error", "DataImportTask syncData onFailure");
                    apiModel.setIsSyncing(false);

                    String errorMsg = "";
                    if (response != null) {
                        errorMsg = response.message();
                    } else if (throwable != null) {
                        errorMsg = throwable.getLocalizedMessage();
                    }
                    DialogManager.showOKOnlyAlert(ImportFilterActivity.this,
                            "Error", errorMsg);
                }

                @Override
                public void onUpdate(long bytesRead, long contentLength, boolean done) {
                }

                @Override
                public void onSuccess(Response response, String content) {
                    apiModel.setIsSyncing(false);

                    binding.loadingLayout.setVisibility(View.GONE);
                    binding.backupFinishedLayout.setVisibility(View.VISIBLE);
                }
            });

        }
    }

}
