package com.example.taxnoteandroid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.example.taxnoteandroid.Library.AsyncOkHttpClient;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.KeyboardUtil;
import com.example.taxnoteandroid.Library.taxnote.TNApiModel;
import com.example.taxnoteandroid.Library.taxnote.TNApiUser;
import com.example.taxnoteandroid.databinding.ActivityLoginCloudBinding;
import com.example.taxnoteandroid.model.OrmaDatabase;

import okhttp3.Response;

/**
 * Created by b0ne on 2017/03/21.
 */

public class LoginCloudActivity extends DefaultCommonActivity {

    private ActivityLoginCloudBinding binding;
    private int mViewType;

    private static final String KEY_VIEW_TYPE = "view_type";
    public static final int VIEW_TYPE_LOGIN = 0;
    public static final int VIEW_TYPE_REGISTER = 1;

    public static void start(Context context) {
        Intent intent = new Intent(context, LoginCloudActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_VIEW_TYPE, VIEW_TYPE_LOGIN);
        context.startActivity(intent);
    }

    public static void startForResult(Activity activity, int requestCode, int viewType) {
        Intent intent = new Intent(activity.getApplicationContext(), LoginCloudActivity.class);
        intent.putExtra(KEY_VIEW_TYPE, viewType);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login_cloud);
        mViewType = getIntent().getIntExtra(KEY_VIEW_TYPE, VIEW_TYPE_LOGIN);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Set view type
        if (mViewType == VIEW_TYPE_REGISTER) {
            setTitle(R.string.cloud_register);
            binding.topDesc.setText(R.string.taxnote_cloud_register_desc);
            binding.passwdConfirmInputLayout.setVisibility(View.VISIBLE);
            binding.btnForgotPasswd.setVisibility(View.GONE);
            binding.btnSendLogin.setText(R.string.cloud_register);
        }

        binding.btnSendLogin.setOnClickListener(onClickAction);
        binding.btnForgotPasswd.setOnClickListener(onClickAction);
    }

    private View.OnClickListener onClickAction = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            KeyboardUtil.hideKeyboard(LoginCloudActivity.this, view);
            int viewId = view.getId();
            switch (viewId) {
                case R.id.btn_send_login:
                    showLoginConfirm();
                    break;
                case R.id.btn_forgot_passwd:
                    sendForgotPasswd();
                    break;
            }
        }
    };

    // ログインまたはアカウント作成の入力チェックと確認ダイアログ表示
    private void showLoginConfirm() {
        final String email = binding.emailInput.getText().toString();
        final String passwd = binding.passwdInput.getText().toString();
        final String passwdConfirm = binding.passwdConfirmInput.getText().toString();

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

        // 登録の場合のチェック
        if (mViewType == VIEW_TYPE_REGISTER) {
            if (passwdConfirm.length() == 0) {
                binding.passwdConfirmInputLayout.setError(getString(R.string.empty_password_confrim_input_error));
                return;
            }
            binding.passwdConfirmInputLayout.setErrorEnabled(false);
            if (!passwd.equals(passwdConfirm)) {
                binding.passwdConfirmInputLayout.setError(getString(R.string.password_confrim_match_error));
                return;
            }
            binding.passwdConfirmInputLayout.setErrorEnabled(false);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (mViewType == VIEW_TYPE_REGISTER) {
            // Show confirm register
            builder.setTitle(email)
                    .setMessage(R.string.confirm_email)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.cloud_register, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sendRegister(email, passwd);
                        }
                    })
                    .show();
        } else {

            // Show confirm sync data dialog message
            builder.setTitle(R.string.login_confirm_title)
                    .setMessage(R.string.login_confirm_desc)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sendLogin(email, passwd);
                        }
                    })
                    .show();
        }
    }

    private void sendLogin(String email, String passwd) {
        // Progress dialog
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.loading));
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        final TNApiUser apiUser = new TNApiUser(this, email, passwd);
        apiUser.signIn(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                dialog.dismiss();

                if (response != null) {
                    int httpStatusCode = response.code();
                    if (httpStatusCode == 401) {
                        DialogManager.showOKOnlyAlert(LoginCloudActivity.this,
                                getResources().getString(R.string.login_error),
                                getResources().getString(R.string.login_error_desc));
                    } else {
                        String errorMsg = response.message();
                        if (throwable != null) {
                            errorMsg = throwable.getLocalizedMessage();
                        }
                        DialogManager.showOKOnlyAlert(LoginCloudActivity.this,
                                getResources().getString(R.string.login_error),
                                errorMsg);
                    }
                }

            }

            @Override
            public void onSuccess(Response response, String content) {

                dialog.setMessage(getString(R.string.login_success_wait_fetching));
                /**/
                final TNApiModel apiModel = new TNApiModel(getApplicationContext());
                //@@  ログイン成功後の処理
                apiModel.resetAllUpdatedKeys();
                //@@ DB全データの削除
                OrmaDatabase _db = TaxnoteApp.getOrmaDatabase();
                _db.deleteAll();

                if (apiModel.isSyncing()) return;

                apiModel.setIsSyncing(true);
                apiModel.getAllDataAfterLogin(new AsyncOkHttpClient.Callback() {
                    @Override
                    public void onFailure(Response response, Throwable throwable) {
                        apiModel.setIsSyncing(false);
                        dialog.dismiss();
                        Log.e("ERROR", "getAllDataAfterLogin onFailure code: ");
                        if (response != null)
                            Log.e("ERROR", "response.code: " + response.code()
                                + ", message : " + response.message());
                    }

                    @Override
                    public void onSuccess(Response response, String content) {
                        apiModel.setIsSyncing(false);
                        dialog.dismiss();
                        Log.v("TEST", "getAllDataAfterLogin onSuccess finish");
                        setResult(RESULT_OK);
                        finish();
                    }
                });/**/
            }
        });
    }

    private void sendRegister(String email, String passwd) {
        // Progress dialog
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.loading));
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        final TNApiUser apiUser = new TNApiUser(this, email, passwd);
        apiUser.setPasswordConfirm(passwd);
        apiUser.register(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                dialog.dismiss();

                String errorMsg = "";
                if (response != null) {
                    errorMsg = response.message();
                } else if (throwable != null) {
                    errorMsg = throwable.getLocalizedMessage();
                }
                DialogManager.showOKOnlyAlert(LoginCloudActivity.this,
                        getString(R.string.register_error),
                        errorMsg);
            }

            @Override
            public void onSuccess(Response response, String content) {

                dialog.setMessage(getString(R.string.register_success_wait_uploading));

                final TNApiModel apiModel = new TNApiModel(getApplicationContext());
                apiModel.saveAllDataAfterRegister(new AsyncOkHttpClient.Callback() {
                    @Override
                    public void onFailure(Response response, Throwable throwable) {
                        dialog.dismiss();
                        String errorMsg = "";
                        if (response != null) {
                            errorMsg = response.message();
                        } else if (throwable != null) {
                            errorMsg = throwable.getLocalizedMessage();
                        }
                        DialogManager.showOKOnlyAlert(LoginCloudActivity.this,
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
