package com.example.taxnoteandroid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.example.taxnoteandroid.Library.AsyncOkHttpClient;
import com.example.taxnoteandroid.Library.taxnote.TNApiUser;
import com.example.taxnoteandroid.databinding.ActivityLoginCloudBinding;

import okhttp3.Headers;
import okhttp3.Response;

/**
 * Created by b0ne on 2017/03/21.
 */

public class LoginCloudActivity extends DefaultCommonActivity {

    private ActivityLoginCloudBinding binding;

    public static void start(Context context) {
        Intent intent = new Intent(context, LoginCloudActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void startForResult(Activity activity, int requestCode) {
        Intent intent = new Intent(activity.getApplicationContext(), LoginCloudActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login_cloud);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        binding.btnSendLogin.setOnClickListener(onClickAction);
        binding.btnForgotPasswd.setOnClickListener(onClickAction);
    }

    private View.OnClickListener onClickAction = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int viewId = view.getId();
            switch (viewId) {
                case R.id.btn_send_login:
                    sendLogin();
                    break;
                case R.id.btn_forgot_passwd:
                    sendForgotPasswd();
                    break;
            }
        }
    };

    private void sendLogin() {
        String email = binding.emailInput.getText().toString();
        String passwd = binding.passwdInput.getText().toString();

        if (email.length() == 0) {
            binding.emailInputLayout.setError(getString(R.string.empty_email_input_error));
            return;
        }
        binding.emailInputLayout.setErrorEnabled(false);
        if (passwd.length() == 0) {
            binding.passwdInputLayout.setError(getString(R.string.empty_password_input_error));
            return;
        }
        binding.passwdInputLayout.setErrorEnabled(false);


        // Progress dialog
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.loading));
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.show();

        final TNApiUser apiUser = new TNApiUser(this, email, passwd);
        apiUser.signIn(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                dialog.dismiss();
                Log.v("TEST", "sign in onFailure ");
                if (throwable != null) {
                    Log.v("TEST", throwable.getMessage());
                }
            }

            @Override
            public void onSuccess(Response response, String content) {
                dialog.dismiss();
                Log.v("TEST", "sign in onSuccess content : " + content);
                Headers headers = response.headers();
                apiUser.saveLoginWithHttpHeaders(headers);

                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void sendForgotPasswd() {

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
