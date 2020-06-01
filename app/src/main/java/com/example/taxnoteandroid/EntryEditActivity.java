package com.example.taxnoteandroid;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.example.taxnoteandroid.Library.BroadcastUtil;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.Library.EntryLimitManager;
import com.example.taxnoteandroid.Library.KeyboardUtil;
import com.example.taxnoteandroid.Library.UpgradeManger;
import com.example.taxnoteandroid.Library.ValueConverter;
import com.example.taxnoteandroid.Library.taxnote.TNApiModel;
import com.example.taxnoteandroid.Library.zeny.ZNUtils;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.databinding.ActivityEntryEditBinding;
import com.example.taxnoteandroid.model.Entry;
import com.helpshift.support.Support;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import static com.example.taxnoteandroid.TaxnoteConsts.MIXPANEL_TOKEN;

public class EntryEditActivity extends DefaultCommonActivity {

    private ActivityEntryEditBinding binding;
    private Entry entry;
    private String entryUuid;
    private TNApiModel mApiModel;
    private EntryDataManager entryDataManager;
    private boolean mIsDeleted;

    private boolean mIsCopy;
    private boolean mIsCopySaved = false;
    private static final int REQUEST_CODE_COPY = 111;

    private static final String KEY_IS_COPY_SAVED = "is_copy_saved";
    private static final String KEY_IS_COPY = "is_copy";

    public static void start(Context context, Entry entry) {
        start(context, entry, false);
    }

    public static void start(Context context, Entry entry, boolean isCopy) {
        Intent intent = new Intent(context, EntryEditActivity.class);
        intent.putExtra(Entry.class.getName(), Parcels.wrap(entry));
        intent.putExtra(KEY_IS_COPY, isCopy);
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
        if (mIsCopy) {
            titleRes = (entry.isExpense) ? R.string.copy_expense
                    : R.string.copy_income;
        }
        setTitle(titleRes);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_entry_edit, menu);
//        if (mIsCopy)
//            menu.getItem(0).setVisible(false);
//
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onCopyFinish();
                return true;
//            case R.id.action_copy:
//                Intent intent = new Intent(this, EntryEditActivity.class);
//                intent.putExtra(Entry.class.getName(), Parcels.wrap(entry));
//                intent.putExtra(KEY_IS_COPY, true);
//                startActivityForResult(intent, REQUEST_CODE_COPY);
//                break;
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
        if (entry == null) {
            finish();
            return;
        }
        entryUuid = entry.uuid;

        mIsCopy = intent.getBooleanExtra(KEY_IS_COPY, false);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (mIsDeleted) return;
        super.startActivityForResult(intent, requestCode);
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

        // コピー用画面ならUIを調整 / Taxnoteのみ
        if (mIsCopy && !ZNUtils.isZeny()) {
            binding.bottomCtrlLayout.setVisibility(View.GONE);
            binding.enterEntry.setVisibility(View.VISIBLE);
            binding.enterEntry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    copyEntry();
                }
            });
            binding.date.setBackground(getDrawable(R.drawable.selector_bg_copy_colors));
            binding.account.setBackground(getDrawable(R.drawable.selector_bg_copy_colors));
            binding.reason.setBackground(getDrawable(R.drawable.selector_bg_copy_colors));
            binding.memo.setBackground(getDrawable(R.drawable.selector_bg_copy_colors));
            binding.reason.setBackground(getDrawable(R.drawable.selector_bg_copy_colors));
            binding.price.setBackground(getDrawable(R.drawable.selector_bg_copy_colors));
        }
    }

    private void copyEntry() {
        // DialogManager.showToast(EntryEditActivity.this, "test");

        boolean limitNewEntry = EntryLimitManager.limitNewEntryForFreeUsersWithDate(this, entry.date);

        // Entry limit for free users check
        if (limitNewEntry) {
            showUpgradeSuggest();
            return;
        }

        // Taxnoteクラウド購入なしで追加された帳簿の入力制限あり
        boolean limitNewEntrySubProject = EntryLimitManager.limitNewEntryAddSubProject(this);
        if (!UpgradeManger.taxnoteCloudIsActive(this) && limitNewEntrySubProject) {
            showUpgradeCloudInputLimit();
            return;
        }

        String text = binding.price.getText().toString().replace(",", "");
        // Empty check
        if (TextUtils.isEmpty(text)) {
            DialogManager.showOKOnlyAlert(this, getResources().getString(R.string.Error), getResources().getString(R.string.please_enter_price));
            return;
        }

        Entry copyEntry = entry;
        copyEntry.id = 0;
        copyEntry.uuid = UUID.randomUUID().toString();
        long newId = entryDataManager.save(copyEntry);

        // Success
        if (EntryDataManager.isSaveSuccess(newId)) {
            mIsCopySaved = true;

            countAndTrackEntry();

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                    getResources().getString(R.string.date_string_format_to_month_day),
                    Locale.getDefault());
            String dateString = simpleDateFormat.format(entry.date);
            DialogManager.showInputDataToast(this, dateString, entry);

            mApiModel.saveEntry(entry.uuid, null);


        } else {
            DialogManager.showOKOnlyAlert(this, getResources().getString(R.string.Error), null);
        }
    }

    private void onCopyFinish() {
        if (mIsCopySaved) {
            BroadcastUtil.sendReloadReport(EntryEditActivity.this);
        }

        Intent intent = new Intent();
        intent.putExtra(KEY_IS_COPY_SAVED, mIsCopySaved);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void loadData() {

        // Load the latest entry
        entry = entryDataManager.findByUuid(entryUuid);
        if (entry == null) {
            finish();
            return;
        }

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

                final View textInputView = LayoutInflater.from(EntryEditActivity.this).inflate(R.layout.dialog_text_input, null);
                final EditText editText = (EditText) textInputView.findViewById(R.id.edit);
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

        binding.copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EntryEditActivity.this, EntryEditActivity.class);
                intent.putExtra(Entry.class.getName(), Parcels.wrap(entry));
                intent.putExtra(KEY_IS_COPY, true);
                startActivityForResult(intent, REQUEST_CODE_COPY);
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
                        mIsDeleted = true;

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

    // ---------
    // for copy method
    // ---------
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
                        Intent intent = new Intent(EntryEditActivity.this, UpgradeActivity.class);
                        startActivity(intent);

                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), null)
                .show();
    }

    private void showUpgradeCloudInputLimit() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.taxnote_cloud_first_free)
                .setMessage(R.string.not_cloud_input_limit_message)
                .setNeutralButton(R.string.view_help, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Support.showSingleFAQ(EntryEditActivity.this, "177");
                    }
                })
                .setPositiveButton(R.string.benefits_of_upgrade, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // Show upgrade activity
                        UpgradeActivity.start(EntryEditActivity.this);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean isCopySaved = false;
        if (data != null) {
            isCopySaved = data.getBooleanExtra(KEY_IS_COPY_SAVED, false);
        }

        // コピー用画面を閉じたあと
        // 一度コピーしたらもとの編集画面も閉じる
        if (requestCode == REQUEST_CODE_COPY) {
            if (resultCode == RESULT_OK) {
                if (isCopySaved) {
                    finish();
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        TaxnoteApp.getInstance().IS_HISTORY_LIST_EDITING = false;

        // バックキー
        if (keyCode == KeyEvent.KEYCODE_BACK && mIsCopySaved) {
            onCopyFinish();
            return false;
        }
        return super.onKeyDown(keyCode, event);

    }
}