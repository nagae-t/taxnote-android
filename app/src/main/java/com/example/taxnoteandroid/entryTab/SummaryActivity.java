package com.example.taxnoteandroid.entryTab;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.taxnoteandroid.R;
import com.example.taxnoteandroid.dataManager.SummaryDataManager;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Reason;
import com.example.taxnoteandroid.model.Summary;

import org.parceler.Parcels;

import java.util.List;

public class SummaryActivity extends AppCompatActivity {

    private static final String EXTRA_IS_EXPENSE = "isExpense";
    private static final String EXTRA_DATE       = "date";
    public boolean isExpense;
    public Account account;
    public Reason reason;
    public long date;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        setIntentData();
        setTitle(reason.name);
        setNextButton();
        setSummaryList(reason);
    }


    //--------------------------------------------------------------//
    //    -- Intent --
    //--------------------------------------------------------------//

    public static Intent createIntent(Context context, boolean isExpense, long date, Account account, Reason reason) {

        Intent i = new Intent(context, SummaryActivity.class);

        i.putExtra(EXTRA_IS_EXPENSE, isExpense);
        i.putExtra(Account.class.getName(), Parcels.wrap(account));
        i.putExtra(Reason.class.getName(), Parcels.wrap(reason));
        i.putExtra(EXTRA_DATE, date);

        return i;
    }

    private void setIntentData() {

        Intent intent = getIntent();

        isExpense   = intent.getBooleanExtra(EXTRA_IS_EXPENSE, false);
        date        = intent.getLongExtra(EXTRA_DATE, 0);
        account     = Parcels.unwrap(intent.getParcelableExtra(Account.class.getName()));
        reason      = Parcels.unwrap(intent.getParcelableExtra(Reason.class.getName()));
    }


    //--------------------------------------------------------------//
    //    -- Summary List --
    //--------------------------------------------------------------//

    private void setNextButton() {

        View next = findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startInputDataActivity(null);
            }
        });
    }

    private void setSummaryList(Reason reason){

        // Get summary list
        SummaryDataManager summaryDataManager   = new SummaryDataManager(this);
        List<Summary> summaries                 = summaryDataManager.findAllWithReason(reason, this);

        ListView listView = (ListView) findViewById(R.id.reason_list_view);

        listView.setAdapter(new ListAdapter(this, summaries));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Summary summary = (Summary) adapterView.getItemAtPosition(position);
                startInputDataActivity(summary);
            }
        });
        listView.addFooterView(getLayoutInflater().inflate(R.layout.listview_footer, null));
    }

    //  https://material.google.com/components/lists.html#
    class ListAdapter extends ArrayAdapter<Summary> {

        private LayoutInflater layoutInflater;

        public ListAdapter(Context context, List<Summary> texts) {
            super(context, 0, texts);
            layoutInflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // getViewでListViewの1つのセルを作る
            // inflateでViewにする
            View v = layoutInflater.inflate(R.layout.row_list_item, null);

            // getItemでcallのViewにbindしたいデータ型を取得できる
            Summary summary = getItem(position);

            TextView textView = (TextView) v.findViewById(R.id.text);
            textView.setText(summary.name);

            return v;
        }
    }


    //--------------------------------------------------------------//
    //    -- View Transition --
    //--------------------------------------------------------------//

    private void startInputDataActivity(Summary summary) {
        startActivityForResult(InputDataActivity.createIntent(SummaryActivity.this, isExpense, System.currentTimeMillis(), account, reason, summary), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // startActivityForResultで呼び出したActivityの処理がどうだったのかを確認する
        // resultCodeがInputDataActivityのsetResultでセットした値
        if (requestCode == 1 && resultCode == RESULT_OK) {
            finish();
        }
    }
}
