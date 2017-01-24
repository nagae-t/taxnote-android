package com.example.taxnoteandroid.entryTab;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.EntryLimitManager;
import com.example.taxnoteandroid.Library.ValueConverter;
import com.example.taxnoteandroid.R;
import com.example.taxnoteandroid.UpgradeActivity;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Entry;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason;
import com.example.taxnoteandroid.model.Summary;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.UUID;

public class InputDataActivity extends AppCompatActivity {

    private TextView priceTextView;
    private static final String EXTRA_IS_EXPENSE = "isExpense";
    private static final String EXTRA_DATE       = "date";
    public boolean isExpense;
    public Account account;
    public Reason reason;
    public Summary summary;
    public long date;
    private long currentPrice = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_input_data);

        setIntentData();
        setTitle();
        setSummary();
        setSaveButton();
        setPriceInputPart();

        DialogManager.showTapRegisterMessage(InputDataActivity.this);
    }


    //--------------------------------------------------------------//
    //    -- Intent --
    //--------------------------------------------------------------//

    public static Intent createIntent(Context context, boolean isExpense, long date, Account account, Reason reason, Summary summary) {

        Intent i = new Intent(context, InputDataActivity.class);

        i.putExtra(EXTRA_IS_EXPENSE, isExpense);
        i.putExtra(Account.class.getName(), Parcels.wrap(account));
        i.putExtra(Reason.class.getName(), Parcels.wrap(reason));
        i.putExtra(Summary.class.getName(), Parcels.wrap(summary));
        i.putExtra(EXTRA_DATE, date);

        return i;
    }

    private void setIntentData() {

        Intent intent = getIntent();

        isExpense   = intent.getBooleanExtra(EXTRA_IS_EXPENSE, false);
        date        = intent.getLongExtra(EXTRA_DATE, 0);
        account     = Parcels.unwrap(intent.getParcelableExtra(Account.class.getName()));
        reason      = Parcels.unwrap(intent.getParcelableExtra(Reason.class.getName()));
        summary     = Parcels.unwrap(intent.getParcelableExtra(Summary.class.getName()));
    }


    //--------------------------------------------------------------//
    //    -- Display Part --
    //--------------------------------------------------------------//

    private void setTitle() {

        String title;
        String dateString = getResources().getString(R.string.date_string_today);

        // Show the date if it is not today
        if (!DateUtils.isToday(date)) {
            SimpleDateFormat simpleDateFormat    = new SimpleDateFormat(getResources().getString(R.string.date_string_format_to_month_day));
            dateString                           = simpleDateFormat.format(date);
        }

        if (isExpense) {
            title = dateString + " " + reason.name + "/" + account.name;
        } else {
            title = dateString + " " + account.name + "/" + reason.name;
        }

        setTitle(title);
    }

    private void setSummary() {

        if (summary != null) {
            EditText memoField = (EditText) findViewById(R.id.memo);
            memoField.setText(summary.name);
        }
    }

    private void setSaveButton() {

        TextView saveButton = (TextView) findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveEntry();
            }
        });
    }

    private void setPriceInputPart() {

        priceTextView = (TextView) findViewById(R.id.title);

        OnPriceClickListener onPriceClickListener = new OnPriceClickListener();
        findViewById(R.id.button_0).setOnClickListener(onPriceClickListener);
        findViewById(R.id.button_1).setOnClickListener(onPriceClickListener);
        findViewById(R.id.button_2).setOnClickListener(onPriceClickListener);
        findViewById(R.id.button_3).setOnClickListener(onPriceClickListener);
        findViewById(R.id.button_4).setOnClickListener(onPriceClickListener);
        findViewById(R.id.button_5).setOnClickListener(onPriceClickListener);
        findViewById(R.id.button_6).setOnClickListener(onPriceClickListener);
        findViewById(R.id.button_7).setOnClickListener(onPriceClickListener);
        findViewById(R.id.button_8).setOnClickListener(onPriceClickListener);
        findViewById(R.id.button_9).setOnClickListener(onPriceClickListener);
        findViewById(R.id.button_00).setOnClickListener(onPriceClickListener);
        findViewById(R.id.button_c).setOnClickListener(onPriceClickListener);
    }

    private class OnPriceClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            String price = "";
            switch (view.getId()) {
                case R.id.button_0:
                    price = "0";
                    break;
                case R.id.button_1:
                    price = "1";
                    break;
                case R.id.button_2:
                    price = "2";
                    break;
                case R.id.button_3:
                    price = "3";
                    break;
                case R.id.button_4:
                    price = "4";
                    break;
                case R.id.button_5:
                    price = "5";
                    break;
                case R.id.button_6:
                    price = "6";
                    break;
                case R.id.button_7:
                    price = "7";
                    break;
                case R.id.button_8:
                    price = "8";
                    break;
                case R.id.button_9:
                    price = "9";
                    break;
                case R.id.button_00:
                    price = "00";
                    break;
                case R.id.button_c:
                    priceTextView.setText(null);
                    return;
            }

            // Remove "," from price text
            String text = priceTextView.getText().toString().replace(",", "");

            if (text.length() >= 9) {
                return;
            }

            // Create price string
            currentPrice                        = Long.parseLong(text + price);
            String priceString                  = ValueConverter.formatPrice(currentPrice);

            priceTextView.setText(priceString);
        }
    }


    //--------------------------------------------------------------//
    //    -- Handle Data --
    //--------------------------------------------------------------//

    private void saveEntry() {

        boolean limitNewEntry = EntryLimitManager.limitNewEntryForFreeUsersWithDate(InputDataActivity.this, date);

        // Entry limit for free users check
        if (limitNewEntry) {
            showUpgradeSuggest();
            return;
        }

        EntryDataManager entryDataManager = new EntryDataManager(InputDataActivity.this);

        String text = priceTextView.getText().toString().replace(",", "");

        // Empty check
        if (TextUtils.isEmpty(text)) {
            DialogManager.showOKOnlyAlert(this, getResources().getString(R.string.Error), getResources().getString(R.string.please_enter_price));
            return;
        }

        ProjectDataManager projectDataManager = new ProjectDataManager(this);
        Project project = projectDataManager.findCurrentProjectWithContext(this);

        Entry entry     = new Entry();
        entry.date      = date;
        entry.updated   = System.currentTimeMillis();
        entry.isExpense = isExpense;
        entry.price     = currentPrice;
        entry.memo      = ((EditText) findViewById(R.id.memo)).getText().toString();
        entry.uuid      = UUID.randomUUID().toString();
        entry.project   = project;
        entry.reason    = reason;
        entry.account   = account;
        long id         = entryDataManager.save(entry);

        // Success
        if (EntryDataManager.isSaveSuccess(id)) {

            SharedPreferencesManager.saveFirstRegisterDone(InputDataActivity.this);

            DialogManager.showInputDataToast(this, entry);
            setResult(RESULT_OK);
            finish();

        } else {
            DialogManager.showOKOnlyAlert(this, getResources().getString(R.string.Error), null);
        }
    }

    private void showUpgradeSuggest() {

        // Confirm dialog
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.upgrade))
                .setMessage(getResources().getString(R.string.upgrade_to_plus_unlock_the_limit))
                .setPositiveButton(getResources().getString(R.string.go_to_upgrade_screen), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // Show upgrade activity
                        Intent intent = new Intent(InputDataActivity.this, UpgradeActivity.class);
                        startActivity(intent);

                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), null)
                .show();
    }


    //--------------------------------------------------------------//
    //    -- View Transition --
    //--------------------------------------------------------------//


}
