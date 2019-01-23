package com.murphy.pokotalk.activity.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.murphy.pokotalk.R;
import com.murphy.pokotalk.data.user.Contact;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactDetailDialog extends AppCompatDialogFragment {
    private Contact contact;
    private View view;
    private TextView nicknameView;
    private TextView emailView;
    private CircleImageView imageView;
    private Button contactChatButton;
    private contactDetailDialogListener listener;
    public static final int CONTACT_CHAT = 0;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.contact_detail_dialog, null, false);
        nicknameView = view.findViewById(R.id.nickname);
        emailView = view.findViewById(R.id.email);
        imageView = view.findViewById(R.id.image);
        contactChatButton = view.findViewById(R.id.contactChatButton);

        /* Set dialog contents and button listeners */
        if (contact != null) {
            nicknameView.setText(contact.getNickname());
            emailView.setText(contact.getEmail());
            contactChatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.detailOptionSelect(contact, CONTACT_CHAT);
                    dismiss();
                }
            });
        }

        builder.setView(view).setTitle("정보");
        return builder.create();
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (contactDetailDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement contactDetailDialogListener");
        }
    }


    public interface contactDetailDialogListener {
        void detailOptionSelect(Contact contact, int option);
    }
}
