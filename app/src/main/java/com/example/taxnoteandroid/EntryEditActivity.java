package com.example.taxnoteandroid;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.databinding.ActivityEntryEditBinding;
import com.example.taxnoteandroid.model.Entry;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EntryEditActivity extends AppCompatActivity {

    private ActivityEntryEditBinding binding;
    private Entry entry;
    private String entryUuid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setIntent();
        setViews();
    }

    @Override
    public void onResume() {
        super.onResume();

        loadData();

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.animation_text_move);
        binding.date.setAnimation(animation);
        animation.start();
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
        EntryDataManager entryDataManager = new EntryDataManager(EntryEditActivity.this);
        entry = entryDataManager.findByUuid(entryUuid);

        binding.setEntry(entry);
        loadCurrentDate();
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

                // @@ あとやる
                fragment.show(getSupportFragmentManager(), DatePickerDialogFragment.class.getName());
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

        EditText memoField = (EditText) findViewById(R.id.memo);
        memoField.setText(entry.memo);

        final View textInputView = LayoutInflater.from(this).inflate(R.layout.dialog_text_input, null);

        final EditText editText = (EditText) textInputView.findViewById(R.id.edit);
        editText.setText(entry.memo);

        //QQ ここエントリーを編集した時に、データをセーブしたい。

        binding.memo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(EntryEditActivity.this)
                        .setView(textInputView)
                        .setTitle(getString(R.string.list_view_rename))
                        .setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String memo = editText.getText().toString();

                                // @@ entryの更新

                                binding.memo.setText(memo);

                                // @@ 残りはやる
//
//                        ReasonDataManager reasonDataManager = new ReasonDataManager(getContext());
//                        reasonDataManager.updateName(reason.id, reasonName);
//
//                        Reason oldReason = reasonListAdapter.getItem(position);
//                        if (oldReason != null) {
//                            oldReason.name = reasonName;
//                            reasonListAdapter.notifyDataSetChanged();
//                        }
//
//                        dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.cancel), null)
                        .show();

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

                        EntryDataManager entryDataManager = new EntryDataManager(EntryEditActivity.this);
                        long deleted = entryDataManager.delete(entry.id);

                        if (deleted != 0) {
                            finish();
                        }

                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), null)
                .show();
    }
}