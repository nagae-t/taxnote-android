package com.example.taxnoteandroid;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.taxnoteandroid.Library.FileUtil;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityImportFilterBinding;

/**
 * Created by b0ne on 2017/03/08.
 */

public class ImportFilterActivity extends AppCompatActivity {

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
                sendBroadcast(new Intent(MainActivity.BROADCAST_RESTART_APP));
                finish();
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                Intent intentCompat = IntentCompat.makeRestartActivityTask(i.getComponent());
                startActivity(intentCompat);

            }
        });

        new DataImportTask().execute(intentUri);
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

            binding.loadingLayout.setVisibility(View.GONE);
            binding.backupFinishedLayout.setVisibility(View.VISIBLE);
        }
    }

}
