package com.example.taxnoteandroid;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
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
import com.example.taxnoteandroid.dataManager.AccountDataManager;
import com.example.taxnoteandroid.dataManager.DefaultDataInstaller;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.ReasonDataManager;
import com.example.taxnoteandroid.dataManager.RecurringDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.dataManager.SummaryDataManager;
import com.example.taxnoteandroid.databinding.ActivityLoginCloudBinding;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason;
import com.example.taxnoteandroid.model.Recurring;
import com.example.taxnoteandroid.model.Summary;

import java.util.List;

import okhttp3.Response;

/**
 * Created by b0ne on 2017/03/21.
 */

public class LoginCloudActivity extends DefaultCommonActivity {

    private ActivityLoginCloudBinding binding;
    private int mViewType;

    private TNSimpleDialogFragment mDialogReg;
    private TNSimpleDialogFragment mLoadingDialog;

    private static final String KEY_VIEW_TYPE = "view_type";
    private static final String KEY_EMAIL = "email";
    public static final int VIEW_TYPE_LOGIN = 0;
    public static final int VIEW_TYPE_REGISTER = 1;

    public static void start(Context context) {
        start(context, null);
    }

    public static void start(Context context, String email) {
        Intent intent = new Intent(context, LoginCloudActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_VIEW_TYPE, VIEW_TYPE_LOGIN);
        intent.putExtra(KEY_EMAIL, email);
        context.startActivity(intent);
    }

    public static void startForResult(Activity activity, int requestCode, int viewType) {
        startForResult(activity, requestCode, viewType, null);
    }
    public static void startForResult(Activity activity, int requestCode, int viewType, String email) {
        Intent intent = new Intent(activity.getApplicationContext(), LoginCloudActivity.class);
        intent.putExtra(KEY_VIEW_TYPE, viewType);
        intent.putExtra(KEY_EMAIL, email);
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

        String emailIntent = getIntent().getStringExtra(KEY_EMAIL);
        if (emailIntent != null) {
            binding.emailInput.setText(emailIntent);
            binding.passwdInput.requestFocus();
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

    // ?????????????????????????????????????????????????????????????????????????????????????????????
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

        // ??????????????????????????????
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
        final TNSimpleDialogFragment dialog = DialogManager.getLoading(this);
        if (!dialog.isVisible())
            dialog.show(getSupportFragmentManager(), null);

        final TNApiUser apiUser = new TNApiUser(this, email, passwd);
        apiUser.signIn(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                Log.e("ERROR", "sendLogin onFailure");
                dialog.dismiss();

                if (response != null) {
                    Log.e("ERROR", "sendLogin onFailure header : " + response.headers());
                    int httpStatusCode = response.code();
                    Log.e("ERROR", "sendLogin onFailure http code: " + httpStatusCode
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
                dialog.dismissAllowingStateLoss();
                
                final TNSimpleDialogFragment afterLoginDialog = getDialogAfterRegister();
                afterLoginDialog.show(getSupportFragmentManager(), null);

                final TNApiModel apiModel = new TNApiModel(getApplicationContext());
                //@@  ??????????????????????????????
                //@@ DB?????????????????????
                OrmaDatabase _db = TaxnoteApp.getOrmaDatabase();
                _db.deleteAll();

                if (apiModel.isSyncing()) return;

                // ?????????????????????????????????????????????????????????????????????
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
                        afterLoginDialog.dismissAllowingStateLoss();
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

                        Context context = getApplicationContext();
                        SharedPreferencesManager.saveAppThemeStyle(context, 0);
                        ProjectDataManager projectManager = new ProjectDataManager(context);
                        DefaultDataInstaller.switchProject(context, projectManager.findAll(true).get(0));

                        setResult(RESULT_OK);
                        finish();
                    }
                }, afterLoginDialog);
            }
        });
    }

    private void sendRegister(String email, String passwd) {
        mDialogReg = getDialogAfterRegister();
        mLoadingDialog = DialogManager.getLoading(this);
        mLoadingDialog.show(getSupportFragmentManager(), null);

        final TNApiUser apiUser = new TNApiUser(this, email, passwd);
        apiUser.setPasswordConfirm(passwd);
        apiUser.register(new AsyncOkHttpClient.Callback() {
            @Override
            public void onFailure(Response response, Throwable throwable) {
                mLoadingDialog.dismiss();

                String errorMsg = "";
                if (response != null) {
                    errorMsg = response.message();
                } else if (throwable != null) {
                    errorMsg = throwable.getLocalizedMessage();
                }
                if (response.code() == 403) {
                    errorMsg = getString(R.string.email_already_use);
                }
                DialogManager.showOKOnlyAlert(LoginCloudActivity.this,
                        getString(R.string.register_error),
                        errorMsg);
            }

            @Override
            public void onSuccess(Response response, String content) {
                mLoadingDialog.dismiss();
                mDialogReg.show(getSupportFragmentManager(), null);
                new SetNeedSaveDataTask().execute();
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
        final TNSimpleDialogFragment loadingDialog = DialogManager.getLoading(this);
        loadingDialog.show(getSupportFragmentManager(), null);

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


    private TNSimpleDialogFragment getDialogAfterRegister() {
        final TNSimpleDialogFragment dialogFragment = TNSimpleDialogFragment.newInstance();

        dialogFragment.setContentViewId(R.layout.dialog_progress_bar_loading);
        dialogFragment.setCloseToFinish(true);
        dialogFragment.setCancelable(false);

        return dialogFragment;
    }

    // ??????????????????????????????????????????
    // iOS?????? setNeedSaveForOldCoreDataModel ?????????
    private class SetNeedSaveDataTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            Context context = getApplicationContext();
            ProjectDataManager projectDataManager = new ProjectDataManager(context);
            List<Project> projectList = projectDataManager.findAll();
            for (Project project : projectList) {
                project.needSave = true;
                project.needSync = false;
                projectDataManager.update(project);
            }

            ReasonDataManager reasonDataManager = new ReasonDataManager(context);
            List<Reason> reasonList = reasonDataManager.findAll();
            for (Reason reason : reasonList) {
                reason.needSave = true;
                reason.needSync = false;
                reasonDataManager.update(reason);
            }

            AccountDataManager accountDataManager = new AccountDataManager(context);
            List<Account> accList = accountDataManager.findAll();
            for (Account account : accList) {
                account.needSave = true;
                account.needSync = false;
                accountDataManager.update(account);
            }

            SummaryDataManager summaryDataManager = new SummaryDataManager(context);
            List<Summary> sumList = summaryDataManager.findAll();
            for (Summary summ : sumList) {
                summ.needSave = true;
                summ.needSync = false;
                summaryDataManager.update(summ);
            }

            RecurringDataManager recurringDataManager = new RecurringDataManager(context);
            List<Recurring> recList = recurringDataManager.findAll();
            for (Recurring rec : recList) {
                rec.needSave = true;
                rec.needSync = false;
                recurringDataManager.update(rec);
            }

            EntryDataManager entryDataManager = new EntryDataManager(context);
            entryDataManager.updateAllNeedSave();


            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            final TNApiModel apiModel = new TNApiModel(getApplicationContext());
            apiModel.setIsSyncing(true);
            apiModel.saveAllDataAfterRegister(new AsyncOkHttpClient.ResponseCallback() {
                @Override
                public void onFailure(Response response, Throwable throwable) {
                    apiModel.setIsSyncing(false);
                    mDialogReg.dismissAllowingStateLoss();
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
                public void onUpdate(long bytesRead, long contentLength, boolean done) {
                }

                @Override
                public void onSuccess(Response response, String content) {
                    apiModel.setIsSyncing(false);
                    mDialogReg.dismissAllowingStateLoss();
                    setResult(RESULT_OK);
                    finish();
                }
            }, mDialogReg);


        }

    }
}
