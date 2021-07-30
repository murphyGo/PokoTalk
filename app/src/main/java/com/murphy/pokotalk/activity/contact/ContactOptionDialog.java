package com.murphy.pokotalk.activity.contact;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;

import com.murphy.pokotalk.data.user.Contact;

public class ContactOptionDialog extends AppCompatDialogFragment {
    private String[] optionStrings = {"채팅", "삭제"};
    private Contact contact;
    private ContactOptionDialogListener listener;

    public static final int CHAT = 0;
    public static final int REMOVE_CONTACT = 1;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("옵션").
                setItems(optionStrings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.contactOptionClick(contact, which);
                    }
                });

        return builder.create();
    }

    public void setContact(Contact contact) {
        this.contact = contact;
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
        void contactOptionClick(Contact contact, int option);
    }
}
