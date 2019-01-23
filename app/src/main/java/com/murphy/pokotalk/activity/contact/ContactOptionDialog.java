package com.murphy.pokotalk.activity.contact;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.murphy.pokotalk.data.user.PendingContact;

public class ContactOptionDialog extends DialogFragment {
    private String[] optionStrings;
    private PendingContact contact;
    private ContactOptionDialogListener listener;

    public static final int DENY_CONTACT = 1;
    public static final int ACCEPT_CONTACT = 0;

    public void setContact(PendingContact c) {
        contact = c;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        optionStrings = new String[] {"친구 추가", "거부"};
        builder.setTitle("옵션")
                .setItems(optionStrings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.contactOptionClick(which, contact);
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ContactOptionDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement ContactOptionDialogListener");
        }
    }

    public interface ContactOptionDialogListener {
        void contactOptionClick(int option, PendingContact contact);
    }
}
