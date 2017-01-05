package com.example.taxnoteandroid;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.View;

import com.example.taxnoteandroid.databinding.ActivityEntryEditBinding;
import com.example.taxnoteandroid.model.Entry;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EntryEditActivity extends AppCompatActivity {

    private ActivityEntryEditBinding binding;
    private Entry entry;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setIntentAndBinding();
        setViews();
    }

    @Override
    public void onResume() {
        super.onResume();

        loadData();
    }


    //--------------------------------------------------------------//
    //    -- Intent --
    //--------------------------------------------------------------//

    public static Intent createIntent(Context context, Entry entry) {
        Intent i = new Intent(context, EntryEditActivity.class);
        i.putExtra(Entry.class.getName(), Parcels.wrap(entry));
        return i;
    }

    private void setIntentAndBinding() {

        binding = DataBindingUtil.setContentView(this, R.layout.activity_entry_edit);
        Intent intent = getIntent();
        entry = Parcels.unwrap(intent.getParcelableExtra(Entry.class.getName()));
        binding.setEntry(entry);
    }


    //--------------------------------------------------------------//
    //    -- Display Part --
    //--------------------------------------------------------------//

    private void setViews() {

        setDateView();
        setAccountView();
        setReasonView();
        setMemoView();
        setPriceView();
    }

    private void loadData() {

        loadCurrentDate();
        loadCurrentAccount();
        loadCurrentReason();
        loadCurrentMemo();
        loadCurrentPrice();
    }


    //--------------------------------------------------------------//
    //    -- Date --
    //--------------------------------------------------------------//

    private void setDateView() {

        binding.date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerDialogFragment fragment = DatePickerDialogFragment.newInstance(entry.date, getResources().getString(R.string.entry_tab_fragment_date));
                fragment.setOnDateSetListener(new DatePickerDialogFragment.OnDateSetListener() {
                    @Override
                    public void onDateSet(Calendar calendar) {
                        entry.date = calendar.getTimeInMillis();
                        loadCurrentDate();
                    }
                });

                //QQここセットしかた
//                fragment.show(getFragmentManager(), DatePickerDialogFragment.class.getName());
            }
        });
    }

    private void loadCurrentDate() {

        String dateString = getResources().getString(R.string.date_string_today);

        // Show the date if it is not today
        if (!DateUtils.isToday(entry.date)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources().getString(R.string.date_string_format_to_year));
            dateString = simpleDateFormat.format(entry.date);
        }

        binding.date.setText(dateString);
    }


    //--------------------------------------------------------------//
    //    -- Account --
    //--------------------------------------------------------------//

    private void setAccountView() {

        binding.account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //@@@ Account Edit Activity作るひつようあり
//                startActivity(EntryEditActivity.createIntent(EntryEditActivity.this, entry.isExpense));
            }
        });
    }

    private void loadCurrentAccount() {
        binding.account.setText(entry.account.name);
    }


    //--------------------------------------------------------------//
    //    -- Reason --
    //--------------------------------------------------------------//

    private void setReasonView() {

        binding.reason.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //@@@ Account Edit Activity作るひつようあり
//                startActivity(EntryEditActivity.createIntent(EntryEditActivity.this, entry.isExpense));
            }
        });
    }

    private void loadCurrentReason() {
        binding.account.setText(entry.reason.name);
    }


    //--------------------------------------------------------------//
    //    -- Memo --
    //--------------------------------------------------------------//

    private void setMemoView() {

        binding.memo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //@@@ Account Edit Activity作るひつようあり
//                startActivity(EntryEditActivity.createIntent(EntryEditActivity.this, entry.isExpense));
            }
        });
    }

    private void loadCurrentMemo() {
        binding.memo.setText(entry.memo);
    }


    //--------------------------------------------------------------//
    //    -- Price --
    //--------------------------------------------------------------//

    private void setPriceView() {

        binding.price.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //QQ ここでPrice edit activity作りたい

            }
        });
    }

    private void loadCurrentPrice() {

        // Create price string
        String priceString = ValueConverter.formatPriceWithSymbol(entry.price, entry.isExpense);
        binding.price.setText(priceString);
    }
}