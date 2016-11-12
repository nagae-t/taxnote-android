package com.example.taxnoteandroid.entryTab;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taxnoteandroid.R;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.model.Entry;

import java.util.UUID;

public class InputDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_input_data);
        setSaveButton();
    }


    //--------------------------------------------------------------//
    //    -- Display Part --
    //--------------------------------------------------------------//

    private void setSaveButton() {

        TextView saveButton = (TextView) findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveEntry();
            }
        });
    }


    //--------------------------------------------------------------//
    //    -- Handle Data --
    //--------------------------------------------------------------//

    private void saveEntry() {

        EntryDataManager entryDataManager = new EntryDataManager(InputDataActivity.this);

        Entry entry = new Entry();
        entry.price = 1000;
        entry.memo  = "memo";
        entry.uuid  = UUID.randomUUID().toString();
        long id     = entryDataManager.save(entry);

        // Success
        if (EntryDataManager.isSaveSuccess(id)) {

            showSavingDoneToast();

            // 処理が正常に終わったりしたら、呼び出された画面にたいしてOKだったと伝えるためにsetResultする
            setResult(RESULT_OK);
            finish();

            // Show error message
            // QQここでアラートを出したい
        } else {

        }
    }


    //--------------------------------------------------------------//
    //    -- Pop View --
    //--------------------------------------------------------------//

    private void showSavingDoneToast() {

        //@@ ここにチェックマークの画像を追加
        Toast toast = Toast.makeText(InputDataActivity.this, "Hogehoge", Toast.LENGTH_SHORT);

        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setView(getLayoutInflater().inflate(R.layout.toast_save, null));
        toast.show();
    }


    //--------------------------------------------------------------//
    //    -- View Transition --
    //--------------------------------------------------------------//

    public static Intent createIntent(Context context) {
        Intent i = new Intent(context, InputDataActivity.class);
        return i;
    }
}
