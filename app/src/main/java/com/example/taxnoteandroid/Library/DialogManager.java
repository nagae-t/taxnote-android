package com.example.taxnoteandroid.Library;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import com.example.taxnoteandroid.model.Entry;

/**
 * Created by umemotonon on 2016/12/24.
 */

public class DialogManager {

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
}
