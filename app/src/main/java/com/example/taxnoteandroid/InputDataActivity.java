package com.example.taxnoteandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class InputDataActivity extends AppCompatActivity {

    public static Intent createIntent(Context context) {
        Intent i = new Intent(context, InputDataActivity.class);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_data);

        TextView save = (TextView) findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast toast = Toast.makeText(InputDataActivity.this, "Hogehoge", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.setView(getLayoutInflater().inflate(R.layout.toast_save, null));
                toast.show();

                // 処理が正常に終わったりしたら、呼び出された画面にたいしてOKだったと伝えるためにsetResultする
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}
