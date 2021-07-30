package com.murphy.pokotalk.activity.contact;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.View;

import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.view.ContactDetailView;

public class ContactDetailDialog extends AppCompatDialogFragment {
    private Contact contact;
    private ContactDetailView view;
    private ContactDetailDialogListener listener;

    public static final int CONTACT_CHAT = 0;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        view = new ContactDetailView(getActivity());
        view.inflate();

        /* Set dialog contents and button listeners */
        if (contact != null) {
            view.setContact(contact);
            view.getChatButton().setOnClickListener(chatButtonClickListener);
        }

        builder.setView(view).setTitle("친구 정보");
        return builder.create();
    }

    private View.OnClickListener chatButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            listener.contactDetailOptionClick(contact, CONTACT_CHAT);
            dismiss();
        }
    };

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ContactDetailDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement contactDetailDialogListener");
        }
    }

    public interface ContactDetailDialogListener {
        void contactDetailOptionClick(Contact contact, int option);
    }
}
