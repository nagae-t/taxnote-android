package com.example.taxnoteandroid;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.example.taxnoteandroid.dataManager.AccountDataManager;
import com.example.taxnoteandroid.dataManager.ReasonDataManager;
import com.example.taxnoteandroid.dataManager.RecurringDataManager;
import com.example.taxnoteandroid.databinding.ActivityInputRecurringEditBinding;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Reason;
import com.example.taxnoteandroid.model.Recurring;

import java.util.List;
import java.util.TimeZone;

/**
 * Created by b0ne on 2017/04/11.
 */

public class InputRecurringEditActivity extends DefaultCommonActivity {

    private ActivityInputRecurringEditBinding binding;
    private RecurringDataManager mRecurringDm;
    private AccountDataManager mAccountDm;
    private ReasonDataManager mReasonDm;
    private String[] mRecurringDates;
    private String[] mTimeZoneAll = TimeZone.getAvailableIDs();
    private Recurring mRecurring;

    private static final String KEY_IS_EXPENSE = "is_expense";
    private static final int REQUEST_CODE_ACCOUNT = 1;
    private static final int REQUEST_CODE_REASON = 2;
    private static final int REQUEST_CODE_PRICE = 3;

    public static void start(Context context, boolean isExpense) {
        Intent intent = new Intent(context, InputRecurringEditActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_IS_EXPENSE, isExpense);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_input_recurring_edit);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mRecurring = new Recurring();
        mAccountDm = new AccountDataManager(this);
        mReasonDm = new ReasonDataManager(this);
        mRecurringDm = new RecurringDataManager(this);
        mRecurringDates = mRecurringDm.getDesignatedDateList();

        boolean isExpense = getIntent().getBooleanExtra(KEY_IS_EXPENSE, false);
        setTitle(R.string.Income);
        if (isExpense) {
            setTitle(R.string.Expense);
        }

//        Recurring _rec = new Recurring();
//        _rec.isExpense = isExpense;
//        binding.setRecurring(_rec);

        // click action
        binding.timezoneSelect.setOnClickListener(onItemClick);
        binding.dateSelect.setOnClickListener(onItemClick);
        binding.accountSelect.setOnClickListener(onItemClick);
        binding.reasonSelect.setOnClickListener(onItemClick);
        binding.memoSelect.setOnClickListener(onItemClick);
        binding.priceSelect.setOnClickListener(onItemClick);

        // set default for new
        mRecurring.isExpense = isExpense;
        mRecurring.timezone = TimeZone.getDefault().getID();
        mRecurring.dateIndex = 0;
        List<Account> accounts = mAccountDm.findAllWithIsExpense(isExpense);
        mRecurring.account = accounts.get(0);
        List<Reason> reasons = mReasonDm.findAllWithIsExpense(isExpense);
        mRecurring.reason = reasons.get(0);

        // set view
        binding.setRecurring(mRecurring);
        binding.dateSelect.setText(mRecurringDates[Integer.valueOf(mRecurring.dateIndex+"")]);
        String priceString = (mRecurring.price == 0) ? "0" : mRecurring.price+"";
        binding.priceSelect.setText(priceString);
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

    private View.OnClickListener onItemClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int viewId = view.getId();
            switch (viewId) {
                case R.id.timezone_select:
                    showTimezoneDialog();
                    break;
                case R.id.date_select:
                    showDateListDialog();
                    break;
                case R.id.account_select:
                    AccountEditActivity.startForRecurring(InputRecurringEditActivity.this,
                            mRecurring.isExpense, true, REQUEST_CODE_ACCOUNT);
                    break;
                case R.id.reason_select:
                    AccountEditActivity.startForRecurring(InputRecurringEditActivity.this,
                            mRecurring.isExpense, false, REQUEST_CODE_REASON);
                    break;
                case R.id.memo_select:
                    showMemoInputDialog();
                    break;
                case R.id.price_select:
                    CalculatorActivity.startForResult(InputRecurringEditActivity.this,
                            mRecurring.price, REQUEST_CODE_PRICE);
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_CODE_ACCOUNT) {
                String accountUuid = data.getStringExtra("account_uuid");
                Account account = mAccountDm.findByUuid(accountUuid);
                mRecurring.account = account;
                binding.setRecurring(mRecurring);

            } else if (requestCode == REQUEST_CODE_REASON) {
                String reasonUuid = data.getStringExtra("reason_uuid");
                Reason reason = mReasonDm.findByUuid(reasonUuid);
                mRecurring.reason = reason;
                binding.setRecurring(mRecurring);
            } else if (requestCode == REQUEST_CODE_PRICE) {
                mRecurring.price = data.getLongExtra(CalculatorActivity.KEY_CURRENT_PRICE, 0);
                String priceString = (mRecurring.price == 0) ? "0" : mRecurring.price+"";
                binding.priceSelect.setText(priceString);
            }
        }
    }

    private void showTimezoneDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setItems(mTimeZoneAll, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog menuDialog = builder.create();
//        menuDialog.setTitle(title);
        menuDialog.show();
    }

    private void showDateListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setItems(mRecurringDates, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog menuDialog = builder.create();
//        menuDialog.setTitle(title);
        menuDialog.show();
    }

    private void showMemoInputDialog() {
        final View textInputView    = LayoutInflater.from(this).inflate(R.layout.dialog_text_input, null);
        final EditText editText     = (EditText) textInputView.findViewById(R.id.edit);
        editText.setText(mRecurring.memo);
        new AlertDialog.Builder(this)
                .setView(textInputView)
                .setTitle(getString(R.string.Details))
                .setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // Get the input string
                        mRecurring.memo = editText.getText().toString();
                        binding.setRecurring(mRecurring);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), null)
                .show();
    }
}
