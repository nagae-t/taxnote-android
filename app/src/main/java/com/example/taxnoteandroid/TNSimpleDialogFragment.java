package com.example.taxnoteandroid;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;

/**
 * Created by b0ne on 2017/03/04.
 */

public class TNSimpleDialogFragment extends DialogFragment
        implements DialogInterface.OnClickListener {

    private static final String KEY_DIALOG_TITLE = "dialog-title";
    private static final String KEY_DIALOG_MESSAGE = "dialog-message";
    private static final String KEY_DIALOG_CONTENT_VIEW = "dialog-content-view";
    private static final String KEY_DIALOG_POSITIVE_BUTTON_TEXT = "dialog-positive-btn-text";
    private static final String KEY_DIALOG_NEUTRAL_BUTTON_TEXT = "dialog-neutral-btn-text";
    private static final String KEY_DIALOG_NEGATIVE_BUTTON_TEXT = "dialog-negative-btn-text";
    private static final String KEY_DIALOG_IS_CLOSE_TO_FINISH = "dialog-is-close-to-finish";

    private static final int DEFAULT_INT_VALUE = 0;
    private View mDialogView;
    private TNSimpleDialogListener mListener;

    // interface
    public interface TNSimpleDialogListener {
        public void onPositiveBtnClick(DialogInterface dialogInterface, int i, String tag);
        public void onNeutralBtnClick(DialogInterface dialogInterface, int i, String tag);
        public void onNegativeBtnClick(DialogInterface dialogInterface, int i, String tag);
        public void onDialogCancel(DialogInterface dialogInterface, String tag);
        public void onDialogDismiss(DialogInterface dialogInterface, String tag);
    }

    public static TNSimpleDialogFragment newInstance() {
        TNSimpleDialogFragment commonDialogFragment = new TNSimpleDialogFragment();
        Bundle args = new Bundle();
        commonDialogFragment.setArguments(args);
        return commonDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getTitle())
                .setMessage(getMessage());

        String positiveBtnText = getPositiveBtnText();
        String neutralBtnText = getNeutralBtnText();
        String negativeBtnText = getNegativeBtnText();
        if (positiveBtnText != null) {
            builder.setPositiveButton(positiveBtnText, this);
        }
        if (neutralBtnText != null) {
            builder.setNeutralButton(neutralBtnText, this);
        }
        if (negativeBtnText != null) {
            builder.setNegativeButton(negativeBtnText, this);
        }

        int contentViewId = getContentViewId();
        if(contentViewId != DEFAULT_INT_VALUE){
            mDialogView = getActivity().getLayoutInflater().inflate(contentViewId, null);
            builder.setView(mDialogView);
        }

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

        if (getIsCloseToFinish()) {
            builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_BACK:
                            getActivity().finish();
                            return true;
                    }
                    return false;
                }
            }).setCancelable(false);
            dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
        }

        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog){
        super.onCancel(dialog);
        if (mListener != null) {
            mListener.onDialogCancel(dialog, getTag());
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog){
        super.onDismiss(dialog);
        if (mListener != null) {
            mListener.onDialogDismiss(dialog, getTag());
        }
    }

    public void setTitle(String title) {
        getArguments().putString(KEY_DIALOG_TITLE, title);
    }

    public String getTitle() {
        return getArguments().getString(KEY_DIALOG_TITLE);
    }

    public void setMessage(String message) {
        getArguments().putString(KEY_DIALOG_MESSAGE, message);
    }

    public String getMessage() {
        return getArguments().getString(KEY_DIALOG_MESSAGE);
    }

    public void setContentViewId(int resId) {
        getArguments().putInt(KEY_DIALOG_CONTENT_VIEW, resId);
    }

    public int getContentViewId() {
        return getArguments().getInt(KEY_DIALOG_CONTENT_VIEW, DEFAULT_INT_VALUE);
    }

    public void setPositiveBtnText(String text) {
        getArguments().putString(KEY_DIALOG_POSITIVE_BUTTON_TEXT, text);
    }

    public String getPositiveBtnText() {
        return getArguments().getString(KEY_DIALOG_POSITIVE_BUTTON_TEXT);
    }

    public void setNeutralBtnText(String text) {
        getArguments().putString(KEY_DIALOG_NEUTRAL_BUTTON_TEXT, text);
    }

    public String getNeutralBtnText() {
        return getArguments().getString(KEY_DIALOG_NEUTRAL_BUTTON_TEXT);
    }

    public void setNegativeBtnText(String text) {
        getArguments().putString(KEY_DIALOG_NEGATIVE_BUTTON_TEXT, text);
    }

    public String getNegativeBtnText() {
        return getArguments().getString(KEY_DIALOG_NEGATIVE_BUTTON_TEXT);
    }

    public void setCloseToFinish(boolean flag) {
        getArguments().putBoolean(KEY_DIALOG_IS_CLOSE_TO_FINISH, flag);
    }

    public boolean getIsCloseToFinish() {
        return getArguments().getBoolean(KEY_DIALOG_IS_CLOSE_TO_FINISH, false);
    }

    public void setDialogListener(TNSimpleDialogListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if (mListener == null) return;
        switch (i) {
            case DialogInterface.BUTTON_POSITIVE:
                mListener.onPositiveBtnClick(dialogInterface, i, getTag());
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                mListener.onNeutralBtnClick(dialogInterface, i, getTag());
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                mListener.onNegativeBtnClick(dialogInterface, i, getTag());
                break;
        }
    }
}
