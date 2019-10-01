package com.example.taxnoteandroid;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.example.taxnoteandroid.Library.BroadcastUtil;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.KeyboardUtil;
import com.example.taxnoteandroid.Library.ValueConverter;
import com.example.taxnoteandroid.Library.taxnote.TNApiModel;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.databinding.ActivityEntryEditBinding;
import com.example.taxnoteandroid.model.Entry;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EntryEditActivity extends DefaultCommonActivity {

    private ActivityEntryEditBinding binding;
    private Entry entry;
    private String entryUuid;
    private TNApiModel mApiModel;
    private EntryDataManager entryDataManager;

    public static void start(Context context, Entry entry) {
        Intent intent = new Intent(context, EntryEditActivity.class);
        intent.putExtra(Entry.class.getName(), Parcels.wrap(entry));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApiModel = new TNApiModel(this);
        entryDataManager = new EntryDataManager(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setIntent();
        setViews();

        int titleRes = (entry.isExpense) ? R.string.edit_entry_expense
                : R.string.edit_entry_income;
        setTitle(titleRes);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_entry_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_copy:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    //--------------------------------------------------------------//
    //    -- Intent --
    //--------------------------------------------------------------//

    public static Intent createIntent(Context context, Entry entry) {
        Intent i = new Intent(context, EntryEditActivity.class);
        i.putExtra(Entry.class.getName(), Parcels.wrap(entry));
        return i;
    }

    private void setIntent() {

        binding = DataBindingUtil.setContentView(this, R.layout.activity_entry_edit);
        Intent intent = getIntent();
        entry = Parcels.unwrap(intent.getParcelableExtra(Entry.class.getName()));
        entryUuid = entry.uuid;
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
        setDeleteView();
    }

    private void loadData() {

        // Load the latest entry
        entry = entryDataManager.findByUuid(entryUuid);

        binding.setEntry(entry);
        loadCurrentDate();
        loadCurrentPrice();
    }


    //--------------------------------------------------------------//
    //    -- Date --
    //--------------------------------------------------------------//

    private void setDateView() {

        binding.date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerDialogFragment fragment = DatePickerDialogFragment.newInstance(entry.date, null);
                fragment.setOnDateSetListener(new DatePickerDialogFragment.OnDateSetListener() {
                    @Override
                    public void onDateSet(Calendar calendar) {

                        entry.date = calendar.getTimeInMillis();

                        // Update
                        long updated = entryDataManager.updateDate(entry.id, entry.date);

                        if (updated != 0) {

                            // Show update dialog
                            String dateString = formatDate(entry.date);
                            DialogManager.showToast(EntryEditActivity.this, dateString);

                            loadCurrentDate();
                        }

                        BroadcastUtil.sendReloadReport(EntryEditActivity.this);

                        mApiModel.updateEntry(entry.uuid, null);
                    }
                });

                fragment.show(getSupportFragmentManager(), DatePickerDialogFragment.class.getName());
            }
        });
    }

    private String formatDate(Long date) {

        String dateString = getResources().getString(R.string.date_string_today);

        // Show the date if it is not today
        if (!DateUtils.isToday(date)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources().getString(R.string.date_string_format_to_year_month_day_weekday));
            dateString = simpleDateFormat.format(date);
        }

        return dateString;
    }

    private void loadCurrentDate() {

        String dateString = formatDate(entry.date);
        binding.date.setText(dateString);
    }


    //--------------------------------------------------------------//
    //    -- Account --
    //--------------------------------------------------------------//

    private void setAccountView() {

        binding.account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(AccountEditActivity.createIntent(EntryEditActivity.this, entry.isExpense, true, entry.id));
            }
        });
    }


    //--------------------------------------------------------------//
    //    -- Reason --
    //--------------------------------------------------------------//

    private void setReasonView() {

        binding.reason.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(AccountEditActivity.createIntent(EntryEditActivity.this, entry.isExpense, false, entry.id));
            }
        });
    }


    //--------------------------------------------------------------//
    //    -- Memo --
    //--------------------------------------------------------------//

    private void setMemoView() {
        binding.memo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
// 複雑なXMLのレイアウトを作り出す時に便利
//                AsyncLayoutInflater asyncLayoutInflater = new AsyncLayoutInflater(EntryEditActivity.this);
//                asyncLayoutInflater.inflate(R.layout.dialog_text_input, null, new AsyncLayoutInflater.OnInflateFinishedListener() {
//                    @Override
//                    public void onInflateFinished(View view, int resid, ViewGroup parent) {
//
//                    }
//                });

                final View textInputView    = LayoutInflater.from(EntryEditActivity.this).inflate(R.layout.dialog_text_input, null);
                final EditText editText     = (EditText) textInputView.findViewById(R.id.edit);
                editText.setText(entry.memo);

                new AlertDialog.Builder(EntryEditActivity.this)
                        .setView(textInputView)
                        .setTitle(getString(R.string.Details))
                        .setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                KeyboardUtil.hideKeyboard(EntryEditActivity.this, editText);
                                // Get the input string
                                String memo = editText.getText().toString();

                                // Update
                                long updated = entryDataManager.updateMemo(entry.id, memo);

                                if (updated != 0) {

                                    if (memo.length() > 0)
                                        DialogManager.showToast(EntryEditActivity.this, memo);

                                    entry.memo = memo;
                                    // Update displayed memo
                                    binding.memo.setText(memo);
                                }

                                BroadcastUtil.sendReloadReport(EntryEditActivity.this);

                                mApiModel.updateEntry(entry.uuid, null);
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                KeyboardUtil.hideKeyboard(EntryEditActivity.this, editText);
                            }
                        })
                        .show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        KeyboardUtil.showKeyboard(EntryEditActivity.this, editText);
                    }
                }, 180);
            }
        });
    }


    //--------------------------------------------------------------//
    //    -- Price --
    //--------------------------------------------------------------//

    private void setPriceView() {

        binding.price.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(PriceEditActivity.createIntent(EntryEditActivity.this, entry.price, entry.id));
            }
        });
    }

    private void loadCurrentPrice() {

        // Create price string
        String priceString = ValueConverter.formatPriceWithSymbol(EntryEditActivity.this, entry.price, entry.isExpense);
        binding.price.setText(priceString);
    }


    //--------------------------------------------------------------//
    //    -- Delete --
    //--------------------------------------------------------------//

    private void setDeleteView() {

        binding.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteDialog();
            }
        });
    }

    private void showDeleteDialog() {

        // Confirm dialog
        new AlertDialog.Builder(EntryEditActivity.this)
                .setTitle(null)
                .setMessage(getResources().getString(R.string.delete_confirm_message))
                .setPositiveButton(getResources().getString(R.string.Delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();

                        BroadcastUtil.sendReloadReport(EntryEditActivity.this);

                        entryDataManager.updateSetDeleted(entry.uuid, mApiModel);


                        DialogManager.showToast(EntryEditActivity.this, getResources().getString(R.string.delete_done));
                        finish();

                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), null)
                .show();
    }
}