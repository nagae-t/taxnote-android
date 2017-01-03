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


    public static Intent createIntent(Context context, Entry entry) {
        Intent i = new Intent(context, EntryEditActivity.class);
        i.putExtra(Entry.class.getName(), Parcels.wrap(entry));
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setIntentAndBinding();

        setDateView();
        setAccountView();
        setPriceView();
    }

    @Override
    public void onResume() {
        super.onResume();

        loadCurrentDate();
        loadCurrentPrice();
    }

    private void setIntentAndBinding() {

        //@@@ entryを表示していく

        binding = DataBindingUtil.setContentView(this, R.layout.activity_entry_edit);
        Intent intent = getIntent();
        entry = Parcels.unwrap(intent.getParcelableExtra(Entry.class.getName()));
        binding.setEntry(entry);
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
                startActivity(AccountSelectActivity.createIntent(EntryEditActivity.this, entry.isExpense));
            }
        });
    }

//    private void loadCurrentAccount() {
//
//        AccountDataManager accountDataManager = new AccountDataManager(getContext());
//        account = accountDataManager.findCurrentSelectedAccount(getContext(), isExpense);
//
//        ((TextView) getView().findViewById(R.id.account_text_view)).setText(account.name);
//    }


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