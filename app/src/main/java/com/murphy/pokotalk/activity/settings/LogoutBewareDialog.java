package com.murphy.pokotalk.activity.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import com.murphy.pokotalk.R;

public class LogoutBewareDialog extends DialogFragment {
    private Listener listener;

    public static final int OPTION_LOGOUT = 7575;
    public static final int OPTION_CANCEL = 0;

    public interface Listener {
        void logoutOptionSelected(int option);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // Set builder
        builder.setTitle(R.string.logout_title)
                .setMessage(R.string.logout_beware)
                .setPositiveButton(R.string.logout_positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Apply action for logoutState
                        listener.logoutOptionSelected(OPTION_LOGOUT);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Canceled, just dismiss the dialog
                        dismiss();
                    }
                });

        return builder.create();
    }
}
