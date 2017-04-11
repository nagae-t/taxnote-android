package com.example.taxnoteandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by b0ne on 2017/04/11.
 */

public class InputRecurringListActivity extends DefaultCommonActivity {

    public static void start(Context context) {
        Intent intent = new Intent(context, InputRecurringListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
