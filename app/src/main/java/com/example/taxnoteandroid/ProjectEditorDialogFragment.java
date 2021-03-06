package com.example.taxnoteandroid;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;

import com.example.taxnoteandroid.databinding.DialogProjectEditorBinding;

/**
 * Created by b0ne on 2017/03/06.
 */

public class ProjectEditorDialogFragment extends DialogFragment
        implements DialogInterface.OnClickListener {

    private DialogProjectEditorBinding binding;
    private Context mContext;
    private AlertDialog mDialog;
    private EditText mEditName;

    private OnEditorSubmitListener mListener;

    public static final int TYPE_ADD_NEW = 1;
    public static final int TYPE_EDIT_NAME = 2;
    private static final String KEY_TYPE = "dialog_type";

    public interface OnEditorSubmitListener {
        void onSubmit(DialogInterface dialogInterface, int dialogType, EditText nameEdit, String tag);
    }

    public static ProjectEditorDialogFragment newInstance(int type) {
        ProjectEditorDialogFragment fragment = new ProjectEditorDialogFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mContext = getActivity().getApplicationContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final int dialogType = getArguments().getInt(KEY_TYPE);
        String titleName = mContext.getString(R.string.add_new_project);
        if (dialogType == TYPE_EDIT_NAME) {
            titleName = mContext.getString(R.string.edit_project_name);
        }

        builder.setTitle(titleName).setMessage(null);

        builder.setNegativeButton(mContext.getText(android.R.string.cancel), this);
        builder.setPositiveButton(mContext.getText(android.R.string.ok), this);

        // ??????????????????????????????????????????????????????
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        if (binding.nameEdit.getText().length() == 0) return false;
                        return true;
                }
                return false;
            }
        }).setCancelable(false);

        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()),
                R.layout.dialog_project_editor, null, false);
        builder.setView(binding.getRoot());
        binding.nameEdit.addTextChangedListener(mTextEditorWatcher);
        if (dialogType == TYPE_EDIT_NAME) {
            binding.nameEdit.setHint(null);
        }
        mEditName = binding.nameEdit;


        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        mDialog = dialog;
        dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mDialog != null) {
            mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        }
    }

    private final TextWatcher mTextEditorWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mDialog == null) return;
            Button positiveBtn = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (count > 0) {
                positiveBtn.setEnabled(true);
            } else {
                positiveBtn.setEnabled(false);
            }
        }

        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if (mListener == null) return;
        if (i == DialogInterface.BUTTON_POSITIVE) {
            int dialogType = getArguments().getInt(KEY_TYPE);
            mListener.onSubmit(dialogInterface, dialogType, mEditName, getTag());
        }
    }

    public void setOnSubmitListener(OnEditorSubmitListener listener) {
        this.mListener = listener;
    }


}
