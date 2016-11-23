package com.example.taxnoteandroid.entryTab;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taxnoteandroid.R;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Entry;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason;
import com.example.taxnoteandroid.model.Summary;

import org.parceler.Parcels;

import java.text.DecimalFormat;
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

        if (isExpense) {
            title = reason.name + "/" + account.name;
        } else {
            title = account.name + "/" + reason.name;
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

        priceTextView = (TextView) findViewById(R.id.price_text_view);

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
                    priceTextView.setText("0");
                    return;
            }

            // Remove "," from price text
            String text = priceTextView.getText().toString().replace(",", "");

            if (text.length() >= 9) {
                return;
            }

            // Create price string
            currentPrice                        = Long.parseLong(text + price);
            DecimalFormat formatForPriceStyle   = new DecimalFormat("#,###.##");
            String priceString                  = formatForPriceStyle.format(currentPrice);

            priceTextView.setText(priceString);
        }
    }


    //--------------------------------------------------------------//
    //    -- Handle Data --
    //--------------------------------------------------------------//

    private void saveEntry() {

        EntryDataManager entryDataManager = new EntryDataManager(InputDataActivity.this);

        String text = priceTextView.getText().toString().replace(",", "");

        if (TextUtils.isEmpty(text)) {
//            Toast.makeText(this, "数字を", Toast.LENGTH_SHORT).show();
//            Snackbar.make(findViewById(R.id.activity_input_data), "@数字を入力してください", Snackbar.LENGTH_INDEFINITE).show();

            // Show error message
            new AlertDialog.Builder(this)
                    .setTitle("@エラー")
                    .setMessage("@数字を入力してください")
                    .setPositiveButton("OK", null)
                    .show();
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

        // QQなんかここ遅い　サクサク感がない。。
        // Success
        if (EntryDataManager.isSaveSuccess(id)) {

            showSavingDoneToast();

            // 処理が正常に終わったりしたら、呼び出された画面にたいしてOKだったと伝えるためにsetResultする
            setResult(RESULT_OK);
            finish();
        } else {
            // Show error message
            new AlertDialog.Builder(this)
                    .setTitle("title")
                    .setMessage("message")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }


    //--------------------------------------------------------------//
    //    -- Pop View --
    //--------------------------------------------------------------//

    private void showSavingDoneToast() {

        //@@ ここにチェックマークの画像を追加
        Toast toast = Toast.makeText(InputDataActivity.this, "Hogehoge", Toast.LENGTH_SHORT);

        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setView(getLayoutInflater().inflate(R.layout.toast_save, null));
        toast.show();
    }


    //--------------------------------------------------------------//
    //    -- View Transition --
    //--------------------------------------------------------------//


}
