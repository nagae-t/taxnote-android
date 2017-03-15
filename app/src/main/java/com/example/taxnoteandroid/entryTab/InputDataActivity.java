package com.example.taxnoteandroid.entryTab;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.calculator2.Calculator;
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
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.UUID;

import static com.example.taxnoteandroid.TaxnoteConsts.MIXPANEL_TOKEN;

public class InputDataActivity extends AppCompatActivity {

    private TextView priceTextView;
    private static final String EXTRA_IS_EXPENSE = "isExpense";
    private static final String EXTRA_DATE = "date";
    public boolean isExpense;
    public Account account;
    public Reason reason;
    public Summary summary;
    public long date;
    private long currentPrice = 0;
    private  String dateString;
    public boolean pinButton = false;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_input_data);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setIntentData();
        setTitle();
        setSummary();
        setSaveButton();
        setPriceInputPart();
        setCalculatorView();

        DialogManager.showTapRegisterMessage(InputDataActivity.this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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

        isExpense = intent.getBooleanExtra(EXTRA_IS_EXPENSE, false);
        date = intent.getLongExtra(EXTRA_DATE, 0);
        account = Parcels.unwrap(intent.getParcelableExtra(Account.class.getName()));
        reason = Parcels.unwrap(intent.getParcelableExtra(Reason.class.getName()));
        summary = Parcels.unwrap(intent.getParcelableExtra(Summary.class.getName()));
    }


    //--------------------------------------------------------------//
    //    -- Menu --
    //--------------------------------------------------------------//

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.pin_button).setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.pin_button:
                togglePinButton();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void togglePinButton() {

        if (pinButton) {
            DialogManager.showToast(this, getResources().getString(R.string.pin_button_off_message));
            pinButton = false;
        } else {
            DialogManager.showToast(this, getResources().getString(R.string.pin_button_on_message));
            pinButton = true;
        }
    }


    //--------------------------------------------------------------//
    //    -- Display Part --
    //--------------------------------------------------------------//

    private void setTitle() {

        String title;
        dateString = getResources().getString(R.string.date_string_today);

        // Show the date if it is not today
        if (!DateUtils.isToday(date)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources().getString(R.string.date_string_format_to_month_day));
            dateString = simpleDateFormat.format(date);
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

        String priceString = ValueConverter.formatPrice(InputDataActivity.this ,currentPrice);
        priceTextView.setHint(priceString);

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

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("InputData Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onResume() {
        super.onResume();

        getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
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
                    currentPrice = 0;
                    return;
            }

            // Remove "," from price text
            String text = priceTextView.getText().toString().replace(",", "");

            if (text.length() >= 9) {
                return;
            }

            // Create price string
            String currentPriceString = Long.toString(currentPrice);
            currentPrice = Long.parseLong(currentPriceString + price);
            String priceString = ValueConverter.formatPrice(InputDataActivity.this ,currentPrice);

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

        Entry entry = new Entry();
        entry.date = date;
        entry.updated = System.currentTimeMillis();
        entry.isExpense = isExpense;
        entry.price = currentPrice;
        entry.memo = ((EditText) findViewById(R.id.memo)).getText().toString();
        entry.uuid = UUID.randomUUID().toString();
        entry.project = project;
        entry.reason = reason;
        entry.account = account;
        long id = entryDataManager.save(entry);

        // Success
        if (EntryDataManager.isSaveSuccess(id)) {

            countAndTrackEntry();
            SharedPreferencesManager.saveFirstRegisterDone(InputDataActivity.this);

            DialogManager.showInputDataToast(this, dateString,entry);

            // Stay in this screen when pinButton is true
            if (pinButton) {
                currentPrice = 0;
                priceTextView.setText(null);
            } else {
                setResult(RESULT_OK);
                finish();
            }

        } else {
            DialogManager.showOKOnlyAlert(this, getResources().getString(R.string.Error), null);
        }
    }

    private void countAndTrackEntry() {

        long entryCount = SharedPreferencesManager.getTrackEntryCount(this);
        entryCount++;
        SharedPreferencesManager.saveTrackEntryCount(this, entryCount);

        if (entryCount == 1 || entryCount % 10 == 0) {
            MixpanelAPI mixpanel = MixpanelAPI.getInstance(this, MIXPANEL_TOKEN);
            mixpanel.track("New Entry");
        }
    }

    private void showUpgradeSuggest() {

        MixpanelAPI mixpanel = MixpanelAPI.getInstance(this, MIXPANEL_TOKEN);
        mixpanel.track("Entry Limit Reached");

        // Confirm dialog
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.upgrade))
                .setMessage(getResources().getString(R.string.upgrade_to_plus_unlock_the_limit))
                .setPositiveButton(getResources().getString(R.string.benefits_of_upgrade), new DialogInterface.OnClickListener() {
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
    //    -- Calculator --
    //--------------------------------------------------------------//

    //@@ 電卓あとで追加
    private void setCalculatorView() {

        ImageView calculatorButton = (ImageView) findViewById(R.id.calculator_button);
        calculatorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivityForResult(CalculatorActivity.createIntent(InputDataActivity.this, currentPrice), 1);

                String roundedDecimalMsg =  getString(R.string.rounded_decimal_numbers);
                Calculator.startForResult(InputDataActivity.this,
                        currentPrice, roundedDecimalMsg,  1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {

            // Update price string
//            currentPrice = data.getLongExtra("EXTRA_CURRENT_PRICE", 0);

            currentPrice = data.getLongExtra(Calculator.KEY_CURRENT_PRICE, 0);
            String priceString = ValueConverter.formatPrice(InputDataActivity.this ,currentPrice);
            priceTextView.setText(priceString);
        }
    }

}
