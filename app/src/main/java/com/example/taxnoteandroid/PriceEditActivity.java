package com.example.taxnoteandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.calculator2.Calculator;
import com.example.taxnoteandroid.Library.BroadcastUtil;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.ValueConverter;
import com.example.taxnoteandroid.Library.taxnote.TNApiModel;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.model.Entry;

public class PriceEditActivity extends DefaultCommonActivity {

    private static final String EXTRA_ENTRY_ID = "EXTRA_ENTRY_ID";
    private static final String EXTRA_CURRENT_PRICE = "EXTRA_CURRENT_PRICE";
    public long currentPrice = 0;
    public long entryId;
    private TextView priceTextView;

    private Entry mEntry;
    private EntryDataManager entryDataManager;
    private TNApiModel mApiModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_price_edit);

        mApiModel = new TNApiModel(this);
        entryDataManager = new EntryDataManager(this);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setIntentData();
        setTitle();
        setSaveButton();
        setPriceInputPart();
        setCalculatorView();
    }


    //--------------------------------------------------------------//
    //    -- Intent --
    //--------------------------------------------------------------//

    public static Intent createIntent(Context context, long currentPrice, long entryId) {

        Intent i = new Intent(context, PriceEditActivity.class);
        i.putExtra(EXTRA_CURRENT_PRICE, currentPrice);
        i.putExtra(EXTRA_ENTRY_ID, entryId);

        return i;
    }

    private void setIntentData() {

        Intent intent = getIntent();
        currentPrice = intent.getLongExtra(EXTRA_CURRENT_PRICE, 0);
        entryId = intent.getLongExtra(EXTRA_ENTRY_ID, 0);
        mEntry = entryDataManager.findById(entryId);
    }


    //--------------------------------------------------------------//
    //    -- Display Part --
    //--------------------------------------------------------------//

    private void setTitle() {
        setTitle(getResources().getString(R.string.Price));
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

        String priceString = ValueConverter.formatPrice(PriceEditActivity.this ,currentPrice);
        priceTextView.setText(priceString);

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

    private void saveEntry() {

        String priceText = priceTextView.getText().toString().replace(",", "");

        // Empty check
        if (TextUtils.isEmpty(priceText)) {
            DialogManager.showOKOnlyAlert(this, getResources().getString(R.string.Error), getResources().getString(R.string.please_enter_price));
            return;
        }

        // Update
        EntryDataManager entryDataManager = new EntryDataManager(PriceEditActivity.this);
        long updated = entryDataManager.updatePrice(entryId, currentPrice);

        mApiModel.updateEntry(mEntry.uuid, null);
        if (updated != 0) {
            BroadcastUtil.sendReloadReport(PriceEditActivity.this);

            // Show update dialog
            String priceString = ValueConverter.formatPrice(PriceEditActivity.this ,currentPrice);
            DialogManager.showToast(PriceEditActivity.this, priceString);

            finish();
        }
    }


    //--------------------------------------------------------------//
    //    -- Handle Data --
    //--------------------------------------------------------------//

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
            String priceString = ValueConverter.formatPrice(PriceEditActivity.this ,currentPrice);

            priceTextView.setText(priceString);
        }
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

    //--------------------------------------------------------------//
    //    -- Calculator --
    //--------------------------------------------------------------//

    private void setCalculatorView() {

        ImageView calculatorButton = (ImageView) findViewById(R.id.calculator_button);
        calculatorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CalculatorActivity.startForResult(PriceEditActivity.this, currentPrice, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {

            currentPrice = data.getLongExtra(Calculator.KEY_CURRENT_PRICE, 0);
            String priceString = ValueConverter.formatPrice(PriceEditActivity.this ,currentPrice);
            priceTextView.setText(priceString);
        }
    }

}
