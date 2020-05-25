package com.example.taxnoteandroid;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.WindowManager;

import com.example.taxnoteandroid.databinding.DialogExportHeaderSettingBinding;

public class ExportHeaderSettingDialogFragment extends DialogFragment
        implements DialogInterface.OnClickListener {

    public interface OnSubmitListener {
        void onSubmit(DialogInterface dialogInterface, String businessName, String summary, String period);
    }

    private static final String KEY_DEFAULT_SUMMARY = "KEY_DEFAULT_SUMMARY";

    private DialogExportHeaderSettingBinding binding;

    private OnSubmitListener mListener;

    static public ExportHeaderSettingDialogFragment newInstance(String summary) {
        ExportHeaderSettingDialogFragment fragment = new ExportHeaderSettingDialogFragment();
        Bundle args = new Bundle();
        args.putString(KEY_DEFAULT_SUMMARY, summary);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnSubmitListener(OnSubmitListener listener) {
        this.mListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.export_header_dialog_title);
        builder.setMessage(R.string.export_header_dialog_message);
        builder.setNegativeButton(context.getText(android.R.string.cancel), this);
        builder.setPositiveButton(context.getText(android.R.string.ok), this);

        binding = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.dialog_export_header_setting, null, false);
        builder.setView(binding.getRoot());

        String defaultSummary = getArguments().getString(KEY_DEFAULT_SUMMARY);
        binding.editSummary.setText(defaultSummary);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mListener == null) return;
        if (which == DialogInterface.BUTTON_POSITIVE) {
            mListener.onSubmit(dialog,
                    binding.editBusinessName.getText().toString(),
                    binding.editSummary.getText().toString(),
                    binding.editPeriod.getText().toString());
        }
    }
}
