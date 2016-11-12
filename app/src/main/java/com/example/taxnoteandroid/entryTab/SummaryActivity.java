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
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Reason;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class SummaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        Intent intent = getIntent();

        // @@
        boolean isExpense = intent.getBooleanExtra(EXTRA_ISEXPENSE, false);
        long date = intent.getLongExtra(EXTRA_DATE, 0);
        Account account = Parcels.unwrap(intent.getParcelableExtra(Account.class.getName()));
        Reason reason = Parcels.unwrap(intent.getParcelableExtra(Reason.class.getName()));

        setTitle(reason.name);

        ListView listView = (ListView) findViewById(R.id.reason_list_view);

        List<String> strings = new ArrayList<>();
        strings.add("test1");
        strings.add("test2");
        strings.add("test3");
        strings.add("test4");
        strings.add("test5");
        strings.add("test3");
        strings.add("test4");
        strings.add("test5");
        strings.add("test3");
        strings.add("test4");
        strings.add("test5");
        strings.add("test3");
        strings.add("test4");
        strings.add("test5");

        View next = findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // request codeを指定する.onActivityResultのrequestCodeで返ってくる
                startActivityForResult(InputDataActivity.createIntent(SummaryActivity.this), 1);
            }
        });

        listView.setAdapter(new ListAdapter(this, strings));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            }
        });
        listView.addFooterView(getLayoutInflater().inflate(R.layout.listview_footer, null));
    }

    //  https://material.google.com/components/lists.html#
    class ListAdapter extends ArrayAdapter<String> {

        private LayoutInflater layoutInflater;

        public ListAdapter(Context context, List<String> texts) {
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
            String s = getItem(position);

            TextView textView = (TextView) v.findViewById(R.id.text);
            textView.setText(s);

            return v;
        }
    }


    //--------------------------------------------------------------//
    //    -- View Transition --
    //--------------------------------------------------------------//

    private static final String EXTRA_ISEXPENSE = "isExpense";
    private static final String EXTRA_DATE = "date";

    public static Intent createIntent(Context context, boolean isExpense, long date, Account account, Reason reason) {
        Intent i = new Intent(context, SummaryActivity.class);
        i.putExtra(EXTRA_ISEXPENSE, isExpense);
        i.putExtra(Account.class.getName(), Parcels.wrap(account));
        i.putExtra(Reason.class.getName(), Parcels.wrap(reason));
        i.putExtra(EXTRA_DATE, date);
        return i;
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
