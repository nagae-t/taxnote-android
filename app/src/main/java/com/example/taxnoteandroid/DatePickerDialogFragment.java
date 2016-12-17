package com.example.taxnoteandroid;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v7.app.AppCompatDialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;

public class DatePickerDialogFragment extends AppCompatDialogFragment implements DatePickerDialog.OnDateSetListener {

    private static final String EXTRA_INIT_DATE = "init_date";
    private static final String EXTRA_PICKER_TITLE = "picker_title";
    private static final String EXTRA_THEME = "theme";

    private OnDateSetListener onDateSetListener;
    private int padding = -1;

    public interface OnDateSetListener {
        void onDateSet(Calendar calendar);
    }

    public DatePickerDialogFragment() {
    }

    public static DatePickerDialogFragment newInstance(long initDate, String pickerTitle) {
        DatePickerDialogFragment fragment = new DatePickerDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_INIT_DATE, initDate);
        bundle.putString(EXTRA_PICKER_TITLE, pickerTitle);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static DatePickerDialogFragment newInstance(@StyleRes int theme, long initDate, String pickerTitle) {
        DatePickerDialogFragment fragment = newInstance(initDate, pickerTitle);
        Bundle bundle = fragment.getArguments();
        bundle.putInt(EXTRA_THEME, theme);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDateSetListener) {
            onDateSetListener = (OnDateSetListener) context;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(bundle.getLong(EXTRA_INIT_DATE));
        final String title = bundle.getString(EXTRA_PICKER_TITLE);
//        int theme = bundle.getInt(EXTRA_THEME, R.style.DatePickerSpinnerDialogTheme);

//        int padding = this.padding.orElse(getContext().getResources().getDimensionPixelSize(R.dimen.date_picker_padding));

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)) {
            @Override
            public void onDateChanged(@NonNull DatePicker view, int year, int month, int day) {
                super.onDateChanged(view, year, month, day);
                setTitle(title); // API 24からtitleが選択中の日付に変わる処理はなくなってるけど、それ以下のバージョンでは変わるので指定のTitleに変えるようにした
            }
        };
        datePickerDialog.setTitle(title);
        // [INFO] API 24でThemeが適用されないで、calendarのUIが出るbugがある. https://code.google.com/p/android/issues/detail?id=222208
        DatePicker datePicker = datePickerDialog.getDatePicker();
//        datePicker.setPadding(padding, padding, padding, padding);
        return datePickerDialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        if (onDateSetListener != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, monthOfYear, dayOfMonth, 0, 0, 0);
            onDateSetListener.onDateSet(calendar);
        }
    }

    public void setOnDateSetListener(OnDateSetListener onDateSetListener) {
        this.onDateSetListener = onDateSetListener;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }
}