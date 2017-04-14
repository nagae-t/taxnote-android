package com.example.taxnoteandroid.Library;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.widget.Toast;

import com.example.taxnoteandroid.DataExportActivity;
import com.example.taxnoteandroid.HistoryListDataActivity;
import com.example.taxnoteandroid.R;
import com.example.taxnoteandroid.TNSimpleDialogFragment;
import com.example.taxnoteandroid.UpgradeActivity;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.model.Entry;
import com.example.taxnoteandroid.model.Reason;
import com.github.javiersantos.appupdater.AppUpdater;
import com.helpshift.support.Support;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by umemotonon on 2016/12/24.
 */

public class DialogManager {

    //--------------------------------------------------------------//
    //    -- Toast --
    //--------------------------------------------------------------//

    public static void showInputDataToast(Context context, String dateString, Entry entry) {

        String message;
        String priceString = ValueConverter.formatPrice(context, entry.price);

        if (entry.isExpense) {
            message = dateString + " " + entry.reason.name + " / " + entry.account.name + " " + priceString;
        } else {
            message = dateString + " " + entry.account.name + " / " + entry.reason.name + " " + priceString;
        }

        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void showToast(Context context, String title) {

        Toast toast = Toast.makeText(context, title, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }


    //--------------------------------------------------------------//
    //    -- AlertDialog --
    //--------------------------------------------------------------//

    public static void showOKOnlyAlert(Context context, String title, String message) {

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    public static void showOKOnlyAlert(Context context, int titleRes, int messageRes) {

        new AlertDialog.Builder(context)
                .setTitle(titleRes)
                .setMessage(messageRes)
                .setPositiveButton("OK", null)
                .show();
    }

    public static void showFirstLaunchMessage(final Context context, final FragmentManager fragmentManager) {

        // Show the dialog only one time
        if (SharedPreferencesManager.isFirstLaunchMessageDone(context)) {
            return;
        }

        SharedPreferencesManager.saveFirstLaunchMessageDone(context);

        // Custom Alert
        final TNSimpleDialogFragment dialogFragment = TNSimpleDialogFragment.newInstance();
        dialogFragment.setTitle(context.getString(R.string.AlertView_firstIntroTitle));
        dialogFragment.setMessage(context.getString(R.string.AlertView_firstIntroMessage));

        dialogFragment.setCloseToFinish(true);
        dialogFragment.setPositiveBtnText(context.getString(android.R.string.ok));
        dialogFragment.setDialogListener(new TNSimpleDialogFragment.TNSimpleDialogListener() {
            @Override
            public void onPositiveBtnClick(DialogInterface dialogInterface, int i, String tag) {
                dialogInterface.dismiss();
                showFirstLaunchMessage2(context, fragmentManager);
            }

            @Override
            public void onNeutralBtnClick(DialogInterface dialogInterface, int i, String tag) {
            }

            @Override
            public void onNegativeBtnClick(DialogInterface dialogInterface, int i, String tag) {
            }

            @Override
            public void onDialogCancel(DialogInterface dialogInterface, String tag) {
            }

            @Override
            public void onDialogDismiss(DialogInterface dialogInterface, String tag) {
            }
        });

        dialogFragment.show(fragmentManager, null);
    }

    private static void showFirstLaunchMessage2(final Context context, final FragmentManager fragmentManager) {

        // Custom Alert
        final TNSimpleDialogFragment dialogFragment = TNSimpleDialogFragment.newInstance();
        dialogFragment.setTitle(context.getString(R.string.AlertView_secondIntroTitle));
        dialogFragment.setMessage(context.getString(R.string.AlertView_secondIntroMessage));

        dialogFragment.setCloseToFinish(true);
        dialogFragment.setPositiveBtnText(context.getString(android.R.string.ok));
        dialogFragment.setDialogListener(new TNSimpleDialogFragment.TNSimpleDialogListener() {
            @Override
            public void onPositiveBtnClick(DialogInterface dialogInterface, int i, String tag) {
                dialogInterface.dismiss();
                showFirstLaunchMessage3(context, fragmentManager);
            }

            @Override
            public void onNeutralBtnClick(DialogInterface dialogInterface, int i, String tag) {
            }

            @Override
            public void onNegativeBtnClick(DialogInterface dialogInterface, int i, String tag) {
            }

            @Override
            public void onDialogCancel(DialogInterface dialogInterface, String tag) {
            }

            @Override
            public void onDialogDismiss(DialogInterface dialogInterface, String tag) {
            }
        });

        dialogFragment.show(fragmentManager, null);
    }

    private static void showFirstLaunchMessage3(final Context context, final FragmentManager fragmentManager) {

        // Custom Alert
        final TNSimpleDialogFragment dialogFragment = TNSimpleDialogFragment.newInstance();
        dialogFragment.setTitle(context.getString(R.string.AlertView_thirdIntroTitle));
        dialogFragment.setMessage(context.getString(R.string.AlertView_thirdIntroMessage));

        dialogFragment.setCloseToFinish(true);
        dialogFragment.setPositiveBtnText(context.getString(android.R.string.ok));
        dialogFragment.setDialogListener(new TNSimpleDialogFragment.TNSimpleDialogListener() {
            @Override
            public void onPositiveBtnClick(DialogInterface dialogInterface, int i, String tag) {
                dialogInterface.dismiss();
                showFirstLaunchMessage4(context, fragmentManager);
            }

            @Override
            public void onNeutralBtnClick(DialogInterface dialogInterface, int i, String tag) {
            }

            @Override
            public void onNegativeBtnClick(DialogInterface dialogInterface, int i, String tag) {
            }

            @Override
            public void onDialogCancel(DialogInterface dialogInterface, String tag) {
            }

            @Override
            public void onDialogDismiss(DialogInterface dialogInterface, String tag) {
            }
        });

        dialogFragment.show(fragmentManager, null);
    }

    private static void showFirstLaunchMessage4(final Context context, FragmentManager fragmentManager) {
        showCustomAlertDialog(context, fragmentManager, context.getString(R.string.AlertView_letsbegin), null);
    }

    public static void showSelectSummaryMessage(final Context context) {

        // Show the dialog only one time
        if (SharedPreferencesManager.isSelectSummaryMessageDone(context)) {
            return;
        }

        SharedPreferencesManager.saveSelectSummaryMessageDone(context);

        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.AlertView_selectSummary))
                .setMessage(null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

    public static void showTapRegisterMessage(final Context context) {

        // Show the dialog only one time
        if (SharedPreferencesManager.isSelectRegisterMessageDone(context)) {
            return;
        }

        SharedPreferencesManager.saveSelectRegisterMessageDone(context);

        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.AlertView_inputPrice))
                .setMessage(null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

    public static void showHistoryTabHelpMessage(final Context context, FragmentManager fragmentManager) {

        if (!SharedPreferencesManager.isFirstRegisterDone(context)) {
            return;
        }

        if (SharedPreferencesManager.isHistoryTabHelpDone(context)) {
            return;
        }

        // Custom Alert
        final TNSimpleDialogFragment dialogFragment = TNSimpleDialogFragment.newInstance();
        dialogFragment.setTitle(context.getString(R.string.AlertView_entryDone));
        dialogFragment.setMessage(context.getString(R.string.AlertView_tapHistory));

        dialogFragment.setCloseToFinish(true);
        dialogFragment.setPositiveBtnText(context.getString(android.R.string.ok));
        dialogFragment.setDialogListener(new TNSimpleDialogFragment.TNSimpleDialogListener() {
            @Override
            public void onPositiveBtnClick(DialogInterface dialogInterface, int i, String tag) {
                dialogInterface.dismiss();
                SharedPreferencesManager.saveHistoryTabHelpDone(context);
            }

            @Override
            public void onNeutralBtnClick(DialogInterface dialogInterface, int i, String tag) {
            }

            @Override
            public void onNegativeBtnClick(DialogInterface dialogInterface, int i, String tag) {
            }

            @Override
            public void onDialogCancel(DialogInterface dialogInterface, String tag) {
            }

            @Override
            public void onDialogDismiss(DialogInterface dialogInterface, String tag) {
            }
        });

        dialogFragment.show(fragmentManager, null);
    }

    public static void showAskAnythingMessage(final Context context, FragmentManager fragmentManager) {

        if (!SharedPreferencesManager.isFirstRegisterDone(context)) {
            return;
        }

        if (!SharedPreferencesManager.isHistoryTabHelpDone(context)) {
            return;
        }

        if (SharedPreferencesManager.isAskAnythingMessageDone(context)) {
            return;
        }

        SharedPreferencesManager.saveAskAnythingMessageDone(context);

        // Custom Alert
        final TNSimpleDialogFragment dialogFragment = TNSimpleDialogFragment.newInstance();
        dialogFragment.setTitle(context.getString(R.string.ask_anything_title));
        dialogFragment.setMessage(context.getString(R.string.ask_anything_message));

        dialogFragment.setCloseToFinish(true);
        dialogFragment.setPositiveBtnText(context.getString(R.string.see_help));
        dialogFragment.setNegativeBtnText(context.getString(R.string.cancel));

        dialogFragment.setDialogListener(new TNSimpleDialogFragment.TNSimpleDialogListener() {
            @Override
            public void onPositiveBtnClick(DialogInterface dialogInterface, int i, String tag) {

                Support.showFAQSection((Activity) context, "22");
                dialogInterface.dismiss();
            }

            @Override
            public void onNeutralBtnClick(DialogInterface dialogInterface, int i, String tag) {
            }

            @Override
            public void onNegativeBtnClick(DialogInterface dialogInterface, int i, String tag) {
                dialogInterface.dismiss();
            }

            @Override
            public void onDialogCancel(DialogInterface dialogInterface, String tag) {
            }

            @Override
            public void onDialogDismiss(DialogInterface dialogInterface, String tag) {
            }
        });

        dialogFragment.show(fragmentManager, null);
    }

    public static void showDataExportSuggestMessage(final Context context, FragmentManager fragmentManager) {

        if (SharedPreferencesManager.isDataExportSuggestDone(context)) {
            return;
        }

        EntryDataManager entryDataManager = new EntryDataManager(context);
        List<Entry> entries = entryDataManager.findAll(null, true);

        if (entries.size() < 5) {
            return;
        }

        SharedPreferencesManager.saveDataExportSuggestDone(context);

        // Custom Alert
        final TNSimpleDialogFragment dialogFragment = TNSimpleDialogFragment.newInstance();
        dialogFragment.setTitle(context.getString(R.string.data_export_suggest_title));
        dialogFragment.setMessage(context.getString(R.string.data_export_suggest_message));

        dialogFragment.setCloseToFinish(true);
        dialogFragment.setPositiveBtnText(context.getString(R.string.data_export_suggest_button));
        dialogFragment.setNegativeBtnText(context.getString(R.string.cancel));

        dialogFragment.setDialogListener(new TNSimpleDialogFragment.TNSimpleDialogListener() {
            @Override
            public void onPositiveBtnClick(DialogInterface dialogInterface, int i, String tag) {

                Intent intent = new Intent(context, DataExportActivity.class);
                context.startActivity(intent);
                dialogInterface.dismiss();
            }

            @Override
            public void onNeutralBtnClick(DialogInterface dialogInterface, int i, String tag) {
            }

            @Override
            public void onNegativeBtnClick(DialogInterface dialogInterface, int i, String tag) {
                dialogInterface.dismiss();
            }

            @Override
            public void onDialogCancel(DialogInterface dialogInterface, String tag) {
            }

            @Override
            public void onDialogDismiss(DialogInterface dialogInterface, String tag) {
            }
        });

        dialogFragment.show(fragmentManager, null);
    }

    public static void showBusinessModelMessage(final Context context, FragmentManager fragmentManager) {

        // Skip Taxnote Plus users
        if (UpgradeManger.taxnotePlusIsActive(context)) {
            return;
        }

        if (!SharedPreferencesManager.isFirstRegisterDone(context)) {
            return;
        }

        if (!SharedPreferencesManager.isHistoryTabHelpDone(context)) {
            return;
        }

        if (!SharedPreferencesManager.isAskAnythingMessageDone(context)) {
            return;
        }

        if (SharedPreferencesManager.isBusinessModelMessageDone(context)) {
            return;
        }

        EntryDataManager entryDataManager = new EntryDataManager(context);
        List<Entry> entries = entryDataManager.findAll(null, true);

        if (entries.size() < 4) {
            return;
        }

        SharedPreferencesManager.saveBusinessModelMessageDone(context);

        // Custom Alert
        final TNSimpleDialogFragment dialogFragment = TNSimpleDialogFragment.newInstance();
        dialogFragment.setTitle(context.getString(R.string.business_model_title));
        dialogFragment.setMessage(context.getString(R.string.business_model_message));

        dialogFragment.setCloseToFinish(true);
        dialogFragment.setPositiveBtnText(context.getString(R.string.benefits_of_upgrade));
        dialogFragment.setNegativeBtnText(context.getString(R.string.cancel));

        dialogFragment.setDialogListener(new TNSimpleDialogFragment.TNSimpleDialogListener() {
            @Override
            public void onPositiveBtnClick(DialogInterface dialogInterface, int i, String tag) {

                // Show upgrade activity
                Intent intent = new Intent(context, UpgradeActivity.class);
                context.startActivity(intent);

                dialogInterface.dismiss();
            }

            @Override
            public void onNeutralBtnClick(DialogInterface dialogInterface, int i, String tag) {
            }

            @Override
            public void onNegativeBtnClick(DialogInterface dialogInterface, int i, String tag) {
                dialogInterface.dismiss();
            }

            @Override
            public void onDialogCancel(DialogInterface dialogInterface, String tag) {
            }

            @Override
            public void onDialogDismiss(DialogInterface dialogInterface, String tag) {
            }
        });

        dialogFragment.show(fragmentManager, null);
    }

    //@@@
    public static void showChartsTapSuggestMessage(final Context context, FragmentManager fragmentManager) {

        if (SharedPreferencesManager.isChartsTapMessageDone(context)) {
            return;
        }

        EntryDataManager entryDataManager = new EntryDataManager(context);
        List<Entry> entries = entryDataManager.findAll( null, true);

        if (entries.size() < 5) {
            return;
        }

        SharedPreferencesManager.saveChartsTapMessageDone(context);

        //@@@
        // Custom Alert
        final TNSimpleDialogFragment dialogFragment = TNSimpleDialogFragment.newInstance();
        dialogFragment.setTitle(context.getString(R.string.data_export_suggest_title));
        dialogFragment.setMessage(context.getString(R.string.data_export_suggest_message));

        dialogFragment.setCloseToFinish(true);
        dialogFragment.setPositiveBtnText(context.getString(android.R.string.ok));

        dialogFragment.setDialogListener(new TNSimpleDialogFragment.TNSimpleDialogListener() {
            @Override
            public void onPositiveBtnClick(DialogInterface dialogInterface, int i, String tag) {
                dialogInterface.dismiss();
            }
            @Override
            public void onNeutralBtnClick(DialogInterface dialogInterface, int i, String tag) {}
            @Override
            public void onNegativeBtnClick(DialogInterface dialogInterface, int i, String tag) {}
            @Override
            public void onDialogCancel(DialogInterface dialogInterface, String tag) {}
            @Override
            public void onDialogDismiss(DialogInterface dialogInterface, String tag) {}
        });

        dialogFragment.show(fragmentManager, null);
    }


    //--------------------------------------------------------------//
    //    -- Release Note --
    //--------------------------------------------------------------//

    public static void showReleaseNoteAfterUpdate(final Context context, FragmentManager fragmentManager) {

        String lastVersionName      = SharedPreferencesManager.getLastVersionName(context);
        String currentVersionName   = com.example.taxnoteandroid.BuildConfig.VERSION_NAME;

        // Skip for the first run
        if (lastVersionName.isEmpty()) {
            SharedPreferencesManager.saveLastVersionName(context, currentVersionName);
            return;
        }

        // Skip when no update is available
        if (lastVersionName.equals(currentVersionName)) {
            return;
        }

        // Save it when a new update is available
        SharedPreferencesManager.saveLastVersionName(context, currentVersionName);

        // Custom Alert
        final TNSimpleDialogFragment dialogFragment = TNSimpleDialogFragment.newInstance();

        String title = context.getString(R.string.release_note_title) + currentVersionName;
        dialogFragment.setTitle(title);
        dialogFragment.setMessage(context.getString(R.string.release_note_message));

        dialogFragment.setCloseToFinish(true);
        dialogFragment.setPositiveBtnText("OK");
        dialogFragment.setNegativeBtnText(context.getString(R.string.cancel));

        dialogFragment.setDialogListener(new TNSimpleDialogFragment.TNSimpleDialogListener() {
            @Override
            public void onPositiveBtnClick(DialogInterface dialogInterface, int i, String tag) {

                dialogInterface.dismiss();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.release_note_url)));
                context.startActivity(intent);
            }

            @Override
            public void onNeutralBtnClick(DialogInterface dialogInterface, int i, String tag) {}
            @Override
            public void onNegativeBtnClick(DialogInterface dialogInterface, int i, String tag) {
                dialogInterface.dismiss();
            }
            @Override
            public void onDialogCancel(DialogInterface dialogInterface, String tag) {}
            @Override
            public void onDialogDismiss(DialogInterface dialogInterface, String tag) {}
        });

        dialogFragment.show(fragmentManager, null);
    }

    public static void checkLatestUpdate(final Context context) {

        AppUpdater appUpdater = new AppUpdater(context)
                .setTitleOnUpdateAvailable(context.getResources().getString(R.string.update_check_title))
                .setContentOnUpdateAvailable(context.getResources().getString(R.string.update_check_message))
                .setButtonUpdate(context.getResources().getString(R.string.update_check_update))
                .setButtonDismiss(context.getResources().getString(R.string.update_check_later))
                .setButtonDoNotShowAgain(context.getResources().getString(R.string.update_check_do_not_show_again));
        appUpdater.start();
    }


    //--------------------------------------------------------------//
    //    -- Show Import Data Confirm AlertDialog --
    //--------------------------------------------------------------//

    public static void showImportDataConfirm(final Context context, FragmentManager fragmentManager,
                                             TNSimpleDialogFragment.TNSimpleDialogListener listener) {
        String title = context.getString(R.string.data_import);
        String message = context.getString(R.string.import_data_overwrite_confirm_message);

        final TNSimpleDialogFragment dialogFragment = TNSimpleDialogFragment.newInstance();
        dialogFragment.setTitle(title);
        dialogFragment.setMessage(message);
        dialogFragment.setCloseToFinish(true);

        dialogFragment.setPositiveBtnText(context.getString(android.R.string.ok));
        dialogFragment.setNegativeBtnText(context.getString(android.R.string.cancel));
        dialogFragment.setDialogListener(listener);

        dialogFragment.show(fragmentManager, null);
    }


    //--------------------------------------------------------------//
    //    -- Input Recurring With Taxnote Cloud Required Dialog --
    //--------------------------------------------------------------//

    public static void showRurringTaxnoteCloudRequired(final Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.taxnote_cloud_first_free)
                .setMessage(R.string.recurring_cloud_required_message)
                .setPositiveButton(R.string.upgrade, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        UpgradeActivity.start(activity);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }


    //--------------------------------------------------------------//
    //    -- Bar Graph --
    //--------------------------------------------------------------//

    public static void showBarInfoDialog(final Activity activity, int periodType,
                                         final Calendar targetCalendar, Reason reason,
                                         final boolean isExpense, long price) {

        String dateFormatString = (periodType == EntryDataManager.PERIOD_TYPE_YEAR)
                ? activity.getString(R.string.date_string_format_to_year_month)
                : activity.getString(R.string.date_string_format_to_year_month_day);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                dateFormatString, Locale.getDefault());
        String calStr = simpleDateFormat.format(targetCalendar.getTime());
        String isExpenseString = (isExpense) ? activity.getString(R.string.Expense)
                : activity.getString(R.string.Income);
        String message = calStr + " " + isExpenseString
                + "\n" + ValueConverter.formatPrice(activity, price);
        final String reasonName = (reason == null) ? null : reason.name;

        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setNeutralButton(R.string.History, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        HistoryListDataActivity.start(activity,
                                targetCalendar, reasonName, isExpense);
                    }
                })
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }


    //--------------------------------------------------------------//
    //    -- Custom AlertDialog --
    //--------------------------------------------------------------//

    public static void showCustomAlertDialog(Context context, FragmentManager fragmentManager,
                                             String title, String message) {
        showCustomAlertDialog(context, fragmentManager, title, message, 0);
    }

    public static void showCustomAlertDialog(Context context, FragmentManager fragmentManager,
                                             String title, int layoutId) {
        showCustomAlertDialog(context, fragmentManager, title, null, layoutId);
    }

    public static void showCustomAlertDialog(Context context, FragmentManager fragmentManager,
                                             String title, String message, int layoutId) {

        // test custom dialog
        final TNSimpleDialogFragment dialogFragment = TNSimpleDialogFragment.newInstance();
        dialogFragment.setTitle(title);
        if (message != null) dialogFragment.setMessage(message);

        // layout 指定する場合
        if (layoutId != 0)
            dialogFragment.setContentViewId(layoutId);

        // 閉じるボタン以外はダイアログ消せない
        dialogFragment.setCloseToFinish(true);
        dialogFragment.setPositiveBtnText(context.getString(android.R.string.ok));
        dialogFragment.setDialogListener(new TNSimpleDialogFragment.TNSimpleDialogListener() {
            @Override
            public void onPositiveBtnClick(DialogInterface dialogInterface, int i, String tag) {
                dialogInterface.dismiss();
            }

            @Override
            public void onNeutralBtnClick(DialogInterface dialogInterface, int i, String tag) {
            }

            @Override
            public void onNegativeBtnClick(DialogInterface dialogInterface, int i, String tag) {
            }

            @Override
            public void onDialogCancel(DialogInterface dialogInterface, String tag) {
            }

            @Override
            public void onDialogDismiss(DialogInterface dialogInterface, String tag) {
            }
        });

        dialogFragment.show(fragmentManager, null);
    }
}
