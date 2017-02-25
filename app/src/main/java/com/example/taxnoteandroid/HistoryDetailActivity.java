package com.example.taxnoteandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by b0ne on 2017/02/25.
 */

public class HistoryDetailActivity extends AppCompatActivity {

    private CommonEntryRecyclerAdapter mEntryAdapter;

    public static void start(Context context) {
        Intent intent = new Intent(context, HistoryDetailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
