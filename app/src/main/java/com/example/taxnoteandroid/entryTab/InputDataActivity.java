package com.example.taxnoteandroid.entryTab;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.taxnoteandroid.CalculatorActivity;
import com.example.taxnoteandroid.DefaultCommonActivity;
import com.example.taxnoteandroid.Library.BroadcastUtil;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.EntryLimitManager;
import com.example.taxnoteandroid.Library.UpgradeManger;
import com.example.taxnoteandroid.Library.ValueConverter;
import com.example.taxnoteandroid.Library.taxnote.TNApiModel;
import com.example.taxnoteandroid.Library.zeny.ZNUtils;
import com.example.taxnoteandroid.R;
import com.example.taxnoteandroid.UpgradeActivity;
import com.example.taxnoteandroid.dataManager.AccountDataManager;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Entry;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason;
import com.example.taxnoteandroid.model.Summary;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.Task;
import com.helpshift.support.Support;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.UUID;

import static com.example.taxnoteandroid.TaxnoteConsts.MIXPANEL_TOKEN;

public class InputDataActivity extends DefaultCommonActivity {

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

    private TNApiModel mApiModel;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
//    private GoogleApiClient client;

    public static void start(Context context, boolean isExpense, long date, Reason reason) {
        Intent i = new Intent(context, InputDataActivity.class);

        i.putExtra(EXTRA_IS_EXPENSE, isExpense);
        i.putExtra(Reason.class.getName(), Parcels.wrap(reason));
        i.putExtra(EXTRA_DATE, date);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_input_data);

        mApiModel = new TNApiModel(this);

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
//        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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
        reason = Parcels.unwrap(intent.getParcelableExtra(Reason.class.getName()));

        Parcelable accountPar = intent.getParcelableExtra(Account.class.getName());
        if (accountPar != null)
            account = Parcels.unwrap(accountPar);
        Parcelable summPar = intent.getParcelableExtra(Summary.class.getName());
        if (summPar != null)
            summary = Parcels.unwrap(summPar);
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
                if (pinButton)
                    BroadcastUtil.sendReloadReport(this);
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

        String title = reason.name;
        dateString = getResources().getString(R.string.date_string_today);

        // Show the date if it is not today
        if (!DateUtils.isToday(date)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                    getResources().getString(R.string.date_string_format_to_month_day),
                    Locale.getDefault());
            dateString = simpleDateFormat.format(date);
        }

        if (account != null) {
            title = (isExpense) ? reason.name + "/" + account.name
                    : account.name + "/" + reason.name;
        }

        setTitle(title);
        getSupportActionBar().setSubtitle(dateString);
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
//    public Action getIndexApiAction() {
//        Thing object = new Thing.Builder()
//                .setName("InputData Page") // TODO: Define a title for the content shown.
//                // TODO: Make sure this auto-generated URL is correct.
//                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
//                .build();
//        return new Action.Builder(Action.TYPE_VIEW)
//                .setObject(object)
//                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
//                .build();
//    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        client.connect();
//        AppIndex.AppIndexApi.start(client, getIndexApiAction());
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
//        AppIndex.AppIndexApi.end(client, getIndexApiAction());
//        client.disconnect();
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

        // Taxnoteクラウド購入なしで追加された帳簿の入力制限あり
        boolean limitNewEntrySubProject = EntryLimitManager.limitNewEntryAddSubProject(this);
        if ( !UpgradeManger.taxnoteCloudIsActive(this) && limitNewEntrySubProject) {
            showUpgradeCloudInputLimit();
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
        Project project = projectDataManager.findCurrent();
        AccountDataManager accDm = new AccountDataManager(this);

        Entry entry = new Entry();
        entry.date = date;
        entry.updated = System.currentTimeMillis();
        entry.isExpense = isExpense;
        entry.price = currentPrice;
        entry.memo = ((EditText) findViewById(R.id.memo)).getText().toString();
        entry.uuid = UUID.randomUUID().toString();
        entry.project = project;
        entry.reason = reason;
        entry.account = (account == null) ? accDm.findCurrentSelectedAccount(isExpense) : account;
        long id = entryDataManager.save(entry);

        // Success
        if (EntryDataManager.isSaveSuccess(id)) {

            countAndTrackEntry();
            SharedPreferencesManager.saveFirstRegisterDone(InputDataActivity.this);

            int count = SharedPreferencesManager.incrementAppReviewRegisterCount(InputDataActivity.this);
            if (count == 30 || count == 60 || count == 100 || count == 150 || count == 200) {
                review();
            }

            DialogManager.showInputDataToast(this, dateString,entry);

            mApiModel.saveEntry(entry.uuid, null);

            // Stay in this screen when pinButton is true
            if (pinButton) {
                currentPrice = 0;
                priceTextView.setText(null);
            } else {
                if (ZNUtils.isZeny()) BroadcastUtil.sendReloadReport(this);

                setResult(RESULT_OK);
                finish();
            }

        } else {
            DialogManager.showOKOnlyAlert(this, getResources().getString(R.string.Error), null);
        }
    }

    private void review() {
        final ReviewManager manager = ReviewManagerFactory.create(this);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(new OnCompleteListener<ReviewInfo>() {
            @Override
            public void onComplete(Task<ReviewInfo> task) {
                if (task.isSuccessful()) {
                    ReviewInfo reviewInfo = task.getResult();
                    manager.launchReviewFlow(InputDataActivity.this, reviewInfo);
                }
            }
        });
    }

    private void showUpgradeCloudInputLimit() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.taxnote_cloud_first_free)
                .setMessage(R.string.not_cloud_input_limit_message)
                .setNeutralButton(R.string.view_help, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Support.showSingleFAQ(InputDataActivity.this, "177");
                    }
                })
                .setPositiveButton(getResources().getString(R.string.benefits_of_upgrade), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // Show upgrade activity
                        UpgradeActivity.start(InputDataActivity.this);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
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

    private void setCalculatorView() {

        ImageView calculatorButton = (ImageView) findViewById(R.id.calculator_button);
        calculatorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CalculatorActivity.startForResult(InputDataActivity.this, currentPrice, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {

            currentPrice = data.getLongExtra(CalculatorActivity.KEY_CURRENT_PRICE, 0);
            String priceString = ValueConverter.formatPrice(InputDataActivity.this ,currentPrice);
            priceTextView.setText(priceString);
        }
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && pinButton) {
            BroadcastUtil.sendReloadReport(this);
        }
        return super.onKeyDown(keyCode, event);

    }

}
