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
import android.widget.EditText;

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
                    showForgotPasswordDialog();
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
        if (passwd.length() < 8) {
            binding.passwdInputLayout.setError(getString(R.string.password_is_too_short));
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
                            dialogInterface.dismiss();
                            sendLogin(email, passwd);
                        }
                    })
                    .show();
        }
    }

    private void sendLogin(String email, String passwd) {
        // Progress dialog
        final ProgressDialog dialog = getLoadingDialog();
        if (!dialog.isShowing())
            dialog.show();

        final TNApiUser apiUser = new TNApiUser(this, email, passwd);
        apiUser.signIn(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e("ERROR", "sendLogin onFailure");
                dialog.dismiss();

                if (response != null) {
                    Log.v("ERROR", "sendLogin onFailure header : " + response.headers());
                    int httpStatusCode = response.code();
                    Log.v("ERROR", "sendLogin onFailure http code: " + httpStatusCode
                            + ", message: " + response.message());
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

                if (throwable != null)
                    Log.e("ERROR", "sendLogin onFailure : " + throwable.getLocalizedMessage());

            }

            @Override
            public void onSuccess(Response response, String content) {

                dialog.setMessage(getString(R.string.login_success_wait_fetching));

                final TNApiModel apiModel = new TNApiModel(getApplicationContext());
                //@@  ログイン成功後の処理
                //@@ DB全データの削除
                OrmaDatabase _db = TaxnoteApp.getOrmaDatabase();
                _db.deleteAll();

                if (apiModel.isSyncing()) return;

                // ログイン後にデータを同期するがその前に少しまつ
                // sleep
                try{
                    Thread.sleep(300);
                }catch (InterruptedException e){
                }

                apiModel.setIsSyncing(true);
                apiModel.getAllDataAfterLogin(new AsyncOkHttpClient.Callback() {
                    @Override
                    public void onFailure(Response response, Throwable throwable) {
                        apiModel.setIsSyncing(false);
                        dialog.dismiss();
                        Log.e("ERROR", "getAllDataAfterLogin onFailure ");
                        if (response != null)
                            Log.e("ERROR", "response.code: " + response.code()
                                + ", message : " + response.message());
                        if (throwable != null)
                            Log.e("ERROR", "message: " + throwable.getLocalizedMessage());
                    }

                    @Override
                    public void onSuccess(Response response, String content) {
                        apiModel.setIsSyncing(false);

                        setResult(RESULT_OK);
                        finish();
                    }
                });
            }
        });
    }

    private void sendRegister(String email, String passwd) {
        // Progress dialog
        final ProgressDialog dialog = getLoadingDialog();
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
                apiModel.setIsSyncing(true);
                apiModel.saveAllDataAfterRegister(new AsyncOkHttpClient.Callback() {
                    @Override
                    public void onFailure(Response response, Throwable throwable) {
                        apiModel.setIsSyncing(false);
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
                        apiModel.setIsSyncing(false);
                        dialog.dismiss();
                        setResult(RESULT_OK);
                        finish();
                    }
                });

            }
        });
    }

    private void showForgotPasswordDialog() {
        String inputEmail = binding.emailInput.getText().toString();

        final TNSimpleDialogFragment dialogFragment = TNSimpleDialogFragment.newInstance();
        dialogFragment.setTitle(getString(R.string.reset_password));
        dialogFragment.setPositiveBtnText(getString(R.string.send));
        dialogFragment.setNegativeBtnText(getString(android.R.string.cancel));

        View _view = getLayoutInflater().inflate(R.layout.forgot_password_dialog, null);
        if (inputEmail.length() > 0) {
            EditText ed = (EditText) _view.findViewById(R.id.email_input);
            ed.setText(inputEmail);
        }
        dialogFragment.setDialogView(_view);
        final View dialogView = dialogFragment.getDialogView();

        dialogFragment.setDialogListener(new TNSimpleDialogFragment.TNSimpleDialogListener() {
            @Override
            public void onPositiveBtnClick(DialogInterface dialogInterface, int i, String tag) {
                KeyboardUtil.hideKeyboard(LoginCloudActivity.this, dialogView);
                EditText ed = (EditText) dialogView.findViewById(R.id.email_input);
                if (ed == null) return;

                String targetEmail = ed.getText().toString();
                if (targetEmail.length() == 0) {
                    DialogManager.showOKOnlyAlert(getApplicationContext(), null,
                            getString(R.string.empty_email_input_error));
                    return;
                }

                 sendForgotPasswd(targetEmail);
            }

            @Override
            public void onNeutralBtnClick(DialogInterface dialogInterface, int i, String tag) {

            }

            @Override
            public void onNegativeBtnClick(DialogInterface dialogInterface, int i, String tag) {

            }

            @Override
            public void onDialogCancel(DialogInterface dialogInterface, String tag) {

            }

            @Override
            public void onDialogDismiss(DialogInterface dialogInterface, String tag) {

            }
        });
        dialogFragment.show(getSupportFragmentManager(), null);
    }

    private void sendForgotPasswd(String email) {
        final ProgressDialog loadingDialog = getLoadingDialog();
        loadingDialog.show();

        final TNApiUser apiUser = new TNApiUser(this);
        apiUser.setEmail(email);
        apiUser.sendForgotPassword(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                loadingDialog.dismiss();
                Log.e("ERROR", "sendForgotPasswd onFailure");
                if (response != null) {
                    String errorMsg = response.message();
                    int httpCode = response.code();
                    if (httpCode == 404) {
                        DialogManager.showOKOnlyAlert(LoginCloudActivity.this,
                                R.string.non_registered_email, R.string.non_registered_email_desc);
                    } else {
                        DialogManager.showOKOnlyAlert(LoginCloudActivity.this,
                                "Error", errorMsg);
                    }
                }
            }

            @Override
            public void onSuccess(Response response, String content) {
                loadingDialog.dismiss();
                DialogManager.showOKOnlyAlert(LoginCloudActivity.this,
                        R.string.reset_password_sent, R.string.reset_password_sent_desc);
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

    private ProgressDialog getLoadingDialog(){
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.loading));
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}
