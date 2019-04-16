package com.murphy.pokotalk.activity.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import com.murphy.pokotalk.R;

public class CameraPermissionDialog extends DialogFragment {
    private Listener listener;

    public static final int GOT_IT = 1;
    public static final int DENY = 2;

    public interface Listener {
        void onPermissionRationaleAction(int which);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement listener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.permission_title)
                .setMessage(R.string.camera_permission_rationale)
                .setPositiveButton(R.string.permission_got_it,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onPermissionRationaleAction(GOT_IT);
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.permission_deny_it,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onPermissionRationaleAction(DENY);
                        dismiss();
                    }
                });

        return builder.create();
    }
}
