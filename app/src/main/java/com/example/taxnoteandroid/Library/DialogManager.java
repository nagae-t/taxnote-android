package com.example.taxnoteandroid.Library;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.widget.Toast;

import com.example.taxnoteandroid.DataExportActivity;
import com.example.taxnoteandroid.R;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.model.Entry;
import com.helpshift.support.Support;

import java.util.List;

/**
 * Created by umemotonon on 2016/12/24.
 */

public class DialogManager {

    //--------------------------------------------------------------//
    //    -- Toast --
    //--------------------------------------------------------------//

    public static void showInputDataToast(Context context, String dateString, Entry entry) {

        String message;
        String priceString = ValueConverter.formatPrice(context ,entry.price);

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

    public static void showFirstLaunchMessage(final Context context) {

        // Show the dialog only one time
        if (SharedPreferencesManager.isFirstLaunchMessageDone(context)) {
            return;
        }

        SharedPreferencesManager.saveFirstLaunchMessageDone(context);

        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.AlertView_firstIntroTitle))
                .setMessage(context.getString(R.string.AlertView_firstIntroMessage))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();
                        showFirstLaunchMessage2(context);
                    }
                })
                .show();
    }

    private static void showFirstLaunchMessage2(final Context context) {

        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.AlertView_secondIntroTitle))
                .setMessage(context.getString(R.string.AlertView_secondIntroMessage))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();

                        //@@ 損益表を作った後に有効
//                        showFirstLaunchMessage3(context);
                    }
                })
                .show();
    }

    private static void showFirstLaunchMessage3(final Context context) {

        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.AlertView_thirdIntroTitle))
                .setMessage(context.getString(R.string.AlertView_thirdIntroMessage))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();
                        showFirstLaunchMessage4(context);
                    }
                })
                .show();
    }

    private static void showFirstLaunchMessage4(final Context context) {

        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.AlertView_letsbegin))
                .setMessage(null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();

                    }
                })
                .show();
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

    public static void showHistoryTabHelpMessage(final Context context) {

        if (!SharedPreferencesManager.isFirstRegisterDone(context)) {
            return;
        }

        if (SharedPreferencesManager.isHistoryTabHelpDone(context)) {
            return;
        }

        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.AlertView_entryDone))
                .setMessage(context.getString(R.string.AlertView_tapHistory))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        SharedPreferencesManager.saveHistoryTabHelpDone(context);
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

    public static void showAskAnythingMessage(final Context context) {

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

        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.ask_anything_title))
                .setMessage(context.getString(R.string.ask_anything_message))
                .setPositiveButton(context.getString(R.string.see_help), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Support.showFAQSection((Activity) context, "22");
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

    public static void showDataExportSuggestMessage(final Context context) {

        if (SharedPreferencesManager.isDataExportSuggestDone(context)) {
            return;
        }

        EntryDataManager entryDataManager = new EntryDataManager(context);
        List<Entry> entries = entryDataManager.findAll(context, null, true);

        if (entries.size() < 3) {
            return;
        }

        SharedPreferencesManager.saveDataExportSuggestDone(context);

        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.data_export_suggest_title))
                .setMessage(context.getString(R.string.data_export_suggest_message))
                .setPositiveButton(context.getString(R.string.data_export_suggest_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Intent intent = new Intent(context, DataExportActivity.class);
                        context.startActivity(intent);
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }
}
