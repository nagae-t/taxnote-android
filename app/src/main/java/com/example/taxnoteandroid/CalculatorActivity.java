package com.example.taxnoteandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.ValueConverter;

public class CalculatorActivity extends AppCompatActivity {

    private static final String EXTRA_CURRENT_PRICE = "EXTRA_CURRENT_PRICE";
    private static final String DECIMAL_SYMBOL      = "DECIMAL_SYMBOL";
    private static final String PLUS_SYMBOL         = "PLUS_SYMBOL";
    private static final String MINUS_SYMBOL        = "MINUS_SYMBOL";
    private static final String MULTIPLE_SYMBOL     = "MULTIPLE_SYMBOL";
    private static final String SPLIT_SYMBOL        = "SPLIT_SYMBOL";
    private static final String EQUAL_SYMBOL        = "EQUAL_SYMBOL";

    public long currentPrice = 0;
    private String priceString;
    private String previousPriceString;
    private String selectedSymbol;
    private TextView priceTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calculator);

        setIntentData();
        setTitle();
        setPriceInputPart();
    }


    //--------------------------------------------------------------//
    //    -- Intent --
    //--------------------------------------------------------------//

    public static Intent createIntent(Context context, long currentPrice) {

        Intent i = new Intent(context, PriceEditActivity.class);
        i.putExtra(EXTRA_CURRENT_PRICE, currentPrice);
        return i;
    }

    private void setIntentData() {

        Intent intent = getIntent();
        currentPrice = intent.getLongExtra(EXTRA_CURRENT_PRICE, 0);
        priceString = Long.toString(currentPrice);
    }


    //--------------------------------------------------------------//
    //    -- Display Part --
    //--------------------------------------------------------------//

    private void setTitle() {
        setTitle(getResources().getString(R.string.Price));
    }



    private void setPriceInputPart() {

        priceTextView = (TextView) findViewById(R.id.title);

        String priceString = ValueConverter.formatPrice(CalculatorActivity.this ,currentPrice);
        priceTextView.setText(priceString);

        CalculatorActivity.OnPriceClickListener onPriceClickListener = new CalculatorActivity.OnPriceClickListener();
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
        findViewById(R.id.button_decimal).setOnClickListener(onPriceClickListener);
        findViewById(R.id.button_c).setOnClickListener(onPriceClickListener);
        findViewById(R.id.button_split).setOnClickListener(onPriceClickListener);
        findViewById(R.id.button_multiple).setOnClickListener(onPriceClickListener);
        findViewById(R.id.button_minus).setOnClickListener(onPriceClickListener);
        findViewById(R.id.button_plus).setOnClickListener(onPriceClickListener);
        findViewById(R.id.button_equal).setOnClickListener(onPriceClickListener);
    }


    //--------------------------------------------------------------//
    //    -- Handle Data --
    //--------------------------------------------------------------//

    private class OnPriceClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            String selectedString = "";
            switch (view.getId()) {
                case R.id.button_0:
                    selectedString = "0";
                    break;
                case R.id.button_1:
                    selectedString = "1";
                    break;
                case R.id.button_2:
                    selectedString = "2";
                    break;
                case R.id.button_3:
                    selectedString = "3";
                    break;
                case R.id.button_4:
                    selectedString = "4";
                    break;
                case R.id.button_5:
                    selectedString = "5";
                    break;
                case R.id.button_6:
                    selectedString = "6";
                    break;
                case R.id.button_7:
                    selectedString = "7";
                    break;
                case R.id.button_8:
                    selectedString = "8";
                    break;
                case R.id.button_9:
                    selectedString = "9";
                    break;
                case R.id.button_decimal:
                    selectedString = DECIMAL_SYMBOL;
                    break;
                case R.id.button_split:
                    selectedString = SPLIT_SYMBOL;
                    break;
                case R.id.button_multiple:
                    selectedString = MULTIPLE_SYMBOL;
                    break;
                case R.id.button_minus:
                    selectedString = MINUS_SYMBOL;
                    break;
                case R.id.button_plus:
                    selectedString = PLUS_SYMBOL;
                    break;
                case R.id.button_equal:
                    selectedString = EQUAL_SYMBOL;
                    break;
                case R.id.button_c:

                    // Reset
                    priceTextView.setText(null);
                    currentPrice = 0;
                    priceString = null;
                    previousPriceString = null;
                    selectedSymbol = null;
                    return;
            }

            handleSelectedString(selectedString);
        }
    }


    //--------------------------------------------------------------//
    //    -- Calculate --
    //--------------------------------------------------------------//

    private void handleSelectedString(String selectedString) {

        if (selectedStringIsCalculationSymbol(selectedString)) {

            // Save price number and symbol after price number is set and symbol is selected
            if (previousPriceString == null) {

                // Save symbol and price number
                previousPriceString = priceString;
                selectedSymbol      = selectedString;
                priceString         = null;

                return;
            }

            // Change selected symbol if current price number is not set
            if (priceString == null) {
                selectedSymbol = selectedString;
                return;
            }

            // Calculate
            priceString = calculatePriceStringAndPreviousPriceString(priceString, previousPriceString, selectedSymbol);

            // Set text
            currentPrice    = Long.parseLong(priceString);
            String text     = ValueConverter.formatPrice(CalculatorActivity.this , currentPrice);
            priceTextView.setText(text);

            // Save symbol and price number
            previousPriceString = priceString;
            selectedSymbol      = selectedString;
            priceString         = null;

            return;
        }

        // Dismiss the activity
        if (selectedString.equals(EQUAL_SYMBOL)) {

            // Calculate if priceNumber and previousNmber and selectedSymbol are set
            if (priceString != null && previousPriceString != null && selectedSymbol != null) {
                priceString = calculatePriceStringAndPreviousPriceString(priceString, previousPriceString, selectedSymbol);
            }

            // Get priceNumber from previousNumber if priceNumber is nil after tapping symbol
            if (priceString == null) {
                priceString = previousPriceString;
            }

            currentPrice    = Long.parseLong(priceString);

            //@@@ priceを受けわたす

            DialogManager.showToast(this, priceString);
            return;
        }

        // Remove "," from price text
        String text = priceTextView.getText().toString().replace(",", "");

        if (text.length() >= 9) {
            return;
        }

        // Create price string
        String currentPriceString = Long.toString(currentPrice);
        currentPrice = Long.parseLong(currentPriceString + priceString);
        String priceString = ValueConverter.formatPrice(CalculatorActivity.this ,currentPrice);

        priceTextView.setText(priceString);
    }

    private String calculatePriceStringAndPreviousPriceString(String priceString, String previousPriceString, String selectedSymbol) {

        double newPriceDoubleNumber = 0;
        double priceDoubleNumber = Double.parseDouble(priceString);
        double previousPriceDoubleNumber = Double.parseDouble(previousPriceString);


        if (selectedSymbol.equals(PLUS_SYMBOL)) {

            newPriceDoubleNumber = previousPriceDoubleNumber + priceDoubleNumber;

        } else if (selectedSymbol.equals(MINUS_SYMBOL)) {
            newPriceDoubleNumber = previousPriceDoubleNumber - priceDoubleNumber;

        } else if (selectedSymbol.equals(MULTIPLE_SYMBOL)) {
            newPriceDoubleNumber = previousPriceDoubleNumber * priceDoubleNumber;

        } else if (selectedSymbol.equals(SPLIT_SYMBOL)) {

            newPriceDoubleNumber = previousPriceDoubleNumber / priceDoubleNumber;

            // @@@ここlongになってない？
            // Round here
            newPriceDoubleNumber = Math.round(newPriceDoubleNumber);
        }

        String calculatedPriceString = Double.toString(newPriceDoubleNumber);

        // Limit max length
        if (calculatedPriceString.length() > 9) {

            // @@@ 翻訳する
            DialogManager.showOKOnlyAlert(this, "Limit", "Limit Reached");
            return Double.toString(previousPriceDoubleNumber);
        }

        return calculatedPriceString;
    }

    private boolean selectedStringIsCalculationSymbol(String priceString) {

        if (priceString.equals(PLUS_SYMBOL)) {
            return true;

        } else if (priceString.equals(MINUS_SYMBOL)) {
            return true;

        } else if (priceString.equals(MULTIPLE_SYMBOL)) {
            return true;

        } else if (priceString.equals(SPLIT_SYMBOL)) {
            return true;
        } else {
            return false;
        }
    }


//    //symbol for calculation is selected
//    if ([MPValueCalculator selectedStringIsSymbol:selectedString]) {
//
//        //save price number and symbol after price number is set and symbol is selected
//        if (!_previousPriceString) {
//
//            //save symbol and price number
//            _previousPriceString    = _priceString;
//            _selectedSymbol         = selectedString;
//            _priceString            = nil;
//
//            return;
//        }
//
//        //change selected symbol if current price number is not set
//        if (!_priceString) {
//            _selectedSymbol = selectedString;
//            return;
//        }
//
//        //calculate and display the result
//        _priceString        = [MPValueCalculator calculatePriceString:_priceString previousPriceString:_previousPriceString symbol:_selectedSymbol];
//
//        // Set , separator
//        _priceField.text        = [KPValueConverter stringWithGroupingSeparatorForPriceString:_priceString];
//
//        //save symbol and price number
//        _previousPriceString    = _priceString;
//        _selectedSymbol         = selectedString;
//        _priceString            = nil;
//
//        return;
//    }
//
//    //dismiss the view controller
//    if ([selectedString isEqualToString:@"="]) {
//
//        //calculate if priceNumber and previousNmber and selectedSymbol are set
//        if (_priceString && _previousPriceString && _selectedSymbol) {
//            _priceString = [MPValueCalculator calculatePriceString:_priceString previousPriceString:_previousPriceString symbol:_selectedSymbol];
//        }
//
//        //get priceNumber from previousNumber if priceNumber is nil after tapping symbol
//        if (!_priceString) {
//            _priceString = _previousPriceString;
//        }
//
//        [[NSNotificationCenter defaultCenter] postNotificationName:CalculatorVCDismissedNotification object:self userInfo:nil];
//        [self dismissViewControllerAnimated:YES completion:nil];
//        return;
//    }
//
//    //reset all
//    if ([selectedString isEqualToString:@"c"]) {
//
//        _priceString            = nil;
//        _previousPriceString    = nil;
//        _selectedSymbol         = nil;
//    }
//
//    // Combine numbers
//    _priceString        = [KPValueConverter updatePriceString:_priceString inputNumberString:selectedString];
//
//    // Set , separator
//    _priceField.text    = [KPValueConverter stringWithGroupingSeparatorForPriceString:_priceString selectedSymbol:_selectedSymbol];
}
