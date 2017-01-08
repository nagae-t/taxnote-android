package com.example.taxnoteandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.example.taxnoteandroid.dataManager.EntryDataManager;

public class PriceEditActivity extends AppCompatActivity {

    private TextView priceTextView;
    private static final String EXTRA_ENTRY_ID      = "EXTRA_ENTRY_ID";
    private static final String EXTRA_CURRENT_PRICE = "EXTRA_CURRENT_PRICE";
    public long currentPrice = 0;
    public long entryId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_price_edit);

        setIntentData();
        setTitle();
        setSaveButton();
        setPriceInputPart();
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
        currentPrice  = intent.getLongExtra(EXTRA_CURRENT_PRICE, 0);
        entryId       = intent.getLongExtra(EXTRA_ENTRY_ID, 0);
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

        String priceString = ValueConverter.formatPrice(currentPrice);
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

        String text = priceTextView.getText().toString().replace(",", "");

        if (TextUtils.isEmpty(text)) {

            // Show error message
            new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.Error))
                    .setMessage(getResources().getString(R.string.please_enter_price))
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        // Update
        EntryDataManager entryDataManager   = new EntryDataManager(PriceEditActivity.this);
        long updated                        = entryDataManager.updatePrice(entryId,currentPrice);

        if (updated != 0) {
            finish();
        }
    }

}
