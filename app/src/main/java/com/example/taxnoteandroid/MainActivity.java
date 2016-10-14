package com.example.taxnoteandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements Consts
{
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    LinearLayout layout = (LinearLayout)this.findViewById(R.id.main_container);
    layout.setOrientation(LinearLayout.VERTICAL);

    TextView text1 = new TextView(this);
    text1.setText("ここに日付を入れる予定");

    TextView text2 = new TextView(this);
    text2.setText("ここに支払い方法を入れる予定");

    ListView list_view = new ListView(this);

    layout.addView(text1, MATCH_PARENT, WRAP_CONTENT);
    layout.addView(text2, MATCH_PARENT, WRAP_CONTENT);
    layout.addView(list_view, MATCH_PARENT, WRAP_CONTENT);

  }
}
