package com.example.taxnoteandroid.Library;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.widget.Toast;

import com.example.taxnoteandroid.R;
import com.example.taxnoteandroid.model.Entry;

/**
 * Created by umemotonon on 2016/12/24.
 */

public class DialogManager {

    //--------------------------------------------------------------//
    //    -- Toast --
    //--------------------------------------------------------------//

    public static void showInputDataToast(Context context, Entry entry) {

        String message;
        String priceString = ValueConverter.formatPrice(entry.price);

        if (entry.isExpense) {
            message =  entry.reason.name + " / " + entry.account.name + " :" + priceString;
        } else {
            message = entry.account.name + " / " + entry.reason.name + " :" + priceString;
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

    //@@@　いまここ、最初のメッセージやってる
    public static void showFirstLaunchMessage(final Context context) {

//        // Show the dialog only one time
//        if (SharedPreferencesManager.isFirstLaunchMessageDone(context)) {
//            return;
//        }
//
//        SharedPreferencesManager.saveFirstLaunchMessageDone(context);

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
                        showFirstLaunchMessage3(context);
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

//        // Show the dialog only one time
//        if (SharedPreferencesManager.isSelectSummaryMessageDone(context)) {
//            return;
//        }
//
//        SharedPreferencesManager.saveSelectSummaryMessageDone(context);

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

//        // Show the dialog only one time
//        if (SharedPreferencesManager.isSelectRegisterMessageDone(context)) {
//            return;
//        }
//
//        SharedPreferencesManager.saveSelectRegisterMessageDone(context);

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


}
