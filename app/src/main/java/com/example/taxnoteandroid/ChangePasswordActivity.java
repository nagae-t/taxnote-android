package com.example.taxnoteandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.example.taxnoteandroid.Library.AsyncOkHttpClient;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.KeyboardUtil;
import com.example.taxnoteandroid.Library.taxnote.TNApiUser;
import com.example.taxnoteandroid.databinding.ActivityChangePasswordBinding;

import okhttp3.Response;

/**
 * Created by b0ne on 2017/03/27.
 */

public class ChangePasswordActivity extends DefaultCommonActivity {

    private ActivityChangePasswordBinding binding;

    public static void start(Context context) {
        Intent intent = new Intent(context, ChangePasswordActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void startForResult(Activity activity, int requestCode) {
        Intent intent = new Intent(activity.getApplicationContext(), ChangePasswordActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_change_password);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        binding.btnSendUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KeyboardUtil.hideKeyboard(ChangePasswordActivity.this, view);
                sendChangePassword();
            }
        });

    }

    private void sendChangePassword() {
        String passwd = binding.passwdInput.getText().toString();
        String passwdConfirm = binding.passwdConfirmInput.getText().toString();
        if (passwd.length() == 0) {
            binding.passwdInputLayout.setError(getString(R.string.empty_password_input_error));
            return;
        }
        binding.passwdInputLayout.setErrorEnabled(false);
        if (passwdConfirm.length() == 0) {
            binding.passwdConfirmInputLayout.setError(getString(R.string.empty_password_confrim_input_error));
            return;
        }
        binding.passwdConfirmInputLayout.setErrorEnabled(false);

        if (passwd.length() < 8) {
            binding.passwdInputLayout.setError(getString(R.string.password_is_too_short));
            return;
        }
        binding.passwdInputLayout.setErrorEnabled(false);

        if (!passwd.equals(passwdConfirm)) {
            binding.passwdConfirmInputLayout.setError(getString(R.string.password_confrim_match_error));
            return;
        }
        binding.passwdConfirmInputLayout.setErrorEnabled(false);

        final TNSimpleDialogFragment dialog = DialogManager.getLoading(this);
        dialog.show(getSupportFragmentManager(), null);


        final TNApiUser apiUser = new TNApiUser(this);
        apiUser.updatePassword(passwd, new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e("ERROR", "sendChangePassword onFailure");
                String errorMsg = "";
                if (response != null) {
                    errorMsg = response.message();
                    Log.e("ERROR", "sendChangePassword onFailure code: "
                        + response.code() + ", message: " + errorMsg);
                }
                if (throwable != null) {
                    errorMsg = throwable.getLocalizedMessage();
                }
                dialog.dismiss();
                DialogManager.showOKOnlyAlert(ChangePasswordActivity.this,
                        "Error", errorMsg);
            }

            @Override
            public void onSuccess(Response response, String content) {
                dialog.dismiss();
                setResult(RESULT_OK);
                finish();
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
