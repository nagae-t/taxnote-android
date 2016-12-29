package com.example.taxnoteandroid;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.taxnoteandroid.databinding.ActivityEntryEditBinding;
import com.example.taxnoteandroid.model.Entry;

import org.parceler.Parcels;

public class EntryEditActivity extends AppCompatActivity {

    private ActivityEntryEditBinding binding;

    public static Intent createIntent(Context context, Entry entry) {
        Intent i = new Intent(context, EntryEditActivity.class);
        i.putExtra(Entry.class.getName(), Parcels.wrap(entry));
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setIntentAndBiding();
    }


    private void setIntentAndBiding() {

        //@@@ entryを表示していく

        binding = DataBindingUtil.setContentView(this, R.layout.activity_entry_edit);
        Intent intent = getIntent();
        Entry entry = Parcels.unwrap(intent.getParcelableExtra(Entry.class.getName()));
        binding.setEntry(entry);
    }
}